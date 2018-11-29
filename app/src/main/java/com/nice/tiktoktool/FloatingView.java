package com.nice.tiktoktool;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.*;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import com.nice.config.Config;
import com.nice.service.TikTokAccessibilityService;

/**
 * 悬浮窗view
 */
public class FloatingView extends FrameLayout {
    private Context mContext;
    private View mView;
    private ImageView beginBtn;
    private WindowManager.LayoutParams mParams;
    private FloatingManager mWindowManager;


    private final int statusHeight;
    int sW;
    private float mTouchStartX;
    private float mTouchStartY;
    private float x;
    private float y;
    private float mLastX;
    private float mLastY;
    private float mStartX;
    private float mStartY;
    private long mLastTime;
    private long mCurrentTime;

    public FloatingView(Context context) {
        super(context);
        mContext = context.getApplicationContext();
        LayoutInflater mLayoutInflater = LayoutInflater.from(context);
        mView = mLayoutInflater.inflate(R.layout.float_static, null);
        beginBtn = mView.findViewById(R.id.begin_btn);
        mWindowManager = FloatingManager.getInstance(mContext);
        statusHeight = getStatusHeight(context);

        beginBtn.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //获取相对屏幕的坐标，即以屏幕左上角为原点
                x = event.getRawX();
                y = event.getRawY();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:  //捕获手指触摸按下动作
                        //获取相对View的坐标，即以此View左上角为原点
                        mTouchStartX = event.getX();
                        mTouchStartY = event.getY();
                        mStartX = event.getRawX();
                        mStartY = event.getRawY();
                        mLastTime = System.currentTimeMillis();
                        break;


                    case MotionEvent.ACTION_MOVE:  //捕获手指触摸移动动作
                        updateViewPosition();
                        break;


                    case MotionEvent.ACTION_UP:  //捕获手指触摸离开动作
                        mLastX = event.getRawX();
                        mLastY = event.getRawY();

                        mCurrentTime = System.currentTimeMillis();
                        if (mCurrentTime - mLastTime < 1500) {
                            if (Math.abs(mStartX - mLastX) < 10.0 && Math.abs(mStartY - mLastY) < 10.0) {
                                //处理点击的事件
                                click();
                            }
                        }
                        break;
                }
                return true;
            }
        });
    }

    private void updateViewPosition() {
        WindowManager wm = (WindowManager) getContext()
                .getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();
        //更新浮动窗口位置参数
        mParams.x = width - (int) x - (int) mTouchStartX;
        mParams.y = (int) y - (height / 2);
        mWindowManager.updateView(mView, mParams); //刷新显示
    }

    public void click() {
        if (Config.getInstance(getContext()).getActivated()) {
            if (Config.getInstance(getContext()).getStatus()) {
                Config.getInstance(getContext()).setStatus(false);
                beginBtn.setImageResource(R.mipmap.start);
                TikTokAccessibilityService.attentionLetter = false;
                Toast.makeText(getContext(), "停止执行...", Toast.LENGTH_SHORT).show();
                TikTokAccessibilityService.privateLetterList.clear();
                TikTokAccessibilityService.attentionCount = 0;
            } else {
                Config.getInstance(getContext()).setStatus(true);
                beginBtn.setImageResource(R.mipmap.stop);
                Toast.makeText(getContext(), "开始执行...", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "软件未激活！", Toast.LENGTH_LONG).show();
        }
    }

    public void show() {
        mParams = new WindowManager.LayoutParams();
        mParams.gravity = Gravity.CENTER | Gravity.RIGHT;
        mParams.x = 0;
        mParams.y = 200;
        //总是出现在应用程序窗口之上
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//8.0
            mParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        mParams.format = PixelFormat.TRANSLUCENT;
        mParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams
                .FLAG_NOT_FOCUSABLE;
        mParams.format = PixelFormat.TRANSPARENT;
        mParams.width = LayoutParams.WRAP_CONTENT;
        mParams.height = LayoutParams.WRAP_CONTENT;
        mWindowManager.addView(mView, mParams);
    }

    public void hide() {
        mWindowManager.removeView(mView);
    }

    /**
     * 获得状态栏的高度
     *
     * @param context
     * @return
     */
    public static int getStatusHeight(Context context) {


        int statusHeight = -1;
        try {
            Class clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField("status_bar_height")
                    .get(object).toString());
            statusHeight = context.getResources().getDimensionPixelSize(height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusHeight;
    }

}