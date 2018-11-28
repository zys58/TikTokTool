package com.nice.service;

import android.accessibilityservice.AccessibilityService;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.nice.config.Config;
import com.nice.utils.PerformClickUtils;

import java.util.ArrayList;
import java.util.List;

public class TikTokAccessibilityService extends AccessibilityService {

    public static List<String> privateLetterList = new ArrayList<>();
    public static Integer attentionCount = 0;
    //操作状态  正在私信/关注
    public static Boolean privatelyLetter = false;
    public static Boolean attentionLetter = false;

    @Override
    protected void onServiceConnected() {
        toast("服务已开启！");
        super.onServiceConnected();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        if (Config.getInstance(this).getActivated()) {

            if (Config.getInstance(this).getStatus()) {
                if (Config.getInstance(this).getOption().equals(Config.CONCERN)) {
                    //当没有私信才进行节点检索
                    if (!privatelyLetter) {
                        AccessibilityNodeInfo accessibilityNodeInfo = this.getRootInActiveWindow();
                        final List<AccessibilityNodeInfo> attentionBtns = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId("com.ss.android.ugc.aweme:id/c11");
                        if (!attentionBtns.isEmpty()) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    //改变当前状态为正在私信
                                    privatelyLetter = true;
                                    //循环当页已获取用户列表
                                    attention(attentionBtns);
                                    //翻页
                                    if (Config.getInstance(TikTokAccessibilityService.this).getStatus()) {
                                        if (!PerformClickUtils.findViewIdAndScroll(TikTokAccessibilityService.this, "com.ss.android.ugc.aweme:id/aaz")) {
                                            toast("脚本已执行完毕");
                                            Config.getInstance(TikTokAccessibilityService.this).setStatus(false);
                                        }
                                    }
                                    privatelyLetter = false;
                                }
                            }).start();
                            //清理节点
                            accessibilityNodeInfo.recycle();
                        }
                    }
                } else if (Config.getInstance(this).getOption().equals(Config.PRIVATELY)) {
                    String currentWindowActivity = event.getClassName().toString();
                    if ("com.ss.android.ugc.aweme.main.MainActivity".equals(currentWindowActivity)) {
                        PerformClickUtils.findViewIdAndClick(this, "com.ss.android.ugc.aweme:id/a_7");
                    }

                    //当没有进行关注操作时才进行节点检索
                    if (!attentionLetter) {
                        AccessibilityNodeInfo accessibilityNodeInfo = this.getRootInActiveWindow();
                        final List<AccessibilityNodeInfo> privatelyViews = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId("com.ss.android.ugc.aweme:id/axn");
                        if (!privatelyViews.isEmpty()) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    //改变当前状态为正在关注
                                    attentionLetter = true;
                                    //循环当页已获取用户列表
                                    privately(privatelyViews);
                                    //翻页
                                    if (Config.getInstance(TikTokAccessibilityService.this).getStatus()) {
                                        PerformClickUtils.findViewIdAndScroll(TikTokAccessibilityService.this, "com.ss.android.ugc.aweme:id/aaz");
                                    }
                                    attentionLetter = false;
                                }
                            }).start();
                            //清理节点
                            accessibilityNodeInfo.recycle();
                        }
                    }

                }
            }
        }

    }

    @Override
    public void onInterrupt() {
        toast("服务已中断！");
    }

    public void toast(final String msg) {
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                Toast.makeText(TikTokAccessibilityService.this, msg, Toast.LENGTH_LONG).show();
                Looper.loop();
            }
        }).start();
    }

    /**
     * 关注
     *
     * @param attentionBtns
     */
    public synchronized void attention(List<AccessibilityNodeInfo> attentionBtns) {

        if (Config.getInstance(this).getStatus() && Config.getInstance(this).getOption().equals(Config.CONCERN) && !attentionBtns.isEmpty()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (AccessibilityNodeInfo concernBtn : attentionBtns) {
                Log.i("执行操作：", "关注");
                if (concernBtn.getText().toString().equals("关注") && Config.getInstance(this).getStatus() && Config.getInstance(this).getOption().equals(Config.CONCERN) && attentionCount <= 200) {
                    concernBtn.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    attentionCount++;
                    toast("已关注" + attentionCount + "人");
                    try {
                        //关注停顿
                        Thread.sleep(Config.getInstance(this).getAttentionSpeed() + Math.round(2000 * Math.random()));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 私信
     *
     * @param privatelyViews
     */
    public synchronized void privately(List<AccessibilityNodeInfo> privatelyViews) {

        if (Config.getInstance(this).getStatus() && Config.getInstance(this).getOption().equals(Config.PRIVATELY) && !privatelyViews.isEmpty()) {
            PerformClickUtils.findViewIdAndClick(TikTokAccessibilityService.this, "com.ss.android.ugc.aweme:id/a_7");

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            for (AccessibilityNodeInfo info : privatelyViews) {
                try {
                    if (Config.getInstance(this).getStatus() && Config.getInstance(this).getOption().equals(Config.PRIVATELY) && !privateLetterList.contains(info.getText().toString())) {

                        Thread.sleep(500);

                        int count = 0;
                        AccessibilityNodeInfo rootInActiveWindow;
                        do {
                            Log.i("昵称：", info.getText().toString() + "--点击主页");
                            PerformClickUtils.performClick(info);
                            rootInActiveWindow = this.getRootInActiveWindow();
                            Thread.sleep(500);
                            count++;
                            if (count >= 5) {
                                break;
                            }
                        }
                        while (rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.ss.android.ugc.aweme:id/a9f").isEmpty() && Config.getInstance(this).getStatus());
                        count = 0;
                        do {
                            Log.i("昵称：", info.getText().toString() + "--进入私聊界面");
                            PerformClickUtils.findViewIdAndClick(this, "com.ss.android.ugc.aweme:id/a9f");
                            rootInActiveWindow = this.getRootInActiveWindow();
                            Thread.sleep(500);
                            count++;
                            if (count >= 5) {
                                break;
                            }
                        }
                        while (rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.ss.android.ugc.aweme:id/bcn").isEmpty() && Config.getInstance(this).getStatus());
                        count = 0;

                        //已经私信过的不操作
                        while (rootInActiveWindow.findAccessibilityNodeInfosByText(Config.getInstance(this).getPrivatelyContent()).isEmpty() && Config.getInstance(this).getStatus()) {
                            Log.i("昵称：", info.getText().toString() + "--私信");
                            PerformClickUtils.findViewIdAndClick(this, "com.ss.android.ugc.aweme:id/bcn");
                            rootInActiveWindow = this.getRootInActiveWindow();
                            Thread.sleep(500);
                            count++;
                            if (count >= 5) {
                                break;
                            }

                            int sendCount = 0;
                            // 模拟粘贴
                            List<AccessibilityNodeInfo> bcn;
                            do {
                                Log.i("昵称：", info.getText().toString() + "--寻找输入框");
                                bcn = this.getRootInActiveWindow().findAccessibilityNodeInfosByViewId("com.ss.android.ugc.aweme:id/bcn");
                                Thread.sleep(500);
                                sendCount++;
                                if (sendCount >= 5) {
                                    break;
                                }
                            }
                            while (bcn.isEmpty() && Config.getInstance(this).getStatus());
                            Thread.sleep(500);

                            Log.i("昵称：", info.getText().toString() + "--发送私信");
                            bcn.get(0).performAction(AccessibilityNodeInfo.ACTION_PASTE);

                            PerformClickUtils.findViewIdAndClick(this, "com.ss.android.ugc.aweme:id/i0");
                            Thread.sleep(500);
                        }
                        count = 0;

                        do {
                            Log.i("昵称：", info.getText().toString() + "--返回主页");
                            PerformClickUtils.findViewIdAndClick(this, "com.ss.android.ugc.aweme:id/bcd");
                            rootInActiveWindow = this.getRootInActiveWindow();
                            Thread.sleep(500);
                            count++;
                            if (count >= 5) {
                                break;
                            }
                        }
                        while (rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.ss.android.ugc.aweme:id/a9f").size() == 0 && Config.getInstance(this).getStatus());
                        count = 0;

                        List<AccessibilityNodeInfo> byText;
                        do {
                            Log.i("昵称：", info.getText().toString() + "--返回列表");
                            PerformClickUtils.findViewIdAndClick(this, "com.ss.android.ugc.aweme:id/k1");
                            Thread.sleep(2000);
                            byText = getRootInActiveWindow().findAccessibilityNodeInfosByViewId("com.ss.android.ugc.aweme:id/aaz");
                            count++;
                            if (count >= 5) {
                                break;
                            }
                        }
                        while (byText.size() == 0 && Config.getInstance(this).getStatus());
                        //放入已私信列表
                        privateLetterList.add(info.getText().toString());
                        toast("已私信" + privateLetterList.size() + "人");
                    }
                    Thread.sleep(Config.getInstance(this).getPrivatelySpeed() + Math.round(2000 * Math.random()));
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
