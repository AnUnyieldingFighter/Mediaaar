package media.library.player.view;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.OptIn;
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
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter;
import media.library.player.manager.PlayerLog;

class BaseExoPlayer extends PlayerDB {
    protected Context playerContext;
    protected ExoPlayer player;
    // 负责控制缓冲区大小和加载时机
    @OptIn(markerClass = UnstableApi.class)
    protected DefaultLoadControl buff;
    //提供实时带宽数据
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
                    if (isMute) {
                        isMute = false;
                        player.setVolume(0);
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
                    //player.release();
                    //player.set
                    //player.prepare();
                    break;
            }
        }

        @Override
        public void onTracksChanged(Tracks tracks) {

        }
    }

    @UnstableApi
    class ExoPlayerAnalyticsListener implements AnalyticsListener {
        @Override
        public void onLoadCompleted(EventTime eventTime, LoadEventInfo loadEventInfo,
                                    MediaLoadData mediaLoadData) {
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
            long bufferedPosition = player.getBufferedPosition();
            long currentPosition = player.getCurrentPosition();
            PlayerLog.d(tag, "缓冲开始:  播放位置=" + currentPosition + " 缓存=" + bufferedPosition);
        }

        @Override
        public void onBandwidthEstimate(EventTime eventTime, int totalLoadTimeMs, long totalBytesLoaded, long bitrateEstimate) {
            PlayerLog.d(tag, "缓冲 带宽:  总加载时长=" + totalLoadTimeMs + " 总加载字节=" + totalBytesLoaded + " 带宽估计值(dps)=" + bitrateEstimate);
            if (trackSelector != null) {
                // 根据带宽调整ABR策略
                DefaultTrackSelector.Parameters.Builder builder = trackSelector.buildUponParameters()
                        .setMaxVideoBitrate((int) (bitrateEstimate * 0.8)); // 保留20%余量
                trackSelector.setParameters(builder);
            }

        }

        @Override
        public void onPlaybackStateChanged(EventTime eventTime, int state) {
            // PlayerLog.d(tag, "playbackState： " + state + "kbps");
        }

    }
}
