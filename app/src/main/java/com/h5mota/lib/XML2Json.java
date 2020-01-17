package com.h5mota.lib;

import android.util.Log;
import com.h5mota.lib.json.JSONArray;
import com.h5mota.lib.json.JSONObject;
import com.h5mota.lib.json.XML;

public class XML2Json {

  public static String toJson(String xml) {
    try {
      // String string=XML.toJSONObject(xml).optJSONObject("capu").optJSONArray("info").toString();
      JSONObject jsonObject = XML.toJSONObject(xml).optJSONObject("capu");
      String string;
      if (jsonObject.optJSONArray("info") != null)
        string = jsonObject.optJSONArray("info").toString();
      else {
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(
            jsonObject.optJSONObject("info") == null
                ? jsonObject
                : jsonObject.optJSONObject("info"));
        string = jsonArray.toString();
      }
      Log.w("json", string);
      return string;
    } catch (Exception e) {
      e.printStackTrace();
    }
    try {
      JSONObject jsonObject = new JSONObject();
      jsonObject.put("code", 20);
      jsonObject.put("msg", "XML解析失败，请联系管理员以获取帮助。");
      JSONArray jsonArray = new JSONArray();
      jsonArray.put(jsonObject);
      return jsonArray.toString();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return "";
  }

  public static String getErrorMessage(int code) {
    switch (code) {
      case -25:
        return "超时，请重新登录 (errorcode: -1)";
      case 1:
        return "用户不存在或密码错误 (errorcode: 1)";
      case 2:
        return "用户不存在 (errorcode: 2)";
      case 3:
        return "您已被封禁 (errorcode: 3)";
      case 4:
        return "两次发表时间差不能少于15s (errorcode: 4)";
      case 5:
        return "文章已被锁定 (errorcode: 5)";
      case 6:
        return "内部错误 (errorcode: 6)";
      case 7:
        return "只能编辑自己的帖子 (errorcode: 7)";
      case 8:
        return "用户名有非法字符 (errorcode: 8)";
      case 9:
        return "用户名已存在 (errorcode: 9)";
      case 10:
        return "权限不够 (errorcode: 10)";
      case 11:
        return "请直接删除主题 (errorcode: 11)";
      default:
        break;
    }
    return "发生内部错误 (errorcode: " + code + ")；请联系管理员获取帮助。";
  }
}
