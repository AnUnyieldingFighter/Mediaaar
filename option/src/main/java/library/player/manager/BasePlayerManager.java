package library.player.manager;

import static androidx.media3.common.Player.EVENT_AVAILABLE_COMMANDS_CHANGED;
import static androidx.media3.common.Player.EVENT_IS_PLAYING_CHANGED;
import static androidx.media3.common.Player.EVENT_PLAYBACK_PARAMETERS_CHANGED;
import static androidx.media3.common.Player.EVENT_PLAYBACK_STATE_CHANGED;
import static androidx.media3.common.Player.EVENT_PLAY_WHEN_READY_CHANGED;
import static androidx.media3.common.Player.EVENT_POSITION_DISCONTINUITY;
import static androidx.media3.common.Player.EVENT_REPEAT_MODE_CHANGED;
import static androidx.media3.common.Player.EVENT_SEEK_BACK_INCREMENT_CHANGED;
import static androidx.media3.common.Player.EVENT_SEEK_FORWARD_INCREMENT_CHANGED;
import static androidx.media3.common.Player.EVENT_SHUFFLE_MODE_ENABLED_CHANGED;
import static androidx.media3.common.Player.EVENT_TIMELINE_CHANGED;
import static androidx.media3.common.Player.EVENT_TRACKS_CHANGED;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.view.Surface;
import android.view.SurfaceControl;
import android.view.SurfaceView;

import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.annotation.RequiresApi;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.DeviceInfo;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Metadata;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.PlaybackParameters;
import androidx.media3.common.Player;
import androidx.media3.common.Timeline;
import androidx.media3.common.TrackSelectionParameters;
import androidx.media3.common.Tracks;
import androidx.media3.common.VideoSize;
import androidx.media3.common.text.CueGroup;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.common.util.Util;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.datasource.DefaultDataSourceFactory;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.drm.DrmSessionManager;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.ui.PlayerControlView;
import androidx.media3.ui.PlayerView;


class BasePlayerManager {

    //true  强制调用other方法
    protected boolean isOther = true;
    protected ExoPlayer exoPlayer;//播放器
    @UnstableApi
    private PlayerListener playerListener;//播放器

    //step 1:初始化播放器
    @OptIn(markerClass = UnstableApi.class)
    public void initExoPlayers(Activity act) {
        if (exoPlayer != null) {
            return;
        }
        Application application = act.getApplication();
        ExoPlayer player = new ExoPlayer.Builder(application).build();
        exoPlayer = player;
        initSurface();
        exoPlayer.setVideoSurface(surface);
        playerListener = new PlayerListener();
        exoPlayer.addListener(playerListener);
    }

    //step 2:初始化画布
    private void initSurface() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            initSurface29();
        } else {
            initSurfaceOther();
        }

    }

    private SurfaceControl surfaceControl;
    //private SurfaceTexture surfaceTexture;
    protected Surface surface;
    private String name = "video_player";

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void initSurface29() {
        if (isOther) {
            initSurfaceOther();
            return;
        }
        if (surface != null) {
            return;
        }
        surfaceControl = new SurfaceControl.Builder().setName(name).setBufferSize(/* width= */ 0, /* height= */ 0).build();
        surface = new Surface(surfaceControl);

    }

    private void initSurfaceOther() {
        //不使用 surfaceControl ，没有必要创建
        /*if (surface != null) {
            return;
        }
        SurfaceTexture surfaceTexture = new SurfaceTexture(false);
        surface = new Surface(surfaceTexture);*/
    }

    //step 3:设置播放url
    public void setPlayUrl(Context context, String url) {
        Uri uri = Uri.parse(url);
        setPlayUri(context, uri);
    }

    @OptIn(markerClass = UnstableApi.class)
    public void setPlayUri(Context context, Uri uri) {
        if (exoPlayer == null) {
            return;
        }
        DrmSessionManager drmSessionManager = DrmSessionManager.DRM_UNSUPPORTED;
        DataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(context);
        MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).setDrmSessionManagerProvider(unusedMediaItem -> drmSessionManager).createMediaSource(MediaItem.fromUri(uri));
        exoPlayer.setMediaSource(mediaSource);
    }

    //step 3:设置播放url
    @OptIn(markerClass = UnstableApi.class)
    public void setPlayUrl2(Context context, String url) {
        MediaItem mediaItem = MediaItem.fromUri(url);
        MediaSource mediaSource = new ProgressiveMediaSource.Factory(new DefaultDataSourceFactory(context, Util.getUserAgent(context, context.getPackageName()))).createMediaSource(mediaItem);
        exoPlayer.setMediaSource(mediaSource);

    }

    //step 4:开始播放
    public void setPlay() {
        if (exoPlayer == null) {
            return;
        }
        exoPlayer.prepare();
        exoPlayer.play();
        exoPlayer.setRepeatMode(Player.REPEAT_MODE_ALL);
    }

    //step 4:开始播放
    public void setPlay(boolean isLoop) {
        if (exoPlayer == null) {
            return;
        }
        exoPlayer.prepare();
        //用于控制播放器的"准备就绪时自动播放"行为
        //true：当播放器准备好媒体资源（调用prepare()完成）后自动开始播放  自动播放 默认playWhenReady=true
        //false：即使播放器准备完成，也不会自动播放，需手动调用play()
        // exoPlayer.setPlayWhenReady(true);
        exoPlayer.play();
        // 循环播放
        if (isLoop) {
            exoPlayer.setRepeatMode(Player.REPEAT_MODE_ALL);
        }
    }

    //设置控制器
    @OptIn(markerClass = UnstableApi.class)
    public void setPlayerControlView(PlayerControlView playerControlView) {
        if (exoPlayer == null) {
            return;
        }
        playerControlView.setPlayer(exoPlayer);
    }

    //设置播放view（它集成了控制器，SurfaceView（默认）），可以跳过 step 2:初始化画布
    public void setPlayerControlView(PlayerView playerView) {
        if (exoPlayer == null) {
            return;
        }
        playerView.setPlayer(exoPlayer);

    }

    //设置暂停
    public void setPause() {
        if (exoPlayer == null) {
            return;
        }
        if (exoPlayer.isPlaying()) {
            exoPlayer.pause();
        } else {
            exoPlayer.setPlayWhenReady(false);
        }
    }

    //设置持续播放/继续播放
    public void setPlayContinue() {
        if (exoPlayer == null) {
            return;
        }
        // if(exoPlayer.isReleased()){}
        //exoPlayer.play();
        exoPlayer.setPlayWhenReady(true);
    }


    //切换画面
    protected void setReparentOther(SurfaceView surfaceView) {
        if (surfaceView == null) {
            exoPlayer.setVideoSurface(null);
        } else {
            //如果是类型切换 要先
            //exoPlayer.clearVideoSurface();
            //surface = surfaceView.getHolder().getSurface();
            //exoPlayer.setVideoSurface(surface);
            //
            //exoPlayer.setVideoTextureView();
            //直接替换
            exoPlayer.setVideoSurfaceView(surfaceView);
        }

    }

    //切换画面
    @RequiresApi(api = Build.VERSION_CODES.Q)
    protected void setReparent29(SurfaceView surfaceView) {
        if (isOther) {
            setReparentOther(surfaceView);
            return;
        }
        //无缝切换 画面
        SurfaceControl tempControl = surfaceControl;
        if (surfaceView == null) {
            SurfaceControl.Transaction transaction = new SurfaceControl.Transaction();
            transaction.reparent(tempControl, /* newParent= */ null);
            transaction.setBufferSize(tempControl, /* w= */ 0, /* h= */ 0);
            transaction.setVisibility(tempControl, /* visible= */ false);
            transaction.apply();

        } else {
            SurfaceControl newParentSurfaceControl = surfaceView.getSurfaceControl();
            SurfaceControl.Transaction transaction = new SurfaceControl.Transaction();
            transaction.reparent(tempControl, newParentSurfaceControl);
            transaction.setBufferSize(tempControl, surfaceView.getWidth(), surfaceView.getHeight());
            transaction.setVisibility(tempControl, /* visible= */ true).apply();
        }

    }

    //获取时长
    public Long[] getDuration() {
        if (exoPlayer == null) {
            return null;
        }
        Long[] times = new Long[2];
        long currentPosition = exoPlayer.getCurrentPosition();
        long duration = exoPlayer.getDuration();
        times[0] = currentPosition;
        times[1] = duration;
        log("当前播放位置：" + currentPosition + "ms，总时长：" + duration + "ms");
        return times;
    }

    //释放资源
    protected void setReleaseOther() {
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
        if (surface != null) {
            surface.release();
            surface = null;
        }
    }

    //释放资源
    @RequiresApi(api = Build.VERSION_CODES.Q)
    protected void setRelease29() {
        if (isOther) {
            setReleaseOther();
            return;
        }
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
        if (surfaceControl != null) {
            surfaceControl.release();
            surfaceControl = null;
        }
        if (surface != null) {
            surface.release();
            surface = null;
        }

    }

    private String tag = "EXO_播放";

    protected void log(String value) {
        PlayerLog.d(tag, value);
    }

    @UnstableApi
    class PlayerListener implements Player.Listener {
        @Override
        public void onEvents(Player player, Player.Events events) {
            Player.Listener.super.onEvents(player, events);
            if (events.containsAny(EVENT_PLAYBACK_STATE_CHANGED, EVENT_PLAY_WHEN_READY_CHANGED, EVENT_AVAILABLE_COMMANDS_CHANGED)) {

            }
            if (events.containsAny(EVENT_PLAYBACK_STATE_CHANGED, EVENT_PLAY_WHEN_READY_CHANGED, EVENT_IS_PLAYING_CHANGED, EVENT_AVAILABLE_COMMANDS_CHANGED)) {

            }
            if (events.containsAny(EVENT_REPEAT_MODE_CHANGED, EVENT_AVAILABLE_COMMANDS_CHANGED)) {

            }
            if (events.containsAny(EVENT_SHUFFLE_MODE_ENABLED_CHANGED, EVENT_AVAILABLE_COMMANDS_CHANGED)) {

            }
            if (events.containsAny(EVENT_REPEAT_MODE_CHANGED, EVENT_SHUFFLE_MODE_ENABLED_CHANGED, EVENT_POSITION_DISCONTINUITY, EVENT_TIMELINE_CHANGED, EVENT_SEEK_BACK_INCREMENT_CHANGED, EVENT_SEEK_FORWARD_INCREMENT_CHANGED, EVENT_AVAILABLE_COMMANDS_CHANGED)) {

            }
            if (events.containsAny(EVENT_POSITION_DISCONTINUITY, EVENT_TIMELINE_CHANGED, EVENT_AVAILABLE_COMMANDS_CHANGED)) {

            }
            if (events.containsAny(EVENT_PLAYBACK_PARAMETERS_CHANGED, EVENT_AVAILABLE_COMMANDS_CHANGED)) {

            }
            if (events.containsAny(EVENT_TRACKS_CHANGED, EVENT_AVAILABLE_COMMANDS_CHANGED)) {

            }
        }

        @Override
        public void onPlaybackStateChanged(int playbackState) {
            Player.Listener.super.onPlaybackStateChanged(playbackState);
            switch (playbackState) {
                case Player.STATE_IDLE:
                    //即播放器停止和播放失败时的状态。
                    log("播放状态 空闲 state=" + playbackState);
                    break;
                case Player.STATE_BUFFERING:
                    log("播放状态 正在缓冲 state=" + playbackState);
                    break;
                case Player.STATE_READY:
                    log("播放状态 准备就绪，可以播放 state=" + playbackState);
                    break;
                case Player.STATE_ENDED:
                    log("播放状态 播放结束 state=" + playbackState);
                    break;
                default:
                    log("播放状态改变时调用 state=" + playbackState);
                    break;
            }
        }

        @Override
        public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
            Player.Listener.super.onPlayWhenReadyChanged(playWhenReady, reason);
            log("接收播放意图  playWhenReady=" + playWhenReady + "  reason=" + reason);
        }

        //过时 拆分为onPlaybackStateChanged 和 onPlayWhenReadyChanged
        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            Player.Listener.super.onPlayerStateChanged(playWhenReady, playbackState);
        }


        @Override
        public void onPlayerError(PlaybackException error) {
            Player.Listener.super.onPlayerError(error);
            log("播放错误：" + error.errorCode + " " + error.getErrorCodeName() + " \n" + error.getMessage());
        }

        @Override
        public void onPlayerErrorChanged(@Nullable PlaybackException error) {
            Player.Listener.super.onPlayerErrorChanged(error);
            log("播放错误2：" + error.errorCode + " " + error.getErrorCodeName() + " \n" + error.getMessage());
        }

        @Override
        public void onPositionDiscontinuity(Player.PositionInfo oldPosition, Player.PositionInfo newPosition, int reason) {
            Player.Listener.super.onPositionDiscontinuity(oldPosition, newPosition, reason);
            // 位置跳跃，例如快进或快退
            log("位置跳跃，例如快进或快退");
        }


        @Override
        public void onIsPlayingChanged(boolean isPlaying) {
            Player.Listener.super.onIsPlayingChanged(isPlaying);
            log("播放中/播放停止   播放中：" + isPlaying);
        }

        @Override
        public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
            String url = "";
            if (mediaItem.requestMetadata != null) {
                if (mediaItem.requestMetadata.mediaUri != null) {
                    url = mediaItem.requestMetadata.mediaUri.toString();
                }
            }
            if (mediaItem.localConfiguration != null) {
                url = mediaItem.localConfiguration.uri.toString();
            }

            Player.Listener.super.onMediaItemTransition(mediaItem, reason);
            //每当播放器更改播放列表中的新媒体项目时调用
            log("播放下一个视频  mediaId:" + mediaItem.mediaId + " url:" + url);
        }

        @Override
        public void onTimelineChanged(Timeline timeline, int reason) {
            Player.Listener.super.onTimelineChanged(timeline, reason);
        }

        @Override
        public void onTracksChanged(Tracks tracks) {
            Player.Listener.super.onTracksChanged(tracks);
        }

        @Override
        public void onMediaMetadataChanged(MediaMetadata mediaMetadata) {
            Player.Listener.super.onMediaMetadataChanged(mediaMetadata);
        }

        @Override
        public void onPlaylistMetadataChanged(MediaMetadata mediaMetadata) {
            Player.Listener.super.onPlaylistMetadataChanged(mediaMetadata);
        }

        @Override
        public void onIsLoadingChanged(boolean isLoading) {
            Player.Listener.super.onIsLoadingChanged(isLoading);
        }

        @Override
        public void onAvailableCommandsChanged(Player.Commands availableCommands) {
            Player.Listener.super.onAvailableCommandsChanged(availableCommands);
        }

        @Override
        public void onTrackSelectionParametersChanged(TrackSelectionParameters parameters) {
            Player.Listener.super.onTrackSelectionParametersChanged(parameters);
        }


        @Override
        public void onPlaybackSuppressionReasonChanged(int playbackSuppressionReason) {
            Player.Listener.super.onPlaybackSuppressionReasonChanged(playbackSuppressionReason);
        }


        @Override
        public void onRepeatModeChanged(int repeatMode) {
            Player.Listener.super.onRepeatModeChanged(repeatMode);
        }

        @Override
        public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
            Player.Listener.super.onShuffleModeEnabledChanged(shuffleModeEnabled);
        }


        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
            Player.Listener.super.onPlaybackParametersChanged(playbackParameters);
        }

        @Override
        public void onSeekBackIncrementChanged(long seekBackIncrementMs) {
            Player.Listener.super.onSeekBackIncrementChanged(seekBackIncrementMs);
        }

        @Override
        public void onSeekForwardIncrementChanged(long seekForwardIncrementMs) {
            Player.Listener.super.onSeekForwardIncrementChanged(seekForwardIncrementMs);
        }

        @Override
        public void onMaxSeekToPreviousPositionChanged(long maxSeekToPreviousPositionMs) {
            Player.Listener.super.onMaxSeekToPreviousPositionChanged(maxSeekToPreviousPositionMs);
        }

        @Override
        public void onAudioSessionIdChanged(int audioSessionId) {
            Player.Listener.super.onAudioSessionIdChanged(audioSessionId);
        }

        @Override
        public void onAudioAttributesChanged(AudioAttributes audioAttributes) {
            Player.Listener.super.onAudioAttributesChanged(audioAttributes);
        }

        @Override
        public void onVolumeChanged(float volume) {
            Player.Listener.super.onVolumeChanged(volume);
        }

        @Override
        public void onSkipSilenceEnabledChanged(boolean skipSilenceEnabled) {
            Player.Listener.super.onSkipSilenceEnabledChanged(skipSilenceEnabled);
        }

        @Override
        public void onDeviceInfoChanged(DeviceInfo deviceInfo) {
            Player.Listener.super.onDeviceInfoChanged(deviceInfo);
        }

        @Override
        public void onDeviceVolumeChanged(int volume, boolean muted) {
            Player.Listener.super.onDeviceVolumeChanged(volume, muted);
        }

        @Override
        public void onVideoSizeChanged(VideoSize videoSize) {
            Player.Listener.super.onVideoSizeChanged(videoSize);
        }

        @Override
        public void onSurfaceSizeChanged(int width, int height) {
            Player.Listener.super.onSurfaceSizeChanged(width, height);
        }

        @Override
        public void onRenderedFirstFrame() {
            Player.Listener.super.onRenderedFirstFrame();
        }

        @Override
        public void onCues(CueGroup cueGroup) {
            Player.Listener.super.onCues(cueGroup);
        }

        @Override
        public void onMetadata(Metadata metadata) {
            Player.Listener.super.onMetadata(metadata);
        }
    }

}
