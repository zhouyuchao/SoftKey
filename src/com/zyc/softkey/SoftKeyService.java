package com.zyc.softkey;

import java.lang.reflect.Field;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
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

    private WindowManager mWindowManager;
    private LayoutParams mWmParams;

    private Button mSoftKey;

    private float mPosToViewX;
    private float mPosToViewY;
    private float mPosToScreenX;
    private float mPosToScreenY;
    
    private float mStartOffsetX;
    private float mStartOffsetY;

    private int mStatusBarHeight;
    
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
        mWindowManager = (WindowManager) getApplication().getSystemService(WINDOW_SERVICE);

        mWmParams = new WindowManager.LayoutParams();
        mWmParams.width = 90; // LayoutParams.WRAP_CONTENT
        mWmParams.height = 90;
        mWmParams.gravity = Gravity.TOP | Gravity.LEFT; // Gravity.RIGHT | Gravity.CENTER_VERTICAL
        mWmParams.type = LayoutParams.TYPE_PHONE;
        mWmParams.flags |= LayoutParams.FLAG_NOT_FOCUSABLE;
        mWmParams.format = 1;
        mWmParams.alpha = 1; // 透明度调节

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
    
    private void updateViewPosition() {
        mWmParams.x = (int) (mPosToScreenX - mPosToViewX);
        mWmParams.y = (int) (mPosToScreenY - mPosToViewY);
        Log.i(TAG, "updateViewPosition (" + mWmParams.x + ", " + mWmParams.y + ")");
        mWindowManager.updateViewLayout(mSoftKey, mWmParams);
    }
    
    OnTouchListener skTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int statusBarHeight = mStatusBarHeight;
            
            switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "---> ACTION_DOWN <---");
                // 获取相对Widget自身左上角的位置
                mPosToViewX = event.getX();
                mPosToViewY = event.getY();
                Log.d(TAG, "mTouchStartX = " + mPosToViewX + ", mTouchStartY = " + mPosToViewY);
                
                mStartOffsetX = event.getRawX();
                mStartOffsetY = event.getRawY();
                
                mSoftKey.setBackgroundResource(R.drawable.ic_floater_hl);
                
                if (mHandler.hasMessages(MSG_UPDATE_SOFTKEY)) {
                    mHandler.removeMessages(MSG_UPDATE_SOFTKEY);
                }
                break;
            case MotionEvent.ACTION_MOVE:
//                Log.d(TAG, "---> ACTION_MOVE <---");
                // 获取相对屏幕左上角的位置
                mPosToScreenX = event.getRawX();
                mPosToScreenY = event.getRawY() - statusBarHeight;
//                Log.d(TAG, "ACTION_MOVE x = " + x + ", y = " + y);
                
                updateViewPosition();
                break;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "---> ACTION_UP <---");
                
                float newOffsetX = event.getRawX();
                float newOffsetY = event.getRawY();
                if (Math.abs(newOffsetX) - Math.abs(mStartOffsetX) > 3
                        && Math.abs(newOffsetY) - Math.abs(mStartOffsetY) > 3){
                    Toast.makeText(getApplicationContext(), "click...", Toast.LENGTH_SHORT).show();
                } else {
                    updateViewPosition();
                }
                
                mHandler.sendEmptyMessageDelayed(MSG_UPDATE_SOFTKEY, INT_SOFTKEY_UPDATE_TIME);
                break;
            }
            return true;
        }
    };
}
