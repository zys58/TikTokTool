package com.nice.config;

import android.content.Context;

import android.os.Build;

import com.nice.tiktoktool.NativeDataManager;

import java.util.List;
import java.util.Map;

public class Config {

    //    public static final String CODE_VALIDATE_URL = "http://yl888.site:8088/auth/codeValidate";
    public static final String CODE_VALIDATE_URL = "http://tt.yl888.site:8088/auth/codeValidate";

    public static final Integer CONCERN = 1;
    public static final Integer PRIVATELY = 2;
    public static final Integer CANCEL_CONCERN = 3;
    public static final Integer COMMENT_PRIVATELY = 4;

    public static final String ATTENTION_BTN = "ATTENTION_BTN";
    public static final String USER_LIST_BG = "USER_LIST_BG";
    public static final String USER_HP_MORE = "USER_HP_MORE";
    public static final String MORE_TALK_BTN = "MORE_TALK_BTN";
    public static final String SEND_MSG_ET = "SEND_MSG_ET";
    public static final String SEND_MSG_BTN = "SEND_MSG_BTN";
    public static final String TALK_BACK = "TALK_BACK";
    public static final String MORE_BACK = "MORE_BACK";
    public static final String USER_HP_BACK = "USER_HP_BACK";
    public static final String LIST_USER_NAME = "LIST_USER_NAME";


    public static final String DEVICE_INFO = Build.MANUFACTURER + "-" + Build.DEVICE + "-" + Build.VERSION.SDK_INT;

    private Boolean status;
    private Integer option;
    private Boolean activated;

    private Map<String, String> viewIdMap;

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
        return (status == null) ? false : status;
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

    public String[] getPrivatelyContent() {
        return mNativeDataManager.getPrivatelyContent().split("\\|");
    }

    public String getPrivatelyContentText() {
        return mNativeDataManager.getPrivatelyContent();
    }

    public void setPrivatelyContent(String privatelyContent) {
        mNativeDataManager.setPrivatelyContent(privatelyContent);
    }

    public String getActivationCode() {
        return mNativeDataManager.getActivationCode();
    }

    public void setActivationCode(String activationCode) {
        mNativeDataManager.setActivationCode(activationCode);
    }

    public Boolean getActivated() {
        return activated != null && activated;
    }

    public void setActivated(Boolean activated) {
        this.activated = activated;
    }

    public String getEndTime() {
        return mNativeDataManager.getEndTime();
    }

    public void setEndTime(String activationCode) {
        mNativeDataManager.setEndTime(activationCode);
    }

    public Map<String, String> getViewIdMap() {
        return viewIdMap;
    }

    public void setViewIdMap(Map<String, String> viewIdMap) {
        this.viewIdMap = viewIdMap;
    }
}
