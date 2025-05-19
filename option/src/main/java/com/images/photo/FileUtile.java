package com.images.photo;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.images.unmix.ImageLog;

import java.io.File;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.os.Environment.getExternalStorageDirectory;

/**
 * Created by guom on 2016/10/17.
 */
public class FileUtile {
    private final static String PATTERN = "yyyyMMddHHmmss";
    private static String TAG = "FileUtile";

    //拍照获取 File
    public static File createPhotoFile(Context context, String filePath, String fileAbsolutePath) {
        File file;
        String timeStamp = new SimpleDateFormat(PATTERN, Locale.CHINA).format(new Date());

        if (FileUtile.existSDCard()) {
            String storage = Environment.getExternalStorageDirectory().getAbsolutePath();
            String fileRootPath = TextUtils.isEmpty(fileAbsolutePath) ? storage : fileAbsolutePath;
            File dir = new File(fileRootPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            file = new File(dir, timeStamp + ".png");
        } else {
            File cacheDir = context.getCacheDir();
            file = new File(cacheDir, timeStamp + ".png");
        }
        ImageLog.e(TAG, "照片存储path:" + file.getAbsolutePath());
        return file;
    }

    public static File createCropFile(Context context, String filePath) {
        File file;
        if (FileUtile.existSDCard()) {
            file = new File(getExternalStorageDirectory() + filePath,
                    FileUtile.getImageName());
        } else {
            file = new File(context.getCacheDir(), FileUtile.getImageName());
        }
        ImageLog.e(TAG, "照片裁剪存储path:" + file.getAbsolutePath());
        return file;
    }

    //
    public static int getStatusBarHeight() {
        Class<?> c;
        Object obj;
        Field field;
        int x = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return x;
    }

    /**
     * exist SDCard
     */
    public static boolean existSDCard() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    //裁剪获取 File
    public static String getImageName() {
        String PATTERN = "yyyyMMddHHmmss";
        return new SimpleDateFormat(PATTERN, Locale.CHINA).format(new Date()) + ".png";
    }

}
