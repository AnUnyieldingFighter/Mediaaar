package media.library.utils;

import android.Manifest;
import android.content.Context;
import android.health.connect.HealthPermissions;
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

    //========================= 相机与录像 =========================

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

    //========================= 公共媒体读写 =========================

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

    //========================= 图片与视频 =========================

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
     * 获取用户选中照片、视频的访问权限，Android 14 起支持，低版本返回空数组。
     * 自建相册需要重选照片时，可与图片、视频读取权限一起申请。
     * 系统照片选择器授予的是 Uri 访问权限，不需要申请此权限。
     */
    public static String[] getSelectedMediaPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return new String[]{Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED};
        }
        return new String[0];
    }

    /** 获取照片原始位置信息权限，Android 10 起需要，仍需具备照片访问权限。 */
    public static String[] getMediaLocationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return new String[]{Manifest.permission.ACCESS_MEDIA_LOCATION};
        }
        return new String[0];
    }

    //========================= 音频与录音 =========================

    /**
     * 获取录音权限。
     */
    public static String[] getRecordAudioPermissions() {
        return new String[]{Manifest.permission.RECORD_AUDIO};
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

    /** 获取音频设置修改权限，属于普通权限，不需要弹窗申请。 */
    public static String[] getModifyAudioSettingsPermissions() {
        return new String[]{Manifest.permission.MODIFY_AUDIO_SETTINGS};
    }

    //========================= 通知 =========================

    /**
     * 获取通知权限，Android 13 以下无需申请，返回空数组。
     */
    public static String[] getNotificationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String[]{Manifest.permission.POST_NOTIFICATIONS};
        }
        return new String[0];
    }

    //========================= 定位 =========================

    /** 获取前台定位权限，同时包含精确定位和大致定位，用户可以只允许大致定位。 */
    public static String[] getLocationPermissions() {
        return new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };
    }

    /** 获取大致定位权限，适用于不需要精确位置的功能。 */
    public static String[] getCoarseLocationPermissions() {
        return new String[]{Manifest.permission.ACCESS_COARSE_LOCATION};
    }

    /**
     * 获取后台定位权限，Android 10 起需要单独授权。
     * 应先获取前台定位权限，Android 11 起后台定位需通过设置页面授权。
     * Android 9 及以下没有单独的后台定位权限，返回空数组。
     */
    public static String[] getBackgroundLocationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION};
        }
        return new String[0];
    }

    //========================= 蓝牙 =========================

    /**
     * 获取前台扫描蓝牙设备的权限，Android 12 起使用蓝牙扫描权限。
     * Android 11 及以下还需要定位权限，传统蓝牙权限只需在 Manifest 中声明。
     * Android 12 起本方法仅返回扫描权限，适用于不使用扫描结果推导位置的场景，
     * 调用方需在 BLUETOOTH_SCAN 的 Manifest 声明中设置 usesPermissionFlags="neverForLocation"。
     * 未声明该标记或需要推导位置时，还需使用 getLocationPermissions() 获取精确定位权限。
     * neverForLocation 可能导致部分 BLE 信标被过滤。
     * 读取设备名称或已配对设备等操作，还需使用 getBluetoothConnectPermissions() 获取连接权限。
     */
    public static String[] getBluetoothScanPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return new String[]{Manifest.permission.BLUETOOTH_SCAN};
        }
        return new String[]{
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };
    }

    /** 获取连接蓝牙设备的权限，Android 12 以下的传统蓝牙权限只需声明。 */
    public static String[] getBluetoothConnectPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return new String[]{Manifest.permission.BLUETOOTH_CONNECT};
        }
        return new String[]{Manifest.permission.BLUETOOTH};
    }

    /** 获取蓝牙广播权限，Android 12 以下的传统蓝牙权限只需声明。 */
    public static String[] getBluetoothAdvertisePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return new String[]{Manifest.permission.BLUETOOTH_ADVERTISE};
        }
        return new String[]{
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
        };
    }

    //========================= Wi-Fi =========================

    /**
     * 获取附近 Wi-Fi 设备权限，Android 13 以下使用定位权限。
     * 获取 Wi-Fi 扫描结果等接口仍需要定位权限，应另用 getLocationPermissions()。
     */
    public static String[] getNearbyWifiPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String[]{Manifest.permission.NEARBY_WIFI_DEVICES};
        }
        return new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };
    }

    /** 获取 Wi-Fi 状态读取权限，属于普通权限，不需要弹窗申请。 */
    public static String[] getWifiStatePermissions() {
        return new String[]{Manifest.permission.ACCESS_WIFI_STATE};
    }

    /** 获取 Wi-Fi 状态修改权限，普通权限；授权不代表新系统允许直接开关 Wi-Fi。 */
    public static String[] getChangeWifiStatePermissions() {
        return new String[]{Manifest.permission.CHANGE_WIFI_STATE};
    }

    //========================= 联系人 =========================

    /** 获取读取联系人权限。 */
    public static String[] getReadContactsPermissions() {
        return new String[]{Manifest.permission.READ_CONTACTS};
    }

    /** 获取写入联系人权限。 */
    public static String[] getWriteContactsPermissions() {
        return new String[]{Manifest.permission.WRITE_CONTACTS};
    }

    //========================= 日历 =========================

    /** 获取读取日历权限。 */
    public static String[] getReadCalendarPermissions() {
        return new String[]{Manifest.permission.READ_CALENDAR};
    }

    /** 获取写入日历权限。 */
    public static String[] getWriteCalendarPermissions() {
        return new String[]{Manifest.permission.WRITE_CALENDAR};
    }

    //========================= 电话与通话记录 =========================

    /** 获取直接拨打电话的权限，仅打开系统拨号页面不需要此权限。 */
    public static String[] getCallPhonePermissions() {
        return new String[]{Manifest.permission.CALL_PHONE};
    }

    /** 获取读取电话状态的权限，不包含读取电话号码或设备唯一标识。 */
    public static String[] getReadPhoneStatePermissions() {
        return new String[]{Manifest.permission.READ_PHONE_STATE};
    }

    /** 获取读取本机电话号码的权限，Android 8 起支持，授权后也不保证能读到号码。 */
    public static String[] getReadPhoneNumbersPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return new String[]{Manifest.permission.READ_PHONE_NUMBERS};
        }
        return new String[]{Manifest.permission.READ_PHONE_STATE};
    }

    /** 获取接听电话权限，Android 8 起支持，低版本返回空数组。 */
    public static String[] getAnswerPhoneCallsPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return new String[]{Manifest.permission.ANSWER_PHONE_CALLS};
        }
        return new String[0];
    }

    /** 获取读取通话记录权限，此权限受系统限制，能否授权取决于应用资格。 */
    public static String[] getReadCallLogPermissions() {
        return new String[]{Manifest.permission.READ_CALL_LOG};
    }

    /** 获取写入通话记录权限，此权限受系统限制，能否授权取决于应用资格。 */
    public static String[] getWriteCallLogPermissions() {
        return new String[]{Manifest.permission.WRITE_CALL_LOG};
    }

    //========================= 短信与彩信 =========================

    /** 获取读取短信权限，此权限受系统限制，能否授权取决于应用资格。 */
    public static String[] getReadSmsPermissions() {
        return new String[]{Manifest.permission.READ_SMS};
    }

    /** 获取直接发送短信权限，此权限受系统限制，能否授权取决于应用资格。 */
    public static String[] getSendSmsPermissions() {
        return new String[]{Manifest.permission.SEND_SMS};
    }

    /** 获取接收短信广播权限，此权限受系统限制，能否授权取决于应用资格。 */
    public static String[] getReceiveSmsPermissions() {
        return new String[]{Manifest.permission.RECEIVE_SMS};
    }

    /** 获取接收彩信相关广播权限，此权限受系统限制，能否授权取决于应用资格。 */
    public static String[] getReceiveMmsPermissions() {
        return new String[]{Manifest.permission.RECEIVE_MMS};
    }

    /** 获取接收 WAP 推送广播权限，此权限受系统限制，能否授权取决于应用资格。 */
    public static String[] getReceiveWapPushPermissions() {
        return new String[]{Manifest.permission.RECEIVE_WAP_PUSH};
    }

    //========================= 运动与身体传感器 =========================

    /** 获取步数、活动识别权限，Android 10 起需要，低版本返回空数组。 */
    public static String[] getActivityRecognitionPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return new String[]{Manifest.permission.ACTIVITY_RECOGNITION};
        }
        return new String[0];
    }

    /**
     * 获取身体传感器读取权限。
     * 设备为 Android 16 及以上且接入应用 targetSdk 为 36 及以上时，返回心率、血氧、皮肤温度权限。
     * 其余情况返回传统身体传感器权限。
     *
     * @param context 接入应用的上下文，用于读取实际的 targetSdk，不检查或申请权限
     */
    public static String[] getBodySensorsPermissions(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA
                && context.getApplicationInfo().targetSdkVersion >= Build.VERSION_CODES.BAKLAVA) {
            return new String[]{
                    HealthPermissions.READ_HEART_RATE,
                    HealthPermissions.READ_OXYGEN_SATURATION,
                    HealthPermissions.READ_SKIN_TEMPERATURE
            };
        }
        return new String[]{Manifest.permission.BODY_SENSORS};
    }

    /**
     * 获取身体传感器后台读取权限，应先获取对应的前台权限，再单独申请后台权限。
     * 设备为 Android 16 及以上且接入应用 targetSdk 为 36 及以上时，使用后台健康数据读取权限。
     * 其余 Android 13 及以上设备使用后台身体传感器权限，该权限受系统限制。
     * Android 12 及以下没有单独的后台身体传感器权限，返回空数组。
     *
     * @param context 接入应用的上下文，用于读取实际的 targetSdk，不检查或申请权限
     */
    public static String[] getBackgroundBodySensorsPermissions(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA
                && context.getApplicationInfo().targetSdkVersion >= Build.VERSION_CODES.BAKLAVA) {
            return new String[]{HealthPermissions.READ_HEALTH_DATA_IN_BACKGROUND};
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String[]{Manifest.permission.BODY_SENSORS_BACKGROUND};
        }
        return new String[0];
    }

    //========================= 网络：普通权限 =========================

    /** 获取网络访问权限，属于普通权限，不需要弹窗申请。 */
    public static String[] getInternetPermissions() {
        return new String[]{Manifest.permission.INTERNET};
    }

    /** 获取网络状态读取权限，属于普通权限，不需要弹窗申请。 */
    public static String[] getNetworkStatePermissions() {
        return new String[]{Manifest.permission.ACCESS_NETWORK_STATE};
    }

    //========================= 设备功能：普通权限 =========================

    /** 获取振动权限，属于普通权限，不需要弹窗申请。 */
    public static String[] getVibratePermissions() {
        return new String[]{Manifest.permission.VIBRATE};
    }

    /** 获取唤醒锁权限，用于保持 CPU 运行，属于普通权限。 */
    public static String[] getWakeLockPermissions() {
        return new String[]{Manifest.permission.WAKE_LOCK};
    }

    /** 获取接收开机广播权限，属于普通权限。 */
    public static String[] getBootCompletedPermissions() {
        return new String[]{Manifest.permission.RECEIVE_BOOT_COMPLETED};
    }

    /** 获取 NFC 权限，属于普通权限，设备仍需具有 NFC 硬件。 */
    public static String[] getNfcPermissions() {
        return new String[]{Manifest.permission.NFC};
    }

    /** 获取生物识别权限，Android 9 起使用生物识别权限，旧版本使用指纹权限。 */
    public static String[] getBiometricPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return new String[]{Manifest.permission.USE_BIOMETRIC};
        }
        return new String[]{Manifest.permission.USE_FINGERPRINT};
    }

    //========================= 前台服务：普通权限 =========================
    //以下类型方法包含基础前台服务权限；仍需声明对应的 service 类型。
    //相机、麦克风、定位等服务还需要各自的运行时权限，屏幕录制需要系统录屏授权。

    /** 获取基础前台服务权限，Android 9 起需要，低版本返回空数组。 */
    public static String[] getForegroundServicePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return new String[]{Manifest.permission.FOREGROUND_SERVICE};
        }
        return new String[0];
    }

    /** 获取相机前台服务权限，Android 14 起增加相机服务类型权限。 */
    public static String[] getCameraForegroundServicePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return new String[]{Manifest.permission.FOREGROUND_SERVICE,
                    Manifest.permission.FOREGROUND_SERVICE_CAMERA};
        }
        return getForegroundServicePermissions();
    }

    /** 获取麦克风前台服务权限，Android 14 起增加麦克风服务类型权限。 */
    public static String[] getMicrophoneForegroundServicePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return new String[]{Manifest.permission.FOREGROUND_SERVICE,
                    Manifest.permission.FOREGROUND_SERVICE_MICROPHONE};
        }
        return getForegroundServicePermissions();
    }

    /** 获取媒体播放前台服务权限，Android 14 起增加媒体播放服务类型权限。 */
    public static String[] getMediaPlaybackForegroundServicePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return new String[]{Manifest.permission.FOREGROUND_SERVICE,
                    Manifest.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK};
        }
        return getForegroundServicePermissions();
    }

    /** 获取定位前台服务权限，Android 14 起增加定位服务类型权限。 */
    public static String[] getLocationForegroundServicePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return new String[]{Manifest.permission.FOREGROUND_SERVICE,
                    Manifest.permission.FOREGROUND_SERVICE_LOCATION};
        }
        return getForegroundServicePermissions();
    }

    /** 获取数据同步前台服务权限，Android 14 起增加数据同步服务类型权限。 */
    public static String[] getDataSyncForegroundServicePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return new String[]{Manifest.permission.FOREGROUND_SERVICE,
                    Manifest.permission.FOREGROUND_SERVICE_DATA_SYNC};
        }
        return getForegroundServicePermissions();
    }

    /** 获取连接外部设备前台服务权限，Android 14 起增加连接设备服务类型权限。 */
    public static String[] getConnectedDeviceForegroundServicePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return new String[]{Manifest.permission.FOREGROUND_SERVICE,
                    Manifest.permission.FOREGROUND_SERVICE_CONNECTED_DEVICE};
        }
        return getForegroundServicePermissions();
    }

    /** 获取健康前台服务权限，Android 14 起增加健康服务类型权限。 */
    public static String[] getHealthForegroundServicePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return new String[]{Manifest.permission.FOREGROUND_SERVICE,
                    Manifest.permission.FOREGROUND_SERVICE_HEALTH};
        }
        return getForegroundServicePermissions();
    }

    /** 获取屏幕录制前台服务权限，Android 14 起增加媒体投影服务类型权限。 */
    public static String[] getMediaProjectionForegroundServicePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return new String[]{Manifest.permission.FOREGROUND_SERVICE,
                    Manifest.permission.FOREGROUND_SERVICE_MEDIA_PROJECTION};
        }
        return getForegroundServicePermissions();
    }

    /** 获取通话前台服务权限，Android 14 起增加通话服务类型权限，仍需符合通话服务资格。 */
    public static String[] getPhoneCallForegroundServicePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return new String[]{Manifest.permission.FOREGROUND_SERVICE,
                    Manifest.permission.FOREGROUND_SERVICE_PHONE_CALL};
        }
        return getForegroundServicePermissions();
    }

    /** 获取跨设备消息传输前台服务权限，Android 14 起增加远程消息服务类型权限。 */
    public static String[] getRemoteMessagingForegroundServicePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return new String[]{Manifest.permission.FOREGROUND_SERVICE,
                    Manifest.permission.FOREGROUND_SERVICE_REMOTE_MESSAGING};
        }
        return getForegroundServicePermissions();
    }

    /** 获取特殊用途前台服务权限，Android 14 起需额外声明具体的特殊用途。 */
    public static String[] getSpecialUseForegroundServicePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return new String[]{Manifest.permission.FOREGROUND_SERVICE,
                    Manifest.permission.FOREGROUND_SERVICE_SPECIAL_USE};
        }
        return getForegroundServicePermissions();
    }

    /** 获取系统豁免前台服务权限，Android 14 起支持，应用仍需符合系统规定的豁免资格。 */
    public static String[] getSystemExemptedForegroundServicePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return new String[]{Manifest.permission.FOREGROUND_SERVICE,
                    Manifest.permission.FOREGROUND_SERVICE_SYSTEM_EXEMPTED};
        }
        return getForegroundServicePermissions();
    }

    /** 获取媒体处理前台服务权限，Android 15 起增加媒体处理服务类型权限。 */
    public static String[] getMediaProcessingForegroundServicePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            return new String[]{Manifest.permission.FOREGROUND_SERVICE,
                    Manifest.permission.FOREGROUND_SERVICE_MEDIA_PROCESSING};
        }
        return getForegroundServicePermissions();
    }

    //========================= 特殊权限：通过对应设置页面授权 =========================

    /** 获取悬浮窗权限，需通过悬浮窗设置页面授权，不能使用普通权限申请接口。 */
    public static String[] getOverlayPermissions() {
        return new String[]{Manifest.permission.SYSTEM_ALERT_WINDOW};
    }

    /**
     * 获取所有文件访问权限，Android 11 起需通过所有文件访问设置页面授权。
     * 此权限不能使用普通权限申请接口，也不代表可以访问其他应用的私有目录。
     * Android 10 及以下没有该权限，返回空数组。
     */
    public static String[] getManageExternalStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return new String[]{Manifest.permission.MANAGE_EXTERNAL_STORAGE};
        }
        return new String[0];
    }

    /** 获取系统设置修改权限，需通过修改系统设置页面授权，不能作为普通权限申请。 */
    public static String[] getWriteSettingsPermissions() {
        return new String[]{Manifest.permission.WRITE_SETTINGS};
    }

    /** 获取安装未知来源应用权限，Android 8 起需通过对应设置页面授权。 */
    public static String[] getInstallPackagesPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return new String[]{Manifest.permission.REQUEST_INSTALL_PACKAGES};
        }
        return new String[0];
    }

    /** 获取应用使用情况访问权限，需通过使用情况访问设置页面授权。 */
    public static String[] getUsageStatsPermissions() {
        return new String[]{Manifest.permission.PACKAGE_USAGE_STATS};
    }

    /** 获取精确闹钟权限，Android 12 起需通过闹钟和提醒设置页面授权，低版本返回空数组。 */
    public static String[] getExactAlarmPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return new String[]{Manifest.permission.SCHEDULE_EXACT_ALARM};
        }
        return new String[0];
    }

    /** 获取请求忽略电池优化的权限，Android 6 起通过系统确认页面授权，低版本返回空数组。 */
    public static String[] getIgnoreBatteryOptimizationsPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return new String[]{Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS};
        }
        return new String[0];
    }
}
