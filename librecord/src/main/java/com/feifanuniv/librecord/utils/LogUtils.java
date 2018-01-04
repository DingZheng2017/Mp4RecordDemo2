package com.feifanuniv.librecord.utils;

import android.util.Log;

/**
 * Created by dingzheng on 2017/12/20.
 */

public class LogUtils {
    private static final String TAG = "LOGUTIL";
    private static boolean LOG_ENABLE = true;

    /**
     * 设置是否显示Log
     * @param enable true-显示 false-不显示
     */
    public static void setLogEnable(boolean enable) {
        LOG_ENABLE = enable;
    }

    /**
     * verbose log
     * @param msg log msg
     */
    public static void v(String msg) {
        if (LOG_ENABLE) {
            Log.v(TAG, msg);
        }
    }

    /**
     * verbose log
     * @param tag tag
     * @param msg log msg
     */
    public static void v(String tag, String msg) {
        if (LOG_ENABLE) {
            Log.v(tag, msg);
        }
    }

    /**
     * debug log
     * @param msg log msg
     */
    public static void d(String msg) {
        if (LOG_ENABLE) {
            Log.d(TAG, msg);
        }
    }

    /**
     * debug log
     * @param tag tag
     * @param msg log msg
     */
    public static void d(String tag, String msg) {
        if (LOG_ENABLE) {
            Log.d(tag, msg);
        }
    }

    /**
     * info log
     * @param msg log msg
     */
    public static void i(String msg) {
        if (LOG_ENABLE) {
            Log.i(TAG, msg);
        }
    }

    /**
     * info log
     * @param tag tag
     * @param msg log msg
     */
    public static void i(String tag, String msg) {
        if (LOG_ENABLE) {
            Log.i(tag, msg);
        }
    }

    /**
     * warning log
     * @param msg log msg
     */
    public static void w(String msg) {
        if (LOG_ENABLE) {
            Log.w(TAG, msg);
        }
    }

    /**
     * warning log
     * @param msg log msg
     * @param e exception
     */
    public static void w(String msg, Exception e) {
        if (LOG_ENABLE) {
            Log.w(TAG, msg, e);
        }
    }

    /**
     * warning log
     * @param tag tag
     * @param msg log msg
     */
    public static void w(String tag, String msg) {
        if (LOG_ENABLE) {
            Log.w(tag, msg);
        }
    }

    /**
     * warning log
     * @param tag tag
     * @param msg log msg
     * @param e exception
     */
    public static void w(String tag, String msg, Exception e) {
        if (LOG_ENABLE) {
            Log.w(tag, msg, e);
        }
    }

    /**
     * error log
     * @param msg log msg
     */
    public static void e(String msg) {
        if (LOG_ENABLE) {
            Log.e(TAG, msg);
        }
    }

    /**
     * error log
     * @param msg log msg
     * @param e exception
     */
    public static void e(String msg, Exception e) {
        if (LOG_ENABLE) {
            Log.e(TAG, msg, e);
        }
    }

    /**
     * error log
     * @param tag tag
     * @param msg msg
     */
    public static void e(String tag, String msg) {
        if (LOG_ENABLE) {
            Log.e(tag, msg);
        }
    }

    /**
     * error log
     * @param tag tag
     * @param msg log msg
     * @param e exception
     */
    public static void e(String tag, String msg, Exception e) {
        if (LOG_ENABLE) {
            Log.e(tag, msg, e);
        }
    }
}
