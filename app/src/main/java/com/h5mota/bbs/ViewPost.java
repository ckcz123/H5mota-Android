package com.h5mota.bbs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Message;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import com.h5mota.R;
import com.h5mota.lib.Constants;
import com.h5mota.lib.Editor;
import com.h5mota.lib.MyBitmapFactory;
import com.h5mota.lib.MyCalendar;
import com.h5mota.lib.MyFile;
import com.h5mota.lib.Parameters;
import com.h5mota.lib.RequestingTask;
import com.h5mota.lib.Utils;
import com.h5mota.lib.ViewSetting;
import com.h5mota.lib.XML2Json;
import com.h5mota.lib.json.JSONArray;
import com.h5mota.lib.json.JSONObject;
import com.h5mota.lib.view.CustomToast;
import java.io.File;
import java.util.ArrayList;

public class ViewPost {
  ArrayList<PostInfo> postInfos = new ArrayList<PostInfo>();
  int page;
  int tmpPage;
  int totalPage;
  String tmpThreadid;
  String title;
  int index;
  String tmpBoard;
  int selectNum;
  int selection = 0;

  ViewActivity viewActivity;

  public ViewPost(ViewActivity activity) {
    viewActivity = activity;
  }

  @SuppressWarnings("unchecked")
  public void getPosts(String threadid, int page) {
    ArrayList<Parameters> arrayList = new ArrayList<Parameters>();
    arrayList.add(new Parameters("ask", "show"));
    arrayList.add(new Parameters("p", page + ""));
    arrayList.add(new Parameters("bid", viewActivity.board));
    arrayList.add(new Parameters("tid", threadid));
    arrayList.add(new Parameters("token", Userinfo.token));
    new RequestingTask(viewActivity, "正在获取内容...", Constants.BBS_URL, Constants.REQUEST_BBS_GET_POST)
        .execute(arrayList);
    tmpPage = page;
    tmpThreadid = threadid;
  }

  void finishRequest(String string) {
    try {

      JSONArray jsonArray = new JSONArray(string);
      int code = jsonArray.getJSONObject(0).getInt("code");
      if (code != 0 && code != -1) {

        if (code == 1 && tmpPage > 1) {
          getPosts(tmpThreadid, tmpPage - 1);
          return;
        }

        CustomToast.showErrorToast(
            viewActivity,
            jsonArray.getJSONObject(0).optString("msg", XML2Json.getErrorMessage(code)));
        viewActivity.setContentView(R.layout.bbs_thread_listview);
        viewActivity.showingPage = ViewActivity.PAGE_THREAD;
        return;
      }
      int tmpNum = selectNum;
      selectNum = 0;
      selection = 0;
      totalPage = jsonArray.getJSONObject(0).getInt("pages");
      title = jsonArray.getJSONObject(0).optString("title");
      postInfos.clear();
      int len = jsonArray.length();
      for (int i = 0; i < len; i++) {
        JSONObject post = jsonArray.getJSONObject(i);
        PostInfo postInfo =
            new PostInfo(
                post.optString("author"),
                post.getInt("floor"),
                post.getInt("fid"),
                post.optString("time"),
                post.optInt("lzl"),
                post.optString("text"),
                post.optString("sig"));
        postInfos.add(postInfo);
        int number = post.getInt("floor");
        if (tmpNum == number) selection = i;
      }

      page = tmpPage;
      viewActivity.threadid = tmpThreadid;
      viewPosts();
    } catch (Exception e) {
      postInfos.clear();
      CustomToast.showErrorToast(viewActivity, "获取失败");
    }
  }

  public void viewPosts() {
    viewActivity.setContentView(R.layout.bbs_post_listview);
    viewActivity.showingPage = ViewActivity.PAGE_POST;
    viewActivity.invalidateOptionsMenu();
    viewActivity.setTitle("(" + page + "/" + totalPage + ") " + title);
    final ListView listView = (ListView) viewActivity.findViewById(R.id.bbs_post_listview);
    listView.setAdapter(
        new BaseAdapter() {

          @SuppressLint("ViewHolder")
          @Override
          public View getView(final int position, View convertView, ViewGroup parent) {
            convertView =
                viewActivity.getLayoutInflater().inflate(R.layout.bbs_post_item, parent, false);
            final PostInfo postInfo = postInfos.get(position);

            ViewSetting.setTextView(
                convertView,
                R.id.bbs_post_item_text,
                Html.fromHtml(
                    postInfo.content
                        + (!"".equals(postInfo.sig.trim()) ? ("<br><br>--<br>" + postInfo.sig) : "")
                        + "<br>",
                    source -> {
                      if (source == null) return null;
                      if (source.startsWith("/")) source = Constants.DOMAIN + source;
                      else if (source.startsWith("../"))
                        source = Constants.DOMAIN + "/bbs" + source.substring(2);

                      if (!source.startsWith(Constants.DOMAIN + "/bbsimg/expr")
                          && !source.matches("http://h5mota\\.com/bbsimg/[\\d]+\\.gif")
                          && !postInfos.get(position).imgs.contains(source))
                        postInfos.get(position).imgs.add(source);

                      final File file = MyFile.getCache(viewActivity, Utils.getHash(source));
                      if (file.exists()) {
                        Bitmap bitmap =
                            MyBitmapFactory.getCompressedBitmap(file.getAbsolutePath(), 2.5);
                        if (bitmap != null) {
                          Drawable drawable =
                              new BitmapDrawable(viewActivity.getResources(), bitmap);
                          int width = drawable.getIntrinsicWidth(),
                              height = drawable.getIntrinsicHeight();
                          width *= 1.8;
                          height *= 1.8;
                          if (width > ViewActivity.listWidth) {
                            height = (ViewActivity.listWidth * height) / width;
                            width = ViewActivity.listWidth;
                          }
                          drawable.setBounds(0, 0, width, height);
                          return drawable;
                        }
                      }
                      if (Editor.getBoolean(viewActivity, "picture", true)) {
                        final String url = source;
                        new Thread(
                                () -> {
                                  if (MyFile.urlToFile(url, file))
                                    viewActivity.handler.sendMessage(
                                        Message.obtain(
                                            viewActivity.handler,
                                            Constants.MESSAGE_IMAGE_REQUEST_FINISHED,
                                            postInfo.pid,
                                            0));
                                })
                            .start();
                      }
                      return null;
                    },
                    null));

            ViewSetting.setTextView(convertView, R.id.bbs_post_item_floor, postInfo.pid + "");
            ViewSetting.setTextView(convertView, R.id.bbs_post_item_author, postInfo.author);
            ViewSetting.setTextView(
                convertView, R.id.bbs_post_item_time, MyCalendar.format(postInfo.timestamp));
            ViewSetting.setTextView(convertView, R.id.bbs_post_item_lzl, "[" + postInfo.lzl + "]");
            convertView.setTag(position);
            return convertView;
          }

          @Override
          public long getItemId(int position) {
            return 0;
          }

          @Override
          public Object getItem(int position) {
            return null;
          }

          @Override
          public int getCount() {
            return postInfos.size();
          }
        });
    listView.setOnItemLongClickListener(
        (parent, view, position, id) -> {
          index = position;
          return false;
        });
    listView.setOnItemClickListener(
        (parent, view, position, id) -> {
          if (!Editor.getBoolean(viewActivity, "hint_post", false)) {
            new android.support.v7.app.AlertDialog.Builder(viewActivity)
                .setTitle("使用提示")
                .setMessage(
                    "如果帖子不存在楼中楼，则单击帖子和长按帖子均会"
                        + "呼出菜单。\n如果帖子存在楼中楼，则单击帖子查看楼中楼，长按帖子呼出菜单。\n\n此提示将不再显示。")
                .setPositiveButton("我知道了", null)
                .show();
            Editor.putBoolean(viewActivity, "hint_post", true);
            return;
          }

          index = position;
          PostInfo postInfo = postInfos.get(position);
          if (postInfo.lzl != 0) {
            Intent intent = new Intent(viewActivity, LZLActivity.class);
            intent.putExtra("fid", postInfo.fid);
            viewActivity.startActivityForResult(intent, 4);
            return;
          }
          listView.showContextMenu();
        });
    listView.setSelection(selection);
    selection = 0;
    viewActivity.registerForContextMenu(listView);
  }

  public void jump() {
    AlertDialog.Builder builder = new AlertDialog.Builder(viewActivity);
    builder.setTitle("跳页");
    builder.setNegativeButton("取消", null);
    final Spinner spinner = new Spinner(builder.getContext());
    ArrayList<String> arrayList = new ArrayList<String>();
    for (int i = 1; i <= totalPage; i++) arrayList.add(i + "");
    spinner.setAdapter(
        new ArrayAdapter<String>(
            builder.getContext(), android.R.layout.simple_spinner_item, arrayList));
    builder.setView(spinner);
    spinner.setSelection(page - 1);
    builder.setPositiveButton(
        "确认",
        (dialog, which) -> {
          getPosts(viewActivity.threadid, spinner.getSelectedItemPosition() + 1);
        });
    builder.show();
  }

  public void share() {
    CustomToast.showInfoToast(viewActivity, "分享功能暂缓上线！");
    // String url="https://www.chexie.net/bbs/content/?bid="+viewActivity.board+"&tid="+tmpThreadid;
    // Share.readyToShareURL(viewActivity, "分享", url, title, "分享自CAPUBBS (Android
    // "+Constants.version+")", null);
  }
}
