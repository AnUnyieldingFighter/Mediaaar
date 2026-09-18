package media.library.player.view.device;

import android.text.TextUtils;

import com.images.imageselect.BuildConfig;

import androidx.media3.common.PlaybackException;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 播放错误 UI 文案工具。
 *
 * <p>底层错误信息保留在日志中，页面上只展示用户能看懂、能行动的短文案。</p>
 */
public class PlayerErrorMessageUtil {

    private PlayerErrorMessageUtil() {
    }

    /**
     * 根据播放器错误生成页面展示文案。
     */
    public static String getPlayerErrorUiMessage(PlaybackException error) {
        if (error == null) {
            return "";
        }
        String message = error.getMessage();
        if (!BuildConfig.DEBUG) {
            return message;
        }
        switch (error.errorCode) {
            case PlaybackException.ERROR_CODE_DECODER_INIT_FAILED:
                if (!TextUtils.isEmpty(message) && message.contains("NO_EXCEEDS_CAPABILITIES")) {
                    return buildCapabilitiesExceededMessage(message);
                }
                return "当前设备解码失败，暂不支持播放该视频";
            case PlaybackException.ERROR_CODE_IO_CLEARTEXT_NOT_PERMITTED:
                return "视频地址不允许明文 HTTP 访问";
            case PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT:
                return "网络连接超时，请稍后重试";
            case PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS:
                return "视频地址无效或服务器拒绝访问";
            case PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND:
                return "视频文件不存在";
            case PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED:
            case PlaybackException.ERROR_CODE_PARSING_MANIFEST_UNSUPPORTED:
                return "暂不支持该视频格式";
            default:
                return "视频播放失败，请稍后重试";
        }
    }

    /**
     * 设备能力不足时，显示当前视频规格和设备支持规格。
     */
    private static String buildCapabilitiesExceededMessage(String message) {
        VideoErrorFormatInfo formatInfo = parseVideoErrorFormatInfo(message);
        StringBuilder builder = new StringBuilder();
        builder.append("当前设备不支持该视频规格");

        if (formatInfo != null) {
            builder.append("\n视频：")
                    .append(formatInfo.width)
                    .append("x")
                    .append(formatInfo.height);
            if (!TextUtils.isEmpty(formatInfo.sampleMimeType)) {
                builder.append("，编码：").append(formatInfo.sampleMimeType);
            }
            if (!TextUtils.isEmpty(formatInfo.bitrate) && !"-1".equals(formatInfo.bitrate)) {
                builder.append("，码率：").append(formatBitrate(formatInfo.bitrate));
            }

            String deviceSupportText = getDeviceSupportText(formatInfo.sampleMimeType);
            if (!TextUtils.isEmpty(deviceSupportText)) {
                builder.append("\n设备支持：").append(deviceSupportText);
            }
        }

        builder.append("\n请切换低清晰度视频");
        return builder.toString();
    }

    /**
     * 从 ExoPlayer 的错误字符串中解析失败视频轨道信息。
     */
    private static VideoErrorFormatInfo parseVideoErrorFormatInfo(String message) {
        if (TextUtils.isEmpty(message)) {
            return null;
        }
        Pattern pattern = Pattern.compile(
                "Format\\([^,]*,\\s*[^,]*,\\s*([^,]*),\\s*([^,]*),\\s*([^,]*),\\s*([0-9-]+),\\s*[^,]*,\\s*\\[([0-9-]+),\\s*([0-9-]+),\\s*([0-9.\\-]+)");
        Matcher matcher = pattern.matcher(message);
        if (!matcher.find()) {
            return null;
        }
        VideoErrorFormatInfo info = new VideoErrorFormatInfo();
        info.containerMimeType = safeTrim(matcher.group(1));
        info.sampleMimeType = safeTrim(matcher.group(2));
        info.codec = safeTrim(matcher.group(3));
        info.bitrate = safeTrim(matcher.group(4));
        info.width = safeTrim(matcher.group(5));
        info.height = safeTrim(matcher.group(6));
        info.frameRate = safeTrim(matcher.group(7));
        return info;
    }

    /**
     * 获取当前设备对某个视频编码的支持范围。
     */
    private static String getDeviceSupportText(String sampleMimeType) {
        if (TextUtils.isEmpty(sampleMimeType)) {
            return null;
        }
        List<DeviceSupportInfo> supportInfoList =
                PlayerDeviceSupport.getInstance().getSupportedVideoResolutionInfo();
        for (DeviceSupportInfo info : supportInfoList) {
            if (info == null || TextUtils.isEmpty(info.supportedVideoMimeType)) {
                continue;
            }
            if (!sampleMimeType.equalsIgnoreCase(info.supportedVideoMimeType)) {
                continue;
            }
            StringBuilder builder = new StringBuilder();
            builder.append(info.supportedVideoMimeType);
            String maxCommonResolution = getMaxCommonResolution(info.commonResolutions);
            if (!TextUtils.isEmpty(maxCommonResolution)) {
                builder.append(" 常见最高 ").append(maxCommonResolution);
            }
            if (info.maxWidth > 0 && info.maxHeight > 0) {
                builder.append("，能力范围 宽")
                        .append(info.minWidth)
                        .append("-")
                        .append(info.maxWidth)
                        .append(" 高")
                        .append(info.minHeight)
                        .append("-")
                        .append(info.maxHeight);
            }
            return builder.toString();
        }
        return sampleMimeType + " 未查询到设备支持信息";
    }

    /**
     * 从常见分辨率列表中取像素面积最大的一个。
     */
    private static String getMaxCommonResolution(List<String> commonResolutions) {
        if (commonResolutions == null || commonResolutions.isEmpty()) {
            return null;
        }
        String maxResolution = null;
        long maxPixels = -1;
        for (String resolution : commonResolutions) {
            long pixels = getResolutionPixels(resolution);
            if (pixels > maxPixels) {
                maxPixels = pixels;
                maxResolution = resolution;
            }
        }
        return maxResolution;
    }

    private static long getResolutionPixels(String resolution) {
        if (TextUtils.isEmpty(resolution)) {
            return -1;
        }
        String[] parts = resolution.toLowerCase().split("x");
        if (parts.length != 2) {
            return -1;
        }
        try {
            return Long.parseLong(parts[0]) * Long.parseLong(parts[1]);
        } catch (Exception e) {
            return -1;
        }
    }

    private static String formatBitrate(String bitrateText) {
        try {
            long bitrate = Long.parseLong(bitrateText);
            if (bitrate <= 0) {
                return bitrateText + "bps";
            }
            return String.format("%.2fMbps", bitrate / 1024f / 1024f);
        } catch (Exception e) {
            return bitrateText + "bps";
        }
    }

    private static String safeTrim(String value) {
        return value == null ? null : value.trim();
    }

    /**
     * 解码失败时从 Format 中解析出来的视频规格。
     */
    private static class VideoErrorFormatInfo {
        String containerMimeType;
        String sampleMimeType;
        String codec;
        String bitrate;
        String width;
        String height;
        String frameRate;
    }
}
