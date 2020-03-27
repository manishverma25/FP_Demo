package airtel.com.kycfingerprint.utility;

import android.util.Log;

import airtel.com.kycfingerprint.BuildConfig;

/**
 * Created by A1P5KF3Z on 7/20/16.
 */
public class ADLogger {

    private static boolean isLogEnabled = BuildConfig.DEBUG;


    public static void d(String data) {
        if (isLogEnabled) d("KYC_TANZANIA", data);
    }

    public static void d(String key, String data) {
        if (isLogEnabled) Log.d(key, data);
    }

    public static void i(String tag, String message) {
        if (isLogEnabled) Log.i(tag, message);
    }

    public static void v(String tag, String message) {
        if (isLogEnabled) Log.v(tag, message);
    }

    public static void w(String tag, String message) {
        if (isLogEnabled) Log.w(tag, message);
    }

    public static void e(String tag, String message) {
        if (isLogEnabled) Log.e(tag, message);
    }

    public static void e(String message) {
        if (isLogEnabled) Log.e("KYC_TZ", message);
    }

    public static void e(String tag, String message, Throwable e) {
        if (isLogEnabled) Log.e(tag, message, e);
    }
}
