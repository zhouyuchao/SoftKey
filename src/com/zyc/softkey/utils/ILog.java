package com.zyc.softkey.utils;

import android.util.Log;

/**
 * 日志打印管理
 * @author zhouyuchao
 */
public class ILog {
    private static final String LOG_TAG = "SoftKey";

    private static final boolean DEBUG = true;

    public static void e(String tag, String msg) {
        if (DEBUG)
            Log.e(LOG_TAG, "[" + tag + "]:" + msg);
    }

    public static void d(String tag, String msg) {
        if (DEBUG)
            Log.d(LOG_TAG, "[" + tag + "]:" + msg);
    }

    public static void w(String tag, String msg) {
        if (DEBUG)
            Log.w(LOG_TAG, "[" + tag + "]:" + msg);
    }

    public static void i(String tag, String msg) {
        if (DEBUG)
            Log.i(LOG_TAG, "[" + tag + "]:" + msg);
    }
}
