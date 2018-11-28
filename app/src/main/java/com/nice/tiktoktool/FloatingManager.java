package com.nice.tiktoktool;

import android.content.Context;
import android.view.View;
import android.view.WindowManager;

/**
 * 悬浮窗管理类
 */
public class FloatingManager {

    private WindowManager mWindowManager;
    private static FloatingManager mInstance;
    private Context mContext;

    public static FloatingManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new FloatingManager(context);
        }
        return mInstance;
    }

    private FloatingManager(Context context) {
        mContext = context;
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);//获得WindowManager对象
    }

    /**
     * 添加悬浮窗
     *
     * @param view
     * @param params
     * @return
     */
    protected boolean addView(View view, WindowManager.LayoutParams params) {
        try {
            mWindowManager.addView(view, params);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 移除悬浮窗
     *
     * @param view
     * @return
     */
    protected boolean removeView(View view) {
        try {
            mWindowManager.removeView(view);
            return true;
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * 更新悬浮窗参数
     *
     * @param view
     * @param params
     * @return
     */
    protected boolean updateView(View view, WindowManager.LayoutParams params) {
        try {
            mWindowManager.updateViewLayout(view, params);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}