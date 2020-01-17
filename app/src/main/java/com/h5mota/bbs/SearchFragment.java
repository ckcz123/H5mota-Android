package com.h5mota.bbs;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
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

public class SearchFragment extends Fragment {
  static View searchView;
  static String searchBoard;
  static String searchType;
  static ArrayList<String> boards = new ArrayList<String>();
  static ArrayList<SearchInfo> searchInfos = new ArrayList<SearchInfo>();

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.bbs_search_view, container, false);
    searchView = rootView;
    set((BBSActivity) getActivity());
    return rootView;
  }

  public static void set(BBSActivity bbsActivity) {
    Spinner spinner = (Spinner) searchView.findViewById(R.id.bbs_search_type);
    ArrayAdapter<String> adapter =
        new ArrayAdapter<String>(
            bbsActivity, android.R.layout.simple_spinner_item, new String[] {"搜索标题", "搜索全文"});
    spinner.setAdapter(adapter);
    ViewSetting.setOnClickListener(searchView, R.id.bbs_search_confirm, v -> search(bbsActivity));

    boards = Board.getNames();

    Spinner boardspinner = (Spinner) searchView.findViewById(R.id.bbs_search_board);
    ArrayAdapter<String> boardadapter =
        new ArrayAdapter<String>(bbsActivity, android.R.layout.simple_spinner_item, boards);
    boardspinner.setAdapter(boardadapter);

    ListView listView = (ListView) searchView.findViewById(R.id.bbs_search_listview);
    listView.setAdapter(
        new BaseAdapter() {

          @SuppressLint("ViewHolder")
          @Override
          public View getView(int position, View convertView, ViewGroup parent) {
            SearchInfo threadInfo = searchInfos.get(position);
            if ("".equals(threadInfo.content)) {
              convertView =
                  bbsActivity.getLayoutInflater().inflate(R.layout.bbs_thread_item, parent, false);
              String title = "<font color='#006060'>" + threadInfo.title + "</font>";
              ViewSetting.setTextView(
                  convertView, R.id.bbs_thread_item_title, Html.fromHtml(title));
              ViewSetting.setTextView(convertView, R.id.bbs_thread_item_author, threadInfo.author);
              ViewSetting.setTextView(
                  convertView, R.id.bbs_thread_item_time, MyCalendar.format(threadInfo.timestamp));
            } else {
              convertView =
                  bbsActivity.getLayoutInflater().inflate(R.layout.bbs_search_item, parent, false);
              String title = "<font color='#006060'>" + threadInfo.title + "</font>";
              ViewSetting.setTextView(convertView, R.id.title, Html.fromHtml(title));
              ViewSetting.setTextView(convertView, R.id.text, Html.fromHtml(threadInfo.content));
              ViewSetting.setTextView(convertView, R.id.floor, threadInfo.pid + "");
              ViewSetting.setTextView(convertView, R.id.author, threadInfo.author);
              ViewSetting.setTextView(
                  convertView, R.id.time, MyCalendar.format(threadInfo.timestamp));
            }
            return convertView;
          }

          @Override
          public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
          }

          @Override
          public Object getItem(int position) {
            // TODO Auto-generated method stub
            return null;
          }

          @Override
          public int getCount() {
            // TODO Auto-generated method stub
            return searchInfos.size();
          }
        });
    listView.setOnItemClickListener(
        (parent, view, position, id) -> {
          SearchInfo threadInfo = searchInfos.get(position);
          Intent intent = new Intent(bbsActivity, ViewActivity.class);
          intent.putExtra("bid", threadInfo.board);
          intent.putExtra("tid", threadInfo.threadid + "");
          intent.putExtra("pid", threadInfo.pid);
          intent.putExtra("type", "thread");
          bbsActivity.startActivity(intent);
        });
  }

  @SuppressWarnings("unchecked")
  public static void search(BBSActivity bbsActivity) {
    Spinner boardSpinner = (Spinner) searchView.findViewById(R.id.bbs_search_board);
    String string = (String) boardSpinner.getSelectedItem();
    if ("".equals(string)) {
      CustomToast.showInfoToast(bbsActivity, "请选择版面！", 1500);
      return;
    }
    String text = ViewSetting.getEditTextValue(searchView, R.id.bbs_search_text).trim();
    String author = ViewSetting.getEditTextValue(searchView, R.id.bbs_search_author).trim();
    if ("".equals(text) && "".equals(author)) {
      CustomToast.showInfoToast(bbsActivity, "没有搜索的内容！", 1500);
      return;
    }
    Spinner type = (Spinner) searchView.findViewById(R.id.bbs_search_type);
    searchType = "thread";
    int index = type.getSelectedItemPosition();
    if (index == 1) searchType = "post";

    ArrayList<Parameters> arrayList = new ArrayList<Parameters>();
    arrayList.add(new Parameters("ask", "search"));
    arrayList.add(new Parameters("token", Userinfo.token));
    arrayList.add(new Parameters("bid", Board.getIdByName(string)));
    arrayList.add(new Parameters("type", searchType));
    arrayList.add(new Parameters("text", text));
    arrayList.add(new Parameters("username", author));
    new RequestingTask(bbsActivity, "正在搜索..", Constants.BBS_URL, Constants.REQUEST_BBS_SEARCH)
        .execute(arrayList);
    searchBoard = string;
  }

  public static void finishSearch(BBSActivity bbsActivity, String string) {
    try {
      JSONArray jsonArray = new JSONArray(string);

      int code = jsonArray.getJSONObject(0).getInt("code");
      if (code != 0 && code != -1) {
        CustomToast.showErrorToast(
            bbsActivity,
            jsonArray.getJSONObject(0).optString("msg", XML2Json.getErrorMessage(code)));
        return;
      }

      searchInfos.clear();
      int len = jsonArray.length();
      for (int i = 1; i < len; i++) {
        JSONObject object = jsonArray.getJSONObject(i);

        if ("thread".equals(searchType))
          searchInfos.add(
              new SearchInfo(
                  object.optString("bid"),
                  object.getInt("tid"),
                  object.getString("text"),
                  object.optString("author"),
                  0,
                  MyCalendar.format(object.optString("time"))));
        else
          searchInfos.add(
              new SearchInfo(
                  object.optString("bid"),
                  object.getInt("tid"),
                  object.optString("title"),
                  object.optString("text"),
                  object.optString("author"),
                  object.optInt("floor"),
                  MyCalendar.format(object.optString("time"))));
      }

      ListView listView = (ListView) searchView.findViewById(R.id.bbs_search_listview);
      BaseAdapter baseAdapter = (BaseAdapter) listView.getAdapter();
      baseAdapter.notifyDataSetChanged();

      if (len >= 100) CustomToast.showInfoToast(bbsActivity, "只显示前100条结果！", 1500);
      if (searchInfos.size() == 0) CustomToast.showInfoToast(bbsActivity, "没有搜索到结果！", 1500);

    } catch (Exception e) {
      CustomToast.showErrorToast(bbsActivity, "搜索失败", 1500);
    }
  }
}
