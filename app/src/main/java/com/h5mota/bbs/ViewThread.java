package com.h5mota.bbs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import com.h5mota.R;
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

public class ViewThread {
  ArrayList<ThreadInfo> threadInfos = new ArrayList<ThreadInfo>();
  int page;
  int tmpPage;
  int totalPage;
  int selection;
  boolean isExtr;

  ViewActivity viewActivity;

  public ViewThread(ViewActivity activity) {
    viewActivity = activity;
    isExtr = false;
  }

  public void getThreads(int page) {
    getThreads(page, isExtr);
  }

  @SuppressWarnings("unchecked")
  public void getThreads(int page, boolean extr) {
    ArrayList<Parameters> arrayList = new ArrayList<Parameters>();
    arrayList.add(new Parameters("ask", "show"));
    arrayList.add(new Parameters("p", page + ""));
    arrayList.add(new Parameters("bid", viewActivity.board));
    arrayList.add(new Parameters("token", Userinfo.token));
    if (extr) arrayList.add(new Parameters("extr", "1"));
    new RequestingTask(viewActivity, "正在获取内容...", Constants.BBS_URL, Constants.REQUEST_BBS_GET_LIST)
        .execute(arrayList);
    tmpPage = page;
    isExtr = extr;
  }

  void finishRequest(String string) {
    try {
      JSONArray jsonArray = new JSONArray(string);
      // JSONObject jsonObject = new JSONObject(string);
      int code = jsonArray.getJSONObject(0).getInt("code");
      if (code != 0 && code != -1) {
        CustomToast.showErrorToast(
            viewActivity,
            jsonArray.getJSONObject(0).optString("msg", XML2Json.getErrorMessage(code)));
        viewActivity.setContentView(R.layout.bbs_thread_listview);
        viewActivity.showingPage = ViewActivity.PAGE_THREAD;
        return;
      }
      totalPage = jsonArray.getJSONObject(0).getInt("pages");
      // JSONArray datas = jsonObject.getJSONArray("datas");
      threadInfos.clear();
      int len = jsonArray.length();
      for (int i = 0; i < len; i++) {
        JSONObject thread = jsonArray.getJSONObject(i);
        String[] strings = thread.optString("author").split("/");
        threadInfos.add(
            new ThreadInfo(
                viewActivity.board,
                Board.getNameById(viewActivity.board),
                strings[0].trim(),
                strings.length > 1 ? strings[1].trim() : "",
                thread.optString("time"),
                thread.optString("text"),
                thread.getInt("tid"),
                thread.optInt("top"),
                thread.optInt("extr"),
                thread.optInt("lock")));
      }

      page = tmpPage;
      selection = 0;
      viewThreads();
    } catch (Exception e) {
      threadInfos.clear();
      CustomToast.showErrorToast(viewActivity, "获取失败");
    }
  }

  public void viewThreads() {
    viewActivity.setContentView(R.layout.bbs_thread_listview);
    viewActivity.showingPage = ViewActivity.PAGE_THREAD;
    viewActivity.invalidateOptionsMenu();
    viewActivity.setTitle("(" + page + "/" + totalPage + ") " + viewActivity.boardName);
    final ListView listView = (ListView) viewActivity.findViewById(R.id.bbs_thread_listview);
    listView.setAdapter(
        new BaseAdapter() {

          @SuppressLint("ViewHolder")
          @Override
          public View getView(int position, View convertView, ViewGroup parent) {
            convertView =
                viewActivity.getLayoutInflater().inflate(R.layout.bbs_thread_item, parent, false);
            ThreadInfo threadInfo = threadInfos.get(position);
            String title = "<font color='#006060'>" + threadInfo.title + "</font>";
            if (threadInfo.isTop) title = "<font color='red'>[置顶]</font> " + title;
            if (threadInfo.isExtr) title += " <font color='blue'>[精品]</font>";
            if (threadInfo.isLock) title += " <font color='black'>[锁定]</font>";
            ViewSetting.setTextView(convertView, R.id.bbs_thread_item_title, Html.fromHtml(title));
            ViewSetting.setTextView(
                convertView,
                R.id.bbs_thread_item_author,
                threadInfo.author + " / " + threadInfo.replyer);
            ViewSetting.setTextView(
                convertView, R.id.bbs_thread_item_time, MyCalendar.format(threadInfo.time));
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
            return threadInfos.size();
          }
        });
    listView.setOnItemClickListener(
        (parent, view, position, id) -> {
          ThreadInfo threadInfo = threadInfos.get(position);
          String threadid = threadInfo.threadid + "";
          selection = listView.getFirstVisiblePosition();
          viewActivity.viewPost.getPosts(threadid, 1);
        });
    listView.setSelection(selection);
  }

  public void jump() {
    AlertDialog.Builder builder = new AlertDialog.Builder(viewActivity);
    builder.setTitle("跳页");
    builder.setNegativeButton("取消", null);
    final Spinner spinner = new Spinner(builder.getContext());
    ArrayList<String> arrayList = new ArrayList<String>();
    for (int i = 1; i <= totalPage; i++) arrayList.add(i + "");
    spinner.setAdapter(
        new ArrayAdapter<>(builder.getContext(), android.R.layout.simple_spinner_item, arrayList));
    builder.setView(spinner);
    spinner.setSelection(page - 1);
    builder.setPositiveButton(
        "确认",
        (dialog, which) -> {
          getThreads(spinner.getSelectedItemPosition() + 1);
        });
    builder.show();
  }
}
