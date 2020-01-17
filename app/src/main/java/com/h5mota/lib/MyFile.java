package com.h5mota.lib;

import android.content.Context;
import android.util.Base64;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MyFile {

  private static File getFilesDir(Context context) {
    return context.getFilesDir();
  }

  public static File getFile(Context context, String _dir, String _name) {
    String dir = (_dir == null ? "" : _dir).trim(),
        name = (_name == null ? "" : _name).trim();

    File file = "".equals(dir) ? getFilesDir(context) : new File(getFilesDir(context), dir);
    if (file.exists() && file.isFile()) file.delete();
    file.mkdirs();
    return "".equals(name) ? file : new File(file, name);
  }

  public static String getString(Context context,
      String username, String name,
      String defaultString) throws Exception {
    File file = getFile(context, username, name);
    if (!file.exists()) {
      file.createNewFile();
      if (defaultString != null) {
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(Base64.encode(defaultString.getBytes(), Base64.DEFAULT));
        fileOutputStream.close();
        return defaultString;
      }
    }
    if (file.length() == 0) return "";
    FileInputStream fileInputStream = new FileInputStream(file);
    byte[] bytes = new byte[(int) file.length()];
    fileInputStream.read(bytes);
    fileInputStream.close();
    return new String(Base64.decode(bytes, Base64.DEFAULT));
  }

  public static void putString(
      Context context, String username, String name, String stringToWrite)
      throws Exception {
    File file = getFile(context, username, name);
    if (!file.exists()) {
      file.createNewFile();
    }
    FileOutputStream fileOutputStream = new FileOutputStream(file);
    fileOutputStream.write(Base64.encode(stringToWrite.getBytes(), Base64.DEFAULT));
    fileOutputStream.close();
  }

  public static File getCache(Context context, String name) {
    File cacheDir = getFilesDir(context);
    return name == null || "".equals(name) ? cacheDir : new File(cacheDir, name);
  }

  public static void deleteFile(String path) {
    deleteFile(new File(path));
  }

  public static void deleteFile(File file) {
    if (file == null || !file.exists()) return;
    try {
      if (file.isDirectory()) {
        File[] files = file.listFiles();
        for (File f : files) {
          deleteFile(f);
        }
      }
      file.delete();
    } catch (Exception e) {
    }
  }

  public static boolean streamToFile(InputStream inputStream, File file) {
    File tmpFile = new File(file + "_tmp");
    try {
      FileOutputStream fileOutputStream = new FileOutputStream(tmpFile);
      byte[] bts = new byte[2048];
      int len;
      while ((len = inputStream.read(bts)) != -1)
        fileOutputStream.write(bts, 0, len);
      fileOutputStream.close();
      file.delete();
      return tmpFile.renameTo(file);
    } catch (Exception e) {
      tmpFile.delete();
      file.delete();
      return false;
    }
  }

  public static boolean urlToFile(String url, Context context) {
    return urlToFile(url, MyFile.getCache(context, Utils.getHash(url)), false);
  }

  public static boolean urlToFile(String url, File file) {
    return urlToFile(url, file, false);
  }

  public static boolean urlToFile(String url, File file, boolean override) {
    if (!override && file != null && file.exists()) return true;
    OkHttpClient okHttpClient = new OkHttpClient().newBuilder().cookieJar(Cookies.getInstance())
        .build();
    try (Response response = okHttpClient.newCall(new Request.Builder().url(url).build()).execute()) {
      return streamToFile(response.body().byteStream(), file);
    } catch (Exception e) {
      return false;
    }
  }

  public static int getFileCount(File file) {
    if (file == null) return 0;
    if (file.isDirectory()) {
      File[] files = file.listFiles();
      int cnt = 0;
      for (File f : files)
        cnt += getFileCount(f);
      return cnt;
    }
    return 1;
  }

  public static long getFileSize(File file) {
    if (file == null) return 0;
    if (file.isDirectory()) {
      File[] files = file.listFiles();
      long size = 0;
      for (File f : files)
        size += getFileSize(f);
      return size;
    }
    return file.length();
  }

  public static String getFileSizeString(File file) {
    return formatFileSize(getFileSize(file));
  }

  public static String formatFileSize(long size) {
    double sz = size + 0.0;
    DecimalFormat decimalFormat = new DecimalFormat("#.00");
    if (size < 1024) return decimalFormat.format(sz) + "B";
    if (size < 1048576) return decimalFormat.format(sz / 1024) + "K";
    if (size < 1073741824) return decimalFormat.format(sz / 1048576) + "M";
    return decimalFormat.format(sz / 1073741824) + "G";
  }

  public static void clearCache(Context context) {
    File file = getCache(context, null);
    if (!file.exists() || !file.isDirectory()) return;
    File[] files = file.listFiles();
    for (File f : files)
      deleteFile(f);
  }

  public static boolean copyFile(String source, String destination) {
    return copyFile(new File(source), new File(destination));
  }

  public static boolean copyFile(File source, File destination) {
    if (source == null || destination == null) return false;
    if (!source.exists()) return false;
    try {
      destination.delete();
      destination.createNewFile();
      FileInputStream inputStream = new FileInputStream(source);
      FileOutputStream outputStream = new FileOutputStream(destination);
      FileChannel fileChannel = inputStream.getChannel();
      fileChannel.transferTo(0, fileChannel.size(), outputStream.getChannel());
      inputStream.close();
      outputStream.close();
      return true;
    } catch (Exception e) {
      destination.delete();
      return false;
    }
  }


}
