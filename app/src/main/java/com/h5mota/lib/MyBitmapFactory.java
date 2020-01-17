package com.h5mota.lib;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

/**
 * @author oc
 * @since 16/9/4
 */
public class MyBitmapFactory {

  public static byte[] getCompressedBitmapBytes(String imagePath, double size) {
    return getCompressedBitmapBytes(imagePath, size, false);
  }

  public static byte[] getCompressedBitmapBytes(String imagePath, double size, boolean ispng) {
    return bitmapToArray(getCompressedBitmap(imagePath, size), size, ispng);
  }

  /**
   * Get a compressed bitmap from file
   *
   * @param filePath
   * @param size 1 for 100KB, 10 for 1MB, -1 for unlimited
   * @return
   */
  public static Bitmap getCompressedBitmap(String filePath, double size) {
    Bitmap bitmap = null;
    boolean outofmemory = true;
    BitmapFactory.Options options = new BitmapFactory.Options();
    int inSampleSize = 0;
    ByteArrayOutputStream output = new ByteArrayOutputStream();

    while (outofmemory) {
      try {
        options.inSampleSize = ++inSampleSize;
        output.reset();
        bitmap = BitmapFactory.decodeFile(filePath, options);
        if (size > 0) {
          bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
          if (output.toByteArray().length > 102400 * size) {
            outofmemory = true;
            bitmap.recycle();
            bitmap = null;
            continue;
          }
        }
        outofmemory = false;
      } catch (Exception e) {
        e.printStackTrace();
        bitmap = null;
        outofmemory = false;
      } catch (OutOfMemoryError err) {
        outofmemory = true;
        try {
          bitmap.recycle();
        } catch (Exception e) {
        }
        System.gc();
      }
    }

    return bitmap;
  }

  public static byte[] bitmapToArray(Bitmap bitmap) {
    return MyBitmapFactory.bitmapToArray(bitmap, -1, false);
  }

  public static byte[] bitmapToArray(Bitmap bitmap, boolean ispng) {
    return MyBitmapFactory.bitmapToArray(bitmap, -1, ispng);
  }

  public static byte[] bitmapToArray(Bitmap bitmap, double size) {
    return MyBitmapFactory.bitmapToArray(bitmap, size, false);
  }

  public static byte[] bitmapToArray(Bitmap bitmap, double size, boolean ispng) {
    if (bitmap == null) return null;
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    int options = 100;
    Bitmap.CompressFormat format = ispng ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG;
    bitmap.compress(format, 100, output);
    while (size > 0 && output.toByteArray().length > 102400 * size) {
      options -= 10;
      output.reset();
      bitmap.compress(format, options, output);
    }
    byte[] result = output.toByteArray();
    try {
      output.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return result;
  }

  public static boolean bitmapToFile(File file, Bitmap bitmap) {
    return bitmapToFile(file, bitmap, -1, false);
  }

  public static boolean bitmapToFile(File file, Bitmap bitmap, double size) {
    return bitmapToFile(file, bitmap, size, false);
  }

  public static boolean bitmapToFile(File file, Bitmap bitmap, double size, boolean ispng) {
    try {
      FileOutputStream fileOutputStream = new FileOutputStream(file);
      byte[] bts = bitmapToArray(bitmap, size, ispng);
      fileOutputStream.write(bts);
      fileOutputStream.close();
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}
