package com.h5mota.lib;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import com.h5mota.lib.view.CustomToast;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RequestingTask extends AsyncTask<Parameters, String, Parameters> {

  private ProgressDialog progressDialog;
  private String requestString;
  private int requestType;
  private BaseActivity baseActivity;

  /**
   * 发起一个http调用；如果访问失败那么直接Toast提醒，无返回
   *
   * @param msg 提示消息
   * @param url 请求地址
   * @param type 访问类型
   */
  public RequestingTask(BaseActivity activity, String msg, String url, int type) {
    baseActivity = activity;
    progressDialog = new ProgressDialog(activity);
    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    progressDialog.setTitle("提示");
    progressDialog.setMessage(msg);
    progressDialog.setIndeterminate(false);
    progressDialog.setCancelable(false);
    requestString = url;
    requestType = type;
  }

  @Override
  protected void onPreExecute() {
    progressDialog.show();
  }

  public void execute(ArrayList<Parameters> list) {
    execute(list.toArray(new Parameters[0]));
  }

  @Override
  protected Parameters doInBackground(Parameters... params) {
    return connect(requestString, params);
  }

  public static Parameters connect(String url, Parameters... params) {
    OkHttpClient okHttpClient =
        new OkHttpClient()
            .newBuilder()
            .followRedirects(true)
            .followSslRedirects(true)
            .connectTimeout(4000, TimeUnit.MILLISECONDS)
            .readTimeout(13000, TimeUnit.MILLISECONDS)
            .cookieJar(Cookies.getInstance())
            .build();
    Request.Builder builder = new Request.Builder().url(url);
    if (params.length == 0) {
      builder.get();
    } else {
      FormBody.Builder formBuilder = new FormBody.Builder();
      for (Parameters parameter : params) {
        formBuilder.add(parameter.name, parameter.value);
      }
      builder.post(formBuilder.build());
    }
    try (Response response = okHttpClient.newCall(builder.build()).execute()) {
      return new Parameters("" + response.code(), XML2Json.toJson(response.body().string()));
    } catch (Exception e) {
      Log.e("Error", Log.getStackTraceString(e));
      return new Parameters("-1", "");
    }
  }

  public static Parameters connect(String url, ArrayList<Parameters> list) {
    return connect(url, list.toArray(new Parameters[0]));
  }

  @Override
  protected void onPostExecute(Parameters parameters) {
    progressDialog.dismiss();
    if (!"200".equals(parameters.name)) {
      if ("-1".equals(parameters.name)) {
        CustomToast.showInfoToast(baseActivity, "无法连接网络(-1,-1)");
      } else CustomToast.showInfoToast(baseActivity, "无法连接到服务器 (HTTP " + parameters.name + ")");
    } else baseActivity.finishRequest(requestType, parameters.value);
  }
}
