package media.library.player.view.device;

import android.content.Context;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import media.library.player.manager.PlayerLog;

/**
 * 单个视频地址的媒体信息和本设备播放支持能力判断。
 *
 * <p>注意：读取网络视频地址时会发生网络访问，不要在主线程直接调用。</p>
 */
public class PlayerVideoSupport {

    private static final String TAG = "PlayerVideoSupport";

    private static volatile PlayerVideoSupport playerVideoSupport;

    private PlayerVideoSupport() {
    }

    /**
     * 获取单例。
     */
    public static PlayerVideoSupport getInstance() {
        if (playerVideoSupport == null) {
            synchronized (PlayerVideoSupport.class) {
                if (playerVideoSupport == null) {
                    playerVideoSupport = new PlayerVideoSupport();
                }
            }
        }
        return playerVideoSupport;
    }

    /**
     * 获取视频地址对应的媒体信息和设备支持结果。
     *
     * <p>网络地址会阻塞读取，请放到子线程执行。</p>
     */
    public PlayerVideoInfo getVideoInfo(Context context, String videoUrl) {
        return getVideoInfo(context, videoUrl, null);
    }

    /**
     * 获取视频地址对应的媒体信息和设备支持结果。
     *
     * @param requestHeaders 网络视频请求头，没有可传 null
     */
    public PlayerVideoInfo getVideoInfo(
            Context context,
            String videoUrl,
            @Nullable Map<String, String> requestHeaders) {
        PlayerVideoInfo result = new PlayerVideoInfo();
        result.videoUrl = videoUrl;

        if (context == null) {
            result.parseSuccess = false;
            result.errorMessage = "Context 为空";
            refreshPlaySupportResult(result);
            return result;
        }
        if (TextUtils.isEmpty(videoUrl)) {
            result.parseSuccess = false;
            result.errorMessage = "视频地址为空";
            refreshPlaySupportResult(result);
            return result;
        }

        readContainerInfo(context, videoUrl, requestHeaders, result);
        MediaExtractor mediaExtractor = new MediaExtractor();
        try {
            setDataSource(context, mediaExtractor, videoUrl, requestHeaders);
            result.parseSuccess = true;
            result.trackCount = mediaExtractor.getTrackCount();

            for (int i = 0; i < result.trackCount; i++) {
                MediaFormat mediaFormat = mediaExtractor.getTrackFormat(i);
                String mimeType = getStringValue(mediaFormat, MediaFormat.KEY_MIME);
                if (isVideoMimeType(mimeType)) {
                    readVideoTrackInfo(result, mediaFormat, i, mimeType);
                } else if (isAudioMimeType(mimeType)) {
                    readAudioTrackInfo(result, mediaFormat, i, mimeType);
                } else if (isSubtitleMimeType(mimeType)) {
                    readSubtitleTrackInfo(result, mediaFormat, i, mimeType);
                } else {
                    result.otherTrackInfos.add("trackIndex=" + i + ", mimeType=" + mimeType);
                }
            }
        } catch (Exception e) {
            result.parseSuccess = false;
            result.errorMessage = e.getMessage();
        } finally {
            mediaExtractor.release();
        }

        refreshPlaySupportResult(result);
        return result;
    }

    /**
     * 打印视频地址对应的媒体信息和设备支持结果。
     */
    public void printVideoInfo(Context context, String videoUrl) {
        PlayerVideoInfo playerVideoInfo = getVideoInfo(context, videoUrl);
        PlayerLog.d(TAG, playerVideoInfo.toString());
    }

    /**
     * 给 MediaExtractor 设置数据源。
     */
    private void setDataSource(
            Context context,
            MediaExtractor mediaExtractor,
            String videoUrl,
            @Nullable Map<String, String> requestHeaders) throws Exception {
        Uri uri = Uri.parse(videoUrl);
        String scheme = uri.getScheme();
        if (TextUtils.isEmpty(scheme)) {
            mediaExtractor.setDataSource(videoUrl);
            return;
        }
        if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) {
            Map<String, String> headers = requestHeaders == null
                    ? new HashMap<String, String>()
                    : requestHeaders;
            mediaExtractor.setDataSource(videoUrl, headers);
            return;
        }
        mediaExtractor.setDataSource(context, uri, requestHeaders);
    }

    /**
     * 读取容器 MIME 和时长等基础信息。
     */
    private void readContainerInfo(
            Context context,
            String videoUrl,
            @Nullable Map<String, String> requestHeaders,
            PlayerVideoInfo result) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            Uri uri = Uri.parse(videoUrl);
            String scheme = uri.getScheme();
            if (TextUtils.isEmpty(scheme)) {
                retriever.setDataSource(videoUrl);
            } else if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) {
                Map<String, String> headers = requestHeaders == null
                        ? new HashMap<String, String>()
                        : requestHeaders;
                retriever.setDataSource(videoUrl, headers);
            } else {
                retriever.setDataSource(context, uri);
            }
            result.containerMimeType =
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
            result.durationMs = parseLong(
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION),
                    -1
            );
        } catch (Exception e) {
            result.containerMimeType = null;
        } finally {
            try {
                retriever.release();
            } catch (Exception ignore) {
            }
        }
    }

    /**
     * 读取视频轨道信息。
     */
    private void readVideoTrackInfo(
            PlayerVideoInfo result,
            MediaFormat mediaFormat,
            int trackIndex,
            String mimeType) {
        result.hasVideo = true;
        result.videoTrackCount++;

        int width = getIntegerValue(mediaFormat, MediaFormat.KEY_WIDTH, -1);
        int height = getIntegerValue(mediaFormat, MediaFormat.KEY_HEIGHT, -1);
        int rotation = getIntegerValue(mediaFormat, MediaFormat.KEY_ROTATION, 0);
        int frameRate = getIntegerValue(mediaFormat, MediaFormat.KEY_FRAME_RATE, -1);
        int bitrate = getIntegerValue(mediaFormat, MediaFormat.KEY_BIT_RATE, -1);
        long durationMs = getDurationMs(mediaFormat);
        CodecSupportResult codecSupportResult = getDecoderSupportResult(mediaFormat, mimeType);

        if (durationMs > result.durationMs) {
            result.durationMs = durationMs;
        }
        if (result.videoTrackCount == 1) {
            result.videoMimeType = mimeType;
            result.videoWidth = width;
            result.videoHeight = height;
            result.videoRotation = rotation;
            result.videoFrameRate = frameRate;
            result.videoBitrate = bitrate;
            result.videoSupported = codecSupportResult.supported;
            result.videoDecoderName = codecSupportResult.decoderName;
            result.videoSupportMessage = codecSupportResult.message;
        }
        if (codecSupportResult.supported) {
            result.hasSupportedVideoTrack = true;
        }

        result.videoTrackInfos.add(
                "trackIndex=" + trackIndex
                        + ", mimeType=" + mimeType
                        + ", resolution=" + width + "x" + height
                        + ", rotation=" + rotation
                        + ", frameRate=" + frameRate
                        + ", bitrate=" + bitrate
                        + ", durationMs=" + durationMs
                        + ", supported=" + codecSupportResult.supported
                        + ", decoder=" + codecSupportResult.decoderName
                        + ", message=" + codecSupportResult.message
        );
    }

    /**
     * 读取音频轨道信息。
     */
    private void readAudioTrackInfo(
            PlayerVideoInfo result,
            MediaFormat mediaFormat,
            int trackIndex,
            String mimeType) {
        result.hasAudio = true;
        result.audioTrackCount++;

        int channelCount = getIntegerValue(mediaFormat, MediaFormat.KEY_CHANNEL_COUNT, -1);
        int sampleRate = getIntegerValue(mediaFormat, MediaFormat.KEY_SAMPLE_RATE, -1);
        int bitrate = getIntegerValue(mediaFormat, MediaFormat.KEY_BIT_RATE, -1);
        String language = getStringValue(mediaFormat, MediaFormat.KEY_LANGUAGE);
        CodecSupportResult codecSupportResult = getDecoderSupportResult(mediaFormat, mimeType);

        if (result.audioTrackCount == 1) {
            result.audioMimeType = mimeType;
            result.audioChannelCount = channelCount;
            result.audioSampleRate = sampleRate;
            result.audioBitrate = bitrate;
            result.audioLanguage = language;
            result.audioSupported = codecSupportResult.supported;
            result.audioDecoderName = codecSupportResult.decoderName;
            result.audioSupportMessage = codecSupportResult.message;
        }
        if (codecSupportResult.supported) {
            result.hasSupportedAudioTrack = true;
        }

        result.audioTrackInfos.add(
                "trackIndex=" + trackIndex
                        + ", mimeType=" + mimeType
                        + ", channelCount=" + channelCount
                        + ", sampleRate=" + sampleRate
                        + ", bitrate=" + bitrate
                        + ", language=" + language
                        + ", supported=" + codecSupportResult.supported
                        + ", decoder=" + codecSupportResult.decoderName
                        + ", message=" + codecSupportResult.message
        );
    }

    /**
     * 读取字幕轨道信息。
     */
    private void readSubtitleTrackInfo(
            PlayerVideoInfo result,
            MediaFormat mediaFormat,
            int trackIndex,
            String mimeType) {
        result.hasSubtitle = true;
        result.subtitleTrackCount++;
        String language = getStringValue(mediaFormat, MediaFormat.KEY_LANGUAGE);
        result.subtitleTrackInfos.add(
                "trackIndex=" + trackIndex
                        + ", mimeType=" + mimeType
                        + ", language=" + language
        );
    }

    /**
     * 刷新本设备是否支持播放的最终结果。
     */
    private void refreshPlaySupportResult(PlayerVideoInfo result) {
        if (!result.parseSuccess) {
            result.deviceSupportPlay = false;
            result.deviceSupportMessage = "媒体信息读取失败：" + result.errorMessage;
            return;
        }
        if (!result.hasVideo) {
            result.deviceSupportPlay = false;
            result.deviceSupportMessage = "没有读取到视频轨道";
            return;
        }
        if (!result.hasSupportedVideoTrack) {
            result.deviceSupportPlay = false;
            result.deviceSupportMessage = "本设备不支持该视频编码或分辨率：" + result.videoMimeType;
            return;
        }
        if (result.hasAudio && !result.hasSupportedAudioTrack) {
            result.deviceSupportPlay = false;
            result.deviceSupportMessage = "本设备不支持该音频编码：" + result.audioMimeType;
            return;
        }
        result.deviceSupportPlay = true;
        result.deviceSupportMessage = result.hasSubtitle
                ? "视频和音频轨道支持播放；字幕轨道已识别，字幕显示能力需由播放器字幕模块决定"
                : "视频和音频轨道支持播放";
    }

    /**
     * 获取指定轨道格式的解码器支持结果。
     */
    private CodecSupportResult getDecoderSupportResult(MediaFormat mediaFormat, String mimeType) {
        CodecSupportResult result = new CodecSupportResult();
        if (TextUtils.isEmpty(mimeType)) {
            result.supported = false;
            result.message = "mimeType 为空";
            return result;
        }

        MediaCodecInfo[] codecInfos = getCodecInfos();
        for (MediaCodecInfo codecInfo : codecInfos) {
            if (!isUsableDecoder(codecInfo) || !isCodecSupportType(codecInfo, mimeType)) {
                continue;
            }
            try {
                MediaCodecInfo.CodecCapabilities capabilities =
                        codecInfo.getCapabilitiesForType(mimeType);
                if (capabilities != null && capabilities.isFormatSupported(mediaFormat)) {
                    result.supported = true;
                    result.decoderName = codecInfo.getName();
                    result.message = "支持";
                    return result;
                }
            } catch (Exception ignore) {
            }
        }

        result.supported = false;
        result.decoderName = null;
        result.message = "没有找到支持该轨道格式的系统解码器";
        return result;
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
     * 获取系统普通解码器列表。
     */
    private MediaCodecInfo[] getCodecInfos() {
        MediaCodecList codecList = new MediaCodecList(MediaCodecList.REGULAR_CODECS);
        return codecList.getCodecInfos();
    }

    /**
     * 判断是否是视频 MIME。
     */
    private boolean isVideoMimeType(String mimeType) {
        return !TextUtils.isEmpty(mimeType)
                && mimeType.toLowerCase(Locale.US).startsWith("video/");
    }

    /**
     * 判断是否是音频 MIME。
     */
    private boolean isAudioMimeType(String mimeType) {
        return !TextUtils.isEmpty(mimeType)
                && mimeType.toLowerCase(Locale.US).startsWith("audio/");
    }

    /**
     * 判断是否是字幕 MIME。
     */
    private boolean isSubtitleMimeType(String mimeType) {
        if (TextUtils.isEmpty(mimeType)) {
            return false;
        }
        String lowerMimeType = mimeType.toLowerCase(Locale.US);
        return lowerMimeType.startsWith("text/")
                || lowerMimeType.contains("subrip")
                || lowerMimeType.contains("ttml")
                || lowerMimeType.contains("cea-608")
                || lowerMimeType.contains("cea-708")
                || lowerMimeType.contains("webvtt")
                || lowerMimeType.contains("vtt")
                || lowerMimeType.contains("pgs")
                || lowerMimeType.contains("vobsub");
    }

    /**
     * 获取 String 类型字段。
     */
    private String getStringValue(MediaFormat mediaFormat, String key) {
        try {
            if (mediaFormat.containsKey(key)) {
                return mediaFormat.getString(key);
            }
        } catch (Exception ignore) {
        }
        return null;
    }

    /**
     * 获取 int 类型字段。
     */
    private int getIntegerValue(MediaFormat mediaFormat, String key, int defaultValue) {
        try {
            if (mediaFormat.containsKey(key)) {
                return mediaFormat.getInteger(key);
            }
        } catch (Exception ignore) {
        }
        return defaultValue;
    }

    /**
     * 获取轨道时长，单位毫秒。
     */
    private long getDurationMs(MediaFormat mediaFormat) {
        try {
            if (mediaFormat.containsKey(MediaFormat.KEY_DURATION)) {
                return mediaFormat.getLong(MediaFormat.KEY_DURATION) / 1000L;
            }
        } catch (Exception ignore) {
        }
        return -1;
    }

    /**
     * 字符串转 long。
     */
    private long parseLong(String value, long defaultValue) {
        if (TextUtils.isEmpty(value)) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * 解码器支持结果。
     */
    private static class CodecSupportResult {
        private boolean supported;
        private String decoderName;
        private String message;
    }
}
