package com.images.unmix;

import android.util.Log;

/**
 * Created by Administrator on 2017/2/28.
 */

public class ImageLog {
    private static boolean isDebug = true;

    public static void d(String tag, Object obj) {
        if (!isDebug) {
            return;
        }
        Log.d("ImageLog===>" + tag, String.valueOf(obj));
    }

    public static void d(Object obj) {
        if (!isDebug) {
            return;
        }
        Log.e("ImageLog===>", String.valueOf(obj));
    }

    public static void setIsDebug(boolean debug) {
        isDebug = debug;
    }
}
