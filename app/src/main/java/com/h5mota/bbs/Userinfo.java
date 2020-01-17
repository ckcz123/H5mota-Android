package com.h5mota.bbs;

import android.app.Dialog;
import android.os.Message;
import com.h5mota.R;
import com.h5mota.lib.Constants;
import com.h5mota.lib.Editor;
import com.h5mota.lib.Parameters;
import com.h5mota.lib.RequestingTask;
import com.h5mota.lib.Utils;
import com.h5mota.lib.ViewSetting;
import com.h5mota.lib.XML2Json;
import com.h5mota.lib.json.JSONArray;
import com.h5mota.lib.json.JSONObject;
import com.h5mota.lib.view.BadgeView;
import com.h5mota.lib.view.CustomToast;
import java.util.ArrayList;

public class Userinfo {
  static String username, nickname, password, token;
  static Dialog dialog;
  static boolean givehint = false;

  public static void load(BBSActivity bbsActivity) {
    username = Editor.getString(bbsActivity, "username");
    password = Editor.getString(bbsActivity, "password");
    nickname = "";
    token = "";
    autoLogin(bbsActivity);
  }

  public static void autoLogin(BBSActivity bbsActivity) {
    if ("".equals(username)) return;
    new Thread(
            () -> {
              ArrayList<Parameters> arrayList = new ArrayList<Parameters>();
              arrayList.add(new Parameters("ask", "login"));
              arrayList.add(new Parameters("username", username));
              arrayList.add(new Parameters("password", password));
              arrayList.add(new Parameters("os", "android"));
              arrayList.add(new Parameters("device", android.os.Build.MODEL));
              arrayList.add(new Parameters("version", android.os.Build.VERSION.RELEASE));
              Parameters parameters = RequestingTask.connect(Constants.BBS_URL, arrayList);
              if ("200".equals(parameters.name)) {
                bbsActivity.handler.sendMessage(
                    Message.obtain(
                        bbsActivity.handler, Constants.MESSAGE_BBS_LOGIN, parameters.value));
              }
            })
        .start();
    givehint = false;
  }

  @SuppressWarnings("unchecked")
  public static void login(BBSActivity bbsActivity) {
    if ("".equals(username)) {
      showLoginView(bbsActivity);
      return;
    }
    ArrayList<Parameters> arrayList = new ArrayList<>();
    arrayList.add(new Parameters("ask", "login"));
    arrayList.add(new Parameters("username", username));
    arrayList.add(new Parameters("password", password));
    arrayList.add(new Parameters("os", "android"));
    arrayList.add(new Parameters("device", android.os.Build.MODEL));
    arrayList.add(new Parameters("version", android.os.Build.VERSION.RELEASE));
    new RequestingTask(bbsActivity, "正在登录 ...", Constants.BBS_URL, Constants.REQUEST_BBS_LOGIN)
        .execute(arrayList);
    givehint = true;
  }

  public static void showLoginView(BBSActivity bbsActivity) {
    username = Editor.getString(bbsActivity, "username");
    password = Editor.getString(bbsActivity, "password");

    dialog = new Dialog(bbsActivity);
    dialog.setContentView(R.layout.bbs_login_view);
    dialog.setCancelable(true);
    dialog.setCanceledOnTouchOutside(true);
    dialog.setTitle("登录HTML5魔塔网站");
    ViewSetting.setEditTextValue(dialog, R.id.username, username);
    ViewSetting.setEditTextValue(dialog, R.id.password, password);
    ViewSetting.setOnClickListener(
        dialog,
        R.id.bbs_login,
        v -> {
          String u = ViewSetting.getEditTextValue(dialog, R.id.username).trim(),
              p = ViewSetting.getEditTextValue(dialog, R.id.password).trim();
          if ("".equals(u) || "".equals(p)) {
            CustomToast.showInfoToast(bbsActivity, "账号或密码不能为空！", 1500);
            return;
          }
          username = u;
          password = Utils.md5(p);
          login(bbsActivity);
        });
    ViewSetting.setOnClickListener(dialog, R.id.bbs_cancel, v -> dialog.dismiss());
    dialog.show();
  }

  public static void finishLogin(BBSActivity bbsActivity, String string) {
    try {
      JSONObject jsonObject = new JSONArray(string).getJSONObject(0);
      int code = jsonObject.getInt("code");
      if (code != 0) {
        CustomToast.showErrorToast(
            bbsActivity, jsonObject.optString("msg", XML2Json.getErrorMessage(code)));
        return;
      }
      Editor.putString(bbsActivity, "username", username);
      Editor.putString(bbsActivity, "password", password);
      token = jsonObject.optString("token");
      nickname = jsonObject.optString("username");
      try {
        dialog.dismiss();
      } catch (Exception e) {
      }
      if (givehint) CustomToast.showSuccessToast(bbsActivity, "登录成功！", 1000);
      givehint = false;
      //	BadgeView.show(BBSActivity.bbsActivity,
      // BBSActivity.bbsActivity.findViewById(R.id.bbs_bottom_img_me)
      //			, "new");
      SettingsFragment.set(bbsActivity);
    } catch (Exception e) {
      CustomToast.showErrorToast(bbsActivity, "登录失败");
    }
  }

  public static void logout(BBSActivity bbsActivity) {
    Editor.putString(bbsActivity, "username", "");
    Editor.putString(bbsActivity, "password", "");
    username = password = token = nickname = "";
    SettingsFragment.set(bbsActivity);
    BadgeView.show(bbsActivity, bbsActivity.findViewById(R.id.bbs_bottom_img_settings), "");
  }
}
