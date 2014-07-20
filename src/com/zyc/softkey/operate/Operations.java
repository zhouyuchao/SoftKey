package com.zyc.softkey.operate;

import android.content.Context;
import android.content.Intent;

import com.zyc.softkey.utils.ILog;

public class Operations {
    private static final String TAG = "Operations";
    
    private Context mContext;
    
    public Operations(Context context){
        mContext = context;
    }
    
    public static void goToHome(Context context){
        ILog.d(TAG, "goToHome");
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

}
