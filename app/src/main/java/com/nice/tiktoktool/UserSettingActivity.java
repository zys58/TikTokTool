package com.nice.tiktoktool;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TextInputEditText;
import android.view.View;
import android.widget.*;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nice.config.Config;
import com.nice.entity.ActivationCode;
import com.nice.entity.ViewId;
import com.nice.utils.AESUtils;
import com.nice.utils.ApplicationUtil;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UserSettingActivity extends Activity {

    private TextInputEditText activationCodeEt;
    private Button activation_btn;
    private ProgressBar progressBar;
    private TextView endTimeTv, checkUpdatetv;
    private ImageView settingBackBtn;

    /*构造一个Handler，主要作用有：1）供非UI线程发送Message  2）处理Message并完成UI更新*/
    public Handler uiHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 200:
                    progressBar.setVisibility(View.GONE);
                    Bundle bundle = msg.getData();
                    JSONObject data = JSONObject.parseObject(bundle.getString("data"));
                    if (data.getInteger("code") == 0) {
                        if (data.getInteger("valid") == 1) {
                            ActivationCode activationCode = JSONObject.toJavaObject(data.getJSONObject("data"), ActivationCode.class);

                            //获得viewId信息
                            List<ViewId> list = JSONArray.parseArray(data.getString("viewIds"), ViewId.class);
                            Map<String, String> viewIdMap = new HashMap<>();
                            for (ViewId viewId : list) {
                                viewIdMap.put(viewId.getClickInfo(), viewId.getViewId());
                            }
                            //配置
                            Config.getInstance(getApplicationContext()).setViewIdByVersionMap(viewIdMap);

                            Config.getInstance(getApplicationContext()).setActivationCode(activationCode.getActivationCode());
                            Config.getInstance(getApplicationContext()).setActivated(true);
                            Config.getInstance(getApplicationContext()).setEndTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(activationCode.getEndTime()));
                            Toast.makeText(getApplicationContext(), "激活成功!", Toast.LENGTH_LONG).show();
                            activationCodeEt.setText(Config.getInstance(getApplicationContext()).getActivationCode());
                            endTimeTv.setText(Config.getInstance(getApplicationContext()).getEndTime());
                            finish();
                        } else {
                            Config.getInstance(getApplicationContext()).setEndTime(" - ");
                            Config.getInstance(getApplicationContext()).setActivated(false);
                            activationCodeEt.setError(data.getString("msg"));
                        }
                    } else {
                        Config.getInstance(getApplicationContext()).setEndTime(" - ");
                        Config.getInstance(getApplicationContext()).setActivated(false);
                        activationCodeEt.setError(data.getString("msg"));
                    }
                    break;
                case 500:
                    progressBar.setVisibility(View.GONE);
                    Config.getInstance(getApplicationContext()).setEndTime(" - ");
                    Config.getInstance(getApplicationContext()).setActivated(false);
                    Toast.makeText(getApplicationContext(), "连接服务器失败！", Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;

            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_setting);

        activationCodeEt = findViewById(R.id.activation_code_et);
        activation_btn = findViewById(R.id.activation_btn);
        progressBar = findViewById(R.id.setting_progress_bar);
        endTimeTv = findViewById(R.id.end_time_tv);
        settingBackBtn = findViewById(R.id.setting_back_btn);
        checkUpdatetv = findViewById(R.id.check_update_tv);

        activation_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestDeviceInfo();
            }
        });
        settingBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        checkUpdatetv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "当前版本：" + ApplicationUtil.getVersionName(getApplicationContext()), Toast.LENGTH_SHORT).show();
                Uri uri = Uri.parse(getString(R.string.update_path));
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        activationCodeEt.setText(Config.getInstance(this).getActivationCode());
        endTimeTv.setText(Config.getInstance(this).getEndTime());
    }

    public void requestDeviceInfo() {

        progressBar.setVisibility(View.VISIBLE);

        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");//"类型,字节码"

        JSONObject encryptedDataEntity = new JSONObject();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("activationCode", activationCodeEt.getText());
        jsonObject.put("InstallationCode", ApplicationUtil.id(this));
        jsonObject.put("deviceInfo", Config.DEVICE_INFO);
        encryptedDataEntity.put("data", AESUtils.encode(jsonObject.toString()));
        //json字符串
        String value = String.valueOf(encryptedDataEntity);
        //1.创建OkHttpClient对象
        OkHttpClient okHttpClient = new OkHttpClient();
        //2.通过RequestBody.create 创建requestBody对象
        RequestBody requestBody = RequestBody.create(mediaType, value);
        //3.创建Request对象，设置URL地址，将RequestBody作为post方法的参数传入
        Request request = new Request.Builder().url(Config.CODE_VALIDATE_URL).post(requestBody).build();
        //4.创建一个call对象,参数就是Request请求对象
        Call call = okHttpClient.newCall(request);
        //5.请求加入调度,重写回调方法
        call.enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                new Thread() {
                    public void run() {
                        uiHandler.sendEmptyMessage(500);
                    }
                }.start();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final JSONObject encryptedData = JSONObject.parseObject(response.body().string());
                new Thread() {
                    public void run() {
                        Bundle bundle = new Bundle();
                        bundle.putString("data", AESUtils.decode(encryptedData.getString("data")));
                        Message message = new Message();
                        message.what = 200;
                        message.setData(bundle);
                        uiHandler.sendMessage(message);
                    }
                }.start();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
