package com.h5mota;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import me.weyye.hipermission.HiPermission;

/**
 * Created by oc on 2018/4/18.
 */
public class JSInterface {

  private final Activity activity;
  private final WebView webView;
  private final File saveDirectory;

  public JSInterface(Activity activity, WebView webView) {
    this.activity = activity;
    this.webView = webView;
    this.saveDirectory = activity.getExternalFilesDir("saves");
    if (!this.saveDirectory.exists()) {
      this.saveDirectory.mkdir();
    }
  }

  @JavascriptInterface
  public void download(String filename, String content) {
    if (!HiPermission.checkPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
      CustomToast.showErrorToast(activity, "你没有SD卡权限！");
      return;
    }

    try {
      // File directory = activity.getExternalFilesDir("downloads");
      File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
      File file = new File(directory, filename);
      FileWriter fileWriter = new FileWriter(file);
      fileWriter.write(content);
      fileWriter.close();
      CustomToast.showSuccessToast(activity, "文件已下载到" + file.getAbsolutePath(), 3000);

      Intent intent = new Intent(Intent.ACTION_VIEW);
      intent.setDataAndType(Uri.parse(directory.getAbsolutePath()), "*/*");

      PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0,
          Intent.createChooser(intent, "推荐使用ES文件浏览器打开目录...")
          , 0);

      NotificationCompat.Builder builder =
          new NotificationCompat.Builder(activity, "notification")
              .setSmallIcon(R.mipmap.ic_launcher)
              .setContentTitle("文件下载成功")
              .setContentText("文件已下载到" + file.getAbsolutePath())
              .setPriority(NotificationCompat.PRIORITY_DEFAULT)
              .setAutoCancel(true)
              .setContentIntent(pendingIntent);

      NotificationManagerCompat.from(activity).notify(1, builder.build());

    } catch (IOException e) {
      Log.i("ERROR", "error", e);
      CustomToast.showErrorToast(activity, "下载失败！");
    }
  }

  @JavascriptInterface
  public void copy(String content) {
    ClipboardManager clipboardManager =
        (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
    if (clipboardManager != null) {
      clipboardManager.setPrimaryClip(ClipData.newPlainText("content", content));
      CustomToast.showSuccessToast(activity, "已成功复制到剪切板！");
    }
  }

  @JavascriptInterface
  public void readFile() {
    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
    intent.setType("*/*");
    activity.startActivityForResult(
        Intent.createChooser(intent, "请选择文件"), WebActivity.JSINTERFACE_SELECT_FILE);
  }

  String replaceContent(String content) {
    if (content == null) {
      content = "";
    }
    return "'" + content.replaceAll("'", "\"").replaceAll("\n", "\\n") + "'";
  }

  private File getFile(String name) {
    int index = name.indexOf('_');
    if (index > 0 && index < name.length() - 1) {
      File dir = new File(saveDirectory, name.substring(0, index));
      if (!dir.exists()) {
        dir.mkdir();
      }
      return new File(dir, name.substring(index + 1));
    }
    return new File(saveDirectory, name);
  }

  @JavascriptInterface
  public void setLocalForage(int id, String name, String data) {
    try (FileWriter writer = new FileWriter(getFile(name))) {
      writer.write(data);
      executeLocalForageCallback(id);
    } catch (IOException e) {
      Log.e("ERROR", "Unable to setLocalForage", e);
      executeLocalForageCallback(id, replaceContent(e.getMessage()), null);
    }
  }

  @JavascriptInterface
  public void getLocalForage(int id, String name) {
    try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(getFile(name))))) {
      String line;
      StringBuilder builder = new StringBuilder();
      while ((line = bufferedReader.readLine()) != null) builder.append(line);
      executeLocalForageCallback(id, null, replaceContent(builder.toString()));
    } catch (IOException e) {
      executeLocalForageCallback(id, null, null);
    }
  }

  @JavascriptInterface
  public void removeLocalForage(int id, String name) {
    getFile(name).delete();
    executeLocalForageCallback(id);
  }

  private void removeDir(File file) {
    if (!file.exists()) {
      return;
    }
    if (file.isDirectory()) {
      for (File f : file.listFiles()) {
        removeDir(f);
      }
    } else {
      file.delete();
    }
  }

  @JavascriptInterface
  public void clearLocalForage(int id) {
    removeDir(saveDirectory);
    saveDirectory.mkdir();
    executeLocalForageCallback(id);
  }

  @JavascriptInterface
  public void iterateLocalForage(int id) {
    executeLocalForageIterate(id, saveDirectory.list());
  }

  void onWebViewPageLoaded() {
    activity.runOnUiThread(() ->
        webView.evaluateJavascript("" +
            "(function () {\n" +
            "  if (!window.core || !window.core.plugin || !window.localforage) return;\n" +
            "  var _afterLoadResources = core.plugin._afterLoadResources;\n" +
            "  core.plugin._afterLoadResources = function () {\n" +
            "    // Check there are no local saves now.\n" +
            "    if (Object.keys(core.saves.ids).length == 0 && window.jsinterface) {\n" +
            "      console.log('Forwarding localforage...');\n" +
            "      core.platform.useLocalForage = true;\n" +
            "      if (window.LZString) LZString.compress = function (s) { return s; };\n" +
            "      if (window.lzw_encode) lzw_encode = function (s) { return s; };\n" +
            "      localforage.setItem = function (name, data, callback) {\n" +
            "        var id = setTimeout(null);\n" +
            "        core['__callback' + id] = callback;\n" +
            "        window.jsinterface.setLocalForage(id, name, data);\n" +
            "      }\n" +
            "      localforage.getItem = function (name, callback) {\n" +
            "        var id = setTimeout(null);\n" +
            "        core['__callback' + id] = callback;\n" +
            "        window.jsinterface.getLocalForage(id, name);\n" +
            "      }\n" +
            "      localforage.removeItem = function (name, callback) {\n" +
            "        var id = setTimeout(null);\n" +
            "        core['__callback' + id] = callback;\n" +
            "        window.jsinterface.removeLocalForage(id, name);\n" +
            "      }\n" +
            "      localforage.clear = function (callback) {\n" +
            "        var id = setTimeout(null);\n" +
            "        core['__callback' + id] = callback;\n" +
            "        window.jsinterface.clearLocalForage(id);\n" +
            "      }\n" +
            "      localforage.iterate = function (iter, callback) {\n" +
            "        var id = setTimeout(null);\n" +
            "        core['__iter' + id] = iter;\n" +
            "        core['__callback' + id] = callback;\n" +
            "        window.jsinterface.iterateLocalForage(id);\n" +
            "      }\n" +
            "      core.control.getSaveIndexes(function (indexes) {\n" +
            "        core.saves.ids = indexes;\n" +
            "      });\n" +
            "    }\n" +
            "    if (_afterLoadResources) _afterLoadResources.call(core.plugin);\n" +
            "  }\n" +
            "})();\n" +
            "", null));
  }

  private void executeLocalForageCallback(int id) {
    activity.runOnUiThread(() -> webView.evaluateJavascript("" +
        "if (window.core && window.core.__callback" + id + ") {\n" +
        "  var callback = core.__callback" + id + ";\n" +
        "  delete core.__callback" + id + ";\n" +
        "  callback();\n" +
        "}\n" +
        "", null));
  }

  private void executeLocalForageCallback(int id, String err, String data) {
    activity.runOnUiThread(() -> webView.evaluateJavascript("" +
        "if (window.core && window.core.__callback" + id + ") {\n" +
        "  var callback = core.__callback" + id + ";\n" +
        "  delete core.__callback" + id + ";\n" +
        "  callback(" + err + ", " + data + ");" +
        "}\n" +
        "", null));
  }

  private void executeLocalForageIterate(int id, String[] keys) {
    StringBuilder builder = new StringBuilder();
    String iterName = "core.__iter" + id;
    builder.append("if (window.core && window.").append(iterName).append(") {\n");
    if (keys != null) {
      for (String key : keys) {
        builder.append("  ")
            .append(iterName)
            .append("(null, ")
            .append(replaceContent(key))
            .append(");\n");
      }
    }
    builder.append("  delete ").append(iterName).append(";");
    builder.append("}\n");
    activity.runOnUiThread(() -> webView.evaluateJavascript(builder.toString(), s -> executeLocalForageCallback(id)));
  }
}
