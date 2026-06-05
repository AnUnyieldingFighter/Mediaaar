package media.library.player.view;


import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.OptIn;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.PlaybackParameters;
import androidx.media3.common.Player;
import androidx.media3.common.TrackSelectionParameters;
import androidx.media3.common.VideoSize;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.database.StandaloneDatabaseProvider;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.datasource.cache.CacheDataSource;
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor;
import androidx.media3.datasource.cache.NoOpCacheEvictor;
import androidx.media3.datasource.cache.SimpleCache;
import androidx.media3.exoplayer.SeekParameters;
import androidx.media3.exoplayer.drm.DrmSessionManager;
import androidx.media3.exoplayer.hls.HlsMediaSource;
import androidx.media3.exoplayer.rtsp.RtspMediaSource;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.exoplayer.source.TrackGroupArray;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.exoplayer.trackselection.MappingTrackSelector;
import androidx.media3.ui.PlayerControlView;
import androidx.media3.ui.PlayerView;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

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
public class CustomExoPlayer extends BaseMediaSource {
    public CustomExoPlayer() {

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
        isReady = false;
        //
        this.isUseCache = isCache;
        this.videoUrl = videoUrl;
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


    //===================================测试中的用法=============================
    public void testTrackSelector() {
        testTrackLog();
        setPreferredAudioLanguage("zh");
        //setMaxVideoResolution(100, 100);
        //
        disableTrackType(C.TRACK_TYPE_VIDEO);// 禁用视频（纯音频播放）
        disableTrackType(C.TRACK_TYPE_AUDIO);// 禁用音频（静音播放）
        disableTrackType(C.TRACK_TYPE_TEXT);// 禁用字幕
    }

    @OptIn(markerClass = UnstableApi.class)
    public void testTrackLog() {
        DefaultTrackSelector.Parameters params = trackSelector.getParameters();
        MappingTrackSelector.MappedTrackInfo infos = trackSelector.getCurrentMappedTrackInfo();
        if (infos == null) {
            return;
        }
        TrackGroupArray track = infos.getTrackGroups(0);
        int length = track.length;
        String str = params.toString();
        PlayerLog.d(tag, "音频信息" + str);
    }

    // 主线程执行
    @OptIn(markerClass = UnstableApi.class)
    public void setPreferredAudioLanguage(String... language) {
        // 语言码：中文=zh, 英文=en, 日文=ja
        DefaultTrackSelector.Parameters.Builder paramsBuilder = trackSelector.buildUponParameters();
        if (language.length == 1) {
            // 启用音画同步（默认开启）
            paramsBuilder.setPreferredAudioLanguage(language[0]);
            paramsBuilder.setPreferredVideoLanguage(language[0]);
        } else {
            // 启用音画同步（默认开启）
            paramsBuilder.setPreferredAudioLanguages(language);
            paramsBuilder.setPreferredVideoLanguages(language);
        }
        DefaultTrackSelector.Parameters params = paramsBuilder.build();
        trackSelector.setParameters(params);
    }

    //设置视频分辨率限制（如最大 720p）setMaxVideoResolution(1280, 720)
    @OptIn(markerClass = UnstableApi.class)
    public void setMaxVideoResolution(int maxWidth, int maxHeight) {
        TrackSelectionParameters params = trackSelector.buildUponParameters().setMaxVideoSize(maxWidth, maxHeight)// 最大宽高：720p=1280x720
                // .setLimitVideoSizeToDeviceSize(true) // 可选：限制为设备屏幕分辨率（避免超屏）无此方法
                .build();
        trackSelector.setParameters(params);
    }

    //禁用视频 / 音频 / 字幕轨道
    @OptIn(markerClass = UnstableApi.class)
    public void disableTrackType(int trackType) {
        Set<Integer> set = new HashSet<>();
        set.add(trackType);
        TrackSelectionParameters params = trackSelector.buildUponParameters().setDisabledTrackTypes(set) // 禁用指定轨道类型
                .build();
        trackSelector.setParameters(params);
    }

    // 主线程执行 无此方法 setForceDisabledTrackTypes
    @OptIn(markerClass = UnstableApi.class)
    public void setPreferredTextTrack(boolean showText, String preferredLanguage) {
        Set<Integer> set = new HashSet<>();
        if (!showText) {
            set.add(C.TRACK_TYPE_TEXT);
        }
        /*TrackSelectionParameters params = trackSelector.buildUponParameters()
                .setPreferredTextLanguage(preferredLanguage) // 字幕优先语言
                .setForceDisabledTrackTypes(set)  // 是否显示字幕
                .build();
        trackSelector.setParameters(params);*/
    }

    @OptIn(markerClass = UnstableApi.class)
    public void setPreferredTextTrack() {
        // 自适应码率（ABR）配置（Media3 内置，无需额外 Factory）
        DefaultTrackSelector.Parameters.Builder params = trackSelector.buildUponParameters();
        /*params.setMinDurationForQualityDecreaseMs()
        params.setBandwidthFraction(0.8f); // 带宽利用率 80%
        params .setMinDurationForQualityIncreaseMs(8000); // 8秒后提升画质*/
    }
    //==================释放资源=======================================================
    //释放全部资源
    public void release() {
        if (cachePlayerView != null) {
            cachePlayerView.setPlayer(null);
            cachePlayerView = null;
        }
        if (player != null) {
            player.release();
            setPlayerBuffRelease();
            player = null;
        }
        setMediaSourceCacheRelease();
        PlayerLog.d(tag, "播放器 释放全部资源：" + videoUrl);
    }
    //====================设置播放器的显示==================================================
    private PlayerView cachePlayerView;

    @OptIn(markerClass = UnstableApi.class)
    public void setPlayerView(PlayerView playerView) {
        if (cachePlayerView == playerView) {
            return;
        }
        if (cachePlayerView != null) {
            //旧视图  清空播放器
            cachePlayerView.setPlayer(null);
        }
        /*if (cachePlayerControlView != null) {
            //旧控制器  清空播放器
            cachePlayerControlView.setPlayer(null);
        }*/
        //新视图  设置播放器
        playerView.setPlayer(player);
        this.cachePlayerView = playerView;
    }

    //清理  缓存的视图
    public void setPlayerViewRelease() {
        if (cachePlayerView != null) {
            cachePlayerView.setPlayer(null);
            cachePlayerView = null;
        }
    }

    //===================设置控制器=====================================================
    @OptIn(markerClass = UnstableApi.class)
    private PlayerControlView cachePlayerControlView;

    @OptIn(markerClass = UnstableApi.class)
    public void setPlayerControlView(PlayerControlView playerControlView) {
        if (cachePlayerControlView == playerControlView) {
            return;
        }
        /*if (cachePlayerView != null) {
            //旧视图  清空播放器
            cachePlayerView.setPlayer(null);
        }*/
        if (cachePlayerControlView != null) {
            //旧控制器  清空播放器
            cachePlayerControlView.setPlayer(null);
        }
        //新控制器  设置播放器
        playerControlView.setPlayer(player);
        cachePlayerControlView = playerControlView;
    }

    //========================================操作方法======================
    //准备
    public void prepare() {
        player.prepare();
    }

    @Deprecated
    public void pause() {
        player.pause();
    }

    @Deprecated
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
            //
            long currentPosition = player.getCurrentPosition();
            long duration = player.getDuration();
            dbUpdateVideoPro(playerContext, videoUrl, currentPosition, duration);
            //
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
        setLockedSpeed();
    }

    //设置播放进度
    public void seekTo(long positionMs) {
        player.seekTo(positionMs);
    }

    //比较播放的url是否相同
    public boolean isEqualVideoPlay(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        if (isError) {
            return false;
        }
        return url.equals(videoUrl);
    }

    //设置静音
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

    //当前播放进度
    public long getCurrentPosition() {
        return player.getCurrentPosition();
    }

    //总时长
    public long getDuration() {
        return player.getDuration();
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


    public void setRepeatMode(@Player.RepeatMode int repeatMode) {
        player.setRepeatMode(repeatMode);
    }

    public boolean isPlaying() {
        if (player == null) {
            return false;
        }
        return player.isPlaying();
    }

    //获取状态
    public int getPlaybackState() {
        return player.getPlaybackState();
    }

    //获取视频大小
    @OptIn(markerClass = UnstableApi.class)
    public VideoSize getVideoSize() {
        VideoSize videoSize = player.getVideoSize();
        int width = videoSize.width;
        int height = videoSize.height;
        if (width <= 0 && height <= 0) {
            List<VideoEntity> datas = getDBVideoSize(playerContext, videoUrl);
            if (datas != null && datas.size() > 0) {
                VideoEntity entity = datas.get(0);
                if (entity.videoWidth > 0 && entity.videoHeight > 0) {
                    videoSize = new VideoSize(entity.videoWidth, entity.videoHeight);
                }
            }
        }
        return videoSize;
    }

    //获取播放倍速
    public float getPlaybackSpeed() {
        PlaybackParameters parameters = getPlaybackParameters();
        if (parameters == null) {
            return 1.0f;
        }
        return parameters.speed;
    }

    //true 锁定播放速度
    private boolean isLockedSpeed;
    //锁定的速度
    private float lockedSpeed = -1;

    /**
     * 锁定播放速度
     *
     * @param isLockedSpeed true 锁定播放速度
     */
    public void setIsLockedSpeed(boolean isLockedSpeed) {
        this.isLockedSpeed = isLockedSpeed;
        setLockedSpeed();
    }

    //设置锁定速度
    private void setLockedSpeed() {
        if (isLockedSpeed) {
            float speedNow = getPlaybackSpeed();
            //锁定播放速度
            if (lockedSpeed == -1) {
                float speed = FileUtil.floatGet(playerContext, FileUtil.video_locked_speed);
                if (speed == -1) {
                    speed = 1;
                }
                lockedSpeed = speed;
            }
            float lookSpeed = lockedSpeed;
            if (lookSpeed == speedNow) {
                return;
            }
            setPlaybackSpeed(lookSpeed);
        }

    }

    //设置倍速 大于于0：1是正常速度，2是两倍速度，0.5是正常速度的一半。
    public void setPlaybackSpeed(float speed) {
        setPlaybackSpeed(speed, isLockedSpeed);
    }


    /**
     * @param speed         大于于0：1是正常速度，2是两倍速度，0.5是正常速度的一半。
     * @param isLockedSpeed true 锁定倍速
     */
    public void setPlaybackSpeed(float speed, boolean isLockedSpeed) {
        //锁定速度
        this.isLockedSpeed = isLockedSpeed;
        if (isLockedSpeed) {
            if (lockedSpeed != speed) {
                FileUtil.floatSave(playerContext, FileUtil.video_locked_speed, speed);
            }
            lockedSpeed = speed;
        }
        if (player == null) {
            this.videoSpeed = speed;
            return;
        }
        videoSpeed = null;
        player.setPlaybackSpeed(speed);
    }

}
