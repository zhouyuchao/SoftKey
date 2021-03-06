package com.zyc.softkey.view;

import java.lang.reflect.Field;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.Toast;

import com.zyc.softkey.R;
import com.zyc.softkey.operate.Operations;
import com.zyc.softkey.utils.ILog;

public class FloatView {
    private static final String TAG = "FloatView";
    
    /** update softkey after touch event ACTION_UP delay */
    private static final int INT_SOFTKEY_UPDATE_TIME = 3000;
    
    /** msg for update softkey */
    private static final int MSG_UPDATE_SOFTKEY = 1000;
    private static final int MSG_CLICK = 1001;
    private static final int MSG_DOUBLE_CLICK = 1002;
    private static final int MSG_LONG_PRESS = 1003;
    
    private static final int MSG_TIME_LOOP = 1004;
    
    private static final int LOOP_TIME = 1000;
    
    private static final int FLOAT_CONTROL_OFFSET = 10;
    private static final int FLOAT_CONTROL_LONGPRESS = 1000;
    

    private WindowManager mWindowManager;
    private LayoutParams mWmParams;
    private Context mContext;

    private Button mSoftKey;
    
    private int mScreenWidth;
    private int mScreenHeight;
    
    private float mStartOffsetX;
    private float mStartOffsetY;
    private int mStatusBarHeight;
    
    Handler mHandler = new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_UPDATE_SOFTKEY:
                if (null != mSoftKey) {
                    ILog.d(TAG, "handler ---> update softkey...");
                    mSoftKey.setBackgroundResource(R.drawable.ic_floater_nor);
                }
                break;
                
            case MSG_CLICK:
                click();
                break;
                
            case MSG_DOUBLE_CLICK:
                doubleClick();
                break;
                
            case MSG_LONG_PRESS:
                longPress();
                break;
                
            case MSG_TIME_LOOP:
                ILog.d(TAG, "handler ---> MSG_TIME_LOOP ...");
                if (mHandler.hasMessages(MSG_TIME_LOOP)) {
                    mHandler.removeMessages(MSG_TIME_LOOP);
                }
                
                new Thread() {
                    @Override
                    public void run() {
                        Operations.doExec();
                    }
                }.start();
                
//                mHandler.sendEmptyMessageDelayed(MSG_TIME_LOOP, LOOP_TIME);
                break;
                
            default:
                break;
            }
        };
    };
    
    public FloatView(Context context){
        mContext = context;
        init();
    }
    
    public void showFloatView(){
        showSoftKey();
    }


    public void init() {
        ILog.d(TAG, "init --->");
        
        mStatusBarHeight = getStatusBarHeight();
        ILog.d(TAG, "init ---> statusBarHeight = " + mStatusBarHeight);
        
        mWindowManager = (WindowManager) mContext.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
    }


    public void release() {
        ILog.d(TAG, "release --->");
        if (mWindowManager != null) {
            mWindowManager.removeView(mSoftKey);
        }
    }

    private void showSoftKey() {
        ILog.d(TAG, "showSoftKey --->");
        DisplayMetrics dm = new DisplayMetrics();
        dm = mContext.getResources().getDisplayMetrics();
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;
        
        mWmParams = new WindowManager.LayoutParams();
        mWmParams.width = 90; // LayoutParams.WRAP_CONTENT
        mWmParams.height = 90;
        mWmParams.gravity = Gravity.TOP | Gravity.LEFT; // Gravity.RIGHT | Gravity.CENTER_VERTICAL
        mWmParams.x = dm.widthPixels - mWmParams.width;
        mWmParams.y = (dm.heightPixels - mWmParams.height) / 2;
        mWmParams.type = LayoutParams.TYPE_PHONE;
        mWmParams.flags |= LayoutParams.FLAG_NOT_FOCUSABLE;
        mWmParams.format = 1;
        mWmParams.alpha = 0.6f; // ͸���ȵ���

        mSoftKey = new Button(mContext);
        mSoftKey.setBackgroundResource(R.drawable.ic_floater_hl);
        mSoftKey.setOnTouchListener(skTouchListener);
        
        mWindowManager.addView(mSoftKey, mWmParams);
        
        mHandler.sendEmptyMessageDelayed(MSG_UPDATE_SOFTKEY, INT_SOFTKEY_UPDATE_TIME);
    }

    private int getStatusBarHeight() {
        if (mStatusBarHeight == 0) {
            try {
                Class c = Class.forName("com.android.internal.R$dimen");
                Object o = c.newInstance();
                Field field = c.getField("status_bar_height");
                int x = (Integer) field.get(o);
                mStatusBarHeight = mContext.getResources().getDimensionPixelSize(x);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return mStatusBarHeight;
    }
    
    private void updateViewPosition(int x, int y) {
        mWmParams.x = x;
        mWmParams.y = y;
        ILog.i(TAG, "updateViewPosition (" + x + ", " + y + ")");
        mWindowManager.updateViewLayout(mSoftKey, mWmParams);
    }
    
    OnTouchListener skTouchListener = new OnTouchListener() {
        float viewPosX = 0;
        float viewPosY = 0;
        float screenPosX = 0;
        float screenPosY = 0;
        
        long startTime = 0;
        
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int statusBarHeight = mStatusBarHeight;
//            ILog.d(TAG, "ouTouch ---> ");
            switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                ILog.d(TAG, "---> ACTION_DOWN <---");
                startTime = System.currentTimeMillis();
                
                viewPosX = event.getX();
                viewPosY = event.getY();
//                ILog.d(TAG, "getX = " + mPosToViewX + ", getY = " + mPosToViewY);
                
                mStartOffsetX = event.getRawX();
                mStartOffsetY = event.getRawY();
                ILog.d(TAG, "getRawX = " + mStartOffsetX + ", getRawY = " + mStartOffsetY);
                
                mSoftKey.setBackgroundResource(R.drawable.ic_floater_hl);
                
                if (mHandler.hasMessages(MSG_UPDATE_SOFTKEY)) {
                    mHandler.removeMessages(MSG_UPDATE_SOFTKEY);
                }
                
                mHandler.sendEmptyMessageDelayed(MSG_LONG_PRESS, FLOAT_CONTROL_LONGPRESS);
                break;
            case MotionEvent.ACTION_MOVE:
//                ILog.d(TAG, "---> ACTION_MOVE <---");
                screenPosX = event.getRawX();
                screenPosY = event.getRawY() - statusBarHeight;
                
                ILog.d(TAG, "ACTION_MOVE x = " + event.getX() + ", y = " + event.getY());
                ILog.d(TAG, "ACTION_MOVE rawx = " + event.getRawX() + ", rawy = " + event.getRawY());
                
                if (Math.abs(mStartOffsetX) - Math.abs(screenPosX) > FLOAT_CONTROL_OFFSET
                        || Math.abs(mStartOffsetY) - Math.abs(screenPosY + statusBarHeight) > FLOAT_CONTROL_OFFSET) {
                    if (mHandler.hasMessages(MSG_LONG_PRESS)) {
                        ILog.d(TAG, "ACTION_MOVE remove MSG_LONG_PRESS");
                        mHandler.removeMessages(MSG_LONG_PRESS);
                    }
                    updateViewPosition((int)(screenPosX - viewPosX), (int)(screenPosY - viewPosY));
                }
                break;
            case MotionEvent.ACTION_UP:
                ILog.d(TAG, "---> ACTION_UP <---");
                float newOffsetX = event.getRawX();
                float newOffsetY = event.getRawY();
                ILog.d(TAG, "newOffsetX = " + newOffsetX + ", newOffsetY = " + newOffsetY);
                
                if (startTime - System.currentTimeMillis() < 500 && mHandler.hasMessages(MSG_LONG_PRESS)) {
                    ILog.d(TAG, "ACTION_UP remove MSG_LONG_PRESS");
                    mHandler.removeMessages(MSG_LONG_PRESS);
                    
                    if (mHandler.hasMessages(MSG_CLICK)) {
                        mHandler.sendEmptyMessage(MSG_DOUBLE_CLICK);
                    } else {
                        mHandler.sendEmptyMessageDelayed(MSG_CLICK, 500);
                    }
                } else {
                    if (newOffsetX < mScreenWidth / 2) {
                        newOffsetX = 0;
                    } else {
                        newOffsetX = mScreenWidth;
                    }
                    updateViewPosition((int)newOffsetX, (int)(newOffsetY - viewPosY - statusBarHeight));
                }
                
                mHandler.sendEmptyMessageDelayed(MSG_UPDATE_SOFTKEY, INT_SOFTKEY_UPDATE_TIME);
                break;
            }
            return true;
        }
    };
    
    private void click(){
        Toast.makeText(mContext.getApplicationContext(), "click...", Toast.LENGTH_SHORT).show();
        mHandler.sendEmptyMessageDelayed(MSG_TIME_LOOP, LOOP_TIME);
    }
    
    private void doubleClick(){
        Toast.makeText(mContext.getApplicationContext(), "doubleClick...", Toast.LENGTH_SHORT).show();
        Operations.goToHome(mContext);
    }
    
    private void longPress(){
        Toast.makeText(mContext.getApplicationContext(), "longPress...", Toast.LENGTH_SHORT).show();
        Operations.stopExec();
    }
}
