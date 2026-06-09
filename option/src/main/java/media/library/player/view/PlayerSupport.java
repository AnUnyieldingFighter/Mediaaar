package media.library.player.view;

import android.app.Activity;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;

import androidx.annotation.OptIn;
import androidx.media3.common.C;
import androidx.media3.common.Format;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.Player;
import androidx.media3.common.Tracks;
import androidx.media3.common.util.Log;
import androidx.media3.common.util.UnstableApi;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media3.exoplayer.ExoPlayer;

public class PlayerSupport {
    // 静态单例实例
    private static volatile PlayerSupport playerSupport;

    // 私有构造，禁止外部创建
    private PlayerSupport() {
    }

    /**
     * 双检锁单例，解决多线程并发安全问题
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

    @OptIn(markerClass = UnstableApi.class)
    public void test(Activity act, String videouUrl) {
        ExoPlayer exoPlayer = new ExoPlayer.Builder(act).build();
        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onTracksChanged(Tracks tracks) {
                Log.d("exoFormat", "onTracksChanged 开始");
                for (Tracks.Group group : tracks.getGroups()) {
                    if (group.getType() == C.TRACK_TYPE_VIDEO) {
                        for (int i = 0; i < group.length; i++) {
                            Format exoFormat = group.getTrackFormat(i);
                            boolean ok = PlayerSupport.getInstance().isVideoFormatSupported(exoFormat);
                            Log.d("exoFormat", "支持？" + ok + " " + exoFormat);
                        }
                    }
                }
                exoPlayer.release();
                Log.d("exoFormat", "onTracksChanged 结束");
            }
        });
        // 4. 设置播放地址
        exoPlayer.setMediaItem(MediaItem.fromUri(videouUrl));
        exoPlayer.prepare();
    }

    /**
     * 检测视频格式是否被设备支持（硬件 → FFmpeg软件）
     */
    @OptIn(markerClass = UnstableApi.class)
    public boolean isVideoFormatSupported(@NonNull Format exoFormat) {
        String mimeType = exoFormat.sampleMimeType;
        // 空值/非视频格式直接返回不支持
        if (mimeType == null || !MimeTypes.isVideo(mimeType)) {
            return false;
        }

        // 只处理 H.264 逻辑，其他格式直接走硬件检测
        if (!MimeTypes.VIDEO_H264.equals(mimeType)) {
            return checkHardwareDecoder(mimeType, exoFormat);
        }

        // ========== H.264 专用逻辑 ==========
        try {
            MediaFormat mediaFormat = MediaFormat.createVideoFormat(
                    mimeType,
                    exoFormat.width,
                    exoFormat.height
            );

            // 设置 H.264 Profile & Level
            int profile = getProfileFromAvcCode(exoFormat.codecs);
            int level = getLevelFromAvcCode(exoFormat.codecs);
            mediaFormat.setInteger(MediaFormat.KEY_PROFILE, profile);
            mediaFormat.setInteger(MediaFormat.KEY_LEVEL, level);

            // 码率（可选，不影响支持性）
            if (exoFormat.bitrate > 0) {
                mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, exoFormat.bitrate);
            }

            // 检测硬件解码器
            MediaCodec codec = MediaCodec.createDecoderByType(mimeType);
            codec.configure(mediaFormat, null, null, 0);
            codec.release();
            return true;
        } catch (Exception e) {
            // 硬件不支持 → 检测 FFmpeg
            return isFfmpegDecoderSupported(mimeType);
        }
    }

    /**
     * 通用硬件解码器检测（非H264格式直接用这个）
     */
    private boolean checkHardwareDecoder(String mimeType, Format exoFormat) {
        try {
            MediaFormat mediaFormat = MediaFormat.createVideoFormat(
                    mimeType,
                    exoFormat.width,
                    exoFormat.height
            );
            MediaCodec codec = MediaCodec.createDecoderByType(mimeType);
            codec.configure(mediaFormat, null, null, 0);
            codec.release();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从 avc1.xxxxxx 解析 H.264 Profile
     * 修复：越界、空指针、非法16进制问题
     */
    private int getProfileFromAvcCode(@Nullable String codecs) {
        if (codecs == null || !codecs.startsWith("avc1.") || codecs.length() < 7) {
            return MediaCodecInfo.CodecProfileLevel.AVCProfileBaseline;
        }
        try {
            String profileHex = codecs.substring(5, 7);
            return Integer.parseInt(profileHex, 16);
        } catch (NumberFormatException e) {
            return MediaCodecInfo.CodecProfileLevel.AVCProfileBaseline;
        }
    }

    /**
     * 从 avc1.xxxxxx 解析 H.264 Level
     * 修复：越界、空指针、非法16进制问题
     */
    private int getLevelFromAvcCode(@Nullable String codecs) {
        if (codecs == null || !codecs.startsWith("avc1.") || codecs.length() < 10) {
            return MediaCodecInfo.CodecProfileLevel.AVCLevel31;
        }
        try {
            String levelHex = codecs.substring(8, 10);
            return Integer.parseInt(levelHex, 16);
        } catch (NumberFormatException e) {
            return MediaCodecInfo.CodecProfileLevel.AVCLevel31;
        }
    }

    /**
     * FFmpeg 软件解码器支持检测（可自行实现）
     */
    private boolean isFfmpegDecoderSupported(String mimeType) {
        // 接入你的 FFmpeg 解码器
        // return FfmpegVideoDecoder.isSupported(mimeType);
        return false;
    }
}