package com.h5mota.bbs;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.HashMap;

/** Created by oc on 2016/10/14. */
public class AllBoardsFragment extends Fragment {
  public static ArrayList<HashMap<String, String>> showList =
      new ArrayList<HashMap<String, String>>();

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    ListView listView = new ListView(inflater.getContext());
    listView.setAdapter(
        new ArrayAdapter<>(
            inflater.getContext(),
            android.R.layout.simple_expandable_list_item_1,
            Board.getNames()));
    listView.setOnItemClickListener(
        (parent, view, position, id) -> {
          Intent intent = new Intent(getActivity(), ViewActivity.class);
          intent.putExtra("type", "board");
          intent.putExtra("bid", Board.getIdByName(Board.getNames().get(position)));
          getActivity().startActivity(intent);
        });
    return listView;
  }
}
