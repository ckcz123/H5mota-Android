package com.h5mota;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import fi.iki.elonen.SimpleWebServer;
import me.weyye.hipermission.HiPermission;
import me.weyye.hipermission.PermissionCallback;
import me.weyye.hipermission.PermissionItem;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
  public static final String DOMAIN = "https://h5mota.com";
  public static final String LOCAL = "http://127.0.0.1:1055/";

  public File directory;

  SimpleWebServer simpleWebServer;
  SharedPreferences preferences;
  double exittime = 0;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    preferences = getSharedPreferences("config", Context.MODE_PRIVATE);

    List<PermissionItem> list = new ArrayList<>();
    list.add(
        new PermissionItem(
            Manifest.permission.WRITE_EXTERNAL_STORAGE, "存储权限", R.drawable.permission_ic_storage));
    list.add(
        new PermissionItem(
            Manifest.permission.READ_PHONE_STATE, "读取手机状态", R.drawable.permission_ic_phone));

    HiPermission.create(this)
        .title("权限申请")
        .permissions(list)
        .msg("你需要如下权限来使用本软件")
        .checkMutiPermission(
            new PermissionCallback() {
              @Override
              public void onClose() {
                Log.i("Main", "onClose");
              }

              @Override
              public void onFinish() {
                Log.i("Main", "onFinish");
                initSDCard();
              }

              @Override
              public void onDeny(String permission, int position) {
                Log.i("Main", "onDeny");
              }

              @Override
              public void onGuarantee(String permission, int position) {
                Log.i("Main", "onGuarantee");
              }
            });

    findViewById(R.id.online)
        .setOnClickListener(
            view -> loadUrl(DOMAIN, "HTML5魔塔列表"));

    findViewById(R.id.bbs)
        .setOnClickListener(
            view -> loadUrl(DOMAIN + "/bbs", "HTML5bbs"));

    if (!findViewById(R.id.offline).hasOnClickListeners()) {
      findViewById(R.id.offline)
          .setOnClickListener(
              view -> new AlertDialog.Builder(MainActivity.this)
                  .setTitle("错误")
                  .setMessage("你没有SD卡的权限！")
                  .setCancelable(true)
                  .setPositiveButton("确定", null)
                  .create()
                  .show());
    }

    new Thread(
        () -> {
          try {
            OkHttpClient okHttpClient =
                new OkHttpClient()
                    .newBuilder()
                    .followRedirects(true)
                    .followSslRedirects(true)
                    .build();
            try (Response response =
                     okHttpClient
                         .newCall(
                             new Request.Builder()
                                 .url(DOMAIN + "/games/_client/")
                                 .build())
                         .execute()) {
              String s = response.body().string();
              JSONObject jsonObject = new JSONObject(s);
              final JSONObject android = jsonObject.getJSONObject("android");
              String version = android.getString("version");
              if (!version.equals(BuildConfig.VERSION_NAME)) {
                runOnUiThread(
                    () -> {
                      try {
                        new AlertDialog.Builder(MainActivity.this)
                            .setTitle("存在版本更新！")
                            .setMessage(android.getString("text"))
                            .setCancelable(true)
                            .setPositiveButton(
                                "下载",
                                (dialogInterface, i) -> {
                                  try {
                                    loadUrl(android.getString("url"), "版本更新");
                                  } catch (Exception e) {
                                    e.printStackTrace();
                                  }
                                })
                            .setNegativeButton("取消", null)
                            .create()
                            .show();
                      } catch (Exception e) {
                        e.printStackTrace();
                      }
                    });
              }
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
        })
        .start();
  }

  private void initSDCard() {

    // check permission
    if (!HiPermission.checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) return;

    directory = getExternalFilesDir("towers");

    File defaultTower = new File(directory, "24层魔塔");
    if (!defaultTower.exists()) {
      defaultTower.mkdir();
      new Thread(
          () -> Utils.copyFilesFassets(
              MainActivity.this, "24层魔塔", defaultTower.getAbsolutePath()))
          .start();
    }

    testMigration();

    try {
      if (simpleWebServer != null) {
        simpleWebServer.stop();
      }
      simpleWebServer = new MyWebServer("127.0.0.1", 1055, directory, true);
      simpleWebServer.start();
    } catch (Exception e) {
      e.printStackTrace();
      simpleWebServer = null;
    }

    findViewById(R.id.offline)
        .setOnClickListener(
            view -> {
              AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
              builder.setCancelable(true);
              builder.setTitle("已下载的游戏列表");

              final List<String> names = new ArrayList<>();
              names.add("（打开目录...）");
              for (File file : directory.listFiles()) {
                if (new File(file, "index.html").exists()
                    && new File(file, "main.js").exists()
                    && new File(file, "libs").exists()) {
                  names.add(file.getName());
                }
              }

              builder.setItems(
                  names.toArray(new String[0]),
                  (dialogInterface, i) -> {
                    try {
                      if (i == 0) {
                        openDirectory(getExternalFilesDir("towers"));
                      } else {
                        String name = names.get(i);
                        loadUrl(LOCAL + URLEncoder.encode(name, "utf-8"), name);
                      }
                    } catch (Exception e) {
                      e.printStackTrace();
                    }
                  });

              builder.create().show();
            });
  }

  protected void onDestroy() {
    if (simpleWebServer != null) {
      simpleWebServer.stop();
    }
    super.onDestroy();
  }

  public void wantToExit() {
    if (System.currentTimeMillis() - exittime > 2000) {
      Toast.makeText(this, "再按一遍退出程序", Toast.LENGTH_SHORT).show();
      exittime = System.currentTimeMillis();
    } else {
      exittime = 0;
      setResult(RESULT_CANCELED);
      finish();
    }
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
      wantToExit();
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }

  public boolean onCreateOptionsMenu(Menu menu) {
    return super.onCreateOptionsMenu(menu);
  }

  public boolean onPrepareOptionsMenu(Menu menu) {
    menu.clear();
    menu.add(Menu.NONE, 0, 0, "")
        .setIcon(android.R.drawable.ic_menu_set_as)
        .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
    menu.add(Menu.NONE, 1, 1, "")
        .setIcon(android.R.drawable.ic_menu_manage)
        .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
    menu.add(Menu.NONE, 2, 2, "")
        .setIcon(android.R.drawable.ic_menu_close_clear_cancel)
        .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    return true;
  }

  public boolean onOptionsItemSelected(MenuItem item) {
    super.onOptionsItemSelected(item);
    switch (item.getItemId()) {
      case 0:
        inputLink();
        break;
      case 1:
        new AlertDialog.Builder(this)
            .setSingleChoiceItems(
                new String[]{"启用本地化存档", "禁用本地化存档"},
                preferences.getBoolean("local_save", true) ? 0 : 1,
                (dialogInterface, i) -> {
                  preferences.edit().putBoolean("local_save", i == 0).apply();
                  CustomToast.showSuccessToast(this, i == 0 ? "本地化存档已启用！" : "本地化存档已禁用！");
                }
            )
            .setPositiveButton("确定", null)
            .setCancelable(true).show();
        break;
      case 2:
        finish();
        break;
    }
    return true;
  }

  public void loadUrl(String url, String title) {
    try {
      Intent intent = new Intent(MainActivity.this, WebActivity.class);
      intent.putExtra("title", title);
      intent.putExtra("url", url);
      startActivity(intent);
    } catch (Exception e) {
      e.printStackTrace();
      CustomToast.showErrorToast(this, "无法打开网页！");
    }
  }

  private void inputLink() {
    final EditText editText = new EditText(this);
    editText.setHint("请输入地址...");
    new AlertDialog.Builder(this)
        .setTitle("浏览网页")
        .setView(editText)
        .setPositiveButton(
            "确定",
            (dialogInterface, i) -> {
              String url = editText.getEditableText().toString();
              if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://" + url;
              }
              loadUrl(url, "浏览网页");
            })
        .setNegativeButton("取消", null)
        .setCancelable(true)
        .create()
        .show();
  }

  private void openDirectory(File directory) {
     Intent intent = new Intent(Intent.ACTION_VIEW);
     intent.setDataAndType(Uri.parse(directory.getAbsolutePath()), "*/*");
     startActivity(Intent.createChooser(intent, "推荐使用ES文件浏览器打开目录..."));
  }

  private void testMigration() {
    if (preferences.getBoolean("migration", false)) {
      return;
    }

    File file = new File(Environment.getExternalStorageDirectory() + "/H5mota/");
    if (!file.exists()) return;

    // test migration
    File[] files = file.listFiles();
    if (files == null || files.length == 0) return;
    List<String> notMigrated = new ArrayList<>();
    for (File f : files) {
      if (!f.isDirectory()) continue;
      if (new File(f, "index.html").exists()
          && new File(f, "main.js").exists()
          && new File(f, "libs").exists()) {
        String name = f.getName();
        File target = new File(directory, name);
        if (!target.exists() && !f.renameTo(target)) {
          notMigrated.add(" - " + name);
        }
      }
    }

    new AlertDialog.Builder(this).setTitle("提示")
        .setMessage("根据Android系统要求，从此版本开始，所有下载的离线塔将挪动至" +
            "SD卡的 /Android/data/com.h5mota/files/towers/ 目录下。\n" +
            (notMigrated.isEmpty() ? "系统已帮你移动了所有离线塔到对应目录。" :
                "系统尝试帮你移动离线塔到对应目录，然而下述塔未移动成功，你需要手动进行移动：\n" +
                TextUtils.join("\n", notMigrated))
        ).setCancelable(false)
        .setNeutralButton("确定，不再提示", (d, i) -> preferences.edit().putBoolean("migration", true).apply())
        .setNegativeButton("打开旧目录", (d, i) -> openDirectory(file))
        .setPositiveButton("打开新目录", (d, i) -> openDirectory(directory))
        .create().show();
  }
}
