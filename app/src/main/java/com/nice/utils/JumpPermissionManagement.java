package com.nice.utils;

/**
 * 跳转设置页面
 */

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.appcompat.BuildConfig;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class JumpPermissionManagement {
    /**
     * Build.MANUFACTURER
     */
    private static final String MANUFACTURER_HUAWEI = "Huawei";//华为
    private static final String MANUFACTURER_MEIZU = "Meizu";//魅族
    private static final String MANUFACTURER_XIAOMI = "Xiaomi";//小米
    private static final String MANUFACTURER_SONY = "Sony";//索尼
    private static final String MANUFACTURER_OPPO = "OPPO";
    private static final String MANUFACTURER_LG = "LG";
    private static final String MANUFACTURER_VIVO = "vivo";
    private static final String MANUFACTURER_SAMSUNG = "samsung";//三星
    private static final String MANUFACTURER_LETV = "Letv";//乐视
    private static final String MANUFACTURER_ZTE = "ZTE";//中兴
    private static final String MANUFACTURER_YULONG = "YuLong";//酷派
    private static final String MANUFACTURER_LENOVO = "LENOVO";//联想


    public static boolean gotoPermissionSetting(Activity activity) {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        String packageName = BuildConfig.APPLICATION_ID;

        boolean other = false;

        switch (Build.MANUFACTURER) {
            case MANUFACTURER_HUAWEI: // 华为
                intent.putExtra("packageName", packageName);
                intent.setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.permissionmanager.ui.MainActivity"));
                break;
            case MANUFACTURER_MEIZU: // 魅族
                intent.setAction("com.meizu.safe.security.SHOW_APPSEC");
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.putExtra("packageName", packageName);
                break;
            case MANUFACTURER_XIAOMI: // 小米
                String rom = getMiuiVersion();
                if ("V6".equals(rom) || "V7".equals(rom)) {
                    intent.setAction("miui.intent.action.APP_PERM_EDITOR");
                    intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
                    intent.putExtra("extra_pkgname", packageName);
                } else if ("V8".equals(rom) || "V9".equals(rom)) {
                    intent.setAction("miui.intent.action.APP_PERM_EDITOR");
                    intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
                    intent.putExtra("extra_pkgname", packageName);
                } else {
                    SystemConfig(activity);
                }
                break;
            case MANUFACTURER_SONY: // 索尼
                intent.putExtra("packageName", packageName);
                intent.setComponent(new ComponentName("com.sonymobile.cta", "com.sonymobile.cta.SomcCTAMainActivity"));
                break;
            case MANUFACTURER_OPPO: // OPPO
                intent.putExtra("packageName", packageName);
                intent.setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.sysfloatwindow.FloatWindowListActivity"));
                break;
            case MANUFACTURER_LETV: // 乐视
                intent.putExtra("packageName", packageName);
                intent.setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.PermissionAndApps"));
                break;
            case MANUFACTURER_LG: // LG
                intent.setAction("android.intent.action.MAIN");
                intent.putExtra("packageName", packageName);
                ComponentName comp = new ComponentName("com.android.settings", "com.android.settings.Settings$AccessLockSummaryActivity");
                intent.setComponent(comp);
                break;
            case MANUFACTURER_SAMSUNG: // 三星
            default:
                other = false;
                break;
        }
        activity.startActivity(intent);
        return other;
    }

    /**
     * 系统设置界面
     *
     * @param activity
     */
    public static void SystemConfig(Activity activity) {
        Intent intent = new Intent(Settings.ACTION_SETTINGS);
        activity.startActivity(intent);
    }

    private static String getMiuiVersion() {
        String propName = "ro.miui.ui.version.name";
        String line;
        BufferedReader input;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + propName);
            input = new BufferedReader(
                    new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        Log.i("", "MiuiVersion = " + line);
        return line;
    }

}