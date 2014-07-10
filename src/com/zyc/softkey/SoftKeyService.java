package com.zyc.softkey;

import java.lang.reflect.Field;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class SoftKeyService extends Service {
    public static final String TAG = "SoftKeyService";
    
    /** update softkey after touch event ACTION_UP delay */
    private static final int INT_SOFTKEY_UPDATE_TIME = 3000;
    
    /** msg for update softkey */
    private static final int MSG_UPDATE_SOFTKEY = 1000;
    private static final int MSG_CLICK = 1001;
    private static final int MSG_DOUBLE_CLICK = 1002;
    private static final int MSG_LONG_PRESS = 1003;
    

    private WindowManager mWindowManager;
    private LayoutParams mWmParams;

    private Button mSoftKey;
    
    private int mScreenWidth;
    private int mScreenHeight;
    
    private float mStartOffsetX;
    private float mStartOffsetY;
    private int mStatusBarHeight;
    
    private boolean mIsClickRange;
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    Handler mHandler = new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_UPDATE_SOFTKEY:
                if (null != mSoftKey) {
                    Log.d(TAG, "handler ---> update softkey...");
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
            default:
                break;
            }
        };
    };

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate --->");
        
        mStatusBarHeight = getStatusBarHeight();
        Log.d(TAG, "onCreate ---> statusBarHeight = " + mStatusBarHeight);
        
        mWindowManager = (WindowManager) getApplication().getSystemService(WINDOW_SERVICE);
        
        showSoftKey();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand ---> ");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy --->");
        if (mWindowManager != null) {
            mWindowManager.removeView(mSoftKey);
        }
        super.onDestroy();
    }

    private void showSoftKey() {
        Log.d(TAG, "showSoftKey --->");
        DisplayMetrics dm = new DisplayMetrics();
        dm = getResources().getDisplayMetrics();
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
        mWmParams.alpha = 0.6f; // 透明度调节

        mSoftKey = new Button(this);
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
                mStatusBarHeight = getResources().getDimensionPixelSize(x);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return mStatusBarHeight;
    }
    
    private void updateViewPosition(int x, int y) {
        mWmParams.x = x;
        mWmParams.y = y;
        Log.i(TAG, "updateViewPosition (" + x + ", " + y + ")");
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
            Log.d(TAG, "ouTouch ---> ");
            switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "---> ACTION_DOWN <---");
                mIsClickRange = true;
                startTime = System.currentTimeMillis();
                
                viewPosX = event.getX();
                viewPosY = event.getY();
//                Log.d(TAG, "getX = " + mPosToViewX + ", getY = " + mPosToViewY);
                
                mStartOffsetX = event.getRawX();
                mStartOffsetY = event.getRawY();
                Log.d(TAG, "getRawX = " + mStartOffsetX + ", getRawY = " + mStartOffsetY);
                
                mSoftKey.setBackgroundResource(R.drawable.ic_floater_hl);
                
                if (mHandler.hasMessages(MSG_UPDATE_SOFTKEY)) {
                    mHandler.removeMessages(MSG_UPDATE_SOFTKEY);
                }
                
                mHandler.sendEmptyMessageDelayed(MSG_LONG_PRESS, 1000);
                break;
            case MotionEvent.ACTION_MOVE:
//                Log.d(TAG, "---> ACTION_MOVE <---");
                screenPosX = event.getRawX();
                screenPosY = event.getRawY() - statusBarHeight;
//                Log.d(TAG, "ACTION_MOVE x = " + mPosToScreenX + ", y = " + mPosToScreenY);
                
                if (mIsClickRange 
                        && Math.abs(mStartOffsetX) - Math.abs(screenPosX) < 3
                        && Math.abs(mStartOffsetY) - Math.abs(screenPosY + statusBarHeight) < 3) {
                    Log.d(TAG, "ACTION_MOVE long click perpare...");
                    if (startTime - System.currentTimeMillis() > 1000) {
                        Toast.makeText(getApplicationContext(), "longClick", Toast.LENGTH_LONG).show();
                    }
                } else if (Math.abs(mStartOffsetX) - Math.abs(screenPosX) > 3
                        || Math.abs(mStartOffsetY) - Math.abs(screenPosY) > 3) {
                    mIsClickRange = false;
                    if (mHandler.hasMessages(MSG_LONG_PRESS)) {
                        Log.d(TAG, "ACTION_MOVE remove MSG_LONG_PRESS");
                        mHandler.removeMessages(MSG_LONG_PRESS);
                    }
                }
                
                updateViewPosition((int)(screenPosX - viewPosX), (int)(screenPosY - viewPosY));
                break;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "---> ACTION_UP <---");
                float newOffsetX = event.getRawX();
                float newOffsetY = event.getRawY();
                Log.d(TAG, "newOffsetX = " + newOffsetX + ", newOffsetY = " + newOffsetY);
                
                if (startTime - System.currentTimeMillis() < 1000 && mHandler.hasMessages(MSG_LONG_PRESS)) {
                    Log.d(TAG, "ACTION_UP remove MSG_LONG_PRESS");
                    mHandler.removeMessages(MSG_LONG_PRESS);
                }
                
                if (mIsClickRange && startTime - System.currentTimeMillis() < 500){
                    if (mHandler.hasMessages(MSG_CLICK)) {
                        mHandler.sendEmptyMessage(MSG_DOUBLE_CLICK);
                    } else {
                        mHandler.sendEmptyMessageDelayed(MSG_CLICK, 300);
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
        Toast.makeText(getApplicationContext(), "click...", Toast.LENGTH_SHORT).show();
    }
    
    private void doubleClick(){
        Toast.makeText(getApplicationContext(), "doubleClick...", Toast.LENGTH_SHORT).show();
        
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    
    private void longPress(){
        
    }
}
