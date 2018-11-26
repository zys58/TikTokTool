package com.nice.config;

import android.app.Application;
import android.content.Context;

public class BaseAppliction extends Application {
    private Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public Context getContext(){
        return  context;
    }

}
