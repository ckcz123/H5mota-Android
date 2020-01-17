package com.h5mota.bbs;

public class SearchInfo {
  String board;
  int threadid;
  String author;
  long timestamp;
  int pid;
  String title;
  String content;

  public SearchInfo(
      String _board, int _threadid, String _title, String _author, int _pid, long _timestamp) {
    board = new String(_board);
    threadid = _threadid;
    title = new String(_title);
    author = new String(_author);
    pid = _pid;
    timestamp = _timestamp;
    content = "";
  }

  public SearchInfo(
      String _board,
      int _threadid,
      String _title,
      String _content,
      String _author,
      int _pid,
      long _timestamp) {
    this(_board, _threadid, _title, _author, _pid, _timestamp);
    content = _content;
  }
}
