package media.library.player.view;


import android.content.Context;
import android.text.TextUtils;
import android.view.Surface;
import android.view.SurfaceView;

import androidx.annotation.OptIn;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.VideoSize;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.database.StandaloneDatabaseProvider;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.datasource.cache.CacheDataSource;
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor;
import androidx.media3.datasource.cache.NoOpCacheEvictor;
import androidx.media3.datasource.cache.SimpleCache;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.drm.DrmSessionManager;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
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

    public void initExoPlayer(Context context) {
        isInit = true;
        if (player != null && playerContext != null && playerContext != context) {
            player.release();
            player = null;
            PlayerLog.d(tag, "播放器 重新构建 播放地址：" + videoUrl);
        }
        if (player == null) {
            player = new ExoPlayer.Builder(context).build();
            playerContext = context;
            player.addListener(new ExoPlayerListener());
            setVideoSurface(surface);
            addListener(listener);
        }
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
        if (player.getPlaybackState() == Player.STATE_READY && !isPlayReady) {
            return true;
        }
        return false;
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
        player.removeListener(listener);
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
        player.setMediaSource(mediaSource);
        //方式二
        /*MediaItem videoItem = new MediaItem.Builder().setUri(videoUrl).setMediaId(videoUrl).build();
        player.setMediaItem(videoItem);*/
    }

    @UnstableApi
    private MediaSource getMediaSource() {
        DrmSessionManager drmSessionManager = DrmSessionManager.DRM_UNSUPPORTED;
        MediaItem videoItem = new MediaItem.Builder().setUri(videoUrl).setMediaId(videoUrl).build();
        MediaSource mediaSource;
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

                    .setFlags(CacheDataSource.FLAG_BLOCK_ON_CACHE | CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR);//缓存出错时自动回退到原始源
            // 高级缓存策略
            //val cacheDataSourceFactory = CacheDataSource.Factory()
            //    .setCache(cache)
            //    .setUpstreamDataSourceFactory(httpDataSourceFactory)
            //    .setCacheWriteDataSinkFactory(
            //        CacheDataSink.Factory()
            //            .setFragmentSize(8 * 1024 * 1024) // 8MB分片
            //    )
            //    .setFlags(CacheDataSource.FLAG_BLOCK_ON_CACHE or CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
            mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                    .setDrmSessionManagerProvider(unusedMediaItem -> drmSessionManager)
                    .createMediaSource(videoItem);
        }
        return mediaSource;
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
            simpleCache = new SimpleCache(simpleCacheFile, new NoOpCacheEvictor(),
                    new StandaloneDatabaseProvider(context));
        } else {
            simpleCache = new SimpleCache(simpleCacheFile,
                    new LeastRecentlyUsedCacheEvictor(maxBytes),
                    new StandaloneDatabaseProvider(context));

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
    private String tag = "播放器_CustomExoPlayer_";

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
            //Unexpected runtime error code:1004 codeName:ERROR_CODE_FAILED_RUNTIME_CHECK
            PlayerLog.d(tag, "发生错误：" + error.getMessage() + " code:" + error.errorCode + " codeName:" + error.getErrorCodeName());
            switch (error.errorCode) {
                case PlaybackException.ERROR_CODE_FAILED_RUNTIME_CHECK:
                    //player.release();
                    //player.set
                    //player.prepare();
                    break;
            }
        }

    }
}
