package com.h5mota.bbs;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.h5mota.R;
import com.h5mota.lib.Constants;
import com.h5mota.lib.ViewSetting;
import com.h5mota.lib.subactivity.SubActivity;
import com.h5mota.lib.view.CustomToast;

public class SettingsFragment extends Fragment {
  static View settingsView;

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.setting_view, container, false);
    settingsView = rootView;
    set((BBSActivity) getActivity());
    setOthers((BBSActivity) getActivity());
    return rootView;
  }

  public static void set(BBSActivity bbsActivity) {
    if (settingsView == null) return;
    try {
      if ("".equals(Userinfo.token)) {
        ViewSetting.setTextView(settingsView, R.id.settings_username, "点击登录...");
        ViewSetting.setOnClickListener(
            settingsView, R.id.settings_username, v -> Userinfo.showLoginView(bbsActivity));
      } else {
        ViewSetting.setTextView(settingsView, R.id.settings_username, Userinfo.nickname);
        ViewSetting.setOnClickListener(
            settingsView,
            R.id.settings_username,
            v ->
                new AlertDialog.Builder(bbsActivity)
                    .setTitle("确认注销？")
                    .setMessage("你想要注销吗？")
                    .setPositiveButton(
                        "确认",
                        (dialog, which) -> {
                          Userinfo.logout(bbsActivity);
                          set(bbsActivity);
                        })
                    .setNegativeButton("取消", null)
                    .show());
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void setOthers(BBSActivity context) {
    if (settingsView == null) return;
    ViewSetting.setOnClickListener(
        settingsView,
        R.id.settings_favorite,
        v -> context.startActivity(new Intent(context, FavoriteActivity.class)));
    /*
    ViewSetting.setSwitchChecked(settingsView, R.id.settings_switch_autologin, Editor
        .getBoolean(context, "autologin", true));
    ViewSetting.setSwitchOnCheckChangeListener(settingsView, R.id.settings_switch_autologin, (buttonView, isChecked) -> {
        Editor.putBoolean(context, "autologin", isChecked);
    });
    ViewSetting.setSwitchChecked(settingsView, R.id.settings_switch_picture, Editor.getBoolean(context, "picture", true));
    ViewSetting.setSwitchOnCheckChangeListener(settingsView, R.id.settings_switch_picture, (buttonView, isChecked) -> {
        Editor.putBoolean(context, "picture", isChecked);
    });
    ViewSetting.setOnClickListener(settingsView, R.id.settings_clearcache, v -> clearcache(context));

    ViewSetting.setOnClickListener(settingsView, R.id.settings_faq, v -> {
        Intent intent=new Intent(context, ViewActivity.class);
        intent.putExtra("type","thread");
        intent.putExtra("bid","1");
        intent.putExtra("tid","29");
        context.startActivity(intent);
    });
     */
    ViewSetting.setOnClickListener(
        settingsView,
        R.id.settings_download_maker,
        v -> {
          Intent intent = new Intent(context, SubActivity.class);
          intent.putExtra("type", Constants.SUBACTIVITY_TYPE_WEBVIEW);
          intent.putExtra("url", Constants.DOMAIN + "/_client/H5motaMaker.apk");
          intent.putExtra("title", "H5安卓造塔器下载");
          context.startActivity(intent);
        });
    ViewSetting.setOnClickListener(
        settingsView,
        R.id.settings_report,
        v -> {
          Intent intent = new Intent(context, ViewActivity.class);
          intent.putExtra("type", "thread");
          intent.putExtra("bid", "1");
          intent.putExtra("tid", "29");
          context.startActivity(intent);
        });
    ViewSetting.setOnClickListener(
        settingsView,
        R.id.settings_about,
        v -> {
          Intent intent = new Intent(context, SubActivity.class);
          intent.putExtra("type", Constants.SUBACTIVITY_TYPE_ABOUT);
          context.startActivity(intent);
        });
    ViewSetting.setOnClickListener(
        settingsView,
        R.id.settings_source,
        v -> {
          String html =
              "<html><meta"
                  + " charset='utf8'><script>window.setTimeout(\"location.href='https://github.com/ckcz123/mota-js'\","
                  + " 3000);</script>"
                  + context.getResources().getString(R.string.settings_source_hint)
                  + "</html>";
          Intent intent = new Intent(context, SubActivity.class);
          intent.putExtra("type", Constants.SUBACTIVITY_TYPE_WEBVIEW_HTML);
          intent.putExtra("html", html);
          intent.putExtra("title", "获取源代码");
          context.startActivity(intent);
        });
    // ViewSetting.setOnClickListener(settingsView, R.id.settings_share, v ->
    // Share.readyToShareURL(context,
    //        "推荐给好友", Constants.updateURL, "欢迎使用CAPUBBS Android "+Constants.updateVersion,
    //        "新版Android CAPUBBS，欢迎您的使用~", null));
    ViewSetting.setOnClickListener(
        settingsView, R.id.settings_share, v -> CustomToast.showInfoToast(context, "分享功能暂时还未上线！"));
    // if (!"".equals(Constants.updateVersion) &&
    // Constants.version.compareTo(Constants.updateVersion)<0) {
    //    ViewSetting.setTextView(settingsView, R.id.settings_update, "检查更新       ");
    //    BadgeView.show(context, settingsView.findViewById(R.id.settings_update), "new");
    // }
    /*
    ViewSetting.setOnClickListener(settingsView, R.id.settings_update, v -> {
        if ("".equals(Constants.updateVersion) || Constants.version.compareTo(Constants.updateVersion)>=0)
            CustomToast.showInfoToast(context, "已是最新版！");
        else {
            new AlertDialog.Builder(context).setTitle("存在版本"+Constants.updateVersion+"更新！")
                    .setMessage(Constants.updateMsg).setPositiveButton("下载", (dialog, which) -> {
                Uri uri= Uri.parse(Constants.updateURL);
                try {
                    DownloadManager.Request request=new DownloadManager.Request(uri);
                    request.setTitle("正在下载CAPUBBS...");
                    File file=MyFile.getFile(context,null,"CAPUBBS.apk");
                    if (file.exists()) file.delete();
                    request.setDestinationUri(Uri.fromFile(file));
                    request.setDescription("文件保存在"+file.getAbsolutePath());
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setMimeType("application/vnd.android.package-archive");
                    request.allowScanningByMediaScanner();
                    DownloadManager downloadManager=(DownloadManager) context.getSystemService(
                        Context.DOWNLOAD_SERVICE);
                    downloadManager.enqueue(request);
                    CustomToast.showInfoToast(context,"文件下载中，请在通知栏查看下载进度");
                }
                catch (Exception e) {
                    e.printStackTrace();
                    context.startActivity(new Intent(Intent.ACTION_VIEW, uri));
                }
            }).setNegativeButton("取消",null).show();
        }
    });
     */
  }
  /*
      public static void clearcache(Context context) {
          try {
              File file = MyFile.getCache(context, null);
              String msg = "缓存路径：\n" + file.getAbsolutePath() + "/\n\n文件数目：" + MyFile.getFileCount(file) + "\n缓存大小：" + MyFile.getFileSizeString(file);
              new AlertDialog.Builder(context).setTitle("清除缓存").setMessage(msg)
                      .setPositiveButton("清除", (dialog, which) -> {
                          MyFile.clearCache(context);
                          CustomToast.showSuccessToast(context, "清除成功！");
                      }).setNegativeButton("取消", null).show();
          }
          catch (Exception e) {e.printStackTrace();}
      }

      public static void checkUpdate(BBSActivity bbsActivity) {
          new Thread(() -> {
              Parameters parameters= WebConnection.connect(Constants.bbs_url+"?ask=main",new ArrayList<>());
              if ("200".equals(parameters.name)) {
                  bbsActivity.handler.sendMessage(
                      Message.obtain(bbsActivity.handler,Constants.MESSAGE_BBS_CHECK_UPDATE,parameters.value));
              }
          }).start();
      }

      public static void update(BBSActivity bbsActivity, String string) {
          try {
              JSONObject jsonObject=new JSONArray(string).getJSONObject(0);
              Constants.updateVersion=jsonObject.optString("updatetime");
              Constants.updateURL=jsonObject.optString("updateurl");
              Constants.updateMsg=jsonObject.optString("updatetext");

              if (!"".equals(Constants.updateVersion) && Constants.version.compareTo(Constants.updateVersion)<0) {
                  BadgeView.show(bbsActivity, bbsActivity.findViewById(R.id.bbs_bottom_img_settings), "new");
                  if (settingsView!=null) {
                      ViewSetting.setTextView(settingsView, R.id.settings_update, "检查更新       ");
                      BadgeView.show(bbsActivity, settingsView.findViewById(R.id.settings_update), "new");
                  }
              }
          }
          catch (Exception e) {e.printStackTrace();}
      }
  */
}
