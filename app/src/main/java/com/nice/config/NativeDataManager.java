package com.nice.config;

import android.content.Context;
import android.content.SharedPreferences;


public class NativeDataManager {

    private SharedPreferences mPreference;


    public NativeDataManager(Context context) {
        mPreference = context.getSharedPreferences("setting", Context.MODE_PRIVATE);
    }

    public Long getAttentionSpeed() {
        return mPreference.getLong("attentionSpeed", 3000);
    }

    public void setAttentionSpeed(Long speed) {
        mPreference.edit().putLong("attentionSpeed", speed).apply();
    }

    public String getPrivatelyContent(){
        return mPreference.getString("privatelyContent", "测试");
    }

    public void setPrivatelyContent(String privatelyContent){
        mPreference.edit().putString("privatelyContent", privatelyContent).apply();
    }

}