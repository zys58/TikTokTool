package com.nice.tiktoktool;

import android.content.Context;
import android.content.SharedPreferences;


public class NativeDataManager {

    private SharedPreferences mPreference;


    public NativeDataManager(Context context) {
        mPreference = context.getSharedPreferences("setting", Context.MODE_PRIVATE);
    }

    public Long getAttentionSpeed() {
        return mPreference.getLong("attentionSpeed", 8000);
    }

    public void setAttentionSpeed(Long speed) {
        mPreference.edit().putLong("attentionSpeed", speed).apply();
    }

    public Long getPrivatelySpeed() {
        return mPreference.getLong("privatelySpeed", 8000);
    }

    public void setPrivatelySpeed(Long privatelySpeed) {
        mPreference.edit().putLong("privatelySpeed", privatelySpeed).apply();
    }

    public String getPrivatelyContent() {
        return mPreference.getString("privatelyContent", "测试");
    }

    public void setPrivatelyContent(String privatelyContent) {
        mPreference.edit().putString("privatelyContent", privatelyContent).apply();
    }

    public String getActivationCode() {
        return mPreference.getString("activationCode", "");
    }

    public void setActivationCode(String activationCode) {
        mPreference.edit().putString("activationCode", activationCode).apply();
    }

    public String getEndTime() {
        return mPreference.getString("endTime", "");
    }

    public void setEndTime(String activationCode) {
        mPreference.edit().putString("endTime", activationCode).apply();
    }
}