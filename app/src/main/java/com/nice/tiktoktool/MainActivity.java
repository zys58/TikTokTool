package com.nice.tiktoktool;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.support.design.widget.TextInputEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nice.config.Config;
import com.nice.entity.ActivationCode;
import com.nice.entity.ViewId;
import com.nice.service.MyService;
import com.nice.utils.AESUtils;
import com.nice.utils.ApplicationUtil;
import com.nice.utils.JumpPermissionManagement;
import com.nice.utils.PerformClickUtils;
import okhttp3.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, AdapterView.OnItemSelectedListener {


    private Switch openPermission, openServiceBtn;
    private Button openTiktokBtn;
    private TextView attentionSpeedTv, privatelySpeedTv, activationStateTv2, activationEndTime;
    private LinearLayout attentionSetting, privatelySetting;
    private TextInputEditText privatelyContent;
    private SeekBar attentionSpeedSb, privatelySpeedSb;
    private ImageView usrSettingBtn;
    private ProgressBar progressBar;
    private Spinner tiktokVersionSpinner, optionSpinner;
    private ArrayAdapter adapter;

    private List<String> versionList;

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
                            List<ViewId> viewIds = JSONArray.parseArray(data.getString("viewIds"), ViewId.class);
                            Config.getInstance(getApplicationContext()).setViewIds(viewIds);
                            Config.getInstance(getApplicationContext()).setActivated(true);
                            Config.getInstance(getApplicationContext()).setActivationCode(activationCode.getActivationCode());
                            Config.getInstance(getApplicationContext()).setEndTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(activationCode.getEndTime()));

                            //获得版本号
                            versionList = new ArrayList<>();
                            for (ViewId viewId : viewIds) {
                                if (!versionList.contains(viewId.getVersion())) {
                                    versionList.add(viewId.getVersion());
                                }
                            }
                            Collections.reverse(versionList);

                            //绑定版本下拉框数据
                            adapter = new ArrayAdapter<>(getApplication(),
                                    android.R.layout.simple_spinner_dropdown_item, versionList);
                            tiktokVersionSpinner.setAdapter(adapter);
                            //请求viewIds完毕后再监听version下拉框
                            tiktokVersionSpinner.setOnItemSelectedListener(MainActivity.this);
                        } else {
                            Config.getInstance(getApplicationContext()).setActivated(false);
                            Config.getInstance(getApplicationContext()).setEndTime(" - ");
                        }
                    } else {
                        Config.getInstance(getApplicationContext()).setActivated(false);
                        Config.getInstance(getApplicationContext()).setEndTime(" - ");
                    }
                    setActiviteLogo();
                    break;
                case 500:
                    progressBar.setVisibility(View.GONE);
                    Config.getInstance(getApplicationContext()).setActivated(false);
                    Config.getInstance(getApplicationContext()).setEndTime(" - ");
                    Toast.makeText(getApplicationContext(), "连接服务器失败！", Toast.LENGTH_LONG).show();
                    setActiviteLogo();
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
        //初始化ui
        init();

        //绑定操作下拉框数据
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, getOptionSource());
        optionSpinner.setAdapter(adapter);

        //启动悬浮窗
        startService(new Intent(this, MyService.class).putExtra(MyService.ACTION, MyService.SHOW));

        //请求设备信息
        requestDeviceInfo();

    }

    public void init() {
        openPermission = findViewById(R.id.open_permission_btn);
        openServiceBtn = findViewById(R.id.open_accessibility_btn);
        openTiktokBtn = findViewById(R.id.open_tiktok_btn);
        attentionSetting = findViewById(R.id.attention_setting);
        privatelySetting = findViewById(R.id.privately_setting);
        privatelyContent = findViewById(R.id.privately_content);
        attentionSpeedTv = findViewById(R.id.attention_speed_tv);
        attentionSpeedSb = findViewById(R.id.attention_speed_sb);
        usrSettingBtn = findViewById(R.id.usr_setting_btn);
        privatelySpeedTv = findViewById(R.id.privately_speed_tv);
        privatelySpeedSb = findViewById(R.id.privately_speed_sb);
        progressBar = findViewById(R.id.progress_bar);
        activationStateTv2 = findViewById(R.id.activation_state_tv2);
        activationEndTime = findViewById(R.id.activation_end_time);
        optionSpinner = findViewById(R.id.option_spinner);
        tiktokVersionSpinner = findViewById(R.id.tiktok_version_spinner);

        changeStatus();

        openServiceBtn.setOnCheckedChangeListener(this);
        openPermission.setOnCheckedChangeListener(this);
        openTiktokBtn.setOnClickListener(this);
        usrSettingBtn.setOnClickListener(this);
        optionSpinner.setOnItemSelectedListener(this);

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
    }

    public List<String> getOptionSource() {
        List<String> list = new ArrayList<>();
        list.add("关注");
        list.add("取消关注");
        list.add("私信粉丝");
        list.add("私信关注");
        list.add("私信他人粉丝");
        list.add("私信他人关注");
//        list.add("私信评论用户");
        return list;
    }

    public void setActiviteLogo() {
        Drawable drawable;
        if (Config.getInstance(this).getActivated()) {
            drawable = getResources().getDrawable(R.mipmap.activated);
            activationStateTv2.setText("已激活");
            activationStateTv2.setTextColor(Color.parseColor("#e91e63"));
            activationEndTime.setText(Config.getInstance(this).getEndTime() + " 到期");
        } else {
            drawable = getResources().getDrawable(R.mipmap.not_active);
            activationStateTv2.setText("未激活");
            activationStateTv2.setTextColor(Color.GRAY);
            activationEndTime.setText("");
        }
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        activationStateTv2.setCompoundDrawables(drawable, null, null, null);

    }

    public void requestDeviceInfo() {

        progressBar.setVisibility(View.VISIBLE);

        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");//"类型,字节码"

        JSONObject encryptedDataEntity = new JSONObject();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("activationCode", Config.getInstance(this).getActivationCode());
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
            public void onResponse(Call call, Response response) throws IOException {
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

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

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


    private void changeStatus() {

        setActiviteLogo();

        if (PerformClickUtils.isStartAccessibilityService(this)) {
            openServiceBtn.setChecked(true);
        } else {
            openServiceBtn.setChecked(false);
        }
        if (isFloatWindowOpAllowed(this)) {
            openPermission.setChecked(true);
        } else {
            openPermission.setChecked(false);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        changeStatus();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (adapterView.getId() == R.id.option_spinner) {
            if (i == 0) {
                Config.getInstance(this).setOption(Config.CONCERN);
                attentionSetting.setVisibility(View.VISIBLE);
                privatelySetting.setVisibility(View.GONE);
                //速度显示
                attentionSpeedTv.setText("(" + Config.getInstance(this).getAttentionSpeed() / 1000 + "~" + (Config.getInstance(this).getAttentionSpeed() / 1000 + 2) + "秒/个)");
                attentionSpeedSb.setProgress((int) (Config.getInstance(this).getAttentionSpeed() / 1000));
                privatelySpeedTv.setText("(" + Config.getInstance(this).getPrivatelySpeed() / 1000 + "~" + (Config.getInstance(this).getPrivatelySpeed() / 1000 + 2) + "秒/个)");
                privatelySpeedSb.setProgress((int) (Config.getInstance(this).getPrivatelySpeed() / 1000));
            }
            if (i == 1) {
                Config.getInstance(this).setOption(Config.CANCEL_CONCERN);
                attentionSetting.setVisibility(View.VISIBLE);
                privatelySetting.setVisibility(View.GONE);
                //速度显示
                attentionSpeedTv.setText("(" + Config.getInstance(this).getAttentionSpeed() / 1000 + "~" + (Config.getInstance(this).getAttentionSpeed() / 1000 + 2) + "秒/个)");
                attentionSpeedSb.setProgress((int) (Config.getInstance(this).getAttentionSpeed() / 1000));
                privatelySpeedTv.setText("(" + Config.getInstance(this).getPrivatelySpeed() / 1000 + "~" + (Config.getInstance(this).getPrivatelySpeed() / 1000 + 2) + "秒/个)");
                privatelySpeedSb.setProgress((int) (Config.getInstance(this).getPrivatelySpeed() / 1000));
            }
            if (i == 2) {
                Config.getInstance(this).setOption(Config.PRIVATELY);
                attentionSetting.setVisibility(View.GONE);
                privatelySetting.setVisibility(View.VISIBLE);
                //私信内容显示
                privatelyContent.setText(Config.getInstance(this).getPrivatelyContentText());
            }
            if (i == 3) {
                Config.getInstance(this).setOption(Config.PRIVATELY);
                attentionSetting.setVisibility(View.GONE);
                privatelySetting.setVisibility(View.VISIBLE);
                //私信内容显示
                privatelyContent.setText(Config.getInstance(this).getPrivatelyContentText());
            }
            if (i == 4) {
                Config.getInstance(this).setOption(Config.PRIVATELY);
                attentionSetting.setVisibility(View.GONE);
                privatelySetting.setVisibility(View.VISIBLE);
                //私信内容显示
                privatelyContent.setText(Config.getInstance(this).getPrivatelyContentText());
            }
            if (i == 5) {
                Config.getInstance(this).setOption(Config.PRIVATELY);
                attentionSetting.setVisibility(View.GONE);
                privatelySetting.setVisibility(View.VISIBLE);
                //私信内容显示
                privatelyContent.setText(Config.getInstance(this).getPrivatelyContentText());
            }
//        if (i == 6) {
//            Config.getInstance(this).setOption(Config.COMMENT_PRIVATELY);
//            attentionSetting.setVisibility(View.GONE);
//            privatelySetting.setVisibility(View.VISIBLE);
//            privatelyContent.setText(Config.getInstance(this).getPrivatelyContentText());
//        }
        }
        if (adapterView.getId() == R.id.tiktok_version_spinner) {
            Map<String, String> viewIdMap = new HashMap<>();
            for (ViewId viewId : Config.getInstance(getApplicationContext()).getViewIds()) {
                //筛选版本
                if (viewId.getVersion().equals(versionList.get(i))) {
                    viewIdMap.put(viewId.getClickInfo(), viewId.getViewId());
                }
            }
            Config.getInstance(getApplicationContext()).setViewIdByVersionMap(viewIdMap);
            Toast.makeText(this, "配置版本成功", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
