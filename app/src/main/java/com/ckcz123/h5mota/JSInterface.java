package com.ckcz123.h5mota;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.webkit.JavascriptInterface;

import com.ckcz123.h5mota.lib.CustomToast;

import java.io.FileWriter;
import java.io.IOException;

import me.weyye.hipermission.HiPermission;

/**
 * Created by oc on 2018/4/18.
 */

public class JSInterface {

    private Activity activity;

    public JSInterface(Activity activity) {
        this.activity = activity;
    }

    @JavascriptInterface
    public void download(String filename, String content) {
        if (!HiPermission.checkPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            CustomToast.showErrorToast(activity, "你没有SD卡权限！");
            return;
        }

        try {
            String directory = Environment.getExternalStorageDirectory()+"/H5mota/";
            String path = directory + filename;
            FileWriter fileWriter = new FileWriter(path);
            fileWriter.write(content);
            fileWriter.close();
            CustomToast.showSuccessToast(activity, "文件已下载到"+path);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(directory), "resource/folder");

            if (intent.resolveActivityInfo(activity.getPackageManager(), 0)!=null) {
                PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, intent, 0);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(activity, "notification")
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("文件下载成功")
                        .setContentText("文件已下载到"+path)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);

                NotificationManagerCompat.from(activity).notify(1, builder.build());
            }

        }
        catch (IOException e) {
            CustomToast.showErrorToast(activity, "下载失败！");
        }

    }

    @JavascriptInterface
    public void copy(String content) {
        ClipboardManager clipboardManager = (ClipboardManager)activity.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboardManager!=null) {
            clipboardManager.setPrimaryClip(ClipData.newPlainText("content", content));
            CustomToast.showSuccessToast(activity, "已成功复制到剪切板！");
        }
    }

    @JavascriptInterface
    public void readFile() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        activity.startActivityForResult(Intent.createChooser(intent, "请选择文件"), WebActivity.JSINTERFACE_SELECT_FILE);

    }

}
