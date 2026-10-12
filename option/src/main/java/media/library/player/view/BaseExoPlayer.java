package media.library.player.view;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Surface;
import android.view.SurfaceView;

import com.images.imageselect.BuildConfig;
import com.google.common.collect.ImmutableList;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.OptIn;
import androidx.media3.common.C;
import androidx.media3.common.Format;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.PlaybackParameters;
import androidx.media3.common.Player;
import androidx.media3.common.Timeline;
import androidx.media3.common.Tracks;
import androidx.media3.common.VideoSize;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.SeekParameters;
import androidx.media3.exoplayer.analytics.AnalyticsListener;

import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;

import media.library.player.manager.PlayerLog;
import media.library.player.videoNew.stats.VideoWatchStats;

//设置播放器
class BaseExoPlayer extends PlayerDB {
    //观看统计
    protected VideoWatchStats watchStatistics = new VideoWatchStats();
    protected Context playerContext;
    protected ExoPlayer player;
    //数据源的传输监听，用于统计缓冲期间的网络下载和本地读取速度
    @UnstableApi
    protected ExoPlayerAnalyticsListener bufferTransferListener;

    //
    protected String videoUrl;
    protected String videoTag;
    protected final String tag = "播放器_CustomExoPlayer_";
    //true 发生错误
    protected boolean isError;
    //true 静音 在播放器准备完成之前 可以设置
    protected Boolean isMute;
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
            //Context 改变需要重建播放器，重建期间不计入观看时长
            watchStatistics.setPlaying(false);
            player.release();
            player = null;
            setPlayerBuffRelease();
            setMediaSourceCacheRelease();
            PlayerLog.d(tag, "播放器 重新构建 播放地址：videoTag=" + videoTag + "\nurl" + videoUrl);
        }
        if (player != null && isError) {
            player.release();
            player = null;
            setPlayerBuffRelease();
            setMediaSourceCacheRelease();
            PlayerLog.d(tag, "播放器发生错误 重新构建/加载播放 播放地址：videoTag=" + videoTag + "\nurl" + videoUrl);
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
            ExoPlayerAnalyticsListener bufferListener = new ExoPlayerAnalyticsListener(this);
            bufferTransferListener = bufferListener;
            player.addAnalyticsListener(bufferListener);
            restoreVideoSurface();
            addListener(listener);
            addAnalyticsListener(analyticsListener);
            onNewPlayer();
        }
        if (videoSpeed != null) {
            player.setPlaybackSpeed(videoSpeed);
            videoSpeed = null;
        }
    }

    //创建了新的播放器
    protected void onNewPlayer() {

    }



    public boolean isError() {
        return isError;
    }

    //=====================================设置幕布==========================
    private Surface surface;
    private SurfaceView surfaceView;

    public void setVideoSurface(Surface surface) {
        SurfaceView oldSurfaceView = this.surfaceView;
        this.surface = surface;
        this.surfaceView = null;
        if (player == null) {
            return;
        }
        if (oldSurfaceView != null) {
            player.clearVideoSurfaceView(oldSurfaceView);
        }
        if (surface == null) {
            player.clearVideoSurface();
        } else {
            player.setVideoSurface(surface);
        }
    }

    public void setVideoSurfaceView(SurfaceView surfaceView) {
        SurfaceView oldSurfaceView = this.surfaceView;
        this.surfaceView = surfaceView;
        this.surface = null;
        if (player == null) {
            return;
        }
        if (oldSurfaceView != null && oldSurfaceView != surfaceView) {
            player.clearVideoSurfaceView(oldSurfaceView);
        }
        if (surfaceView == null) {
            if (oldSurfaceView == null) {
                player.clearVideoSurface();
            }
        } else if (oldSurfaceView != surfaceView) {
            player.setVideoSurfaceView(surfaceView);
        }
    }

    //播放器重新创建后恢复原来的画面输出
    private void restoreVideoSurface() {
        if (surfaceView != null) {
            player.setVideoSurfaceView(surfaceView);
        } else if (surface != null) {
            player.setVideoSurface(surface);
        }
    }

    //最终释放播放器时清空画面引用
    protected void setVideoSurfaceRelease() {
        surface = null;
        surfaceView = null;
    }

    //===============================设置添加/取消监听===========================
    private Player.Listener listener;

    public void addListener(Player.Listener listener) {
        if (listener == null) {
            return;
        }
        if (isError) {
            //发生错误时 会重新创建播放播，重新创建时用
            this.listener = listener;
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
    protected CustomTrackSelector customTrackSelector;
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

    //打印当前轨道信息。详细轨道信息统一放到 CustomTrackSelector 中处理
    protected void setTrackLog() {
        if (customTrackSelector == null) {
            PlayerLog.d(tag, "轨道信息：customTrackSelector为空");
            return;
        }
        customTrackSelector.setTrackLog();
    }

    //按语言优先选择音轨
    protected void setPreferredAudioLanguage(final String... language) {
        if (customTrackSelector == null) {
            return;
        }
        customTrackSelector.setPreferredAudioLanguage(language);
    }

    //禁用指定类型轨道，比如视频、音频、字幕
    protected void disableTrackType(final int trackType) {
        if (customTrackSelector == null) {
            return;
        }
        customTrackSelector.disableTrackType(trackType);
    }

    //禁用指定类型轨道集合
    protected void disableTrackType(Set<Integer> trackTypes) {
        if (customTrackSelector == null) {
            return;
        }
        customTrackSelector.disableTrackType(trackTypes);
    }

    //显示/隐藏字幕，并可指定字幕优先语言
    protected void setPreferredTextTrack(final boolean showText, final String preferredLanguage) {
        if (customTrackSelector == null) {
            return;
        }
        customTrackSelector.setPreferredTextTrack(showText, preferredLanguage);
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

        @OptIn(markerClass = UnstableApi.class)
        @Override
        public void onPlaybackStateChanged(int playbackState) {
            Player.Listener.super.onPlaybackStateChanged(playbackState);
            switch (playbackState) {
                case Player.STATE_IDLE:
                    //即播放器停止和播放失败时的状态。
                    break;
                case Player.STATE_BUFFERING:
                    //缓冲状态和速度日志由 AnalyticsListener 统一打印
                    break;
                case Player.STATE_READY:
                    // 准备就绪，可以播放
                    setVideoDuration();
                    isReady = true;
                    isError = false;
                    if (isMute != null) {
                        if (isMute) {
                            //player.setVolume(0);
                            player.mute();
                        } else {
                            //player.setVolume(1);
                            player.unmute();
                        }
                        isMute = null;
                    }
                    break;
                case Player.STATE_ENDED:
                    //播放结束
                    dbSaveVideoHistory(playerContext, videoUrl, player);
                    watchStatistics.setVideoEnd(1);
                    break;
                default:

                    break;
            }
        }


        @Override
        public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
            //接收播放意图改变；暂停进度由 CustomExoPlayer.setPause() 直接保存
        }

        @Override
        public void onPositionDiscontinuity(Player.PositionInfo oldPosition, Player.PositionInfo newPosition, int reason) {
            Player.Listener.super.onPositionDiscontinuity(oldPosition, newPosition, reason);
            // 位置跳跃，例如快进或快退
            switch (reason) {
                case Player.DISCONTINUITY_REASON_AUTO_TRANSITION:
                    // 循环播放
                    watchStatistics.setVideoEnd(2);
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
            watchStatistics.setPlaying(isPlaying);
        }

        //媒体提供标题、描述、封面或自定义标识时更新数据库，缺失字段保留原值
        @Override
        public void onMediaMetadataChanged(MediaMetadata mediaMetadata) {
            MediaItem mediaItem = player == null ? null : player.getCurrentMediaItem();
            if (mediaItem != null) {
                dbSetVideoMetadata(playerContext, videoUrl, mediaItem.mediaId, mediaMetadata);
            }
        }

        //切换媒体时也读取标识和配置元数据，兼容元数据没有变化而不触发上一回调的情况
        @Override
        public void onMediaItemTransition(MediaItem mediaItem, int reason) {
            if (mediaItem != null) {
                dbSetVideoMetadata(playerContext, videoUrl, mediaItem.mediaId, mediaItem.mediaMetadata);
            }
        }

        @Override
        public void onVideoSizeChanged(VideoSize videoSize) {
            PlayerLog.d(tag, "视频大小  width：" + videoSize.width + " height:" + videoSize.height);
            //视频尺寸变化时，将播放器返回的宽高保存到观看统计，单位像素
            watchStatistics.setVideoWidth(videoSize.width);
            watchStatistics.setVideoHeight(videoSize.height);
            if (!TextUtils.isEmpty(videoUrl)) {
                dbSetVideoSize(playerContext, videoUrl, videoSize);
            }
        }

        //播放时间线变化时，尝试更新视频总时长
        @Override
        public void onTimelineChanged(Timeline timeline, int reason) {
            setVideoDuration();
        }

        //视频时长更新到统计里
        private void setVideoDuration() {
            long duration = -1;
            if (player != null) {
                duration = player.getDuration();
                if (duration == C.TIME_UNSET) {
                    duration = -1;
                }
            }
            watchStatistics.setVideoDuration(duration);
            //数据库只接收有效时长，保留上面现有的统计赋值方式
            dbSetVideoDuration(playerContext, videoUrl, duration);
        }

        @OptIn(markerClass = UnstableApi.class)
        @Override
        public void onPlayerError(PlaybackException error) {

            //发生错误：
            //Source error code:2007 codeName:ERROR_CODE_IO_CLEARTEXT_NOT_PERMITTED  不允许明文传输
            //Source error code:2002 codeName:ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT 网络连接失败
            //Source error code:2004 codeName:ERROR_CODE_IO_BAD_HTTP_STATUS   404 地址错误
            //Unexpected runtime error code:1004 codeName:ERROR_CODE_FAILED_RUNTIME_CHECK //意外错误
            PlayerLog.d(tag, buildPlayerErrorMessage(error));
            //播放失败不保存观看历史，避免未播放成功的视频被记录为已观看
            watchStatistics.setPlaying(false);
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

        private String buildPlayerErrorMessage(PlaybackException error) {
            if (error == null) {
                return "发生错误：未知错误";
            }
            StringBuilder builder = new StringBuilder();
            builder.append("发生错误：")
                    .append(error.getMessage())
                    .append(" code:")
                    .append(error.errorCode)
                    .append(" codeName:")
                    .append(error.getErrorCodeName());

            if (error.errorCode == PlaybackException.ERROR_CODE_DECODER_INIT_FAILED) {
                builder.append("\n真实失败原因：")
                        .append(buildDecoderInitFailedReason(error.getMessage()));
            }

            Throwable cause = error.getCause();
            if (cause != null) {
                builder.append("\n底层异常：").append(buildThrowableMessage(cause));
            }
            return builder.toString();
        }

        private String buildDecoderInitFailedReason(String message) {
            StringBuilder builder = new StringBuilder();
            if (!TextUtils.isEmpty(message) && message.contains("NO_EXCEEDS_CAPABILITIES")) {
                builder.append("视频轨道参数超出当前设备 MediaCodec 解码能力，不是 URL、缓存或 MediaSource 选择问题。");
            } else {
                builder.append("设备解码器初始化失败，可能是编码格式不支持、分辨率/码率过高、DRM不支持或解码器兼容问题。");
            }

            String formatInfo = extractDecoderFormatInfo(message);
            if (!TextUtils.isEmpty(formatInfo)) {
                builder.append(" ").append(formatInfo);
            }
            builder.append(" 建议服务端提供低清晰度/低码率版本，或转码为更通用的 H.264 1080P/标准4K、合理码率。");
            return builder.toString();
        }

        private String extractDecoderFormatInfo(String message) {
            if (TextUtils.isEmpty(message)) {
                return null;
            }
            Pattern pattern = Pattern.compile(
                    "Format\\([^,]*,\\s*[^,]*,\\s*([^,]*),\\s*([^,]*),\\s*([^,]*),\\s*([0-9-]+),\\s*[^,]*,\\s*\\[([0-9-]+),\\s*([0-9-]+),\\s*([0-9.\\-]+)");
            Matcher matcher = pattern.matcher(message);
            if (!matcher.find()) {
                return null;
            }
            String containerMime = matcher.group(1);
            String sampleMime = matcher.group(2);
            String codec = matcher.group(3);
            String bitrate = matcher.group(4);
            String width = matcher.group(5);
            String height = matcher.group(6);
            String frameRate = matcher.group(7);
            return "解析到失败格式：容器=" + containerMime
                    + " 编码=" + sampleMime
                    + " codec=" + codec
                    + " 码率=" + bitrate + "bps"
                    + " 分辨率=" + width + "x" + height
                    + " 帧率=" + frameRate + "fps。";
        }

        private String buildThrowableMessage(Throwable throwable) {
            StringBuilder builder = new StringBuilder();
            Throwable temp = throwable;
            int index = 0;
            while (temp != null && index < 5) {
                if (index > 0) {
                    builder.append(" <- cause: ");
                }
                builder.append(temp.getClass().getName());
                if (!TextUtils.isEmpty(temp.getMessage())) {
                    builder.append(": ").append(temp.getMessage());
                }
                temp = temp.getCause();
                index++;
            }
            return builder.toString();
        }

        @OptIn(markerClass = UnstableApi.class)
        @Override
        public void onTracksChanged(Tracks tracks) {
            if (!BuildConfig.DEBUG) {
                return;
            }
            if (tracks == null) {
                PlayerLog.d(tag, "轨道变化：无轨道信息");
                return;
            }
            ImmutableList<Tracks.Group> trackGroups = tracks.getGroups();
            if (trackGroups == null || trackGroups.size() == 0) {
                PlayerLog.d(tag, "轨道变化：无轨道信息");
                return;
            }

            PlayerLog.d(tag, "轨道变化：轨道组数量=" + trackGroups.size());
            for (int i = 0; i < trackGroups.size(); i++) {
                Tracks.Group temp = trackGroups.get(i);
                int length = temp.length;
                int trackType = temp.getType();
                PlayerLog.d(tag, "轨道组[" + i + "] 类型=" + getTrackTypeName(trackType) + " 轨道数量=" + length);
                for (int j = 0; j < length; j++) {
                    boolean isSupported = temp.isTrackSupported(j);
                    boolean isSelected = temp.isTrackSelected(j);
                    Format format = temp.getTrackFormat(j);
                    PlayerLog.d(tag, "轨道[" + i + "][" + j + "]"
                            + " 支持=" + isSupported
                            + " 选中=" + isSelected
                            + " mime=" + (format == null ? "未知" : format.sampleMimeType)
                            + " codecs=" + (format == null ? "未知" : format.codecs)
                            + " bitrate=" + (format == null ? "未知" : format.bitrate)
                            + " width=" + (format == null ? "未知" : format.width)
                            + " height=" + (format == null ? "未知" : format.height)
                            + " language=" + (format == null ? "未知" : format.language));
                }
            }


        }
    }

    private String getTrackTypeName(int trackType) {
        switch (trackType) {
            case C.TRACK_TYPE_AUDIO:
                return "音频(" + trackType + ")";
            case C.TRACK_TYPE_VIDEO:
                return "视频(" + trackType + ")";
            case C.TRACK_TYPE_TEXT:
                return "字幕(" + trackType + ")";
            case C.TRACK_TYPE_METADATA:
                return "元数据(" + trackType + ")";
            case C.TRACK_TYPE_IMAGE:
                return "图片(" + trackType + ")";
            case C.TRACK_TYPE_CAMERA_MOTION:
                return "相机运动(" + trackType + ")";
            case C.TRACK_TYPE_NONE:
                return "无(" + trackType + ")";
            case C.TRACK_TYPE_UNKNOWN:
                return "未知(" + trackType + ")";
            default:
                return "其他(" + trackType + ")";
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

    //数据库取得本地或网络视频总大小后，同步更新当前视频的观看统计
    @Override
    protected void onDbVideoFileSizeChanged(long sizeKb, String sizeStr) {
        watchStatistics.setVideoFileSize(sizeKb, sizeStr);
    }
}
