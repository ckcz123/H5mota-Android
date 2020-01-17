package com.h5mota.bbs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import com.h5mota.R;
import com.h5mota.lib.BaseActivity;
import com.h5mota.lib.Constants;
import com.h5mota.lib.MyCalendar;
import com.h5mota.lib.Parameters;
import com.h5mota.lib.RequestingTask;
import com.h5mota.lib.ViewSetting;
import com.h5mota.lib.XML2Json;
import com.h5mota.lib.json.JSONArray;
import com.h5mota.lib.json.JSONObject;
import com.h5mota.lib.view.CustomToast;
import java.util.ArrayList;

/** Created by oc on 2016/10/15. */
public class LZLActivity extends BaseActivity {

  int fid;

  class LZLInfo {
    String author;
    int lzlid;
    String content;
    long time;

    public LZLInfo(int _lzlid, String _author, String _content, String _time) {
      lzlid = _lzlid;
      author = _author;
      content = _content;
      time = MyCalendar.format(_time);
    }
  }

  ArrayList<LZLInfo> lzlInfos = new ArrayList<>();
  ListView listView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    fid = getIntent().getIntExtra("fid", -1);
    if (fid == -1) {
      wantToExit();
      return;
    }
    listView = new ListView(this);
    setContentView(listView);
    setTitle("查看楼中楼");
    get();
    if (getIntent().getBooleanExtra("add", false)) reply("");
  }

  protected void finishRequest(int requesttype, String string) {
    if (requesttype == Constants.REQUEST_BBS_LZL_SHOW) {
      try {
        lzlInfos.clear();
        JSONArray jsonArray = new JSONArray(string);
        int code = jsonArray.getJSONObject(0).getInt("code");
        if (code != 0 && code != -1) {
          CustomToast.showErrorToast(
              this, jsonArray.getJSONObject(0).optString("msg", XML2Json.getErrorMessage(code)));
        } else {
          int len = jsonArray.length();
          for (int i = 1; i < len; i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            lzlInfos.add(
                new LZLInfo(
                    jsonObject.optInt("id"),
                    jsonObject.optString("author"),
                    jsonObject.optString("text"),
                    jsonObject.optString("time")));
          }
        }
      } catch (Exception e) {
        lzlInfos.clear();
      }
      show();
      return;
    }
    if (requesttype == Constants.REQUEST_BBS_LZL_POST) {
      try {
        JSONArray jsonArray = new JSONArray(string);
        int code = jsonArray.getJSONObject(0).getInt("code");
        if (code != 0 && code != -1)
          CustomToast.showErrorToast(
              this, jsonArray.getJSONObject(0).optString("msg", XML2Json.getErrorMessage(code)));
        else {
          CustomToast.showSuccessToast(this, "发表成功！");
          setResult(RESULT_OK);
          get();
        }
      } catch (Exception e) {
        CustomToast.showErrorToast(this, "发表失败");
      }
    }
  }

  @SuppressWarnings("unchecked")
  void get() {
    ArrayList<Parameters> arrayList = new ArrayList<>();
    arrayList.add(new Parameters("ask", "lzl"));
    arrayList.add(new Parameters("method", "show"));
    arrayList.add(new Parameters("fid", "" + fid));
    arrayList.add(new Parameters("token", Userinfo.token));

    new RequestingTask(this, "正在获取...", Constants.BBS_URL, Constants.REQUEST_BBS_LZL_SHOW)
        .execute(arrayList);
  }

  void show() {
    listView.setAdapter(
        new BaseAdapter() {
          @Override
          public int getCount() {
            return lzlInfos.size();
          }

          @Override
          public Object getItem(int position) {
            return null;
          }

          @Override
          public long getItemId(int position) {
            return 0;
          }

          @SuppressLint("ViewHolder")
          @Override
          public View getView(int position, View convertView, ViewGroup parent) {
            convertView = getLayoutInflater().inflate(R.layout.lzl_view, parent, false);
            LZLInfo lzlInfo = lzlInfos.get(position);
            ViewSetting.setTextView(convertView, R.id.author, lzlInfo.author);
            ViewSetting.setTextView(convertView, R.id.text, lzlInfo.content);
            ViewSetting.setTextView(convertView, R.id.time, MyCalendar.format(lzlInfo.time));
            return convertView;
          }
        });
    listView.setOnItemClickListener(
        (parent, view, position, id) -> {
          String author = ViewSetting.getTextView(view, R.id.author);
          if (!"".equals(author)) reply("回复 @" + author + ": ");
          else reply("");
        });
  }

  @SuppressWarnings("unchecked")
  @SuppressLint("InflateParams")
  void reply(String string) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    final View view = getLayoutInflater().inflate(R.layout.edittext, null, false);
    ViewSetting.setEditTextValue(view, R.id.edittext, string);
    builder
        .setView(view)
        .setTitle("回复楼中楼")
        .setPositiveButton(
            "发送",
            (dialog, which) -> {
              String content = ViewSetting.getEditTextValue(view, R.id.edittext).trim();
              if (content.length() >= 240) {
                CustomToast.showErrorToast(LZLActivity.this, "请不要超过240字！");
                reply(content);
                return;
              }
              if (content.length() == 0) {
                CustomToast.showErrorToast(LZLActivity.this, "内容不能为空");
                reply("");
                return;
              }
              ArrayList<Parameters> arrayList = new ArrayList<>();
              arrayList.add(new Parameters("ask", "lzl"));
              arrayList.add(new Parameters("method", "post"));
              arrayList.add(new Parameters("fid", fid + ""));
              arrayList.add(new Parameters("text", content));
              arrayList.add(new Parameters("token", Userinfo.token));

              new RequestingTask(
                      LZLActivity.this, "正在发表", Constants.BBS_URL, Constants.REQUEST_BBS_LZL_POST)
                  .execute(arrayList);
            })
        .setNegativeButton("取消", null)
        .show();
  }

  protected void wantToExit() {
    Intent intent = new Intent();
    intent.putExtra("lzl", lzlInfos.size());
    setResult(RESULT_OK, intent);
    finish();
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    menu.clear();
    menu.add(Menu.NONE, Constants.MENU_BBS_LZL_POST, Constants.MENU_BBS_LZL_POST, "")
        .setIcon(R.drawable.ic_reply_white_36dp)
        .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    return super.onPrepareOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == Constants.MENU_BBS_LZL_POST) {
      reply("");
    }
    return super.onOptionsItemSelected(item);
  }
}
