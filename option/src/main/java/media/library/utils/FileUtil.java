package media.library.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.text.TextUtils;

import media.library.images.unmix.ImageLog;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.os.Environment.getExternalStorageDirectory;

/**
 * Environment.getDataDirectory():      /data
 * Context.getCacheDir():               /data/data/com.learn.test/cache
 * Context.getFilesDir():               /data/data/com.learn.test/files
 * <p>
 * Context.getExternalFilesDir()：SDCard/Android/data/你的应用的包名/files/
 * Context.getExternalCacheDir()：SDCard/Android/data/你的应用包名/cache/
 * <p>
 * Created by guom on 2016/10/17.
 */
public class FileUtil {
    private final static String PATTERN = "yyyyMMddHHmmss";
    private static String TAG = "FileUtil";
    private static String imgDir = "_media";
    private static String videoDir = "_video";

    /**
     * @param context
     * @return 拍照储存的地址
     */
    public static File createPhotoFile(Context context) {
        return createImgFile(context, ".png");
    }

    /**
     * @param context
     * @return 裁剪图片的file
     */
    public static File createCropFile(Context context) {
        String filePath = getFileCacheInside(context, imgDir);
        File file = new File(filePath, getImageName(".png"));
        ImageLog.d(TAG, "照片裁剪存储path:" + file.getAbsolutePath());
        return file;
    }

    /**
     * @param context
     * @return 拍照储存的地址
     */
    public static File createImgFile(Context context, String name) {
        String filePath = getFileExternal(imgDir);
        if (TextUtils.isEmpty(filePath)) {
            filePath = getFileCacheInside(context, imgDir);
        }
        File file = new File(filePath, getImageName(name));
        ImageLog.d(TAG, "照片存储path:" + file.getAbsolutePath());
        return file;
    }

    //裁剪获取 File
    public static String getImageName(String name) {
        String PATTERN = "yyyyMMddHHmmss";
        return new SimpleDateFormat(PATTERN, Locale.CHINA).format(new Date()) + name;
    }


    /**
     * 获取视频缓存目录
     *
     * @param context
     * @return
     */
    public static String getVideoCacheDir(Context context) {
        String filePath = getFileCacheExternal(context, videoDir);
        if (TextUtils.isEmpty(filePath)) {
            filePath = getFileCacheInside(context, videoDir);
        }
        return filePath;
    }

//==================================================================

    private static String fileCacheRoot;
    private static String tempName1;

    //获取内部缓存文件目录
    public static String getFileCacheInside(Context context, String dirName) {
        if (!dirName.equals(tempName1)) {
            fileCacheRoot = "";
        }
        if (!TextUtils.isEmpty(fileCacheRoot)) {
            return fileCacheRoot;
        }
        File file = new File(context.getFilesDir(), dirName);
        if (!file.exists()) {
            file.mkdir();
        }
        tempName1 = dirName;
        fileCacheRoot = file.getPath();
        return fileCacheRoot;
    }

    private static String fileCacheRoot2;
    private static String tempName2;

    //获取外部缓存文件目录
    public static String getFileCacheExternal(Context context, String dirName) {
        if (!dirName.equals(tempName2)) {
            fileCacheRoot2 = "";
        }
        if (!TextUtils.isEmpty(fileCacheRoot2)) {
            return fileCacheRoot2;
        }
        File externalCacheDir = context.getExternalCacheDir();
        File file = new File(externalCacheDir, dirName);
        if (!file.exists()) {
            file.mkdir();
        }
        tempName2 = dirName;
        fileCacheRoot2 = file.getPath();
        return fileCacheRoot2;
    }

    //获取外部文件目录
    private static String fileRoot;
    private static String tempName3;

    public static String getFileExternal(String dirName) {
        if (!dirName.equals(tempName3)) {
            fileRoot = "";
        }
        if (!existSDCard()) {
            return "";
        }
        if (!TextUtils.isEmpty(fileRoot)) {
            return fileRoot;
        }
        File file = getExternalStorageDirectory();
        File dir = new File(file, dirName);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        tempName3 = dirName;
        fileRoot = file.getPath();
        return fileRoot;
    }

    private static boolean existSDCard() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }


    //=============================
    private final static String NAME = "video_data";
    private static SharedPreferences shared;
    public final static String video_locked_speed = "video_data";


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

    public static void floatSave(Context context, String name, float value) {
        if (shared == null) {
            shared = context.getSharedPreferences(NAME, 0);
        }
        shared.edit().putFloat(name, value).commit();
        shared = null;
    }

    /**
     * 读取数据
     */
    public static float floatGet(Context context, String name) {
        if (shared == null) {
            shared = context.getSharedPreferences(NAME, 0);
        }
        return shared.getFloat(name, -1f);
    }
}
