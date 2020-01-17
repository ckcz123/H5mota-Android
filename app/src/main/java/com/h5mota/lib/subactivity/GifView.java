package com.h5mota.lib.subactivity;

import android.os.Environment;
import android.widget.ImageView.ScaleType;
import com.h5mota.R;
import com.h5mota.lib.MyFile;
import com.h5mota.lib.view.CustomToast;
import com.h5mota.lib.view.GifImageView;
import com.h5mota.lib.view.MyNotification;
import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GifView {
  SubActivity subActivity;
  String title;
  String filepath = null;
  GifImageView gifImageView;
  byte[] bts;

  public GifView(SubActivity subActivity) {
    this.subActivity = subActivity;
  }

  public GifView showGif(String filepath, String _title) {
    title = _title;
    title = title.trim();
    if (title.endsWith(".gif")) title = title.substring(0, title.lastIndexOf("."));
    if ("".equals(title)) title = "查看图片";
    subActivity.setTitle(title);
    subActivity.setContentView(R.layout.subactivity_gifview);
    gifImageView = subActivity.findViewById(R.id.subactivity_gifview);
    gifImageView.setScaleType(ScaleType.FIT_CENTER);
    File file = new File(filepath);
    if (file != null && file.exists()) {
      try {
        byte[] bts = new byte[(int) file.length()];
        FileInputStream fileInputStream = new FileInputStream(file);
        fileInputStream.read(bts);
        fileInputStream.close();
        gifImageView.setBytes(bts);
        gifImageView.startAnimation();
      } catch (Exception e) {
      }
    }
    this.filepath = filepath;
    return this;
  }

  public void savePicture() throws Exception {
    if (this.filepath == null || "".equals(this.filepath)) {
      CustomToast.showErrorToast(subActivity, "该图片无法保存到本地");
      return;
    }
    SimpleDateFormat simpleDateFormat =
        new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.getDefault());
    String time = simpleDateFormat.format(new Date());
    new File(Environment.getExternalStorageDirectory() + "/H5mota/_Pictures/").mkdirs();
    String filepath =
        Environment.getExternalStorageDirectory()
            + "/H5mota/_Pictures/"
            + title
            + "__"
            + time
            + ".gif";
    if (MyFile.copyFile(this.filepath, filepath)) {
      if (android.os.Build.VERSION.SDK_INT < 16)
        CustomToast.showSuccessToast(subActivity, "图片保存在\n" + filepath, 3500);
      else {
        MyNotification.sendNotificationToOpenfile(
            "图片已保存", "图片保存在" + filepath, "图片保存在" + filepath, subActivity, new File(filepath));
      }
    } else CustomToast.showErrorToast(subActivity, "保存失败");
  }

  public void stop() {
    try {
      gifImageView.stopAnimation();
    } catch (Exception e) {
    }
  }
}
