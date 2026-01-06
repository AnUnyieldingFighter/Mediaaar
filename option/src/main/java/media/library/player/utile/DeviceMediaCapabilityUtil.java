package media.library.player.utile;

import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.nfc.Tag;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import media.library.player.manager.PlayerLog;

/**
 * 设备媒体能力工具类（Media3 配套使用，规避 HEVCLevel 常量缺失问题）
 * 功能：1. 获取设备支持的视频/音频硬解码格式  2. 获取指定格式最大硬解码码率
 */
public class DeviceMediaCapabilityUtil {
    private static final String TAG = "设备支持";

    // ==================== 手动定义 HEVC Level 整型值（替代系统缺失常量） ====================
    // 参考 Android 官方文档，HEVC Level 对应的整型字面量（避免依赖系统常量）
    private static final int HEVC_LEVEL_1 = 1;
    private static final int HEVC_LEVEL_2 = 2;
    private static final int HEVC_LEVEL_3 = 3;
    private static final int HEVC_LEVEL_4 = 4;
    private static final int HEVC_LEVEL_5 = 5;
    private static final int HEVC_LEVEL_5_1 = 6;
    private static final int HEVC_LEVEL_6 = 64;
    private static final int HEVC_LEVEL_6_1 = 65;

    // ==================== 目标1：获取设备支持的媒体格式（视频/音频） ====================

    /**
     * 获取设备支持的所有视频硬解码格式（MIME类型）
     */
    public static List<String> getSupportedVideoDecodeFormats() {
        Set<String> videoFormatSet = new HashSet<>();
        MediaCodecList codecList = getCompatibleMediaCodecList();
        MediaCodecInfo[] codecInfos = codecList.getCodecInfos();

        for (MediaCodecInfo codecInfo : codecInfos) {
            if (codecInfo.isEncoder()) {
                continue; // 跳过编码器，只处理解码器
            }

            String[] supportedTypes = codecInfo.getSupportedTypes();
            for (String mimeType : supportedTypes) {
                if (mimeType != null && mimeType.startsWith("video/")) {
                    videoFormatSet.add(mimeType);
                }
            }
        }
        return new ArrayList<>(videoFormatSet);
    }

    /**
     * 获取设备支持的所有音频硬解码格式（MIME类型）
     */
    public static List<String> getSupportedAudioDecodeFormats() {
        Set<String> audioFormatSet = new HashSet<>();
        MediaCodecList codecList = getCompatibleMediaCodecList();
        MediaCodecInfo[] codecInfos = codecList.getCodecInfos();

        for (MediaCodecInfo codecInfo : codecInfos) {
            if (codecInfo.isEncoder()) {
                continue;
            }

            String[] supportedTypes = codecInfo.getSupportedTypes();
            for (String mimeType : supportedTypes) {
                if (mimeType != null && mimeType.startsWith("audio/")) {
                    audioFormatSet.add(mimeType);
                }
            }
        }
        return new ArrayList<>(audioFormatSet);
    }

    // ==================== 目标2：获取指定格式的最大硬解码码率（无 HEVCLevel 常量依赖） ====================

    /**
     * 获取指定视频格式的最大硬解码码率
     *
     * @param videoMimeType 视频MIME类型（video/avc=H.264、video/hevc=H.265）
     * @return 最大码率（bps，-1表示不支持该格式硬解码）
     */
    public static long getMaxVideoDecodeBitrate(String videoMimeType) {
        if (videoMimeType == null || !videoMimeType.startsWith("video/")) {
            PlayerLog.d(TAG, "无效的视频MIME类型：" + videoMimeType);
            return -1;
        }

        MediaCodecList codecList = getCompatibleMediaCodecList();
        MediaCodecInfo[] codecInfos = codecList.getCodecInfos();
        int maxSupportLevel = -1;

        for (MediaCodecInfo codecInfo : codecInfos) {
            if (codecInfo.isEncoder()) {
                continue;
            }

            // 检查是否支持目标格式
            boolean isSupportTarget = false;
            String[] supportedTypes = codecInfo.getSupportedTypes();
            for (String type : supportedTypes) {
                if (videoMimeType.equalsIgnoreCase(type)) {
                    isSupportTarget = true;
                    break;
                }
            }
            if (!isSupportTarget) {
                continue;
            }

            // 获取解码器能力
            MediaCodecInfo.CodecCapabilities capabilities = null;
            try {
                capabilities = codecInfo.getCapabilitiesForType(videoMimeType);
            } catch (IllegalArgumentException e) {
                PlayerLog.d(TAG, "获取视频解码器能力失败：" + e.getMessage());
                continue;
            }

            // 提取最高Level
            if (capabilities != null && capabilities.profileLevels != null) {
                for (MediaCodecInfo.CodecProfileLevel pl : capabilities.profileLevels) {
                    if (pl.level > maxSupportLevel) {
                        maxSupportLevel = pl.level;
                    }
                }
            }

            if (maxSupportLevel != -1) {
                break;
            }
        }

        if (maxSupportLevel != -1) {
            long maxBitrate = calculateVideoBitrateByLevel(videoMimeType, maxSupportLevel);
            PlayerLog.d(TAG, videoMimeType + " 最高Level：" + maxSupportLevel + "，最大硬解码码率：" + (maxBitrate / 1024 / 1024) + " Mbps");
            return maxBitrate;
        } else {
            PlayerLog.d(TAG, "设备不支持 " + videoMimeType + " 硬解码");
            return -1;
        }
    }

    /**
     * 获取指定音频格式的最大硬解码码率
     *
     * @param audioMimeType 音频MIME类型（audio/aac=AAC、audio/mpeg=MP3）
     * @return 最大码率（bps，-1表示不支持该格式硬解码）
     */
    public static long getMaxAudioDecodeBitrate(String audioMimeType) {
        if (audioMimeType == null || !audioMimeType.startsWith("audio/")) {
            PlayerLog.d(TAG, "无效的音频MIME类型：" + audioMimeType);
            return -1;
        }

        MediaCodecList codecList = getCompatibleMediaCodecList();
        MediaCodecInfo[] codecInfos = codecList.getCodecInfos();

        for (MediaCodecInfo codecInfo : codecInfos) {
            if (codecInfo.isEncoder()) {
                continue;
            }

            // 检查是否支持目标格式
            boolean isSupportTarget = false;
            String[] supportedTypes = codecInfo.getSupportedTypes();
            for (String type : supportedTypes) {
                if (audioMimeType.equalsIgnoreCase(type)) {
                    isSupportTarget = true;
                    break;
                }
            }
            if (!isSupportTarget) {
                continue;
            }

            // 返回音频通用最大码率（经验值，无版本依赖）
            long maxBitrate = getAudioDefaultMaxBitrate(audioMimeType);
            PlayerLog.d(TAG, audioMimeType + " 最大硬解码码率：" + (maxBitrate / 1024) + " Kbps");
            return maxBitrate;
        }

        PlayerLog.d(TAG, "设备不支持 " + audioMimeType + " 硬解码");
        return -1;
    }

    // ==================== 辅助方法：兼容不同版本 MediaCodecList ====================
    private static MediaCodecList getCompatibleMediaCodecList() {
        return new MediaCodecList(MediaCodecList.REGULAR_CODECS);
    }

    // ==================== 辅助方法：计算视频最大码率（无 HEVCLevel 常量依赖） ====================

    /**
     * 根据视频格式和 Level 整型值计算最大码率（使用手动定义的 HEVC Level 常量）
     */
    private static long calculateVideoBitrateByLevel(String mimeType, int level) {
        // H.264 (AVC) 格式（直接使用系统常量，兼容性更好；若缺失可同样用整型字面量）
        if ("video/avc".equalsIgnoreCase(mimeType)) {
            switch (level) {
                case MediaCodecInfo.CodecProfileLevel.AVCLevel1:
                    return 64 * 1024;
                case MediaCodecInfo.CodecProfileLevel.AVCLevel1b:
                    return 128 * 1024;
                case MediaCodecInfo.CodecProfileLevel.AVCLevel11:
                    return 192 * 1024;
                case MediaCodecInfo.CodecProfileLevel.AVCLevel2:
                    return 2 * 1024 * 1024;
                case MediaCodecInfo.CodecProfileLevel.AVCLevel3:
                    return 10 * 1024 * 1024;
                case MediaCodecInfo.CodecProfileLevel.AVCLevel4:
                    return 25 * 1024 * 1024;
                case MediaCodecInfo.CodecProfileLevel.AVCLevel41:
                    return 50 * 1024 * 1024;
                case MediaCodecInfo.CodecProfileLevel.AVCLevel5:
                    return 100 * 1024 * 1024;
                default:
                    return 10 * 1024 * 1024;
            }
        }
        // H.265 (HEVC) 格式（使用手动定义的 Level 整型值，规避系统常量缺失）
        else if ("video/hevc".equalsIgnoreCase(mimeType)) {
            switch (level) {
                case HEVC_LEVEL_1:
                    return 128 * 1024; // 128 Kbps
                case HEVC_LEVEL_2:
                    return 1 * 1024 * 1024; // 1 Mbps
                case HEVC_LEVEL_3:
                    return 4 * 1024 * 1024; // 4 Mbps
                case HEVC_LEVEL_4:
                    return 10 * 1024 * 1024; // 10 Mbps
                case HEVC_LEVEL_5:
                    return 25 * 1024 * 1024; // 25 Mbps
                case HEVC_LEVEL_5_1:
                    return 50 * 1024 * 1024; // 50 Mbps
                case HEVC_LEVEL_6:
                    return 100 * 1024 * 1024; // 100 Mbps
                case HEVC_LEVEL_6_1:
                    return 200 * 1024 * 1024; // 200 Mbps
                default:
                    return 10 * 1024 * 1024; // 默认10 Mbps
            }
        }
        // VP9 格式（兜底）
        else if ("video/x-vnd.on2.vp9".equalsIgnoreCase(mimeType)) {
            return 50 * 1024 * 1024;
        }
        // 其他视频格式
        else {
            return 8 * 1024 * 1024;
        }
    }

    // ==================== 辅助方法：音频默认最大码率（无版本依赖） ====================
    private static long getAudioDefaultMaxBitrate(String mimeType) {
        switch (mimeType.toLowerCase()) {
            case "audio/aac":
            case "audio/mp4a-latm":
                return 320 * 1024; // AAC 320 Kbps
            case "audio/mpeg": // MP3
                return 320 * 1024; // MP3 320 Kbps
            case "audio/opus":
                return 128 * 1024; // Opus 128 Kbps
            case "audio/wav":
                return 1411 * 1024; // WAV 1411 Kbps
            case "audio/flac":
                return 1000 * 1024; // FLAC 1000 Kbps
            default:
                return 256 * 1024; // 默认256 Kbps
        }
    }

    //======================
    public static void test() {
        // 1. 获取设备支持的视频格式
        List<String> videoFormats = DeviceMediaCapabilityUtil.getSupportedVideoDecodeFormats();
        PlayerLog.d(TAG, "支持的视频格式：" + videoFormats);

        // 2. 获取设备支持的音频格式
        List<String> audioFormats = DeviceMediaCapabilityUtil.getSupportedAudioDecodeFormats();
        PlayerLog.d(TAG, "支持的音频格式：" + audioFormats);

        // 3. 获取 H.265（HEVC）最大硬解码码率（无 HEVCLevel 常量依赖）
        long maxH265Bitrate = DeviceMediaCapabilityUtil.getMaxVideoDecodeBitrate("video/hevc");
        if (maxH265Bitrate != -1) {
            float maxMbps = (float) maxH265Bitrate / 1024 / 1024;
            PlayerLog.d(TAG, "H.265 最大码率：" + String.format("%.2f Mbps", maxMbps));
        }

        // 4. 获取 AAC 最大硬解码码率
        long maxAacBitrate = DeviceMediaCapabilityUtil.getMaxAudioDecodeBitrate("audio/aac");
        if (maxAacBitrate != -1) {
            float maxKbps = (float) maxAacBitrate / 1024;
            PlayerLog.d(TAG, "AAC 最大码率：" + String.format("%.2f Kbps", maxKbps));
        }
    }
}