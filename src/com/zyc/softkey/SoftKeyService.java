package com.zyc.softkey;

import java.lang.reflect.Field;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;

public class SoftKeyService extends Service {
    public static final String TAG = "SoftKeyService";
    
    /** update softkey after touch event ACTION_UP delay */
    private static final int INT_SOFTKEY_UPDATE_TIME = 3000;
    
    /** msg for update softkey */
    private static final int MSG_UPDATE_SOFTKEY = 1000;

    private WindowManager mWindowManager;
    private LayoutParams mWmParams;

    private Button mSoftKey;

    private float mTouchStartX;
    private float mTouchStartY;
    private float x;
    private float y;

    private int statusBarHeight;
    
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
            default:
                break;
            }
        };
    };

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate --->");
        
        statusBarHeight = getStatusBarHeight();
        Log.d(TAG, "onCreate ---> statusBarHeight = " + statusBarHeight);
        
        mWindowManager = (WindowManager) getApplication().getSystemService(WINDOW_SERVICE);
        
        showSoftKey();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand --->");
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
        mWindowManager = ((WindowManager) getSystemService("window"));

        mWmParams = new WindowManager.LayoutParams();
        mWmParams.width = 90; // LayoutParams.WRAP_CONTENT
        mWmParams.height = 90;
        mWmParams.gravity = 51;
        mWmParams.type = LayoutParams.TYPE_PHONE;
        mWmParams.flags |= LayoutParams.FLAG_NOT_FOCUSABLE;

        WindowManager.LayoutParams localLayoutParams = mWmParams;
        localLayoutParams.flags = (0x8 | localLayoutParams.flags);
        mWmParams.format = 1;

        mSoftKey = new Button(this);
        mSoftKey.setBackgroundResource(R.drawable.ic_floater_hl);

        mWindowManager.addView(mSoftKey, mWmParams);

        mSoftKey.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.d(TAG, "---> ACTION_DOWN <---");
                    mTouchStartX = event.getX();
                    mTouchStartY = event.getY();
//                    Log.d(TAG, "mTouchStartX = " + mTouchStartX + ", mTouchStartY = " + mTouchStartY);
                    
                    mSoftKey.setBackgroundResource(R.drawable.ic_floater_hl);
                    if (mHandler.hasMessages(MSG_UPDATE_SOFTKEY)) {
                        mHandler.removeMessages(MSG_UPDATE_SOFTKEY);
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
//                    Log.d(TAG, "---> ACTION_MOVE <---");
                    x = event.getRawX();
                    y = event.getRawY() - mSoftKey.getHeight() / 2;
//                    Log.d(TAG, "x = " + x + ", y = " + y);
                    
                    updateViewPosition();
                    break;
                case MotionEvent.ACTION_UP:
                    Log.d(TAG, "---> ACTION_UP <---");
                    updateViewPosition();
                    mHandler.sendEmptyMessageDelayed(MSG_UPDATE_SOFTKEY, INT_SOFTKEY_UPDATE_TIME);
                    break;
                }
                return true;
            }
        });
    }

    private void updateViewPosition() {
        mWmParams.x = ((int) (x - mTouchStartX));
        mWmParams.y = ((int) (y - mTouchStartY));
        mWindowManager.updateViewLayout(mSoftKey, mWmParams);
    }

    private int getStatusBarHeight() {
        if (statusBarHeight == 0) {
            try {
                Class c = Class.forName("com.android.internal.R$dimen");
                Object o = c.newInstance();
                Field field = c.getField("status_bar_height");
                int x = (Integer) field.get(o);
                statusBarHeight = getResources().getDimensionPixelSize(x);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return statusBarHeight;
    }
}
