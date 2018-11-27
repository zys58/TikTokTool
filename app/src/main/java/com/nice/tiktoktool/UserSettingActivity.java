package com.nice.tiktoktool;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.nice.service.MyService;

public class UserSettingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_setting);
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
