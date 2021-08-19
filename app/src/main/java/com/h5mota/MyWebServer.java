package com.h5mota;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.SimpleWebServer;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by oc on 2018/4/24.
 */
public class MyWebServer extends SimpleWebServer {

  public MyWebServer(String host, int port, File wwwroot, boolean quiet) {
    super(host, port, wwwroot, quiet);
  }

  public boolean shouldRedirect(String path) {
    // 只有"存档同步"才会上传服务器
    return path.equals("/games/sync.php");
  }

  @Override
  public Response serve(IHTTPSession session) {
    String path = session.getUri();
    if (session.getMethod() == Method.POST && shouldRedirect(path)) {

      try {
        session.parseBody(new HashMap<>());
      } catch (ResponseException | IOException e) {
        Log.e("Parse Body", "error", e);
      }

      OkHttpClient okHttpClient =
          new OkHttpClient().newBuilder().build();
      FormBody.Builder formBody = new FormBody.Builder();

      Map<String, List<String>> map = session.getParameters();
      for (Map.Entry<String, List<String>> entry : map.entrySet()) {
        formBody.add(entry.getKey(), entry.getValue().get(0));
      }

      try (okhttp3.Response response =
               okHttpClient
                   .newCall(
                       new Request.Builder().url(MainActivity.DOMAIN + path).post(formBody.build()).build())
                   .execute()) {
        int code = response.code();
        if (code == 200) {
          return newFixedLengthResponse(
              Response.Status.OK, "application/json", response.body().string());
        } else {
          return newFixedLengthResponse(
              Response.Status.lookup(code), "text/plain", response.message());
        }
      } catch (Exception ignore) {
      }

      return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "");
    } else {
      return super.serve(session);
    }
  }
}
