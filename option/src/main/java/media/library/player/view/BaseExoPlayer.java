package media.library.player.view;

import android.app.ActivityManager;
import android.content.Context;
import android.text.TextUtils;
import android.view.Surface;
import android.view.SurfaceView;

import com.google.common.collect.ImmutableList;

import androidx.annotation.OptIn;
import androidx.media3.common.Format;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.PlaybackParameters;
import androidx.media3.common.Player;
import androidx.media3.common.TrackSelectionParameters;
import androidx.media3.common.Tracks;
import androidx.media3.common.VideoSize;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.SeekParameters;
import androidx.media3.exoplayer.analytics.AnalyticsListener;
import androidx.media3.exoplayer.source.LoadEventInfo;
import androidx.media3.exoplayer.source.MediaLoadData;
import androidx.media3.exoplayer.trackselection.AdaptiveTrackSelection;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter;
import media.library.player.manager.PlayerLog;
//设置播放器
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
    //设置倍数
    protected Float videoSpeed = null;

    public boolean isReady() {
        return isReady;
    }

    //==========================初始化播放器=====================================
    //true 使用arb
    private boolean isArb;

    public void setARB(boolean isArb) {
        this.isArb = isArb;
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
        /*if (player != null) {
            //因为释放了 release 所以要重新设置
            player.addListener(new ExoPlayerListener());
            player.addAnalyticsListener(new ExoPlayerAnalyticsListener());
            setVideoSurface(surface);
            addListener(listener);
            addAnalyticsListener(analyticsListener);
        }*/
        playerContext = context;
        if (player == null) {
            ExoPlayer.Builder builder = null;
            //
            DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(context);
            // 开启扩展解码器（FFmpeg），优先使用系统硬件解码，不支持时切换 FFmpeg
            renderersFactory.setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON);
            // 开启解码器降级策略，解码失败时自动切换备选解码器
            renderersFactory.setEnableDecoderFallback(true);
            //
            //ABR 不能在非主线程里加载
            if (!isArb) {
                //‌启用异步缓冲区排队
                renderersFactory.forceEnableMediaCodecAsynchronousQueueing();
                builder = new ExoPlayer.Builder(context);
                builder.setRenderersFactory(renderersFactory);
            } else {
                builder = new ExoPlayer.Builder(context);
                builder.setRenderersFactory(renderersFactory);
            }
            //设置缓存
            builder.setLoadControl(getPlayerDefBuffer());
            // 动态码率切换（ABR）的核心组件，通过智能选择最优码率轨道来平衡播放流畅性和画质
            // 已知 ABR 不能在非主线程里加载
            // 报错 不知道什么原因 DefaultTrackSelector is accessed on the wrong thread.
            if (isArb) {
                builder.setTrackSelector(getPlayerDefARB());
            }
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
    //设置播放器中的缓存
    @OptIn(markerClass = UnstableApi.class)
    public void setPlayerBuffer(DefaultLoadControl buff) {
        this.buff = buff;
    }

    //获取播放器中的缓存
    @OptIn(markerClass = UnstableApi.class)
    private DefaultLoadControl getPlayerDefBuffer() {
        if (bandwidthMeter == null) {
            bandwidthMeter = new DefaultBandwidthMeter.Builder(playerContext).build();
        }
        if (buff == null) {
            //减少缓冲区大小（单位：字节） 典型默认值（单位：毫秒）
            DefaultLoadControl.Builder build = new DefaultLoadControl.Builder();
            build.setBufferDurationsMs(15000,  // 最小预留15s缓冲
                    30000,  // 内存最多缓存30s视频
                    2500,  // 预加载满2.5s即可开始播放
                    5000  // 卡顿后需缓冲5s恢复
            );
            //限制播放器内存缓冲上限
            build.setTargetBufferBytes(getTargetBufferBytes());
            build.setPrioritizeTimeOverSizeThresholds(true); //优先时间阈值而非数据量阈值
            //setBufferDurationsMs	时间（秒）	预加载多少秒视频
            //setTargetBufferBytes	内存（字节）	最多占用多少内存
            //只要有一个生效 就停止缓存 防止OOM
            //禁止回退缓存 一般直播用
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
            str = "低内存设备 缓存3MB";
            targetBufferBytes = 3 * 1024 * 1024; // 3MB
        } else if (memoryClass <= 256) { // 中等内存设备
            str = "中等内存设备 缓存6MB";
            targetBufferBytes = 6 * 1024 * 1024; // 6MB
        } else { // 高内存设备
            str = "高内存设备 缓存10MB";
            targetBufferBytes = 10 * 1024 * 1024; // 10MB
        }
        PlayerLog.d(tag, "内存等级：" + str + " memoryClass=" + memoryClass);
        return targetBufferBytes;
    }

    //===================设置 DefaultTrackSelector 是 “选轨道”，不是 “转码”，单轨道场景必无效========================
    @OptIn(markerClass = UnstableApi.class)
    private DefaultTrackSelector getPlayerDefARB() {
        PlayerLog.d(tag, "设置码率");
        if (bandwidthMeter == null) {
            // 带宽检测器：监测网络带宽
            bandwidthMeter = new DefaultBandwidthMeter.Builder(playerContext).build();
        }
        // 动态码率切换（ABR）的核心组件，通过智能选择最优码率轨道来平衡播放流畅性和画质
        if (trackSelector == null) {
            //minDurationForQualityIncreaseMs	切换到更高质量轨道所需的最小缓冲时长	点播：5-8k，直播：15k+
            //maxDurationForQualityDecreaseMs	当缓冲时长低于此值时触发质量降低	波动网络：15k，稳定网络：30k
            //minDurationToRetainAfterDiscardMs	切换高质量轨道时需保留的低质量缓冲最小时长	必须 > 质量提升阈值
            //bandwidthFraction	带宽利用率系数（0-1），预留余量应对波动	弱网：0.5-0.6，优质网络：0.8-0.85
            //bufferedFractionToLiveEdgeForQualityIncrease	直播场景中需缓冲至直播边缘的比例才能提升质量
            // 自定义Factory实现差异化配置
            AdaptiveTrackSelection.Factory factoryTemp = new AdaptiveTrackSelection.Factory(8 * 1000,  // 质量提升阈值
                    20 * 1000, // 质量降低阈值
                    25 * 1000, // 保留缓冲
                    0.8f  // 带宽利用率
            );
            // 1. 初始化自适应轨道选择工厂（用于多码率场景）
            AdaptiveTrackSelection.Factory factoryTemp2 = new AdaptiveTrackSelection.Factory();
            trackSelector = new DefaultTrackSelector(playerContext, factoryTemp);
            //
            PlayerLog.d(tag, "设置最大码率");
            //仅能在多轨道媒体源中，筛选出码率低于设定值的备选
            TrackSelectionParameters trackSelectionParameters = new TrackSelectionParameters.Builder(playerContext)
                    .setMaxVideoSize(1920, 1080) // 限制最大分辨率为1080P
                    .setMaxVideoBitrate(10 * 1024 * 1024) // 限制最大码率为10Mbps（10*1024*1024 bps）
                    .build();
            trackSelector.setParameters(trackSelectionParameters);

        }
        return trackSelector;
    }

    //=========================释放缓存====================================
    //释放播放器中的缓存
    @OptIn(markerClass = UnstableApi.class)
    protected void setPlayerBuffRelease() {
        if (trackSelector != null) {
            //不要设置释放，会报错  DefaultTrackSelector is accessed on the wrong thread.
            //trackSelector.release();
            trackSelector = null;
         /*   if (Looper.myLooper() == Looper.getMainLooper()) {
                //已在主线程，直接执行
                trackSelector.release();
                trackSelector = null;

            } else {
                HandlerMedia.runInMainThread(new Runnable() {
                    @Override
                    public void run() {
                        // 子线程，切换主线程
                        trackSelector.release();
                        trackSelector = null;
                    }
                });
            }*/

        }
        if (buff != null) {
            //buff.onReleased();
            //buff.onReleased(player.is);
            buff = null;
        }
        if (bandwidthMeter != null) {
            bandwidthMeter = null;
        }
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
