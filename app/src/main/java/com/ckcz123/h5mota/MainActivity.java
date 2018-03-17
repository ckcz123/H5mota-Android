package com.ckcz123.h5mota;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.ckcz123.h5mota.lib.CustomToast;
import com.ckcz123.h5mota.lib.HttpRequest;
import com.ckcz123.h5mota.lib.Utils;
import com.tencent.smtt.sdk.QbSdk;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import fi.iki.elonen.SimpleWebServer;
import fi.iki.elonen.util.ServerRunner;

public class MainActivity extends AppCompatActivity {

    SimpleWebServer simpleWebServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        QbSdk.initX5Environment(this, new QbSdk.PreInitCallback() {
            @Override
            public void onCoreInitFinished() {

            }

            @Override
            public void onViewInitFinished(boolean b) {
                Log.e("@@","加载内核是否成功:"+b);
            }
        });

        final File directory = new File(Environment.getExternalStorageDirectory()+"/H5mota/");

        if (!directory.exists()) {
            directory.mkdirs();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Utils.copyFilesFassets(MainActivity.this, "24层魔塔", directory.getPath()+"/24层魔塔");
                }
            }).start();
        }

        findViewById(R.id.online).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this, TBSActivity.class);
                intent.putExtra("title", "HTML5魔塔列表");
                intent.putExtra("url", "http://mota.pw/");
                startActivity(intent);
            }
        });

        findViewById(R.id.offline).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
                builder.setCancelable(true);
                builder.setTitle("已下载的游戏列表");

                final List<String> names=new ArrayList<>();
                for (File file: directory.listFiles()) {
                    if (new File(file, "index.html").exists() && new File(file, "main.js").exists() && new File(file, "libs").exists()) {
                        names.add(file.getName());
                    }
                }

                builder.setItems(names.toArray(new String[0]), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            String name=names.get(i);
                            Intent intent=new Intent(MainActivity.this, TBSActivity.class);
                            intent.putExtra("title", name);
                            intent.putExtra("url", "http://127.0.0.1:1055/"+ URLEncoder.encode(name, "utf-8"));
                            startActivity(intent);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                builder.create().show();
            }
        });

        try {
            simpleWebServer = new SimpleWebServer("127.0.0.1", 1055, new File(Environment.getExternalStorageDirectory()+"/H5mota"), true);
            simpleWebServer.start();
        }
        catch (Exception e) {
            e.printStackTrace();
            simpleWebServer=null;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpRequest httpRequest=HttpRequest.get("https://ckcz123.com/games/_client/").useCaches(false);
                    String s=httpRequest.body();
                    httpRequest.disconnect();
                    JSONObject jsonObject=new JSONObject(s);
                    final JSONObject android = jsonObject.getJSONObject("android");
                    String version = android.getString("version");
                    if (!version.equals(BuildConfig.VERSION_NAME)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    new AlertDialog.Builder(MainActivity.this).setTitle("存在版本更新！")
                                            .setMessage(android.getString("text")).setCancelable(true)
                                            .setPositiveButton("下载", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    try {
                                                        Intent intent=new Intent(MainActivity.this, TBSActivity.class);
                                                        intent.putExtra("title", "版本更新");
                                                        intent.putExtra("url", android.getString("url"));
                                                        startActivity(intent);
                                                    }
                                                    catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }).setNegativeButton("取消", null).create().show();
                                }
                                catch (Exception e) {e.printStackTrace();}
                            }
                        });


                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    protected void onDestroy() {
        if (simpleWebServer!=null) {
            simpleWebServer.stop();
        }
        super.onDestroy();
    }

    double exittime=0;
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode==KeyEvent.KEYCODE_BACK && event.getAction()==KeyEvent.ACTION_DOWN) {
            if (System.currentTimeMillis()-exittime>2000) {
                Toast.makeText(this, "再按一遍退出程序", Toast.LENGTH_SHORT).show();
                exittime=System.currentTimeMillis();
            }
            else
            {
                exittime=0;
                finish();
            }
            return true;
        }
        return false;
    }
}
