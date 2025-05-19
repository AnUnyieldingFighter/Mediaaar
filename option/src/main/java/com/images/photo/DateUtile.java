package com.images.photo;

import android.text.TextUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * TimeUtils
 * Created by Yancy on 2015/12/2.
 */
public class DateUtile {
    private final static String PATTERN = "yyyy-MM-dd";

    public static String timeFormat(long timeMillis, String pattern) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, Locale.CHINA);
        return simpleDateFormat.format(new Date(timeMillis));
    }


    public static String formatPhotoDate(long time) {
        return timeFormat(time, PATTERN);
    }

    public static String formatPhotoDate(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return "";
        }
        File file = new File(filePath);
        if (file.exists()) {
            long time = file.lastModified();
            return formatPhotoDate(time);
        }
        return "";
    }

}