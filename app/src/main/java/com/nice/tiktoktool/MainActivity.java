package com.nice.tiktoktool;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.nice.config.Config;
import com.nice.entity.ActivationCode;
import com.nice.service.MyService;
import com.nice.utils.AESUtils;
import com.nice.utils.InstallationUtil;
import com.nice.utils.JumpPermissionManagement;

import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends Activity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {


    private Switch openPermission, openServiceBtn;
    private Button openTiktokBtn;
    private TextView attentionSpeedTv, privatelySpeedTv, activationStateTv1, activationStateTv2, activationEndTime;
    private RadioButton attentionRa, privatelyRa;
    private LinearLayout attentionSetting, privatelySetting;
    private EditText privatelyContent;
    private SeekBar attentionSpeedSb, privatelySpeedSb;
    private ImageView usrSettingBtn;
    private ProgressBar progressBar;

    /*构造一个Handler，主要作用有：1）供非UI线程发送Message  2）处理Message并完成UI更新*/
    public Handler uiHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    progressBar.setVisibility(View.GONE);
                    break;
                case 1:
                    activationStateTv1.setTextColor(Color.GREEN);
                    activationStateTv2.setText("已激活");
                    activationEndTime.setText("(" + Config.getInstance(getApplicationContext()).getEndTime() + "到期)");
                    break;
                case 2:
                    Bundle bundle = msg.getData();
                    Toast.makeText(getApplicationContext(), bundle.getString("msg"), Toast.LENGTH_LONG).show();
                    activationStateTv1.setTextColor(Color.RED);
                    activationStateTv2.setText("未激活");
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
        setContentView(R.layout.activity_main);

        openPermission = findViewById(R.id.open_permission_btn);
        openServiceBtn = findViewById(R.id.open_accessibility_btn);
        openTiktokBtn = findViewById(R.id.open_tiktok_btn);
        attentionRa = findViewById(R.id.attention_ra);
        privatelyRa = findViewById(R.id.privately_ra);
        attentionSetting = findViewById(R.id.attention_setting);
        privatelySetting = findViewById(R.id.privately_setting);
        privatelyContent = findViewById(R.id.privately_content);
        attentionSpeedTv = findViewById(R.id.attention_speed_tv);
        attentionSpeedSb = findViewById(R.id.attention_speed_sb);
        usrSettingBtn = findViewById(R.id.usr_setting_btn);
        privatelySpeedTv = findViewById(R.id.privately_speed_tv);
        privatelySpeedSb = findViewById(R.id.privately_speed_sb);
        progressBar = findViewById(R.id.progress_bar);
        activationStateTv1 = findViewById(R.id.activation_state_tv1);
        activationStateTv2 = findViewById(R.id.activation_state_tv2);
        activationEndTime = findViewById(R.id.activation_end_time);

        changeStatus();
        openServiceBtn.setOnCheckedChangeListener(this);
        openPermission.setOnCheckedChangeListener(this);
        attentionRa.setOnCheckedChangeListener(this);
        privatelyRa.setOnCheckedChangeListener(this);
        openTiktokBtn.setOnClickListener(this);
        usrSettingBtn.setOnClickListener(this);
        attentionSpeedSb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                attentionSpeedTv.setText("(" + i + "~" + (i + 2) + "秒/个)");
                if (i < 1) {
                    attentionSpeedSb.setProgress(1);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Config.getInstance(MainActivity.this).setAttentionSpeed((long) seekBar.getProgress());
            }
        });

        privatelySpeedSb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                privatelySpeedTv.setText("(" + i + "~" + (i + 2) + "秒/个)");
                if (i < 1) {
                    privatelySpeedSb.setProgress(1);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Config.getInstance(MainActivity.this).setPrivatelySpeed((long) seekBar.getProgress());
            }
        });

        privatelyContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                Config.getInstance(MainActivity.this).setPrivatelyContent(editable.toString());
            }
        });

        //显示对应的配置界面
        settingViewChange();
        //请求设备信息
        requestDeviceInfo();

    }

    public void requestDeviceInfo() {

        progressBar.setVisibility(View.VISIBLE);

        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");//"类型,字节码"

        JSONObject encryptedDataEntity = new JSONObject();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("activationCode", Config.getInstance(this).getActivationCode());
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
                final JSONObject data = JSONObject.parseObject(AESUtils.decode(encryptedData.getString("data")));
                if (data.getInteger("code") == 0) {
                    if (data.getInteger("valid") == 1) {
                        ActivationCode activationCode = JSONObject.toJavaObject(data.getJSONObject("data"), ActivationCode.class);
                        Config.getInstance(getApplicationContext()).setActivated(true);
                        Config.getInstance(getApplicationContext()).setActivationCode(activationCode.getActivationCode());
                        Config.getInstance(getApplicationContext()).setEndTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(activationCode.getEndTime()));
                        new Thread() {
                            public void run() {
                                uiHandler.sendEmptyMessage(1);
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
                        Log.i("返回：", data.getString("msg"));
                    }
                } else {
                    new Thread() {
                        public void run() {
                            Bundle bundle = new Bundle();
                            Log.i("返回：", data.getString("msg"));

                            Message message = new Message();
                            message.what = 2;
                            message.setData(bundle);
                            uiHandler.sendMessage(message);
                        }
                    }.start();
                    Config.getInstance(getApplicationContext()).setEndTime(" - ");
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
    public void onClick(View view) {
        if (view.getId() == R.id.open_tiktok_btn) {
            if (Config.getInstance(this).getActivated()) {
                //打开抖音app
                Intent intent = new Intent(Intent.ACTION_MAIN);
                ComponentName componentName = new ComponentName("com.ss.android.ugc.aweme", "com.ss.android.ugc.aweme.main.MainActivity");
                intent.setComponent(componentName);
                startActivity(intent);
            } else {
                Toast.makeText(this, "软件未激活！", Toast.LENGTH_LONG).show();
            }

        }

        if (view.getId() == R.id.usr_setting_btn) {
            Intent intent = new Intent(MainActivity.this, UserSettingActivity.class);
            startActivity(intent);
        }
    }

    private void settingViewChange() {
        if (attentionRa.isChecked()) {
            attentionSetting.setVisibility(View.VISIBLE);
            privatelySetting.setVisibility(View.GONE);
            //速度显示
            attentionSpeedTv.setText("(" + Config.getInstance(this).getAttentionSpeed() / 1000 + "~" + (Config.getInstance(this).getAttentionSpeed() / 1000 + 2) + "秒/个)");
            attentionSpeedSb.setProgress((int) (Config.getInstance(this).getAttentionSpeed() / 1000));
            privatelySpeedTv.setText("(" + Config.getInstance(this).getPrivatelySpeed() / 1000 + "~" + (Config.getInstance(this).getPrivatelySpeed() / 1000 + 2) + "秒/个)");
            privatelySpeedSb.setProgress((int) (Config.getInstance(this).getPrivatelySpeed() / 1000));
        } else {
            attentionSetting.setVisibility(View.GONE);
            privatelySetting.setVisibility(View.VISIBLE);
            //私信内容显示
            privatelyContent.setText(Config.getInstance(this).getPrivatelyContentText());
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        settingViewChange();

        if (compoundButton.getId() == R.id.open_accessibility_btn) {
            if (b) {
                Toast.makeText(MainActivity.this, "请打开[抖音引流脚本]辅助服务！", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            }
        }
        if (compoundButton.getId() == R.id.open_permission_btn) {
            if (b) {
                openSetting();
            }
        }
        if (compoundButton.getId() == R.id.attention_ra) {
            if (b) {
                Config.getInstance(this).setOption(Config.CONCERN);
            }
        }
        if (compoundButton.getId() == R.id.privately_ra) {
            if (b) {
                Config.getInstance(this).setOption(Config.PRIVATELY);
            }
        }
    }

    /**
     * 打开应用设置
     */

    public void openAppSetting() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 11);
        Log.e("", "启动悬浮窗界面");
    }

    /**
     * 打开权限设置界面
     */
    public void openSetting() {
        try {
            if (JumpPermissionManagement.gotoPermissionSetting(this)) {
                openAppSetting();
            } else {
                Toast.makeText(MainActivity.this, "请授权应用[允许出现在其他应用上]权限！", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            openAppSetting();
        }

    }

    /**
     * 判断悬浮窗权限
     *
     * @param context
     * @return
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean isFloatWindowOpAllowed(Context context) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 19) {
            return checkOp(context, 24);  // AppOpsManager.OP_SYSTEM_ALERT_WINDOW
        } else {
            if ((context.getApplicationInfo().flags & 1 << 27) == 1 << 27) {
                return true;
            } else {
                return false;
            }
        }
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean checkOp(Context context, int op) {
        final int version = Build.VERSION.SDK_INT;

        if (version >= 19) {
            AppOpsManager manager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            try {
                Method method = manager.getClass().getDeclaredMethod("checkOp", int.class, int.class, String.class);
                int property = (Integer) method.invoke(manager, op,
                        Binder.getCallingUid(), context.getPackageName());
                Log.e("399", " property: " + property);

                if (AppOpsManager.MODE_ALLOWED == property) {
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.e("399", "Below API 19 cannot invoke!");
        }
        return false;
    }

    /**
     * 判断AccessibilityService服务是否已经启动
     *
     * @param context
     * @return
     */
    public static boolean isStartAccessibilityService(Context context) {
        AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> serviceInfos = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        for (AccessibilityServiceInfo info : serviceInfos) {
            String id = info.getId();
            if (id.contains("TikTokAccessibilityService")) {
                return true;
            }
        }
        return false;
    }

    private void changeStatus() {
        if (isStartAccessibilityService(this)) {
            openServiceBtn.setChecked(true);
        } else {
            openServiceBtn.setChecked(false);
        }
        if (isFloatWindowOpAllowed(this)) {
            openPermission.setChecked(true);
        } else {
            openPermission.setChecked(false);
        }

        if (Config.getInstance(this).getActivated()) {
            activationStateTv1.setTextColor(Color.GREEN);
            activationStateTv2.setText("已激活");
            activationEndTime.setText("(" + Config.getInstance(this).getEndTime() + "到期)");
        } else {
            activationStateTv1.setTextColor(Color.RED);
            activationStateTv2.setText("未激活");
            activationEndTime.setText("");
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        changeStatus();
        startService(new Intent(this, MyService.class).putExtra(MyService.ACTION, MyService.HIDE));
    }

    @Override
    protected void onPause() {
        super.onPause();
        startService(new Intent(this, MyService.class).putExtra(MyService.ACTION, MyService.SHOW));
    }

}
