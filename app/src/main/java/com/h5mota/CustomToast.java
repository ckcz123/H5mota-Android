package com.h5mota;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class CustomToast {

  private static final int TYPE_SUCCESS = 0;
  private static final int TYPE_INFO = 1;
  private static final int TYPE_ERROR = 2;

  @SuppressLint("InflateParams")
  private static void showToast(Context context, String text, int type, long time) {
    if (time <= 0) time = 1500;
    if (time >= 3500) time = 3500;
    final Toast toast = new Toast(context);
    View view = LayoutInflater.from(context).inflate(R.layout.toast_view, null, false);

    ImageView imageView = view.findViewById(R.id.toast_image);
    if (type == TYPE_SUCCESS) {
      imageView.setImageResource(R.mipmap.success);
      view.setBackgroundColor(Color.parseColor("#b4eeb4"));
    } else if (type == TYPE_INFO) {
      imageView.setImageResource(R.mipmap.info);
      view.setBackgroundColor(Color.parseColor("#87ceeb"));
    } else if (type == TYPE_ERROR) {
      imageView.setImageResource(R.mipmap.error);
      view.setBackgroundColor(Color.parseColor("#ffa54f"));
    }

    ((TextView) view.findViewById(R.id.toast_text)).setText(text);
    toast.setDuration(Toast.LENGTH_LONG);
    toast.setView(view);
    toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, 0);
    toast.setMargin(0, 0.35f);
    toast.show();
    Handler handler = new Handler();
    handler.postDelayed(
        new Runnable() {
          public void run() {
            toast.cancel();
          }
        },
        time);
  }

  public static void showSuccessToast(Context context, String text) {
    showToast(context, text, TYPE_SUCCESS, 1500);
  }

  public static void showInfoToast(Context context, String text) {
    showToast(context, text, TYPE_INFO, 1500);
  }

  public static void showErrorToast(Context context, String text) {
    showToast(context, text, TYPE_ERROR, 1500);
  }

  public static void showSuccessToast(Context context, String text, long millseconds) {
    showToast(context, text, TYPE_SUCCESS, millseconds);
  }

  public static void showInfoToast(Context context, String text, long millseconds) {
    showToast(context, text, TYPE_INFO, millseconds);
  }

  public static void showErrorToast(Context context, String text, long millseconds) {
    showToast(context, text, TYPE_ERROR, millseconds);
  }
}
