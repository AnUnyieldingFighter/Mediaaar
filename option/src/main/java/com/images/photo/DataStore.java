package com.images.photo;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 私有数据：data目录
 */
public class DataStore {
    private final static String NAME = "photos";
    private static SharedPreferences shared;
    public static String PATH_TAKE = "path_take";
    public static String PATH_CROP = "path_crop";

    /**
     * 保存数据
     */
    public static void stringSave(Context context, String name, Object value) {
        if (shared == null) {
            shared = context.getSharedPreferences(NAME, 0);
        }
        shared.edit().putString(name, String.valueOf(value)).commit();
        shared = null;
    }

    /**
     * 读取数据
     */
    public static String stringGet(Context context, String name) {
        if (shared == null) {
            shared = context.getSharedPreferences(NAME, 0);
        }
        return shared.getString(name, "");
    }

    public static void restData(Context context) {
        stringSave(context, DataStore.PATH_TAKE, "");
        stringSave(context, DataStore.PATH_CROP, "");
    }
}
