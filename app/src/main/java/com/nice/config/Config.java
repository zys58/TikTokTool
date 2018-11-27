package com.nice.config;

import android.content.Context;

import com.nice.tiktoktool.NativeDataManager;

public class Config {

    public static final Integer CONCERN = 1;
    public static final Integer PRIVATELY = 2;

    private Boolean status;
    private Integer option;

    private static NativeDataManager mNativeDataManager;

    private static Config instance = new Config();

    private Config() {

    }

    public static synchronized Config getInstance(Context context) {
        if (instance == null) {
            instance = new Config();
        }
        if (mNativeDataManager == null) {
            mNativeDataManager = new NativeDataManager(context);
        }
        return instance;
    }

    public Long getAttentionSpeed() {
        return mNativeDataManager.getAttentionSpeed();
    }

    public void setAttentionSpeed(Long attentionSpeed) {
        mNativeDataManager.setAttentionSpeed(attentionSpeed * 1000);
    }

    public Long getPrivatelySpeed() {
        return mNativeDataManager.getPrivatelySpeed();
    }

    public void setPrivatelySpeed(Long privatelySpeed) {
        mNativeDataManager.setPrivatelySpeed(privatelySpeed * 1000);
    }

    public boolean getStatus() {
        return status == null ? false : status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public Integer getOption() {
        return option == null ? CONCERN : option;
    }

    public void setOption(Integer option) {
        this.option = option;
    }

    public String getPrivatelyContent() {
        return mNativeDataManager.getPrivatelyContent();
    }

    public void setPrivatelyContent(String privatelyContent) {
        mNativeDataManager.setPrivatelyContent(privatelyContent);
    }
}
