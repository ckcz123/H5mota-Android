package com.h5mota.bbs;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import com.h5mota.R;
import com.h5mota.lib.BaseActivity;
import com.h5mota.lib.Constants;
import com.h5mota.lib.Parameters;
import com.h5mota.lib.RequestingTask;
import com.h5mota.lib.ViewSetting;
import com.h5mota.lib.XML2Json;
import com.h5mota.lib.json.JSONArray;
import com.h5mota.lib.json.JSONObject;
import com.h5mota.lib.view.CustomToast;
import java.util.ArrayList;

public class PostActivity extends BaseActivity {
  String type = "";
  String board = "";
  String threadid = "";
  String postid = "";
  String quote = "";
  String text = "";

  @Override
  @SuppressWarnings("unchecked")
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if ("".equals(Userinfo.token)) {
      CustomToast.showInfoToast(this, "请先登录！");
      super.wantToExit();
      return;
    }

    Bundle bundle = getIntent().getExtras();

    type = bundle.getString("type", "post");

    board = bundle.getString("bid", "");
    threadid = bundle.getString("tid", "");
    String title = bundle.getString("title", "").trim();
    postid = bundle.getString("pid", "");
    quote = bundle.getString("quote", "");
    text = bundle.getString("text", "");

    setContentView(R.layout.bbs_postpage);

    String tt = "发表帖子";
    if ("reply".equals(type)) tt = "回复帖子";
    if ("edit".equals(type)) tt = "编辑帖子";
    setTitle(tt);
    if (!"".equals(title)) {
      ViewSetting.setEditTextValue(this, R.id.bbs_postpage_title, "Re: " + title);
    }
    Button button = (Button) findViewById(R.id.bbs_postpage_button);
    String hint = "发表";
    if ("reply".equals(type)) hint = "回复";
    if ("edit".equals(type)) hint = "编辑";
    button.setText(hint);
    button.setOnClickListener(v -> post());
    if ("reply".equals(type)) {
      if (!"".equals(quote)) {
        ViewSetting.setEditTextValue(this, R.id.bbs_postpage_text, quote + "\n\n");
      }
    }
    if ("edit".equals(type)) {
      ViewSetting.setEditTextValue(this, R.id.bbs_postpage_text, text);
    }
    if (!"".equals(title)) findViewById(R.id.bbs_postpage_text).requestFocus();
  }

  protected void finishRequest(int type, String string) {
    if (type == Constants.REQUEST_BBS_POST) finishPost(string);
  }

  @SuppressWarnings("unchecked")
  void post() {

    if ("edit".equals(type)) {
      edit();
      return;
    }

    String title = ViewSetting.getEditTextValue(this, R.id.bbs_postpage_title).trim();
    String text = ViewSetting.getEditTextValue(this, R.id.bbs_postpage_text);
    if ("".equals(title)) {
      CustomToast.showErrorToast(this, "标题不能为空！", 1500);
      return;
    }
    // if (!text.contains("发自 CAPUBBS"))
    //    text += "\n\n--\n发自 CAPUBBS (Android " + Constants.version + ")\n";
    // CheckBox checkBox = (CheckBox) findViewById(R.id.bbs_postpage_anonymous);
    // String anonymous = checkBox.isChecked() ? "1" : "0";

    String sig = "0";
    // if (((RadioButton) findViewById(R.id.sig1)).isChecked()) sig = "1";
    // if (((RadioButton) findViewById(R.id.sig2)).isChecked()) sig = "2";
    // if (((RadioButton) findViewById(R.id.sig3)).isChecked()) sig = "3";

    ArrayList<Parameters> arrayList = new ArrayList<>();
    arrayList.add(new Parameters("ask", "post"));
    arrayList.add(new Parameters("token", Userinfo.token));
    arrayList.add(new Parameters("bid", board));
    arrayList.add(new Parameters("title", title));
    arrayList.add(new Parameters("text", text));
    arrayList.add(new Parameters("sig", sig));
    arrayList.add(new Parameters("os", "android"));
    if ("reply".equals(type)) {
      arrayList.add(new Parameters("tid", threadid));
    }
    new RequestingTask(this, "正在发表...", Constants.BBS_URL, Constants.REQUEST_BBS_POST)
        .execute(arrayList);
  }

  @SuppressWarnings("unchecked")
  void edit() {
    String title = ViewSetting.getEditTextValue(this, R.id.bbs_postpage_title).trim();
    String text = ViewSetting.getEditTextValue(this, R.id.bbs_postpage_text);
    if ("".equals(title)) {
      CustomToast.showErrorToast(this, "标题不能为空！", 1500);
      return;
    }
    // if (!text.contains("发自 CAPUBBS"))
    //    text += "\n\n--\n发自 CAPUBBS (Android " + Constants.version + ")\n";
    String sig = "0";
    // if (((RadioButton) findViewById(R.id.sig1)).isChecked()) sig = "1";
    // if (((RadioButton) findViewById(R.id.sig2)).isChecked()) sig = "2";
    // if (((RadioButton) findViewById(R.id.sig3)).isChecked()) sig = "3";

    ArrayList<Parameters> arrayList = new ArrayList<>();
    arrayList.add(new Parameters("ask", "post"));
    arrayList.add(new Parameters("token", Userinfo.token));
    arrayList.add(new Parameters("bid", board));
    arrayList.add(new Parameters("title", title));
    arrayList.add(new Parameters("text", text));
    arrayList.add(new Parameters("sig", sig));
    arrayList.add(new Parameters("tid", threadid));
    arrayList.add(new Parameters("pid", postid));
    new RequestingTask(this, "正在修改...", Constants.BBS_URL, Constants.REQUEST_BBS_POST)
        .execute(arrayList);
  }

  void finishPost(String string) {
    try {
      JSONObject jsonObject = new JSONArray(string).getJSONObject(0);
      int code = jsonObject.getInt("code");
      if (code != 0) {
        CustomToast.showErrorToast(
            this, jsonObject.optString("msg", XML2Json.getErrorMessage(code)), 1500);
        return;
      }
      setResult(RESULT_OK);
      CustomToast.showSuccessToast(this, "发表成功！");
      finish();
    } catch (Exception e) {
      CustomToast.showErrorToast(this, "发表失败", 1500);
    }
  }

  public void finishGetEdit(String string) {
    try {
      JSONObject jsonObject = new JSONObject(string);
      int code = jsonObject.getInt("code");
      if (code != 0) {
        CustomToast.showErrorToast(this, jsonObject.optString("msg", "内容拉取失败"), 1300);
        super.wantToExit();
        return;
      }
      String title = jsonObject.optString("title");
      String text = jsonObject.optString("text");

      ViewSetting.setEditTextValue(this, R.id.bbs_postpage_title, title);
      ViewSetting.setEditTextValue(this, R.id.bbs_postpage_text, "\n\n" + text + "\n");
      findViewById(R.id.bbs_postpage_text).requestFocus();
      ((EditText) findViewById(R.id.bbs_postpage_text)).setSelection(0);

    } catch (Exception e) {
      CustomToast.showErrorToast(this, "内容获取失败", 1300);
      super.wantToExit();
    }
  }
}
