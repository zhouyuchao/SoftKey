package com.zyc.softkey;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.zyc.softkey.utils.ILog;
import com.zyc.softkey.view.FloatView;

@SuppressLint("HandlerLeak")
public class SoftKeyService extends Service {
    public static final String TAG = "SoftKeyService";
    
    private FloatView mFloatView;
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public void onCreate() {
        ILog.d(TAG, "onCreate --->");
        
        mFloatView = new FloatView(getApplicationContext());
        mFloatView.showFloatView();
        
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ILog.d(TAG, "onStartCommand ---> ");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        ILog.d(TAG, "onDestroy --->");
        
        if (null != mFloatView){
            mFloatView.release();
        }
        
        super.onDestroy();
    }

}
