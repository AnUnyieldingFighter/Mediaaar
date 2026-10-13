package media.library.utils;

import android.Manifest;
import android.os.Build;

/**
 * 按 Android 版本返回常用权限列表。
 *
 * <p>这里只返回权限，不负责检查和申请权限。</p>
 * Build.VERSION.SDK_INT 是当前运行设备的 Android API 等级，
 * 不是应用版本，也不是项目的 compileSdk 或 targetSdk
 */
public class PermissionManager {

    private PermissionManager() {
    }

    /**
     * 获取相机权限，用于相机预览、拍照和无声录像。
     */
    public static String[] getCameraPermissions() {
        return new String[]{Manifest.permission.CAMERA};
    }

    /**
     * 获取带声音的录像权限，需要相机和录音权限。
     */
    public static String[] getRecordVideoPermissions() {
        return new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
        };
    }

    /**
     * 获取录音权限。
     */
    public static String[] getRecordAudioPermissions() {
        return new String[]{Manifest.permission.RECORD_AUDIO};
    }

    /**
     * 获取公共媒体的读写权限，不代表可以读写任意文件。
     * Android 13  (Build.VERSION_CODES.TIRAMISU) 起按图片、视频、音频分别申请读取权限。
     * Android 10 (Build.VERSION_CODES.Q) 起通过 MediaStore 写入自己创建的媒体不需要写权限。
     * Android 9 及以下需要传统的读写权限。
     */
    public static String[] getReadWritePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO
            };
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
        }
        return new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
    }

    /**
     * 获取读取其他应用图片的权限，Android 13(Build.VERSION_CODES.TIRAMISU)起使用图片专用权限。
     */
    public static String[] getReadImagePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String[]{Manifest.permission.READ_MEDIA_IMAGES};
        }
        return new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
    }

    /**
     * 获取读取其他应用视频的权限，Android 13 起使用视频专用权限。
     */
    public static String[] getReadVideoPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String[]{Manifest.permission.READ_MEDIA_VIDEO};
        }
        return new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
    }

    /**
     * 获取读取其他应用音频的权限，Android 13 起使用音频专用权限。
     */
    public static String[] getReadAudioPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String[]{Manifest.permission.READ_MEDIA_AUDIO};
        }
        return new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
    }

    /**
     * 获取通知权限，Android 13 以下无需申请，返回空数组。
     */
    public static String[] getNotificationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String[]{Manifest.permission.POST_NOTIFICATIONS};
        }
        return new String[0];
    }
}
