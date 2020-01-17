package com.h5mota.lib;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.Log;

import android.webkit.WebView;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by oc on 2018/3/17.
 */

public class Utils {

    public static void copyFilesFassets(Context context, String oldPath, String newPath) {
        try {
            String fileNames[] = context.getAssets().list(oldPath);//获取assets目录下的所有文件及目录名
            if (fileNames.length > 0) {//如果是目录
                File file = new File(newPath);
                file.mkdirs();//如果文件夹不存在，则递归
                for (String fileName : fileNames) {
                    copyFilesFassets(context,oldPath + "/" + fileName,newPath+"/"+fileName);
                }
            } else {//如果是文件
                InputStream is = context.getAssets().open(oldPath);
                FileOutputStream fos = new FileOutputStream(new File(newPath));
                byte[] buffer = new byte[1024];
                int byteCount=0;
                while((byteCount=is.read(buffer))!=-1) {//循环从输入流读取 buffer字节
                    fos.write(buffer, 0, byteCount);//将读取的输入流写入到输出流
                }
                fos.flush();//刷新缓冲区
                is.close();
                fos.close();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @TargetApi(24)
    public static boolean unzip(File file, File directory) {

        if (Build.VERSION.SDK_INT<24) return false;

        try (InputStream inputStream=new FileInputStream(file);
             ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(inputStream), Charset.forName("GBK"))) {

            ZipEntry zipEntry;
            byte[] buffer = new byte[1024];
            int count;
            while ((zipEntry=zipInputStream.getNextEntry())!=null) {
                String filename = zipEntry.getName();
                if (zipEntry.isDirectory()) {
                    new File(directory, filename).mkdirs();
                }
                else {
                    FileOutputStream outputStream = new FileOutputStream(new File(directory, filename));
                    while ((count=zipInputStream.read(buffer))!=-1) {
                        outputStream.write(buffer, 0, count);
                    }
                    outputStream.close();
                    zipInputStream.closeEntry();
                }
            }

            return true;
        }
        catch (Exception e) {
            Log.e("unzip", "error", e);
            return false;
        }
    }

    @SuppressWarnings("deprecation")
    public static Bitmap captureWebView(WebView webView) {
        if (webView == null) return null;
        Picture snapShot = webView.capturePicture();
        Bitmap bmp = Bitmap.createBitmap(snapShot.getWidth(), snapShot.getHeight(), Bitmap.Config.ARGB_8888);
        Bitmap bg=null;
        try {
            Bitmap bgr = ((BitmapDrawable) webView.getBackground()).getBitmap();
            bg= Bitmap.createScaledBitmap(bgr, snapShot.getWidth(), snapShot.getHeight(), true);
        }
        catch (Exception | OutOfMemoryError e) {bg=null;}
        Canvas canvas = new Canvas(bmp);
        if (bg!=null)
            canvas.drawBitmap(bg,0,0,new Paint());
        snapShot.draw(canvas);
        return bmp;
    }

    public static String getHash(String string) {
        return getHash(string, "");
    }

    public static String getHash(String string, String defaultString) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return bytes2Hex(md.digest(string.getBytes()));
        } catch (Exception e) {
            return defaultString;
        }
    }

    public static String md5(String string) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return bytes2Hex(md.digest(string.getBytes()));
        } catch (Exception e) {
            return "";
        }
    }

    private static String bytes2Hex(byte[] bts) {
        String des = "";
        for (int i = 0; i < bts.length; i++) {
            String tmp = (Integer.toHexString(bts[i] & 0xFF));
            if (tmp.length() == 1)
                des += "0";
            des += tmp;
        }
        return des;
    }

    public static int sp2px(Context context, int sp) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (sp * fontScale + 0.5f);
    }

    public static int px2sp(Context context, int px) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (px / fontScale + 0.5f);
    }

    public static int generateColorInt() {
        return Color.parseColor(generateColorString());
    }

    public static String generateColorString() {
        Random random = new Random();
        int[] x = new int[6];
        while (true) {
            for (int i=0;i<6;i++)
                x[i]=random.nextInt(16);
            int val=x[0]+x[2]+x[4];
            if (val>=27 && val<=40) break;
        }
        String string = "#";
        for (int i = 0; i < 6; i++)
            string += intToHex(x[i]);
        return string;
    }

    private static String intToHex(int i) {
        if (i <= 9) return i + "";
        if (i == 10) return "a";
        if (i == 11) return "b";
        if (i == 12) return "c";
        if (i == 13) return "d";
        if (i == 14) return "e";
        return "f";
    }

    public static String getValue(String url, String name) {
        int pos=url.lastIndexOf("?");
        int anchor=url.lastIndexOf("#");
        if (pos<0) return "";
        String string;
        if (anchor<=pos+1)
            string=url.substring(pos+1);
        else string=url.substring(pos+1, anchor-pos-1);
        if ("".equals(string)) return "";
        String[] strings=string.split("&");
        for (String str: strings) {
            String[] para=str.split("=");
            if (name.equals(para[0].trim()))
                return para[1].trim();
        }
        return "";
    }

}
