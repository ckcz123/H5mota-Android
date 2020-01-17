package com.h5mota.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import org.jetbrains.annotations.NotNull;

public class Cookies implements CookieJar {

  private static Cookies instance = new Cookies();
  private final HashMap<String, HashMap<String, String>> cookies = new HashMap<>();

  public static Cookies getInstance() {
    return instance;
  }

  @NotNull
  @Override
  public List<Cookie> loadForRequest(@NotNull HttpUrl httpUrl) {
    List<Cookie> list = new ArrayList<>();
    HashMap<String, String> hashMap = cookies.get(httpUrl.host());
    if (hashMap == null) return list;
    for (Map.Entry<String, String> entry : hashMap.entrySet()) {
      list.add(new Cookie.Builder().name(entry.getKey()).value(entry.getValue()).build());
    }
    return list;
  }

  @Override
  public void saveFromResponse(@NotNull HttpUrl httpUrl, @NotNull List<Cookie> list) {
    String domain = httpUrl.host();
    HashMap<String, String> hashMap = cookies.get(domain);
    if (hashMap == null) hashMap = new HashMap<>();
    for (Cookie cookie : list) {
      hashMap.put(cookie.name(), cookie.value());
    }
    cookies.put(domain, hashMap);
  }
}
