package com.h5mota.bbs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Created by oc on 2016/10/14. */
public class Board {
  String board, name, category;
  boolean anonymous;
  int total;
  public static HashMap<String, Board> boards = new HashMap<String, Board>();

  public static void load() {
    boards.clear();
    boards.put("1", new Board("1", "交流区"));
    boards.put("2", new Board("2", "灌水区"));
  }

  public static ArrayList<String> getNames() {
    if (boards.isEmpty()) load();
    ArrayList<String> arrayList = new ArrayList<>();
    for (Map.Entry<String, Board> entry : boards.entrySet()) arrayList.add(entry.getValue().name);
    // return arrayList;
    Collections.sort(
        arrayList,
        (o1, o2) -> Integer.parseInt(getIdByName(o1)) - Integer.parseInt(getIdByName(o2)));
    return arrayList;
  }

  public static String getIdByName(String name) {
    if (boards.isEmpty()) load();
    for (Map.Entry<String, Board> entry : boards.entrySet())
      if (entry.getValue().name.equals(name)) return entry.getKey();
    return "0";
  }

  public static String getNameById(String id) {
    if (boards.isEmpty()) load();
    if (boards.containsKey(id)) return boards.get(id).name;
    else return "未知版面";
  }

  public static boolean containBoard(String id) {
    if (boards.isEmpty()) load();
    return boards.containsKey(id);
  }

  public Board(String _board, String _name) {
    this(_board, _name, "", 0, 0);
  }

  public Board(String _board, String _name, String _category, int _anonymous, int total) {
    board = new String(_board);
    name = new String(_name);
    category = new String(_category);
    anonymous = _anonymous == 1;
    this.total = total;
  }
}
