package media.library.player.view;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;

import androidx.annotation.OptIn;
import androidx.media3.common.Format;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.util.UnstableApi;

public class PlayerSupport {
    private static PlayerSupport playerSupport;

    public static PlayerSupport getInstance() {
        if (playerSupport == null) {
            playerSupport = new PlayerSupport();
        }
        return playerSupport;
    }

    // 检测视频格式是否支持
    @OptIn(markerClass = UnstableApi.class)
    public boolean isVideoFormatSupported(Format exoFormat) {
        // 1. 构建 Android 原生 MediaFormat
        MediaFormat mediaFormat = MediaFormat.createVideoFormat(
                exoFormat.sampleMimeType,
                exoFormat.width,
                exoFormat.height
        );
        // 设置编码参数（H.264 必需）
        mediaFormat.setString(MediaFormat.KEY_MIME, MimeTypes.VIDEO_H264);
        mediaFormat.setInteger(MediaFormat.KEY_PROFILE, getProfileFromAvcCode(exoFormat.codecs));
        mediaFormat.setInteger(MediaFormat.KEY_LEVEL, getLevelFromAvcCode(exoFormat.codecs));
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, exoFormat.bitrate);

        // 2. 检测硬件解码器是否支持
        try {
            MediaCodec codec = MediaCodec.createDecoderByType(exoFormat.sampleMimeType);
            codec.configure(mediaFormat, null, null, 0);
            codec.release();
            return true;
        } catch (Exception e) {
            // 硬件解码不支持，检测软件解码（FFmpeg）
            return isFfmpegDecoderSupported(exoFormat.sampleMimeType);
        }
    }

    // 从 avc1.xxxxxx 中解析 Profile
    private int getProfileFromAvcCode(String codecs) {
        if (codecs == null || !codecs.startsWith("avc1.")) {
            return MediaCodecInfo.CodecProfileLevel.AVCProfileBaseline;
        }
        // 解析 avc1.640034 中的 640034（16进制）
        String profileHex = codecs.substring(5, 7);
        return Integer.parseInt(profileHex, 16);
    }

    // 从 avc1.xxxxxx 中解析 Level
    private int getLevelFromAvcCode(String codecs) {
        if (codecs == null || !codecs.startsWith("avc1.")) {
            return MediaCodecInfo.CodecProfileLevel.AVCLevel31;
        }
        String levelHex = codecs.substring(8, 10);
        return Integer.parseInt(levelHex, 16);
    }

    // 检测 FFmpeg 软件解码器是否支持
    private boolean isFfmpegDecoderSupported(String mimeType) {
        //return FfmpegVideoDecoder.isSupported(mimeType);
        return false;
    }
}
