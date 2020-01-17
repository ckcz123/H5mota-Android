package com.h5mota.lib;

public class Constants {
  public static final String DOMAIN = "https://h5mota.com";
  public static final String LOCAL = "http://127.0.0.1:1055/";
  public static final String BBS_URL = DOMAIN + "/api/client.php";

  public static final int REQUEST_BBS_GET_TOP = 1600;
  public static final int REQUEST_BBS_LOGIN = 1602;
  public static final int REQUEST_BBS_GET_LIST = 1603;
  public static final int REQUEST_BBS_GET_POST = 1604;
  public static final int REQUEST_BBS_POST = 1606;
  public static final int REQUEST_BBS_SEARCH = 1610;
  public static final int REQUEST_BBS_LZL_SHOW = 1614;
  public static final int REQUEST_BBS_LZL_POST = 1615;

  public static final int MENU_SUBACTIVITY_OPEN_IN_BROWSER = 10303;
  public static final int MENU_SUBACTIVITY_SHARE = 10304;
  public static final int MENU_SUBACTIVITY_SAVE_PICTURE = 10305;
  public static final int MENU_SUBACTIVITY_CLOSE = 10310;
  public static final int MENU_BBS_FAVORITE = 10900;
  public static final int MENU_BBS_REFRESH = 10901;
  public static final int MENU_BBS_VIEW_PREVIOUS = 10902;
  public static final int MENU_BBS_VIEW_NEXT = 10903;
  public static final int MENU_BBS_VIEW_EXTR = 10904;
  public static final int MENU_BBS_VIEW_POST = 10906;
  public static final int MENU_BBS_VIEW_SHARE = 10908;
  public static final int MENU_BBS_VIEW_JUMP = 10910;
  public static final int MENU_BBS_OPEN_IN_BROWSER = 10912;
  public static final int MENU_BBS_VIEW_EXIT = 10914;
  public static final int MENU_BBS_LZL_POST = 10916;

  public static final int SUBACTIVITY_TYPE_ABOUT = 30000;
  public static final int SUBACTIVITY_TYPE_PICTURE_RESOURCE = 30010;
  public static final int SUBACTIVITY_TYPE_PICTURE_FILE = 30011;
  public static final int SUBACTIVITY_TYPE_PICTURE_GIF = 30012;
  public static final int SUBACTIVITY_TYPE_PICTURE_URL = 30013;
  public static final int SUBACTIVITY_TYPE_WEBVIEW = 30020;
  public static final int SUBACTIVITY_TYPE_WEBVIEW_HTML = 30022;

  public static final int CONTEXT_MENU_BBS_POST_LZL = 15203;
  public static final int CONTEXT_MENU_BBS_POST = 15204;
  public static final int CONTEXT_MENU_BBS_EDIT = 15206;
  public static final int CONTEXT_MENU_BBS_URL = 15207;
  // do not use 15208-15249 !!!
  public static final int CONTEXT_MENU_BBS_IMAGE = 15250;
  // do not use 15251-15299 !!!
  public static final int CONTEXT_MENU_BBS_PICTURE = 15260;
  public static final int CONTEXT_MENU_SUBACTIVITY_SHARE_PICTURE = 15300;
  public static final int CONTEXT_MENU_SUBACTIVITY_SAVE_PICTURE = 15301;
  public static final int CONTEXT_MENU_SUBACTIVITY_DECODE_PICTURE = 15302;

  public static final int MESSAGE_SLEEP_FINISHED = 20001;
  public static final int MESSAGE_IMAGE_REQUEST_FINISHED = 20002;
  public static final int MESSAGE_IMAGE_REQUEST_FAILED = 20003;
  public static final int MESSAGE_SUBACTIVITY_DECODE_PICTURE = 21100;
  public static final int MESSAGE_BBS_LOGIN = 21200;
  public static final int MESSAGE_BBS_CHECK_UPDATE = 21300;
}
