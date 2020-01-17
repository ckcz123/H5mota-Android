package com.h5mota.lib.subactivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.h5mota.R;
import com.h5mota.lib.ImageRequest;
import com.h5mota.lib.view.CustomToast;
import java.io.File;
import java.net.URL;

class MyWebView {
  private SubActivity subActivity;
  private String title;
  boolean loading;
  SwipeRefreshLayout swipeRefreshLayout;

  public MyWebView(SubActivity _sub) {
    subActivity = _sub;
  }

  private void initWebView(WebView webView, boolean single_column) {
    webView.getSettings().setJavaScriptEnabled(true);
    webView.addJavascriptInterface(new JSInterface(subActivity), "imageclick");
    // webView.setVerticalScrollBarEnabled(false);
    webView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_INSET);
    if (single_column) {
      webView.getSettings().setUseWideViewPort(false);
      webView.setHorizontalScrollBarEnabled(false);
      webView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
    } else {
      webView.getSettings().setUseWideViewPort(true);
      webView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
      webView.getSettings().setSupportZoom(true);
      webView.getSettings().setLoadWithOverviewMode(true);
      webView.getSettings().setBuiltInZoomControls(true);
    }
    webView.setDownloadListener(
        (url, userAgent, contentDisposition, mimetype, contentLength) -> {
          try {
            CookieManager cookieManager = CookieManager.getInstance();
            URL url2 = new URL(url);
            String cookie = cookieManager.getCookie(url2.getHost());

            Request request = new Request(Uri.parse(url));

            request.addRequestHeader("Cookie", cookie);
            request.setMimeType(mimetype);
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            String filename = URLUtil.guessFileName(url, contentDisposition, mimetype);
            new File(Environment.getExternalStorageDirectory() + "/H5mota/").mkdirs();
            File file =
                new File(
                    Environment.getExternalStorageDirectory() + "/H5mota/" + filename);
            if (file.exists()) file.delete();
            request.setDestinationUri(Uri.fromFile(file));
            request.setTitle("正在下载" + filename + "...");
            request.setDescription("文件保存在" + file.getAbsolutePath());
            DownloadManager downloadManager =
                (DownloadManager) subActivity.getSystemService(Context.DOWNLOAD_SERVICE);
            downloadManager.enqueue(request);

            CustomToast.showInfoToast(subActivity, "文件下载中，请在通知栏查看进度");
          } catch (Exception e) {
            subActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
          }
        });
    webView.setWebViewClient(
        new WebViewClient() {
          @Override
          public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith("javascript:")) {
              view.loadUrl(url);
              return true;
            }

            MyWebView.this.title = "";
            subActivity.url = url;
            view.loadUrl(url);
            loading = true;
            swipeRefreshLayout.setRefreshing(true);
            subActivity.setTitle("Loading...");
            subActivity.invalidateOptionsMenu();
            return true;
          }

          @Override
          public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            String string = view.getTitle();
            loading = false;
            subActivity.setRefresh();
            subActivity.invalidateOptionsMenu();
            if (!"".equals(MyWebView.this.title)) subActivity.setTitle(MyWebView.this.title);
            else if (!"".equals(string)) subActivity.setTitle(string);
            else subActivity.setTitle("查看网页");

            view.loadUrl(
                "javascript:(function(){"
                    + "var objs=document.getElementsByTagName(\"img\");"
                    + "for (var i=0;i<objs.length;i++) {"
                    + "    objs[i].onclick=function() {"
                    + "        window.imageclick.openImage(this.src);"
                    + "    }"
                    + "}"
                    + "})()");
          }
        });
    webView.setWebChromeClient(
        new WebChromeClient() {
          @Override
          public boolean onJsAlert(
              WebView view, String url, String message, final JsResult result) {
            new AlertDialog.Builder(subActivity)
                .setTitle("提示")
                .setMessage(message)
                .setPositiveButton(
                    "确认",
                    (dialog, which) -> {
                      result.confirm();
                    })
                .setCancelable(true)
                .setOnCancelListener(dialog -> result.confirm())
                .show();
            return true;
          }
        });
  }

  @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
  public MyWebView showWebView(String title, String url) {
    subActivity.setTitle("loading...");
    if (title == null) title = "";
    title = title.trim();
    this.title = title;
    subActivity.setContentView(R.layout.subactivity_webview);
    swipeRefreshLayout =
        (SwipeRefreshLayout) subActivity.findViewById(R.id.subactivity_swipeRefreshLayout);
    swipeRefreshLayout.setColorSchemeResources(
        android.R.color.holo_purple,
        android.R.color.holo_green_light,
        android.R.color.holo_blue_bright,
        android.R.color.holo_orange_light);
    swipeRefreshLayout.setOnRefreshListener(
        new SwipeRefreshLayout.OnRefreshListener() {
          public void onRefresh() {
            subActivity.setRefresh();
          }
        });

    loading = true;
    swipeRefreshLayout.setRefreshing(loading);
    subActivity.invalidateOptionsMenu();
    subActivity.webView = (WebView) subActivity.findViewById(R.id.subactivity_webview);
    WebView webView = subActivity.webView;
    initWebView(webView, subActivity.getIntent().getBooleanExtra("single_column", true));
    url = url.trim();
    String postArea = subActivity.getIntent().getStringExtra("post");
    if (postArea == null) webView.loadUrl(url);
    else webView.postUrl(url, postArea.getBytes());
    return this;
  }

  public MyWebView showWebHtml(String title, String html) {
    subActivity.setContentView(R.layout.subactivity_webview);
    swipeRefreshLayout =
        (SwipeRefreshLayout) subActivity.findViewById(R.id.subactivity_swipeRefreshLayout);
    swipeRefreshLayout.setColorSchemeResources(
        android.R.color.holo_purple,
        android.R.color.holo_green_light,
        android.R.color.holo_blue_bright,
        android.R.color.holo_orange_light);
    swipeRefreshLayout.setOnRefreshListener(() -> subActivity.setRefresh());
    loading = false;
    if (title == null || "".equals(title.trim())) title = "查看网页";
    subActivity.setTitle(title);
    subActivity.webView = (WebView) subActivity.findViewById(R.id.subactivity_webview);
    initWebView(
        subActivity.webView, subActivity.getIntent().getBooleanExtra("single_column", true));
    subActivity.webView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
    return this;
  }

  private class JSInterface {
    private Context context;

    public JSInterface(Context _context) {
      context = _context;
    }

    @JavascriptInterface
    public void openImage(String imgurl) {
      ImageRequest.showImage(context, imgurl);
    }
  }
}
