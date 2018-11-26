package com.nice.tiktoktool;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.*;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.*;
import com.nice.config.Config;
import com.nice.service.MyService;

import java.lang.reflect.Method;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {


    private Switch openPermission, openServiceBtn;
    private Button openTiktokBtn;
    private TextView attentionSpeedTv;
    private RadioButton attentionRa, privatelyRa;
    private LinearLayout attentionSetting, privatelySetting;
    private EditText privatelyContent;
    private SeekBar attentionSpeedSb;

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

        changeStatus();
        openServiceBtn.setOnCheckedChangeListener(this);
        openPermission.setOnCheckedChangeListener(this);
        attentionRa.setOnCheckedChangeListener(this);
        privatelyRa.setOnCheckedChangeListener(this);
        openTiktokBtn.setOnClickListener(this);
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
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.open_tiktok_btn) {
            if (Config.getInstance(this).getOption().equals(Config.PRIVATELY)) {
                setPrivatelyContent();
            }
            //打开抖音app
            Intent intent = new Intent(Intent.ACTION_MAIN);
            ComponentName componentName = new ComponentName("com.ss.android.ugc.aweme", "com.ss.android.ugc.aweme.main.MainActivity");
            intent.setComponent(componentName);
            startActivity(intent);
        }
    }

    private void setPrivatelyContent() {
        //获取设置的私信内容
        String content = privatelyContent.getText().toString();
        //获取剪贴板管理器：
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        // 创建普通字符型ClipData
        ClipData mClipData = ClipData.newPlainText("Label", content);
        // 将ClipData内容放到系统剪贴板里。
        cm.setPrimaryClip(mClipData);
    }

    private void settingViewChange() {
        if (attentionRa.isChecked()) {
            attentionSetting.setVisibility(View.VISIBLE);
            privatelySetting.setVisibility(View.GONE);
            //速度显示
            attentionSpeedTv.setText("(" + Config.getInstance(this).getAttentionSpeed() / 1000 + "~" + (Config.getInstance(this).getAttentionSpeed() / 1000 + 2) + "秒/个)");
            attentionSpeedSb.setProgress((int) (Config.getInstance(this).getAttentionSpeed() / 1000));
        } else {
            attentionSetting.setVisibility(View.GONE);
            privatelySetting.setVisibility(View.VISIBLE);
            //私信内容显示
            privatelyContent.setText(Config.getInstance(this).getPrivatelyContent());
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        settingViewChange();

        if (compoundButton.getId() == R.id.open_accessibility_btn) {
            if (b) {
                Toast.makeText(MainActivity.this, "请打开[抖音引流脚本]辅助服务！", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            } else {
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            }
        }
        if (compoundButton.getId() == R.id.open_permission_btn) {
            if (b) {
                Toast.makeText(MainActivity.this, "请授权应用[允许出现在其他应用上]权限！", Toast.LENGTH_SHORT).show();
                openSetting();
            } else {
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
     * 打开权限设置界面
     */
    public void openSetting() {
        try {
            Intent localIntent = new Intent(
                    "miui.intent.action.APP_PERM_EDITOR");
            localIntent.setClassName("com.miui.securitycenter",
                    "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
            localIntent.putExtra("extra_pkgname", getPackageName());
            startActivityForResult(localIntent, 11);
            Log.e("", "启动小米悬浮窗设置界面");
        } catch (ActivityNotFoundException localActivityNotFoundException) {
            Intent intent1 = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent1.setData(uri);
            startActivityForResult(intent1, 11);
            Log.e("", "启动悬浮窗界面");
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
