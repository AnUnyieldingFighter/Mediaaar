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
        setPlayerVideo(context, videoUrl, "", isCache);
    }

    /**
     * @param videoUrl 播放器url
     * @param isCache  true 可以缓存
     */
    @UnstableApi
    public void setPlayerVideo(Context context, String videoUrl, String videoTag, boolean isCache) {
        isReady = false;
        //
        this.isUseCache = isCache;
        this.videoUrl = videoUrl;
        this.videoTag = videoTag;
        //
        initExoPlayer(context);
        //方式一 自己决定 URL 用什么 MediaSource 播
        MediaSource mediaSource = getMediaSource();
        player.setMediaSource(mediaSource);
        //方式二 是把 MediaItem 直接交给 ExoPlayer，让它自己根据 URI / MIME 猜怎么播。
        //好处是简单 普通 mp4/m3u8 通常能播  适合不需要特殊控制的场景
        //不好插入你自己的缓存逻辑
        //不好针对直播关闭缓存
        //不好针对不同协议做不同处理
        //rtsp/flv/特殊 URL 可能识别不稳定
       /* MediaItem videoItem = new MediaItem.Builder()
                .setUri(videoUrl)
                .setMediaId(videoUrl).build();
        player.setMediaItem(videoItem);*/
    }

    //===================================测试中的用法=============================
    public void testTrackSelector() {
        if (customTrackSelector != null) {
            customTrackSelector.setTrackLog();
            customTrackSelector.setPreferredAudioLanguage("zh");
            //
            customTrackSelector.disableTrackType(C.TRACK_TYPE_VIDEO);// 禁用视频（纯音频播放）
            customTrackSelector.disableTrackType(C.TRACK_TYPE_AUDIO);// 禁用音频（静音播放）
            customTrackSelector.disableTrackType(C.TRACK_TYPE_TEXT);// 禁用字幕
        }
    }


    public void setPreferredTextTrackTest() {
        if (customTrackSelector != null) {
            //显示字幕，但不指定语言，由播放器按默认规则选择字幕轨道
            customTrackSelector.setPreferredTextTrack(true, null);
        }
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
        PlayerLog.d(tag, "播放器 释放全部资源：videoTag=" + videoTag + "\nurl" + videoUrl);
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
