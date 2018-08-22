package com.ywj.qianghongbao;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

/**
 * Created by weijing on 2017-01-05 17:22
 */

public class RobMoney extends AccessibilityService {
    String tag = "RobMoneyTag";
    /**
     * 是否打开红包标记
     */
    boolean isOpen = false;
    Handler handler =new Handler();

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        ToastUtils.show(getApplicationContext(),"服务开启成功");
        Intent intent = new Intent();
        intent.putExtra("isOpen",true);
        intent.setAction("com.ywj.qianghongbao.isOpen");
        sendBroadcast(intent);
    }

    @Override
    public void onInterrupt() {
        ToastUtils.show(getApplicationContext(),"服务关闭");
        Intent intent = new Intent();
        intent.putExtra("isOpen",false);
        intent.setAction("com.ywj.qianghongbao.isOpen");
        sendBroadcast(intent);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        Log.i(tag, "通知类型:" + eventType);
        switch (eventType) {
            //第一步：监听通知栏消息
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                List<CharSequence> texts = event.getText();
                if (!texts.isEmpty()) {
                    for (CharSequence text : texts) {
                        String content = text.toString();
                        Log.i(tag, "通知内容:" + content);
                        if (content.contains("[微信红包]")) {
                            //模拟打开通知栏消息
                            if (event.getParcelableData() != null && event.getParcelableData() instanceof Notification) {
                                Notification notification = (Notification) event.getParcelableData();
                                PendingIntent pendingIntent = notification.contentIntent;
                                try {
                                    isOpen = true;
                                    pendingIntent.send();
                                } catch (CanceledException e) {
                                    e.printStackTrace();
                                }
                                //处理停留在微信首页，不会触发TYPE_WINDOW_STATE_CHANGED事件
                                String className = event.getClassName().toString();
                                if (className.equals("com.tencent.mm.ui.LauncherUI")) {

                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            getPacket();
                                        }
                                    },1000);
                                }
                            }
                        }
                    }
                }
                break;
            //第二步：监听是否进入微信红包消息界面
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                String className = event.getClassName().toString();
                Log.e(tag, "页面：" + className);
                /**
                 * com.tencent.mm.ui.base.p
                 * com.tencent.mm.ui.LauncherUI 微信聊天列表、页面
                 * com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI 红包页面
                 * com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI  抢到红包的详情页面
                 */
                if (isOpen) {
                    if (className.equals("com.tencent.mm.ui.LauncherUI")) {
                        //开始抢红包，微信聊天页面
                        getPacket();
                    } else if (className.equals("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI")) {
                        //开始打开红包
                        openPacket();
                        isOpen = false;
                    }
                } else {
                    //如果打开过了，就点击返回，离开聊天室
                    if (className.equals("com.tencent.mm.ui.LauncherUI")) {
                        chatRoomGoBack();
                    }

                }

                if (className.equals("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI")) {
                    //抢到红包的详情页面 开始返回
                    detailGoBack();
                }

                break;
        }
    }

    /**
     * 红包详情页面 返回
     */
    private void detailGoBack() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo != null) {
            List<AccessibilityNodeInfo> viewIds = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/gq");
            for (AccessibilityNodeInfo viewId : viewIds) {
                viewId.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }
    }

    /**
     * 聊天室 返回
     */
    private void chatRoomGoBack() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo != null) {
            List<AccessibilityNodeInfo> viewIds = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/gb");
            for (AccessibilityNodeInfo viewId : viewIds) {
                viewId.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }
    }

    /**
     * 打开红包
     */
    private void openPacket() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo != null) {
            //如果红包发完，关闭页面
            List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("手慢了，红包派完了");
            if (list.size() > 0) {
                List<AccessibilityNodeInfo> backs = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bdl");
                for (AccessibilityNodeInfo back : backs) {
                    back.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            }
            //如果红包可以抢，就点击开抢
            List<AccessibilityNodeInfo> viewIds = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bdh");
            for (AccessibilityNodeInfo viewId : viewIds) {
                viewId.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }

    }

    private void getPacket() {
        AccessibilityNodeInfo info = getRootInActiveWindow();

        List<AccessibilityNodeInfo> lingquhongbaos = info.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/a50");
        Log.e(tag, "看到红包1");
        if (lingquhongbaos.size() > 0) {
            Log.e(tag, "看到红包2");
            click(lingquhongbaos.get(lingquhongbaos.size() - 1));
        }
//        recycle(rootNode);
    }

    /**
     * 打印一个节点的结构
     *
     * @param info
     */
    public void recycle(AccessibilityNodeInfo info) {
        if (info.getChildCount() == 0) {
            if (info.getText() != null && "领取红包".equals(info.getText().toString())) {
                List<AccessibilityNodeInfo> lingquhongbaos = info.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/a50");
                if (lingquhongbaos.size() > 0) {
                    click(lingquhongbaos.get(lingquhongbaos.size() - 1));
                }
//                if ("领取红包".equals(info.getText().toString())) {
//                    //这里有一个问题需要注意，就是需要找到一个可以点击的View
//                    Log.i("demo", "Click" + ",isClick:" + info.isClickable());
//                    info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
//                    AccessibilityNodeInfo parent = info.getParent();
//                    while (parent != null) {
//                        Log.i("demo", "parent isClick:" + parent.isClickable());
//                        if (parent.isClickable()) {
//                            parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
//                            break;
//                        }
//                        parent = parent.getParent();
//                    }
//
//                }
            }

        } else {
            for (int i = 0; i < info.getChildCount(); i++) {
                if (info.getChild(i) != null) {
                    recycle(info.getChild(i));
                }
            }
        }
    }


    int clickTimes = 0;

    /**
     * 点击
     *
     * @param info
     */
    private void click(AccessibilityNodeInfo info) {
        Log.e(tag, "点击红包1：" + clickTimes);
        clickTimes = 0;
        if (info != null) {
            if (info.isCheckable()) {
                info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                Log.e(tag, "点击红包11：" + clickTimes);
                clickTimes = 0;
            } else {
                reClick(info.getParent());
            }
        }

    }

    private void reClick(AccessibilityNodeInfo info) {
        Log.e(tag, "点击红包2：" + clickTimes);


        clickTimes += 1;
        if (clickTimes > 6) {
            //向上遍历6层父节点
            clickTimes = 0;
            return;
        }
        if (info != null) {
            info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            if (info.isCheckable()) {
                info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                Log.e(tag, "点击红包22：" + clickTimes);
                clickTimes = 0;
            } else {
                reClick(info.getParent());
            }
        }

    }

}