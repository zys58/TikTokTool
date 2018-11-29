package com.nice.tiktoktool;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.*;

import com.alibaba.fastjson.JSONObject;
import com.nice.config.Config;
import com.nice.entity.ActivationCode;
import com.nice.service.MyService;
import com.nice.utils.AESUtils;
import com.nice.utils.InstallationUtil;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UserSettingActivity extends Activity {

    private EditText activationCodeEt;
    private Button activation_btn;
    private ProgressBar progressBar;
    private TextView endTimeTv;
    private ImageView settingBackBtn;

    /*构造一个Handler，主要作用有：1）供非UI线程发送Message  2）处理Message并完成UI更新*/
    public Handler uiHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    progressBar.setVisibility(View.GONE);
                    break;
                case 1:
                    Toast.makeText(getApplicationContext(), msg.getData().getString("msg"), Toast.LENGTH_LONG).show();
                    activationCodeEt.setText(Config.getInstance(getApplicationContext()).getActivationCode());
                    endTimeTv.setText(Config.getInstance(getApplicationContext()).getEndTime());
                    finish();
                    break;
                case 2:
                    Toast.makeText(getApplicationContext(), msg.getData().getString("msg"), Toast.LENGTH_LONG).show();
                    break;
                case 500:
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

        activationCodeEt.setText(Config.getInstance(this).getActivationCode());
        endTimeTv.setText(Config.getInstance(this).getEndTime());
    }

    public void requestDeviceInfo() {

        progressBar.setVisibility(View.VISIBLE);

        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");//"类型,字节码"

        JSONObject encryptedDataEntity = new JSONObject();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("activationCode", activationCodeEt.getText());
        jsonObject.put("InstallationCode", InstallationUtil.id(this));
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
                Log.i("异常返回：", "");
                e.printStackTrace();
                new Thread() {
                    public void run() {
                        uiHandler.sendEmptyMessage(0);
                        uiHandler.sendEmptyMessage(500);
                        Config.getInstance(getApplicationContext()).setEndTime(" - ");
                        Config.getInstance(getApplicationContext()).setActivated(false);
                    }
                }.start();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                JSONObject encryptedData = JSONObject.parseObject(response.body().string());
                final com.alibaba.fastjson.JSONObject data = com.alibaba.fastjson.JSONObject.parseObject(AESUtils.decode(encryptedData.getString("data")));
                if (data.getInteger("code") == 0) {
                    if (data.getInteger("valid") == 1) {
                        ActivationCode activationCode = JSONObject.toJavaObject(data.getJSONObject("data"), ActivationCode.class);
                        Config.getInstance(getApplicationContext()).setActivationCode(activationCode.getActivationCode());
                        Config.getInstance(getApplicationContext()).setActivated(true);
                        Config.getInstance(getApplicationContext()).setEndTime(DateUtils.formatDateTime(getApplicationContext(), activationCode.getEndTime(), DateUtils.FORMAT_SHOW_YEAR));
                        new Thread() {
                            public void run() {
                                Bundle bundle = new Bundle();
                                bundle.putString("msg", "激活成功！");

                                Message message = new Message();
                                message.what = 1;
                                message.setData(bundle);
                                uiHandler.sendMessage(message);
                            }
                        }.start();
                    } else {
                        new Thread() {
                            public void run() {
                                Bundle bundle = new Bundle();
                                bundle.putString("msg", data.getString("msg"));

                                Message message = new Message();
                                message.what = 2;
                                message.setData(bundle);
                                uiHandler.sendMessage(message);
                            }
                        }.start();
                        Config.getInstance(getApplicationContext()).setEndTime(" - ");
                        Config.getInstance(getApplicationContext()).setActivated(false);
                        Log.i("返回：", data.getString("msg"));
                    }
                } else {
                    new Thread() {
                        public void run() {
                            Bundle bundle = new Bundle();
                            bundle.putString("msg", data.getString("msg"));

                            Message message = new Message();
                            message.what = 2;
                            message.setData(bundle);
                            uiHandler.sendMessage(message);
                        }
                    }.start();
                    Config.getInstance(getApplicationContext()).setEndTime(" - ");
                    Config.getInstance(getApplicationContext()).setActivated(false);
                }
                new Thread() {
                    public void run() {
                        uiHandler.sendEmptyMessage(0);
                    }
                }.start();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        startService(new Intent(this, MyService.class).putExtra(MyService.ACTION, MyService.HIDE));
    }

    @Override
    protected void onPause() {
        super.onPause();
        startService(new Intent(this, MyService.class).putExtra(MyService.ACTION, MyService.SHOW));
    }
}
