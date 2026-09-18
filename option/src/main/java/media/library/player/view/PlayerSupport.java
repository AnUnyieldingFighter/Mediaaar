package media.library.player.view;

import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.os.Build;
import android.util.Range;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import media.library.player.manager.PlayerLog;

/**
 * 播放器支持能力判断。
 *
 * <p>这里判断的是当前设备 MediaCodec 解码器支持的视频编码类型，
 * 例如 video/avc、video/hevc、video/x-vnd.on2.vp9。
 * 注意：video/mp4 是容器类型，不是解码器支持的视频编码类型。</p>
 */
public class PlayerSupport {

    private static final String TAG = "PlayerSupport";

    private static final int[][] COMMON_RESOLUTIONS = new int[][]{
            {640, 480},
            {1280, 720},
            {1920, 1080},
            {2560, 1440},
            {3840, 2160},
            {7680, 4320}
    };

    private static volatile PlayerSupport playerSupport;

    private List<VideoSupportInfo> supportedVideoSupportInfoList;

    private PlayerSupport() {
    }

    /**
     * 获取播放器支持能力单例。
     */
    public static PlayerSupport getInstance() {
        if (playerSupport == null) {
            synchronized (PlayerSupport.class) {
                if (playerSupport == null) {
                    playerSupport = new PlayerSupport();
                }
            }
        }
        return playerSupport;
    }


    /**
     * 打印当前设备支持的视频编码和分辨率信息。
     */
    public void printSupportedVideoResolutionInfo() {
        List<VideoSupportInfo> infoList = getSupportedVideoResolutionInfo();
        for (VideoSupportInfo info : infoList) {
            PlayerLog.d(TAG, info.toString());
        }
    }

    /**
     * 获取当前设备支持的视频编码分辨率信息。
     */
    public List<VideoSupportInfo> getSupportedVideoResolutionInfo() {
        if (supportedVideoSupportInfoList == null) {
            supportedVideoSupportInfoList = querySupportedVideoResolutionInfo();
        }
        return new ArrayList<>(supportedVideoSupportInfoList);
    }

    /**
     * 判断当前设备是否支持指定视频编码类型。
     */
    public boolean isVideoMimeTypeSupported(@Nullable String mimeType) {
        if (!isVideoMimeType(mimeType)) {
            return false;
        }
        return getSupportedVideoMimeTypes().contains(mimeType.toLowerCase());
    }


    /**
     * 获取当前设备支持解码的视频编码 MIME 类型。
     */
    private List<String> getSupportedVideoMimeTypes() {
        List<String> mimeTypes = new ArrayList<>();
        List<VideoSupportInfo> supportInfoList = getSupportedVideoResolutionInfo();
        for (VideoSupportInfo supportInfo : supportInfoList) {
            mimeTypes.add(supportInfo.supportedVideoMimeType);
        }
        return mimeTypes;
    }

    /**
     * 查询设备所有视频解码器支持的编码类型。
     */
    private List<String> querySupportedVideoMimeTypes() {
        Set<String> mimeTypeSet = new HashSet<>();
        MediaCodecInfo[] codecInfos = getCodecInfos();
        for (MediaCodecInfo codecInfo : codecInfos) {
            if (!isUsableDecoder(codecInfo)) {
                continue;
            }
            String[] supportedTypes = codecInfo.getSupportedTypes();
            for (String type : supportedTypes) {
                if (isVideoMimeType(type)) {
                    mimeTypeSet.add(type.toLowerCase());
                }
            }
        }
        List<String> result = new ArrayList<>(mimeTypeSet);
        Collections.sort(result);
        return result;
    }

    /**
     * 查询当前设备支持的视频编码和分辨率信息。
     */
    private List<VideoSupportInfo> querySupportedVideoResolutionInfo() {
        List<VideoSupportInfo> result = new ArrayList<>();
        List<String> mimeTypes = querySupportedVideoMimeTypes();
        for (String mimeType : mimeTypes) {
            result.add(buildVideoSupportInfo(mimeType));
        }
        return result;
    }

    /**
     * 生成指定视频编码类型的分辨率支持数据。
     */
    private VideoSupportInfo buildVideoSupportInfo(String mimeType) {
        int minWidth = -1;
        int maxWidth = -1;
        int minHeight = -1;
        int maxHeight = -1;
        List<String> supportedCommonResolutions = new ArrayList<>();
        List<String> decoderNames = new ArrayList<>();

        MediaCodecInfo[] codecInfos = getCodecInfos();
        for (MediaCodecInfo codecInfo : codecInfos) {
            if (!isUsableDecoder(codecInfo) || !isCodecSupportType(codecInfo, mimeType)) {
                continue;
            }
            decoderNames.add(codecInfo.getName());

            MediaCodecInfo.VideoCapabilities videoCapabilities =
                    getVideoCapabilities(codecInfo, mimeType);
            if (videoCapabilities == null) {
                continue;
            }

            Range<Integer> widthRange = videoCapabilities.getSupportedWidths();
            Range<Integer> heightRange = videoCapabilities.getSupportedHeights();
            if (widthRange != null) {
                minWidth = minWidth == -1
                        ? widthRange.getLower()
                        : Math.min(minWidth, widthRange.getLower());
                maxWidth = Math.max(maxWidth, widthRange.getUpper());
            }
            if (heightRange != null) {
                minHeight = minHeight == -1
                        ? heightRange.getLower()
                        : Math.min(minHeight, heightRange.getLower());
                maxHeight = Math.max(maxHeight, heightRange.getUpper());
            }

            addSupportedCommonResolutions(videoCapabilities, supportedCommonResolutions);
        }

        return new VideoSupportInfo(
                mimeType,
                minWidth,
                maxWidth,
                minHeight,
                maxHeight,
                supportedCommonResolutions,
                decoderNames
        );
    }

    /**
     * 获取指定解码器的 VideoCapabilities。
     */
    private MediaCodecInfo.VideoCapabilities getVideoCapabilities(
            MediaCodecInfo codecInfo,
            String mimeType) {
        try {
            MediaCodecInfo.CodecCapabilities capabilities =
                    codecInfo.getCapabilitiesForType(mimeType);
            if (capabilities == null) {
                return null;
            }
            return capabilities.getVideoCapabilities();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 添加设备支持的常见分辨率。
     */
    private void addSupportedCommonResolutions(
            MediaCodecInfo.VideoCapabilities videoCapabilities,
            List<String> supportedCommonResolutions) {
        for (int[] resolution : COMMON_RESOLUTIONS) {
            int width = resolution[0];
            int height = resolution[1];
            String supportResolution = width + "x" + height;
            if (supportedCommonResolutions.contains(supportResolution)) {
                continue;
            }
            if (isResolutionSupported(videoCapabilities, width, height)) {
                supportedCommonResolutions.add(supportResolution);
            }
        }
    }

    /**
     * 判断指定分辨率是否支持。
     */
    private boolean isResolutionSupported(
            MediaCodecInfo.VideoCapabilities videoCapabilities,
            int width,
            int height) {
        return videoCapabilities.isSizeSupported(width, height)
                || videoCapabilities.isSizeSupported(height, width);
    }

    /**
     * 判断解码器是否支持指定 MIME。
     */
    private boolean isCodecSupportType(MediaCodecInfo codecInfo, String mimeType) {
        String[] supportedTypes = codecInfo.getSupportedTypes();
        for (String type : supportedTypes) {
            if (mimeType.equalsIgnoreCase(type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否是可用于普通播放的解码器。
     */
    private boolean isUsableDecoder(MediaCodecInfo codecInfo) {
        if (codecInfo == null || codecInfo.isEncoder()) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && codecInfo.isAlias()) {
            return false;
        }
        return true;
    }

    /**
     * 判断是否是视频编码 MIME 类型。
     */
    private boolean isVideoMimeType(@Nullable String mimeType) {
        return mimeType != null && mimeType.toLowerCase().startsWith("video/");
    }

    /**
     * 获取系统普通解码器列表。
     */
    private MediaCodecInfo[] getCodecInfos() {
        MediaCodecList codecList = new MediaCodecList(MediaCodecList.REGULAR_CODECS);
        return codecList.getCodecInfos();
    }
}
