package media.library.player.view;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Surface;
import android.view.SurfaceView;

import com.google.common.collect.ImmutableList;

import java.util.HashSet;
import java.util.Set;

import androidx.annotation.OptIn;
import androidx.media3.common.C;
import androidx.media3.common.Format;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.PlaybackParameters;
import androidx.media3.common.Player;
import androidx.media3.common.TrackGroup;
import androidx.media3.common.TrackSelectionParameters;
import androidx.media3.common.Tracks;
import androidx.media3.common.VideoSize;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.RendererCapabilities;
import androidx.media3.exoplayer.SeekParameters;
import androidx.media3.exoplayer.analytics.AnalyticsListener;
import androidx.media3.exoplayer.source.LoadEventInfo;
import androidx.media3.exoplayer.source.MediaLoadData;
import androidx.media3.exoplayer.source.TrackGroupArray;
import androidx.media3.exoplayer.trackselection.AdaptiveTrackSelection;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.exoplayer.trackselection.MappingTrackSelector;
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter;
import media.library.player.manager.PlayerLog;

//设置播放器
class BaseExoPlayer extends PlayerDB {
    protected Context playerContext;
    protected ExoPlayer player;


    //
    protected String videoUrl;
    protected final String tag = "播放器_CustomExoPlayer_";
    //true 发生错误
    protected boolean isError;
    //true 静音 在播放器准备完成之前 可以设置
    protected boolean isMute;
    //true 准备好了
    protected boolean isReady;
    //设置倍数
    protected Float videoSpeed = null;

    public boolean isReady() {
        return isReady;
    }

    //==========================初始化播放器=====================================

    //true 强制启用 MediaCodec 异步队列
    private boolean forceAsyncQueueing;


    /**
     * 设置是否强制启用 MediaCodec 异步队列。
     *
     * <p>默认关闭。开启后可能减少部分高码率/高分辨率视频卡顿，
     * 但也可能在部分机型上带来黑屏、花屏、seek 异常等兼容问题。</p>
     *
     * @param forceAsyncQueueing true 强制启用异步队列
     */
    public void setForceAsyncQueueing(boolean forceAsyncQueueing) {
        this.forceAsyncQueueing = forceAsyncQueueing;
    }

    //是否已经初始化
    public boolean isInit() {
        return player != null;
    }

    @OptIn(markerClass = UnstableApi.class)
    protected void initExoPlayer(Context context) {
        if (player != null && playerContext != null && playerContext != context) {
            player.release();
            player = null;
            setPlayerBuffRelease();
            setMediaSourceCacheRelease();
            PlayerLog.d(tag, "播放器 重新构建 播放地址：" + videoUrl);
        }
        if (player != null && isError) {
            player.release();
            player = null;
            setPlayerBuffRelease();
            setMediaSourceCacheRelease();
            PlayerLog.d(tag, "播放器发生错误 重新构建 播放地址：" + videoUrl);
        }
        isError = false;
        playerContext = context;
        if (player == null) {
            ExoPlayer.Builder builder = null;
            //创建一个默认渲染器工厂
            //视频渲染器
            //音频渲染器
            //字幕渲染器
            //元数据渲染器
            DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(context);
            //允许使用扩展解码器 开启扩展解码器（FFmpeg），优先使用系统硬件解码，不支持时切换 FFmpeg
            //EXTENSION_RENDERER_MODE_OFF     不用扩展解码器
            //EXTENSION_RENDERER_MODE_PREFER  优先扩展解码器，比如优先 FFmpeg
            renderersFactory.setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON);
            // 开启解码器降级策略，解码失败时自动切换备选解码器
            //解码器失败时，允许尝试备用解码器。 比如设备上可能有多个 H.264 解码器：
            //硬件解码器 A
            //硬件解码器 B
            //软件解码器
            renderersFactory.setEnableDecoderFallback(true);
            //
            if (forceAsyncQueueing) {
                //启用异步缓冲区排队
                renderersFactory.forceEnableMediaCodecAsynchronousQueueing();
            }
            builder = new ExoPlayer.Builder(context);
            builder.setRenderersFactory(renderersFactory);
            //设置缓存
            builder.setLoadControl(getPlayerDefBuffer());
            // 音视频/字幕轨道选择器。
            // 即使不开 ABR，也要设置 trackSelector，方便后面切换音轨、字幕、分辨率限制。
            builder.setTrackSelector(getPlayerTrackSelector());
            //
            player = builder.build();
            player.addListener(new ExoPlayerListener());
            player.addAnalyticsListener(new ExoPlayerAnalyticsListener());
            setVideoSurface(surface);
            addListener(listener);
            addAnalyticsListener(analyticsListener);
        }
        if (videoSpeed != null) {
            player.setPlaybackSpeed(videoSpeed);
            videoSpeed = null;
        }
    }

    //=====================================设置幕布==========================
    private Surface surface;

    public void setVideoSurface(Surface surface) {
        if (surface == null) {
            return;
        }
        if (player == null) {
            this.surface = surface;
            return;
        }
        this.surface = null;
        player.setVideoSurface(surface);
    }

    public void setVideoSurfaceView(SurfaceView surfaceView) {
        player.setVideoSurfaceView(surfaceView);
    }

    //===============================设置添加/取消监听===========================
    private Player.Listener listener;

    public void addListener(Player.Listener listener) {
        if (listener == null) {
            return;
        }
        if (player == null) {
            this.listener = listener;
            return;
        }
        player.addListener(listener);
        this.listener = null;
    }


    public void removeListener(Player.Listener listener) {
        if (player == null) {
            return;
        }
        player.removeListener(listener);
    }

    AnalyticsListener analyticsListener;

    public void addAnalyticsListener(AnalyticsListener listener) {
        if (listener == null) {
            return;
        }
        if (player == null) {
            this.analyticsListener = listener;
            return;
        }
        player.addAnalyticsListener(listener);
        this.analyticsListener = null;
    }

    public void removeAnalyticsListener(AnalyticsListener listener) {
        if (player == null) {
            return;
        }
        player.removeAnalyticsListener(listener);
    }

    //=========================设置缓存策略====================================
    // 负责控制缓冲区大小和加载时机
    @OptIn(markerClass = UnstableApi.class)
    protected DefaultLoadControl buff;

    //设置播放器中的缓存
    @OptIn(markerClass = UnstableApi.class)
    public void setPlayerBuffer(DefaultLoadControl buff) {
        this.buff = buff;
    }


    //获取播放器中的缓存
    @OptIn(markerClass = UnstableApi.class)
    private DefaultLoadControl getPlayerDefBuffer() {
        if (buff == null) {
            //配置播放器内存缓冲策略，单位都是毫秒
            DefaultLoadControl.Builder build = new DefaultLoadControl.Builder();
            build.setBufferDurationsMs(
                    15000,  // 最少希望缓冲15秒，低于这个值会继续加载
                    30000,  // 最多缓冲30秒的媒体时长
                    2500,   // 首次播放前至少缓冲2.5秒
                    5000    // 卡顿重新缓冲后，至少缓冲5秒再恢复播放
            );
            //限制目标缓冲大小，单位是字节，用来控制内存占用
            build.setTargetBufferBytes(getTargetBufferBytes());
            //true：优先按时间阈值控制缓冲；即使达到 targetBufferBytes，也更倾向先满足时间缓冲
            build.setPrioritizeTimeOverSizeThresholds(true);
            //设置后向缓冲。0表示不保留已播放内容，直播或低内存场景可用
            //build.setBackBuffer(0, false);
            DefaultLoadControl loadControl = build.build();
            buff = loadControl;
        }
        return buff;
    }

    private int getTargetBufferBytes() {
        ActivityManager am = (ActivityManager) playerContext.getSystemService(Context.ACTIVITY_SERVICE);
        int memoryClass = am.getMemoryClass(); // 获取设备内存等级（MB）
        int targetBufferBytes;
        String str = "";
        if (memoryClass <= 128) { // 低内存设备
            str = "低内存设备 缓存8MB";
            targetBufferBytes = 8 * 1024 * 1024; // 8MB
        } else if (memoryClass <= 256) { // 中等内存设备
            str = "中等内存设备 缓存16MB";
            targetBufferBytes = 16 * 1024 * 1024; // 16MB
        } else { // 高内存设备
            str = "高内存设备 缓存32MB";
            targetBufferBytes = 32 * 1024 * 1024; // 32MB
        }
        PlayerLog.d(tag, "内存等级：" + str + " memoryClass=" + memoryClass);
        return targetBufferBytes;
    }

    //===================设置 DefaultTrackSelector 是 “选轨道”，不是 “转码”，单轨道场景必无效========================
    private CustomTrackSelector customTrackSelector;
    //true 使用arb
    private boolean isArb;

    public void setARB(boolean isArb) {
        this.isArb = isArb;
        if (customTrackSelector == null) {
            return;
        }
        customTrackSelector.setIsArb(isArb);
        runOnPlayerThread(new Runnable() {
            @Override
            public void run() {
                customTrackSelector.applyTrackSelectorParameters();
            }
        });
    }

    @OptIn(markerClass = UnstableApi.class)
    private DefaultTrackSelector getPlayerTrackSelector() {
        if (customTrackSelector == null) {
            customTrackSelector = new CustomTrackSelector(this, playerContext);
        }
        customTrackSelector.setIsArb(isArb);
        return customTrackSelector.getPlayerTrackSelector();
    }

    //=========================释放缓存====================================
    //清空播放器本类持有的缓存/轨道选择器引用
    @OptIn(markerClass = UnstableApi.class)
    protected void setPlayerBuffRelease() {
        //DefaultTrackSelector 由 ExoPlayer 持有，player.release() 时会自动调用 trackSelector.release()。
        //这里不要手动调用 trackSelector.release()，否则如果当前线程不是播放器线程，会报：
        //DefaultTrackSelector is accessed on the wrong thread.
        if (customTrackSelector != null) {
            customTrackSelector.setRelease();
            customTrackSelector = null;
        }
        //DefaultLoadControl 不需要手动释放，清空引用即可，下次初始化播放器时重新创建。
        buff = null;
    }

    //释放播放源中的缓存
    protected void setMediaSourceCacheRelease() {
    }

    //======================获取播放器的相关数据，设置播放器数据==============================
    //获取播放参数
    public PlaybackParameters getPlaybackParameters() {
        if (player == null) {
            return null;
        }
        PlaybackParameters parameters = player.getPlaybackParameters();
        return parameters;
    }

    @OptIn(markerClass = UnstableApi.class)
    public void setSeekParameters(SeekParameters seekParameters) {
        player.setSeekParameters(seekParameters);
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
                    isError = false;
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
            isError = true;
            isReady = false;
            switch (error.errorCode) {
                case PlaybackException.ERROR_CODE_IO_CLEARTEXT_NOT_PERMITTED:
                case PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT:
                case PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS:
                case PlaybackException.ERROR_CODE_FAILED_RUNTIME_CHECK:
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
            if (!isArb) {
                return;
            }

            if (customTrackSelector!=null){
                final int maxVideoBitrate = (int) (bitrateEstimate * 0.8);
                customTrackSelector.setVideoBitrate(maxVideoBitrate);
            }
        }

        @Override
        public void onPlaybackStateChanged(EventTime eventTime, int state) {
            // PlayerLog.d(tag, "playbackState： " + state + "kbps");
        }

    }


    /**
     * 在播放器所属线程执行任务。
     *
     * <p>DefaultTrackSelector 需要在播放器 applicationLooper 所在线程访问，
     * 否则容易报：DefaultTrackSelector is accessed on the wrong thread。</p>
     */
    protected void runOnPlayerThread(Runnable runnable) {
        if (runnable == null) {
            return;
        }
        if (player == null) {
            runnable.run();
            return;
        }
        Looper playerLooper = player.getApplicationLooper();
        if (Looper.myLooper() == playerLooper) {
            runnable.run();
        } else {
            new Handler(playerLooper).post(runnable);
        }
    }
}
