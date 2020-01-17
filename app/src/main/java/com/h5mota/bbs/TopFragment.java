package com.h5mota.bbs;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import com.h5mota.R;
import com.h5mota.lib.Constants;
import com.h5mota.lib.Editor;
import com.h5mota.lib.MyCalendar;
import com.h5mota.lib.RequestingTask;
import com.h5mota.lib.ViewSetting;
import com.h5mota.lib.json.JSONArray;
import com.h5mota.lib.json.JSONObject;
import com.h5mota.lib.view.CustomToast;
import java.util.ArrayList;

public class TopFragment extends Fragment {
  public static TopFragment topFragment;
  public static ArrayList<ThreadInfo> tops = new ArrayList<>();
  static View topView;

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.bbs_top_listview, container, false);
    topFragment = this;
    topView = rootView;
    realShowView((BBSActivity) getActivity(), true);
    return rootView;
  }

  @SuppressWarnings("unchecked")
  public static void showView(BBSActivity bbsActivity) {
    new RequestingTask(
            bbsActivity,
            "正在获取最近热点...",
            Constants.BBS_URL + "?ask=hot&hotnum=30",
            Constants.REQUEST_BBS_GET_TOP)
        .execute();
  }

  public static void finishRequest(BBSActivity bbsActivity, String string) {
    tops = new ArrayList<>();
    try {
      JSONArray jsonArray = new JSONArray(string);
      int len = jsonArray.length();
      for (int i = 0; i < len; i++) {
        JSONObject jsonObject = jsonArray.getJSONObject(i);
        try {
          tops.add(
              new ThreadInfo(
                  i + 1,
                  jsonObject.optString("bid"),
                  Board.getNameById(jsonObject.optString("bid")),
                  jsonObject.optString("author"),
                  jsonObject.optString("replyer"),
                  jsonObject.optString("time"),
                  jsonObject.optString("text"),
                  jsonObject.getInt("tid"),
                  jsonObject.getInt("pid") + 1));
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    } catch (Exception e) {
      tops = new ArrayList<ThreadInfo>();
    } finally {
      if (tops.size() == 0) {
        CustomToast.showInfoToast(bbsActivity, "暂时没有最近热点，请刷新重试", 1500);
      }
      realShowView(bbsActivity, false);
    }
  }

  public static void realShowView(BBSActivity bbsActivity, boolean refresh) {
    if (tops.size() == 0 && refresh) {
      showView(bbsActivity);
      return;
    }

    ListView listView = (ListView) topView.findViewById(R.id.bbs_top_listview);
    listView.setAdapter(
        new BaseAdapter() {
          @Override
          @SuppressLint("ViewHolder")
          public View getView(int position, View convertView, ViewGroup parent) {
            convertView =
                bbsActivity.getLayoutInflater().inflate(R.layout.bbs_top_listitem, parent, false);
            ThreadInfo threadInfo = tops.get(position);
            ViewSetting.setTextView(convertView, R.id.bbs_top_item_from, threadInfo.boardName);
            ViewSetting.setTextView(convertView, R.id.bbs_top_item_rank, "#" + threadInfo.rank);
            ViewSetting.setTextView(convertView, R.id.bbs_top_item_title, threadInfo.title);
            ViewSetting.setTextView(convertView, R.id.bbs_top_item_author, threadInfo.replyer);
            ViewSetting.setTextView(
                convertView, R.id.bbs_top_item_time, MyCalendar.format(threadInfo.time));
            return convertView;
          }

          @Override
          public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
          }

          @Override
          public Object getItem(int position) {
            return null;
          }

          @Override
          public int getCount() {
            return tops.size();
          }
        });

    listView.setOnItemClickListener(
        (parent, view, position, id) -> {
          if (!Editor.getBoolean(bbsActivity, "hint_top", false)) {
            new AlertDialog.Builder(bbsActivity)
                .setTitle("使用提示")
                .setMessage("单击热点可查看最新回复；长按热点可查看该主题。\n\n此提示将不再显示。")
                .setPositiveButton("我知道了", null)
                .show();
            Editor.putBoolean(bbsActivity, "hint_top", true);
            return;
          }

          ThreadInfo threadInfo = tops.get(position);
          Intent intent = new Intent(bbsActivity, ViewActivity.class);
          intent.putExtra("bid", threadInfo.board);
          intent.putExtra("tid", threadInfo.threadid + "");
          intent.putExtra("pid", threadInfo.pid);
          intent.putExtra("type", "thread");
          bbsActivity.startActivity(intent);
        });

    listView.setOnItemLongClickListener(
        (parent, view, position, id) -> {
          ThreadInfo threadInfo = tops.get(position);
          Intent intent = new Intent(bbsActivity, ViewActivity.class);
          intent.putExtra("bid", threadInfo.board);
          intent.putExtra("tid", threadInfo.threadid + "");
          intent.putExtra("type", "thread");
          bbsActivity.startActivity(intent);
          return true;
        });
  }
}
