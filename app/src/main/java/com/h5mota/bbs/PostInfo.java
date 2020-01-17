package com.h5mota.bbs;

import com.h5mota.lib.MyCalendar;
import java.util.ArrayList;

public class PostInfo {
  String author;
  int pid;
  int fid;
  long timestamp;
  int lzl;
  String content;
  String sig;
  ArrayList<String> imgs;

  public PostInfo(
      String _author, int _pid, int _fid, String _time, int _lzl, String _content, String _sig) {
    author = _author;
    pid = _pid;
    fid = _fid;
    timestamp = MyCalendar.format(_time);
    lzl = _lzl;
    content = _content;
    sig = _sig;
    imgs = new ArrayList<>();
  }
}
