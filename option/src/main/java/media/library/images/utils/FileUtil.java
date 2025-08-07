package media.library.images.utils;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import media.library.images.unmix.ImageLog;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.os.Environment.getExternalStorageDirectory;

/**
 * Created by guom on 2016/10/17.
 */
public class FileUtil {
    private final static String PATTERN = "yyyyMMddHHmmss";
    private static String TAG = "FileUtil";

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
        String filePath = getFileCacheInside(context);
        File file = new File(filePath, getImageName(".png"));
        ImageLog.d(TAG, "照片裁剪存储path:" + file.getAbsolutePath());
        return file;
    }
    /**
     * @param context
     * @return 拍照储存的地址
     */
    public static File createImgFile(Context context, String name) {
        String filePath = getFileExternal();
        if (TextUtils.isEmpty(filePath)) {
            filePath = getFileCacheInside(context);
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

    private static String fileCacheRoot;

    //获取内部缓存文件地址
    public static String getFileCacheInside(Context context) {
        if (!TextUtils.isEmpty(fileCacheRoot)) {
            return fileCacheRoot;
        }
        File file = new File(context.getFilesDir(), "_media");
        if (!file.exists()) {
            file.mkdir();
        }
        fileCacheRoot = file.getPath();
        return fileCacheRoot;
    }

    private static String fileCacheRoot2;

    //获取外部缓存文件地址
    public static String getFileCacheExternal(Context context) {
        if (!TextUtils.isEmpty(fileCacheRoot2)) {
            return fileCacheRoot2;
        }
        File externalCacheDir = context.getExternalCacheDir();
        File file = new File(externalCacheDir, "_media");
        if (!file.exists()) {
            file.mkdir();
        }
        fileCacheRoot2 = file.getPath();
        return fileCacheRoot2;
    }
    //获取外部储存地址


    //获取外部缓存文件地址
    private static String fileRoot;

    public static String getFileExternal() {
        if (!existSDCard()) {
            return "";
        }
        if (!TextUtils.isEmpty(fileRoot)) {
            return fileRoot;
        }
        File file = getExternalStorageDirectory();
        File dir = new File(file, "_media");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        fileRoot = file.getPath();
        return fileRoot;
    }

    private static boolean existSDCard() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }
}
