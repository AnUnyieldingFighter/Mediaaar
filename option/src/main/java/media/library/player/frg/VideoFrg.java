package media.library.player.frg;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;

import com.images.imageselect.R;

import java.io.File;
import java.util.List;

import media.library.db.MediaRoom;
import media.library.images.config.entity.MediaEntity;
import media.library.player.able.OnVideoOperate;
import media.library.player.bean.VideoEntity;
import media.library.player.manager.PlayerLog;
import media.library.utils.FileUtil;
import media.library.utils.Md5Media;

public class VideoFrg extends VideoBaseFrg {
    @Override
    protected int getRootViewLayout() {
        return R.layout.frg_video_play;
    }

    @Override
    public ExoPlayer getExoPlayer() {
        return exoPlayer;
    }

    protected ExoPlayer exoPlayer;
    protected PlayerView playerView;
    protected TextView tvIndex;

    protected OnVideoOperate videoOperate = null;
    protected int pageIndex = -1;
    private String videoUrl;
    //true 横屏
    protected Boolean isVideoHorizontalScreen = false;

    //step 1
    @Override
    protected void setViewInit(View view, Bundle savedInstanceState) {
        viewInit(view, savedInstanceState);
    }

    private void viewInit(View view, Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        tvIndex = view.findViewById(R.id.tv_index);
        int tempPageIndex = bundle.getInt("index", -1);
        String tempVideoUrl = bundle.getString("url", "");
        PlayerLog.d("视频播放", "初始化时设置播放地址 pageIndex：" + pageIndex + " videoUrl:" + videoUrl);
        PlayerView tempPlayerView = view.findViewById(R.id.player_view);
        //
        initData(tempPageIndex, tempVideoUrl);
        initPlayerView(tempPlayerView);

    }

    public int getPageIndex() {
        return pageIndex;
    }

    public PlayerView getPlayerView() {
        return playerView;
    }

    //step 2
    protected void initData(int pageIndex, String videoUrl) {
        this.pageIndex = pageIndex;
        this.videoUrl = videoUrl;
    }

    //step 3
    protected void initPlayerView(PlayerView playerView) {
        this.playerView = playerView;
        //setPlayerViewParameter();
        if (act instanceof OnVideoOperate) {
            videoOperate = (OnVideoOperate) act;
        }
        PlayerLog.d("初始化", "------- index=" + pageIndex);
        setClick();
        initExoPlayer();
    }

    @OptIn(markerClass = UnstableApi.class)
    private void setPlayerViewParameter() {
        //禁止控制器在用户触摸时自动隐藏
        playerView.setControllerAutoShow(false);
        // 强制启用控制器
        playerView.setUseController(true);
    }

    protected void setClick() {
        playerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //暂停和播放
                setClickPauseOrPlay();
            }
        });
    }

    //暂停和播放
    @OptIn(markerClass = UnstableApi.class)
    protected void setClickPauseOrPlay() {
        Player temp = playerView.getPlayer();
        boolean isUse = playerView.getUseController();
        if (isUse) {
            //控制器可用
            return;
        }
        if (temp != null) {
            boolean playWhenReady = temp.getPlayWhenReady();
            temp.setPlayWhenReady(!playWhenReady);
        }
    }

    //调整视频比例 没有用到
    @OptIn(markerClass = UnstableApi.class)
    protected void adjustVideoAspectRatio() {
        playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
        // 根据视频的宽高比调整布局
        playerView.post(() -> {
            int width = playerView.getWidth();
            int height = playerView.getHeight();
            float videoAspectRatio = 16f / 9f; // 假设视频宽高比为16:9

            ViewGroup.LayoutParams layoutParams = playerView.getLayoutParams();
            layoutParams.height = (int) (width / videoAspectRatio);
            playerView.setLayoutParams(layoutParams);
        });
    }

    //更新播放源
    public void updateVideoUrl(int pageIndex, String url) {
        PlayerLog.d("视频播放Url", "计划播放 pageIndex：" + pageIndex + " url:" + url);
        if (playerView == null) {
            PlayerLog.d("未初始化", "------- index=" + pageIndex + "  " + this.pageIndex);
            return;
        }
        Player temp = playerView.getPlayer();
        if (temp != null) {
            MediaItem mediaItem = temp.getCurrentMediaItem();
            String mediaId = "";
            if (mediaItem != null) {
                mediaId = mediaItem.mediaId;
                MediaItem.RequestMetadata requestMetadata = mediaItem.requestMetadata;
                Uri mediaUri = requestMetadata.mediaUri;
                PlayerLog.d("视频播放Url", "计划播放地址 mediaId：" + mediaId + " mediaUri:" + mediaUri + " pageIndex:" + pageIndex);
            }
            if (url.equals(videoUrl) && pageIndex == this.pageIndex && url.equals(mediaId)) {
                PlayerLog.d("视频播放Url", "计划播放地址相同");
                return;
            }
        }

        this.videoUrl = url;
        //更换 播放url
        PlayerLog.d("更换新", "------- index=" + this.pageIndex + " 更新 " + pageIndex);
        this.pageIndex = pageIndex;
        initExoPlayer();
    }

    protected long maxBytes = 100 * 1024 * 1024;//100M
    @UnstableApi
    protected SimpleCache simpleCache;

    //构建缓存
    @OptIn(markerClass = UnstableApi.class)
    protected SimpleCache createCache(File file) {
        setCacheRelease();
        SimpleCache cache = null;
        if (maxBytes == 0) {
            cache = new SimpleCache(file, new NoOpCacheEvictor(), new StandaloneDatabaseProvider(getContext()));
        } else {
            cache = new SimpleCache(file, new LeastRecentlyUsedCacheEvictor(maxBytes), new StandaloneDatabaseProvider(getContext()));
        }
        return cache;
    }

    //释放缓存
    @OptIn(markerClass = UnstableApi.class)
    protected void setCacheRelease() {
        if (simpleCache != null) {
            simpleCache.release();
        }
        simpleCache = null;
    }

    //获取缓存文件
    protected File getCacheFile(int pageIndex, String videoUrl) {
        File file = null;
        List<VideoEntity> datas = MediaRoom.geVideoDb(getContext())
                .queryVideoCache(videoUrl);
        if (datas == null || datas.size() == 0) {
            String dir = FileUtil.getVideoCacheDir(getContext());
            String md5 = Md5Media.encode(videoUrl);
            file = new File(dir, md5);
            VideoEntity videoEntity = new VideoEntity();
            videoEntity.videoUrl = videoUrl;
            videoEntity.videoCachePath = file.getPath();
            MediaRoom.geVideoDb(getContext()).put(videoEntity);
            PlayerLog.d("缓存地址", "新建 url:" + videoUrl + "\npath:" + file.getPath());
        } else {
            VideoEntity bean = datas.get(0);
            file = new File(bean.videoCachePath);
            PlayerLog.d("缓存地址", "取得 url:" + videoUrl + "\npath:" + file.getPath());
        }
        return file;
    }

    @OptIn(markerClass = UnstableApi.class)
    protected MediaSource getMediaSource(boolean isDef) {
        DrmSessionManager drmSessionManager = DrmSessionManager.DRM_UNSUPPORTED;
        MediaItem videoItem = new MediaItem.Builder()
                .setUri(videoUrl)
                .setMediaId(videoUrl).build();
        MediaSource mediaSource;
        if (isDef) {
            //默认的
            DataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(getContext());
            mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                    .setDrmSessionManagerProvider(unusedMediaItem -> drmSessionManager)
                    .createMediaSource(videoItem);
        } else {
            //有缓存的
            File file = getCacheFile(pageIndex, videoUrl);
            //构建缓存
            simpleCache = createCache(file);
            DataSource.Factory dataSourceFactory = new CacheDataSource.Factory().setCache(simpleCache)
                    .setUpstreamDataSourceFactory(new DefaultDataSource.Factory(getContext()));

            mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                    .setDrmSessionManagerProvider(unusedMediaItem -> drmSessionManager)
                    .createMediaSource(videoItem);
        }
        return mediaSource;
    }

    @OptIn(markerClass = UnstableApi.class)
    protected void initExoPlayer() {
        isVideoHorizontalScreen = false;
        //
        exoPlayer = videoOperate.getExoPlayer(pageIndex, videoUrl);
        //方式一
        MediaSource mediaSource = getMediaSource(false);
        exoPlayer.setMediaSource(mediaSource);
        //方式二
        /*MediaItem videoItem = new MediaItem.Builder().setUri(videoUrl).setMediaId(videoUrl).build();
        exoPlayer.setMediaItem(videoItem);*/
        //
        playerView.setPlayer(exoPlayer);
        exoPlayer.prepare();
        exoPlayer.setPlayWhenReady(false);
        //设置循环播放
        exoPlayer.setRepeatMode(Player.REPEAT_MODE_ALL);
        //立即显示控制器（需在setPlayer()之后调用）
        playerView.showController();
        onNewPlay();
    }

    //一个新的播放
    protected void onNewPlay() {
    }

    //设置播放
    public void setVideoPlay() {
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(true);
            //player.play();
        }

    }

    //设置暂停
    public void setVideoPause() {
        if (exoPlayer != null) {
            if (exoPlayer.isPlaying()) {
                exoPlayer.pause();
            } else {
                exoPlayer.setPlayWhenReady(false);
            }

        }
    }

    //释放播放器
    public void setExoPlayerRelease() {
        if (playerView != null) {
            playerView.setPlayer(null);
        }
        setCacheRelease();
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePlay();
        if (exoPlayer != null) {
            exoPlayer.addListener(playerListener);
        }
        setVideoPlay();
        PlayerLog.d("frg_", "onResume " + pageIndex);

    }

    //更新播放器 和视频大小
    private void updatePlay() {
        if (videoOperate == null) {
            return;
        }
        int tempIndex = videoOperate.getIndex();
        setShowIndex(tempIndex);
        pageIndex = tempIndex;
        if (playerView == null) {
            return;
        }
        //重新设置播放器
        //playerView.setPlayer(null);
        //playerView.setPlayer(exoPlayer);
        //
        var tempPlay = playerView.getPlayer();
        if (tempPlay == null) {
            //清理这个播放器 出窝以外的持有者
            videoOperate.setClearFrgHoldPlayer(exoPlayer);
            playerView.setPlayer(exoPlayer);
            onVideoSize(exoPlayer);
        } else {
            onVideoSize(tempPlay);
        }
        ExoPlayer temp = exoPlayer;
        int height = playerView.getHeight();
        int width = playerView.getWidth();
        videoOperate.onCheck(pageIndex, exoPlayer);
        PlayerLog.d("播放器", "temp==null " + (temp == null) + " 相同:" + (temp == tempPlay) + " height:" + height + " width:" + width);
        int playbackState = temp.getPlaybackState();
        PlayerLog.d("视频播放", "正在播放：pageIndex" + pageIndex + " playbackState:" + playbackState + " url:" + videoUrl);

    }

    private void setShowIndex(int tempIndex) {
        if (tvIndex != null) {
            var tempPlay = playerView.getPlayer();
            tvIndex.setText("页面：" + tempIndex + "  内置：" + pageIndex + "\nplayer==null:" + (tempPlay == null) + "\n" + videoUrl);
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        setVideoPause();
        if (exoPlayer != null) {
            exoPlayer.removeListener(playerListener);
        }
        PlayerLog.d("frg_", "onPause  " + pageIndex);
    }

    @Override
    public void onStop() {
        super.onStop();
        PlayerLog.d("frg_", "onStop " + pageIndex);
    }

    @Override
    public void onDestroy() {
        setExoPlayerRelease();
        super.onDestroy();
        PlayerLog.d("frg_", "onDestroy " + pageIndex);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //视图不可见  Fragment实例还在
        PlayerLog.d("frg_", "onDestroyView " + pageIndex);
    }

    //设置进度 毫秒
    public void setSeek(long positionMs) {
        if (playerView == null) {
            return;
        }
        Player temp = playerView.getPlayer();
        if (temp == null) {
            return;
        }
        temp.seekTo(positionMs);
    }

    //更新播放进度
    public void setUpdatePlayProgress() {
        Player temp = playerView.getPlayer();
        if (temp == null) {
            return;
        }
        int playbackState = temp.getPlaybackState();
        PlayerLog.d("播放进度", "当前播放进度   playbackState=" + playbackState);
        if (playbackState == Player.STATE_READY) {
            updatePlaybackStatus(false);
        }
    }

    //计算播放进度 isPlayEnd true:播放结束
    private void updatePlaybackStatus(boolean isPlayEnd) {
        long currentPosition = exoPlayer.getCurrentPosition();
        long duration = exoPlayer.getDuration();
        if (isPlayEnd) {
            currentPosition = duration;
        }
        PlayerLog.d("播放进度", "当前播放进度   " + currentPosition + " / " + duration);
        onVideoProgress(currentPosition, duration);
    }

    //视频播放进度
    public void onVideoProgress(long currentPosition, long duration) {

    }

    //视频大小回调
    public void onVideoSize(VideoSize videoSize) {
        int width = videoSize.width;
        int height = videoSize.height;
        if (width > height) {
            isVideoHorizontalScreen = true;
        } else {
            isVideoHorizontalScreen = false;
        }
    }

    //视频大小
    protected void onVideoSize(Player player) {
        VideoSize video = player.getVideoSize();
        if (video != null) {
            onVideoSize(video);
        }
    }

    //返回视频实际宽高
    protected int[] getVideoWH(VideoSize videoSize) {
        // 视频实际宽高（可能包含旋转补偿）
        int width = videoSize.width;
        int height = videoSize.height;
        // 像素宽高比（通常为1.0，用于非方形像素的情况）
        var pixelWidthHeightRatio = videoSize.pixelWidthHeightRatio;
        // 未应用的旋转角度（0/90/180/270）
        var unappliedRotationDegrees = videoSize.unappliedRotationDegrees;
        // 计算最终显示尺寸（考虑旋转和像素比例）
        int finalWidth = 0;
        int finalHeight = 0;
        if (unappliedRotationDegrees % 180 == 0) {
            float temp = (width * pixelWidthHeightRatio);
            finalWidth = (int) temp;
            finalHeight = height;
        } else {
            float temp = height * pixelWidthHeightRatio;
            finalWidth = (int) temp;
            finalHeight = width;
        }

        int[] res = new int[]{finalWidth, finalHeight};
        return res;
    }

    //视频回调播放状态
    protected void onIsPlaying(boolean isPlay, int state) {

    }

    //准备就绪
    protected void onPlayReady() {

    }

    @OptIn(markerClass = UnstableApi.class)
    protected void onPlayError(PlaybackException error) {
        playerView.setCustomErrorMessage(error.getMessage());
    }

    private PlayerListener playerListener = new PlayerListener();

    class PlayerListener implements Player.Listener {

        @Override
        public void onPlaybackStateChanged(int playbackState) {
            Player.Listener.super.onPlaybackStateChanged(playbackState);
            switch (playbackState) {
                case Player.STATE_IDLE:
                    //即播放器停止和播放失败时的状态。
                    PlayerLog.d("视频播放", "播放状态 空闲 state=" + playbackState);
                    break;
                case Player.STATE_BUFFERING:
                    PlayerLog.d("视频播放", "播放状态 正在缓冲 state=" + playbackState);
                    break;
                case Player.STATE_READY:
                    PlayerLog.d("视频播放", "播放状态 准备就绪，可以播放 state=" + playbackState);
                    onPlayReady();
                    updatePlaybackStatus(false);
                    break;
                case Player.STATE_ENDED:
                    updatePlaybackStatus(true);
                    PlayerLog.d("视频播放", "播放状态 播放结束 state=" + playbackState);
                    break;
                default:
                    PlayerLog.d("视频播放", "播放状态改变时调用 state=" + playbackState);
                    break;
            }
        }


        @Override
        public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
            Player.Listener.super.onPlayWhenReadyChanged(playWhenReady, reason);
            PlayerLog.d("视频播放", "接收播放意图  playWhenReady=" + playWhenReady + "  reason=" + reason);
        }

        @Override
        public void onPositionDiscontinuity(Player.PositionInfo oldPosition, Player.PositionInfo newPosition, int reason) {
            Player.Listener.super.onPositionDiscontinuity(oldPosition, newPosition, reason);
            // 位置跳跃，例如快进或快退
            switch (reason) {
                case Player.DISCONTINUITY_REASON_AUTO_TRANSITION:
                    // 循环播放
                    updatePlaybackStatus(true);
                    break;
                case Player.DISCONTINUITY_REASON_SEEK:
                    // 用户进行了快进或快退操作
                    break;
                case Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT:
                    // 由于缓冲或其他原因，播放位置被自动调整
                    break;
                // 其他原因...
            }
            PlayerLog.d("视频播放", "位置跳跃，例如快进或快退 reason" + reason);
            updatePlaybackStatus(false);
        }

        @Override
        public void onIsPlayingChanged(boolean isPlaying) {
            PlayerLog.d("视频播放", "播放中/播放停止   播放中：" + isPlaying);
            Player temp = playerView.getPlayer();
            int state = -1;
            if (temp != null) {
                state = temp.getPlaybackState();
            }
            onIsPlaying(isPlaying, state);
        }

        @Override
        public void onVideoSizeChanged(VideoSize videoSize) {
            PlayerLog.d("视频播放", "视频大小  width：" + videoSize.width + " height:" + videoSize.height);
            onVideoSize(videoSize);
        }

        @OptIn(markerClass = UnstableApi.class)
        @Override
        public void onPlayerError(PlaybackException error) {
            //Source error code:2007 codeName:ERROR_CODE_IO_CLEARTEXT_NOT_PERMITTED  不允许明文传输
            //发生错误：Source error code:2002 codeName:ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT 网络连接失败
            PlayerLog.d("视频播放", "发生错误：" + error.getMessage() + " code:" + error.errorCode + " codeName:" + error.getErrorCodeName());
            onPlayError(error);

        }

    }

    public static VideoFrg newInstance(int index, String url) {
        VideoFrg frg = new VideoFrg();
        Bundle bundle = new Bundle();
        bundle.putInt("index", index);
        bundle.putString("url", url);
        frg.setArguments(bundle);
        return frg;
    }


}
