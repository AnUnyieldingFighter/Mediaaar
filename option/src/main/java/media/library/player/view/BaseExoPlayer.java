package media.library.player.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.TextureView;
import android.view.View;

import com.google.common.collect.ImmutableList;

import androidx.annotation.OptIn;
import androidx.media3.common.Format;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.Tracks;
import androidx.media3.common.VideoSize;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.analytics.AnalyticsListener;
import androidx.media3.exoplayer.source.LoadEventInfo;
import androidx.media3.exoplayer.source.MediaLoadData;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter;
import media.library.player.manager.PlayerLog;

class BaseExoPlayer extends PlayerDB {
    protected Context playerContext;
    protected ExoPlayer player;
    // 负责控制缓冲区大小和加载时机
    @OptIn(markerClass = UnstableApi.class)
    protected DefaultLoadControl buff;
    //带宽检测器：监测网络带宽
    @OptIn(markerClass = UnstableApi.class)
    protected DefaultBandwidthMeter bandwidthMeter;
    //根据网络状况选择最佳码率轨道,音视频轨道选择组件
    @OptIn(markerClass = UnstableApi.class)
    protected DefaultTrackSelector trackSelector;
    //
    protected String videoUrl;
    protected final String tag = "播放器_CustomExoPlayer_";
    //true 发生错误
    protected boolean isError;
    //true 静音 在播放器准备完成之前 可以设置
    protected boolean isMute;
    //true 准备好了
    protected boolean isReady;

    public boolean isReady() {
        return isReady;
    }

    //==========================监听=================================================
    class ExoPlayerListener implements Player.Listener {

        @Override
        public void onPlaybackStateChanged(int playbackState) {
            Player.Listener.super.onPlaybackStateChanged(playbackState);
            switch (playbackState) {
                case Player.STATE_IDLE:
                    //即播放器停止和播放失败时的状态。
                    break;
                case Player.STATE_BUFFERING:
                    // 正在缓冲 state=

                    break;
                case Player.STATE_READY:
                    // 准备就绪，可以播放
                    isReady = true;
                    if (isMute) {
                        isMute = false;
                        if (player != null) {
                            player.setVolume(0);
                        }
                    }
                    break;
                case Player.STATE_ENDED:
                    //播放结束
                    break;
                default:

                    break;
            }
        }


        @Override
        public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
            //接收播放意图改变
        }

        @Override
        public void onPositionDiscontinuity(Player.PositionInfo oldPosition, Player.PositionInfo newPosition, int reason) {
            Player.Listener.super.onPositionDiscontinuity(oldPosition, newPosition, reason);
            // 位置跳跃，例如快进或快退
            switch (reason) {
                case Player.DISCONTINUITY_REASON_AUTO_TRANSITION:
                    // 循环播放
                    break;
                case Player.DISCONTINUITY_REASON_SEEK:
                    // 用户进行了快进或快退操作
                    break;
                case Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT:
                    // 由于缓冲或其他原因，播放位置被自动调整
                    break;
                // 其他原因...
            }

        }

        @Override
        public void onIsPlayingChanged(boolean isPlaying) {
            PlayerLog.d(tag, "播放中/播放停止   播放中：" + isPlaying);

        }

        @Override
        public void onVideoSizeChanged(VideoSize videoSize) {
            PlayerLog.d(tag, "视频大小  width：" + videoSize.width + " height:" + videoSize.height);
            if (!TextUtils.isEmpty(videoUrl)) {
                dbSetVideoSize(playerContext, videoUrl, videoSize);
            }
        }

        @OptIn(markerClass = UnstableApi.class)
        @Override
        public void onPlayerError(PlaybackException error) {

            //发生错误：
            //Source error code:2007 codeName:ERROR_CODE_IO_CLEARTEXT_NOT_PERMITTED  不允许明文传输
            //Source error code:2002 codeName:ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT 网络连接失败
            //Source error code:2004 codeName:ERROR_CODE_IO_BAD_HTTP_STATUS   404 地址错误
            //Unexpected runtime error code:1004 codeName:ERROR_CODE_FAILED_RUNTIME_CHECK //意外错误
            PlayerLog.d(tag, "发生错误：" + error.getMessage() + " code:" + error.errorCode + " codeName:" + error.getErrorCodeName());
            switch (error.errorCode) {
                case PlaybackException.ERROR_CODE_IO_CLEARTEXT_NOT_PERMITTED:
                case PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT:
                case PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS:
                case PlaybackException.ERROR_CODE_FAILED_RUNTIME_CHECK:
                    isError = true;
                    isReady = false;
                    //player.release();
                    //player.set
                    //player.prepare();
                    break;
                case PlaybackException.ERROR_CODE_DECODER_INIT_FAILED:
                    //MediaCodecVideoRenderer error, index=0, format=Format(1, null, video/mp4, video/avc, avc1.640034, 22400000, und, [4354, 2160, 29.999998, ColorInfo(BT709, Limited range, SDR SMPTE 170M, false, 8bit Luma, 8bit Chroma)], [-1, -1]), format_supported=NO_EXCEEDS_CAPABILITIES code:4001 codeName:ERROR_CODE_DECODER_INIT_FAILED
                    //关键信息：
                    //分辨率：4354x2160（接近 4K 超高清，部分中低端设备 / 旧设备不支持该分辨率硬解码）
                    //1080P (1920x1080) 或 720P (1280x720)	 绝大多数 Android 设备硬解码支持
                    //码率：22400000 bps = 22.4 Mbps（码率过高，设备解码芯片无法处理这么大的数据流）
                    //2Mbps - 10Mbps	1080P 对应 5-10Mbps，720P 对应 2-5Mbps，避免过高
                    // NO_EXCEEDS_CAPABILITIES ：超出设备能力上限 说明不是视频格式 / 编码不兼容（视频是 MP4 封装 + H.264 (avc1) 编码，这是 Android 通用支持的），而是视频的具体参数超出了当前设备硬解码（MediaCodec）的处理极限。
                    //解码初始化失败
                    isError = true;
                    isReady = false;
                    break;
            }
        }

        @Override
        public void onTracksChanged(Tracks tracks) {
            if (tracks == null) {
                PlayerLog.d(tag, "无轨道信息1");
                return;
            }
            ImmutableList<Tracks.Group> trackGroups = tracks.getGroups();
            if (trackGroups == null || trackGroups.size() == 0) {
                PlayerLog.d(tag, "无轨道信息1");
                return;
            }

            for (int i = 0; i < trackGroups.size(); i++) {
                Tracks.Group temp = trackGroups.get(i);
                int length = temp.length;
                int trackType = temp.getType();
                PlayerLog.d(tag, "轨道" + i + "数量:" + length + " 类型：" + trackType);
                for (int j = 0; j < length; j++) {
                    boolean isSupported = temp.isTrackSupported(j);
                    boolean isSelected = temp.isTrackSelected(j);
                    Format format = temp.getTrackFormat(j);
                    testFormat(format, j);
                    // 处理轨道信息
                    PlayerLog.d(tag, "轨道信息 " + j + " isSupported:" + isSupported + " isSelected：" + isSelected + " format:" + format);
                }
            }


        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private void testFormat(Format format, int trackIndex) {
        if (format == null) {
            return;
        }
        // 通用参数
        String mimeType = format.sampleMimeType;//编码类型
        long bitrate = format.bitrate;//码率
        float bitrateMbps = (float) bitrate / 1024 / 1024;
        String codec = format.codecs;//编码细节
        // 视频专属参数
        Integer width = format.width;
        Integer height = format.height;
        Float frameRate = format.frameRate;//帧率
        Integer rotation = format.rotationDegrees;//旋转角度
        // 拼接分辨率字符串（避免空指针）
        String resolution = (width != null && height != null) ? width + "x" + height : "未知分辨率";
        // 拼接帧率字符串
        String frameRateStr = (frameRate != null) ? frameRate + " fps" : "未知帧率";
        // 拼接旋转角度字符串
        String rotationStr = (rotation != null) ? rotation + "°" : "0°";
        // 打印解析结果
        String str = "视频轨道" + trackIndex + "  编码类型：" + mimeType + "  编码细节：" + codec +
                "  分辨率：" + resolution + "  码率：" + String.format("%.2f Mbps", bitrateMbps) +
                "  帧率：" + frameRateStr + "  旋转角度：" + rotationStr;
        PlayerLog.d(tag, str);

    }

    @UnstableApi
    class ExoPlayerAnalyticsListener implements AnalyticsListener {
        @Override
        public void onLoadCompleted(EventTime eventTime, LoadEventInfo loadEventInfo, MediaLoadData mediaLoadData) {
            AnalyticsListener.super.onLoadCompleted(eventTime, loadEventInfo, mediaLoadData);
            // 记录下载速度
            long speedKbps = loadEventInfo.bytesLoaded * 8 / (loadEventInfo.loadDurationMs * 1000);
            PlayerLog.d(tag, "缓冲完成 下载速度:  " + speedKbps + "kbps");
            /*PlayerLog.d(tag, "下载速度 loadTaskId:  " + loadEventInfo.loadTaskId + " Uri="+loadEventInfo.uri);
            PlayerLog.d(tag, "下载速度 加载耗时:  " + loadEventInfo.loadDurationMs + " 已加载字节数："+loadEventInfo.bytesLoaded);
*/
        }

        @Override
        public void onLoadStarted(EventTime eventTime, LoadEventInfo loadEventInfo, MediaLoadData mediaLoadData, int retryCount) {
            if (player == null) {
                return;
            }
            long bufferedPosition = player.getBufferedPosition();
            long currentPosition = player.getCurrentPosition();
            PlayerLog.d(tag, "缓冲开始:  播放位置=" + currentPosition + " 缓存=" + bufferedPosition);
        }

        @Override
        public void onBandwidthEstimate(EventTime eventTime, int totalLoadTimeMs, long totalBytesLoaded, long bitrateEstimate) {
            PlayerLog.d(tag, "缓冲 带宽:  总加载时长=" + totalLoadTimeMs + " 总加载字节=" + totalBytesLoaded + " 带宽估计值(dps)=" + bitrateEstimate);
            if (trackSelector != null) {
                // 根据带宽调整ABR策略
                DefaultTrackSelector.Parameters.Builder builder = trackSelector.buildUponParameters().setMaxVideoBitrate((int) (bitrateEstimate * 0.8)); // 保留20%余量
                trackSelector.setParameters(builder);
            }

        }

        @Override
        public void onPlaybackStateChanged(EventTime eventTime, int state) {
            // PlayerLog.d(tag, "playbackState： " + state + "kbps");
        }

    }
}
