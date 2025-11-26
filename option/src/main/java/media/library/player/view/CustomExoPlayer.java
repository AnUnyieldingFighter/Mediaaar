package media.library.player.view;


import android.app.ActivityManager;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.view.Surface;
import android.view.SurfaceView;

import androidx.annotation.OptIn;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.TrackSelectionParameters;
import androidx.media3.common.Tracks;
import androidx.media3.common.VideoSize;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.database.StandaloneDatabaseProvider;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.datasource.cache.CacheDataSource;
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor;
import androidx.media3.datasource.cache.NoOpCacheEvictor;
import androidx.media3.datasource.cache.SimpleCache;
import androidx.media3.datasource.rtmp.RtmpDataSource;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.analytics.AnalyticsListener;
import androidx.media3.exoplayer.drm.DrmSessionManager;
import androidx.media3.exoplayer.hls.HlsMediaSource;
import androidx.media3.exoplayer.rtsp.RtspMediaSource;
import androidx.media3.exoplayer.source.LoadEventInfo;
import androidx.media3.exoplayer.source.MediaLoadData;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.exoplayer.trackselection.AdaptiveTrackSelection;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter;
import androidx.media3.ui.PlayerControlView;
import androidx.media3.ui.PlayerView;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Random;

import media.library.db.MediaRoom;
import media.library.player.bean.VideoEntity;
import media.library.player.manager.PlayerLog;
import media.library.utils.FileUtil;
import media.library.utils.Md5Media;

//        exoPlayer = new CustomExoPlayer();
//        exoPlayer.setPlayerVideo(act, videoUrl, true);
//        //
//        exoPlayer.setPlayerView(playerView);
//        exoPlayer.prepare();
//        exoPlayer.setPlayWhenReady(false);
//        //设置循环播放
//        exoPlayer.setRepeatMode(Player.REPEAT_MODE_ALL);
public class CustomExoPlayer {
    private ExoPlayer player;
    private Context playerContext;
    private boolean isInit = false;

    public CustomExoPlayer() {

    }

    //true 使用arb
    private boolean isArb;

    public void setARB(boolean isArb) {
        this.isArb = isArb;
    }

    @OptIn(markerClass = UnstableApi.class)
    public void initExoPlayer(Context context) {
        isInit = true;
        if (player != null && playerContext != null && playerContext != context) {
            player.release();
            player = null;
            setCacheRelease();
            PlayerLog.d(tag, "播放器 重新构建 播放地址：" + videoUrl);
        }
        if (player != null && isError) {
            player.release();
            player = null;
            setCacheRelease();
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
        if (player == null) {
            //启用异步缓冲
            DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(context);
            //‌启用异步缓冲区排队
            renderersFactory.forceEnableMediaCodecAsynchronousQueueing();
            ExoPlayer.Builder builder = new ExoPlayer.Builder(context, renderersFactory);
            //设置缓存
            builder.setLoadControl(getDefBuffer());
            // 动态码率切换（ABR）的核心组件，通过智能选择最优码率轨道来平衡播放流畅性和画质
            // 报错 不知道什么原因 DefaultTrackSelector is accessed on the wrong thread.
            if (isArb) {
                builder.setTrackSelector(getDefARB());
            }
            //
            player = builder.build();
            playerContext = context;
            player.addListener(new ExoPlayerListener());
            player.addAnalyticsListener(new ExoPlayerAnalyticsListener());
            setVideoSurface(surface);
            addListener(listener);
            addAnalyticsListener(analyticsListener);
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private DefaultLoadControl buff;// 负责控制缓冲区大小和加载时机


    @OptIn(markerClass = UnstableApi.class)
    public void setBuffer(DefaultLoadControl buff) {
        this.buff = buff;
    }

    @OptIn(markerClass = UnstableApi.class)
    private DefaultLoadControl getDefBuffer() {
        if (bandwidthMeter == null) {
            bandwidthMeter = new DefaultBandwidthMeter.Builder(context).build();
        }
        if (buff == null) {
            //减少缓冲区大小（单位：字节） 典型默认值（单位：毫秒）
            DefaultLoadControl.Builder build = new DefaultLoadControl.Builder();
            build.setBufferDurationsMs(
                    15000,  // 最小缓冲时间（触发加载的阈值
                    30000,  // 最大缓冲时间（停止加载的阈值）
                    2500,  // 播放开始前预缓冲时间
                    5000  // 重新缓冲后预缓冲时间
            );
            build.setTargetBufferBytes(getTargetBufferBytes());
            build.setPrioritizeTimeOverSizeThresholds(true); //优先时间阈值而非数据量阈值
            //禁止回退缓存 一般直播用
            //build.setBackBuffer(0, false);
            DefaultLoadControl loadControl = build.build();
            buff = loadControl;
        }
        return buff;
    }

    @OptIn(markerClass = UnstableApi.class)
    private DefaultBandwidthMeter bandwidthMeter;//提供实时带宽数据

    @OptIn(markerClass = UnstableApi.class)
    private DefaultTrackSelector trackSelector;//根据网络状况选择最佳码率轨道

    @OptIn(markerClass = UnstableApi.class)
    private DefaultTrackSelector getDefARB() {
        if (bandwidthMeter == null) {
            bandwidthMeter = new DefaultBandwidthMeter.Builder(context).build();
        }
        // 动态码率切换（ABR）的核心组件，通过智能选择最优码率轨道来平衡播放流畅性和画质
        if (trackSelector == null) {
            //minDurationForQualityIncreaseMs	切换到更高质量轨道所需的最小缓冲时长	点播：5-8k，直播：15k+
            //maxDurationForQualityDecreaseMs	当缓冲时长低于此值时触发质量降低	波动网络：15k，稳定网络：30k
            //minDurationToRetainAfterDiscardMs	切换高质量轨道时需保留的低质量缓冲最小时长	必须 > 质量提升阈值
            //bandwidthFraction	带宽利用率系数（0-1），预留余量应对波动	弱网：0.5-0.6，优质网络：0.8-0.85
            //bufferedFractionToLiveEdgeForQualityIncrease	直播场景中需缓冲至直播边缘的比例才能提升质量
            // 自定义Factory实现差异化配置
            AdaptiveTrackSelection.Factory factoryTemp = new AdaptiveTrackSelection.Factory(
                    8000,  // 质量提升阈值
                    20000, // 质量降低阈值
                    25000, // 保留缓冲
                    0.8f  // 带宽利用率
            );
            trackSelector = new DefaultTrackSelector(context, factoryTemp);
        }
        return trackSelector;
    }

    private int getTargetBufferBytes() {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
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

    public boolean isInit() {
        return isInit;
    }

    private PlayerView cachePlayerView;

    @OptIn(markerClass = UnstableApi.class)
    public void setPlayerView(PlayerView playerView) {
        if (cachePlayerView == playerView) {
            return;
        }
        if (cachePlayerView != null) {
            cachePlayerView.setPlayer(null);
        }
        if (cachePlayerControlView != null) {
            cachePlayerControlView.setPlayer(null);
        }
        playerView.setPlayer(player);

        this.cachePlayerView = playerView;
    }

    //设置倍数 大于于0，1是正常速度，2是两倍速度，0.5是正常速度的一半。
    public void setPlaybackSpeed(float speed) {
        player.setPlaybackSpeed(speed);
    }

    @OptIn(markerClass = UnstableApi.class)
    private PlayerControlView cachePlayerControlView;

    @OptIn(markerClass = UnstableApi.class)
    public void setPlayerControlView(PlayerControlView playerControlView) {
        if (cachePlayerControlView == playerControlView) {
            return;
        }
        if (cachePlayerView != null) {
            cachePlayerView.setPlayer(null);
        }
        if (cachePlayerControlView != null) {
            cachePlayerControlView.setPlayer(null);
        }
        playerControlView.setPlayer(player);
        cachePlayerControlView = playerControlView;
    }

    //准备
    public void prepare() {
        player.prepare();
    }

    public boolean isPlayerPaused() {
        if (player == null) {
            return false;
        }
        boolean isPlayReady = player.getPlayWhenReady();
        return player.getPlaybackState() == Player.STATE_READY && !isPlayReady;
    }

    public boolean getPlayWhenReady() {
        if (player == null) {
            return false;
        }
        return player.getPlayWhenReady();
    }

    //用于控制播放器的"准备就绪时自动播放"行为
    //true： 当播放器准备好媒体资源（调用prepare()完成）后自动开始播放  自动播放 默认playWhenReady=true
    //false：即使播放器准备完成，也不会自动播放，需手动调用play()
    public void setPlayWhenReady(boolean playWhenReady) {
        player.setPlayWhenReady(playWhenReady);
    }

    public void pause() {
        player.pause();
    }

    public void play() {
        player.play();
    }

    //设置暂停
    public void setPause() {
        if (player == null) {
            return;
        }
        if (player.isPlaying()) {
            player.pause();
            onUpdateVideoHis();
        } else {
            player.setPlayWhenReady(false);
        }
    }

    //设置持续播放/继续播放
    public void setPlayContinue() {
        if (player == null) {
            return;
        }
        // if(exoPlayer.isReleased()){}
        //exoPlayer.play();
        player.setPlayWhenReady(true);
    }

    public void setRepeatMode(@Player.RepeatMode int repeatMode) {
        player.setRepeatMode(repeatMode);
    }

    public boolean isPlaying() {
        if (player == null) {
            return false;
        }
        return player.isPlaying();
    }

    Player.Listener listener;

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

    //当前播放进度
    public long getCurrentPosition() {
        return player.getCurrentPosition();
    }

    //总时长
    public long getDuration() {
        return player.getDuration();
    }

    //获取视频大小
    @OptIn(markerClass = UnstableApi.class)
    public VideoSize getVideoSize() {
        VideoSize videoSize = player.getVideoSize();
        int width = videoSize.width;
        int height = videoSize.height;
        if (width <= 0 && height <= 0) {
            List<VideoEntity> datas = MediaRoom.geVideoDb(context).queryVideoCache(videoUrl);
            if (datas != null && datas.size() > 0) {
                VideoEntity entity = datas.get(0);
                if (entity.videoWidth > 0 && entity.videoHeight > 0) {
                    videoSize = new VideoSize(entity.videoWidth, entity.videoHeight);
                }
            }

        }
        return videoSize;
    }

    //获取状态
    public int getPlaybackState() {
        return player.getPlaybackState();
    }

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

    @OptIn(markerClass = UnstableApi.class)
    public void setMediaSource(MediaSource mediaSource) {
        player.setMediaSource(mediaSource);
    }

    public void setVideoSurfaceView(SurfaceView surfaceView) {
        player.setVideoSurfaceView(surfaceView);
    }

    //true 静音
    private boolean isMute;

    public void setMute(boolean isMute) {
        int playbackState = player.getPlaybackState();
        if (playbackState == Player.STATE_READY || player.isPlaying()) {
            this.isMute = false;
            if (isMute) {
                player.setVolume(0);
            } else {
                player.setVolume(1);
            }
        } else {
            this.isMute = isMute;
        }

    }

    //设置播放进度
    public void seekTo(long positionMs) {
        player.seekTo(positionMs);
    }

    //======================设置缓存===============================
    private boolean isUseCache;
    private String videoUrl;
    private Context context;

    public boolean isEqualVideoPaly(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        if (isError) {
            return false;
        }
        return url.equals(videoUrl);
    }

    /**
     * @param videoUrl 播放器url
     */
    @UnstableApi
    public void setPlayerVideo(Context context, String videoUrl) {
        setPlayerVideo(context, videoUrl, false);
    }


    /**
     * @param videoUrl 播放器url
     * @param isCache  true 可以缓存
     */
    @UnstableApi
    public void setPlayerVideo(Context context, String videoUrl, boolean isCache) {
        setCacheRelease();
        //
        this.isUseCache = isCache;
        this.videoUrl = videoUrl;
        this.context = context;
        //
        initExoPlayer(context);
        //方式一
        MediaSource mediaSource = getMediaSource();
        //
        player.setMediaSource(mediaSource);
        //方式二
        /*MediaItem videoItem = new MediaItem.Builder().setUri(videoUrl).setMediaId(videoUrl).build();
        player.setMediaItem(videoItem);*/
    }

    @UnstableApi
    private MediaSource getMediaSource() {
       /* if (videoUrl.endsWith("m3u8")) {
            // hls链接
        } else if (videoUrl.startsWith("rtsp")) {
            // rtsp链接
        } else if (videoUrl.startsWith("rtmp")) {
            // rtmp链接
        } else {
        }*/
        return getMediaSource("");
    }

    private String img1 = "https://img0.baidu.com/it/u=3404601552,3434841255&fm=253&app=138&f=JPEG?w=800&h=1200";

    @OptIn(markerClass = UnstableApi.class)
    private MediaSource getMediaSource(String type) {
        //媒体元数据描述 不知道用法
        /*MediaMetadata mediaMetadata = new MediaMetadata.Builder()
                .setTitle("示例标题")
                .setArtist("示例艺术家")
                .setSubtitle("小标题")
                .setDescription("描述")
                .setAlbumTitle("示例专辑")
                .setArtworkUri(Uri.parse(img1))
                .build();*/
        MediaItem.Builder builder = new MediaItem.Builder()
                //.setMediaMetadata(mediaMetadata)
                //.setMimeType(MimeTypes.VIDEO_MP4)// 设置较低分辨率
                .setUri(videoUrl)
                .setMediaId(videoUrl);
        //字幕
        //setSubtitle(builder);
        MediaItem videoItem = builder.build();
        MediaSource mediaSource;
        switch (type) {
            case "m3u8":
                // hls链接
                DataSource.Factory factory = new DefaultDataSource.Factory(context);
                mediaSource = new HlsMediaSource.Factory(factory)
                        .createMediaSource(videoItem);
                break;
            case "rtsp":
                // rtsp链接
                mediaSource = new RtspMediaSource.Factory()
                        .createMediaSource(videoItem);
                break;
            case "rtmp":
                // rtmp链接
                mediaSource = new ProgressiveMediaSource
                        .Factory(new RtmpDataSource.Factory())
                        .createMediaSource(videoItem);
                break;
            default:
                //ProgressiveMediaSource 处理数据源 并异步加载

                DrmSessionManager drmSessionManager = DrmSessionManager.DRM_UNSUPPORTED;
                // 其他链接（http开头或https开头的普通视频链接）
                if (!isUseCache) {
                    //默认的  无缓存
                    DataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(context);
                    mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                            .setDrmSessionManagerProvider(unusedMediaItem -> drmSessionManager)
                            .createMediaSource(videoItem);
                } else {
                    //构建缓存
                    simpleCache = createSimpleCache();
                    CacheDataSource.Factory dataSourceFactory = new CacheDataSource.Factory()
                            .setCache(simpleCache)
                            .setUpstreamDataSourceFactory(new DefaultDataSource.Factory(context))
                            //缓存出错时自动回退到原始源
                            .setFlags(CacheDataSource.FLAG_BLOCK_ON_CACHE | CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR);
                    //
                    mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                            .setDrmSessionManagerProvider(unusedMediaItem -> drmSessionManager)
                            .createMediaSource(videoItem);
                }
                break;
        }
        return mediaSource;
    }

    //设置字幕
    private void setSubtitle(MediaItem.Builder builder) {
        //builder.setSubtitleConfigurations();
    }

    protected long maxBytes = 100 * 1024 * 1024;//100M
    @UnstableApi
    protected SimpleCache simpleCache;
    @UnstableApi
    protected File simpleCacheFile;

    //构建缓存
    @OptIn(markerClass = UnstableApi.class)
    private SimpleCache createSimpleCache() {
        //有缓存的
        simpleCacheFile = getCacheFile();
        SimpleCache simpleCache;
        if (maxBytes == 0) {
            simpleCache = new SimpleCache(simpleCacheFile, new NoOpCacheEvictor(), new StandaloneDatabaseProvider(context));
        } else {
            simpleCache = new SimpleCache(simpleCacheFile, new LeastRecentlyUsedCacheEvictor(maxBytes), new StandaloneDatabaseProvider(context));
        }
        return simpleCache;
    }

    //获取缓存文件
    private File getCacheFile() {
        List<VideoEntity> datas = MediaRoom.geVideoDb(context).queryVideoCache(videoUrl);
        int cacheSize = 0;
        File videoFile = null;
        if (datas != null && datas.size() > 0) {
            File optFile = null;
            cacheSize = datas.size();
            long time = System.currentTimeMillis();
            for (int i = 0; i < datas.size(); i++) {
                VideoEntity entity = datas.get(i);
                long videoCacheTime = entity.videoCacheTime;
                /*if ((time - videoCacheTime) < 10 * 1000) {
                    //10 秒以内 不使用这个缓存
                    continue;
                }*/
                if (entity.videoCacheType == 1) {
                    continue;
                }
                File cacheFile = new File(entity.videoCachePath);
                if (optFile == null) {
                    optFile = cacheFile;
                    continue;
                }
                if (!optFile.isFile() && cacheFile.isFile()) {
                    optFile = cacheFile;
                    continue;
                }
                if (optFile.isFile() && cacheFile.isFile() && cacheFile.length() > optFile.length()) {
                    optFile = cacheFile;
                }
            }
            videoFile = optFile;

        }
        if (videoFile == null) {
            videoFile = createFile(cacheSize);
            VideoEntity videoEntity = new VideoEntity();
            videoEntity.videoUrl = videoUrl;
            videoEntity.videoCachePath = videoFile.getPath();
            videoEntity.videoCacheType = 1;
            long time = new Date().getTime();
            videoEntity.recordTime = time;
            MediaRoom.geVideoDb(context).put(videoEntity);
            PlayerLog.d(tag + "缓存地址", "新建 url:" + videoUrl + "\npath:" + videoFile.getPath());
        } else {
            MediaRoom.geVideoDb(context).setCacheFileUse(videoFile.getPath());
            PlayerLog.d(tag + "缓存地址", "取得 url:" + videoUrl + "\npath:" + videoFile.getPath());
        }

        return videoFile;
    }

    private File createFile(int index) {
        Random random = new Random();
        int randomNumber = random.nextInt(9999999);
        String md5 = Md5Media.encode(videoUrl);
        String fileName = md5 + "_" + index + "_" + randomNumber;
        String dir = FileUtil.getVideoCacheDir(context);
        File file = new File(dir, fileName);
        return file;
    }

    //释放缓存
    @OptIn(markerClass = UnstableApi.class)
    private void setCacheRelease() {
        if (TextUtils.isEmpty(videoUrl)) {
            return;
        }
        if (simpleCache != null) {
            simpleCache.release();
            simpleCache = null;
        }
        if (simpleCacheFile != null) {
            MediaRoom.geVideoDb(context).setCacheFileRelease(simpleCacheFile.getPath());
            simpleCacheFile = null;
        }
        PlayerLog.d(tag, "播放器 释放缓存：" + videoUrl);
    }

    //释放全部资源
    public void release() {
        if (cachePlayerView != null) {
            cachePlayerView.setPlayer(null);
            cachePlayerView = null;
        }
        if (player != null) {
            isInit = false;
            player.release();
            player = null;
        }
        setCacheRelease();
        PlayerLog.d(tag, "播放器 释放全部资源：" + videoUrl);
    }

    //清理  缓存的视图
    public void setPlayerViewRelease() {
        if (cachePlayerView != null) {
            cachePlayerView.setPlayer(null);
            cachePlayerView = null;
        }
    }

    //读取信息 test
    @OptIn(markerClass = UnstableApi.class)
    public void readData() {
        if (player == null) {
            return;
        }
        MediaItem mediaItem = player.getCurrentMediaItem();
        String mediaId = "";
        if (mediaItem != null) {
            mediaId = mediaItem.mediaId;
            MediaMetadata mediaMetadata = mediaItem.mediaMetadata;
            String str = "";
            if (mediaMetadata != null) {

                str = mediaMetadata.title + "-" + mediaMetadata.albumTitle + "-" +
                        mediaMetadata.subtitle + "-" + mediaMetadata.description + "-"
                        + mediaMetadata.artworkUri;
            }
            PlayerLog.d("视频播放Url", "当前播放地址 mediaId："
                    + mediaId + " str:" + str);
        }

    }

    //=====================更新数据库数据==============================================
    public void onUpdateVideoHis() {
        if (player != null) {
            long currentPosition = player.getCurrentPosition();
            long duration = player.getDuration();
            long time = new Date().getTime();
            MediaRoom.geVideoDb(context).updateVideoLookHis(videoUrl, currentPosition, duration, time);
        }
    }

    //
    private final String tag = "播放器_CustomExoPlayer_";
    //true 发生错误
    private boolean isError;

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
                MediaRoom.geVideoDb(context).updateVideoSize(videoUrl, videoSize.width, videoSize.height);
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
            PlayerLog.d(tag, "下载速度:  " + speedKbps + "kbps");

        }

        @Override
        public void onLoadStarted(EventTime eventTime, LoadEventInfo loadEventInfo, MediaLoadData mediaLoadData, int retryCount) {
            //long bufferedPosition = player.getBufferedPosition();
            PlayerLog.d(tag, "缓冲:  ----");
        }

        @Override
        public void onBandwidthEstimate(EventTime eventTime, int totalLoadTimeMs, long totalBytesLoaded, long bitrateEstimate) {
            AnalyticsListener.super.onBandwidthEstimate(eventTime, totalLoadTimeMs, totalBytesLoaded, bitrateEstimate);
        }

        @Override
        public void onPlaybackStateChanged(EventTime eventTime, int state) {
            // PlayerLog.d(tag, "playbackState： " + state + "kbps");
        }

    }

}
