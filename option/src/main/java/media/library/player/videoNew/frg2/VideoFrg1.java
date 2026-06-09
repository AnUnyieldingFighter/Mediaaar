package media.library.player.videoNew.frg2;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.VideoSize;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.ui.PlayerView;

import com.images.imageselect.R;

import media.library.player.manager.PlayerLog;
import media.library.player.videoNew.able.OnVideoOperate2;
import media.library.player.view.CustomExoPlayer;


public class VideoFrg1 extends VideoBaseFrg0 {
    @Override
    protected int getRootViewLayout() {
        return R.layout.frg_video_play;
    }


    protected CustomExoPlayer exoPlayer;
    protected PlayerView playerView;
    protected TextView tvIndex;

    protected OnVideoOperate2 videoOperate2 = null;
    protected int pageIndex = -1;
    protected String videoUrl;
    //true 横屏
    protected Boolean isVideoHorizontalScreen = false;
    //true 添加到缓存
    protected boolean isUseCache = true;

    //成功添加到 Activity/容器里  它在onCreate 之前
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    //=================================设置view/初始化
    @Override
    protected void setViewInit(View view, Bundle savedInstanceState) {
        viewInit(view, savedInstanceState);
        PlayerLog.d("frg_", "setViewInit  " + pageIndex + " video:" + videoUrl);
    }

    private void viewInit(View view, Bundle savedInstanceState) {
        tvIndex = view.findViewById(R.id.tv_index);
        PlayerView tempPlayerView = view.findViewById(R.id.player_view);
        initPlayerView(tempPlayerView);

    }

    protected void initPlayerView(PlayerView playerView) {
        this.playerView = playerView;
        if (act instanceof OnVideoOperate2) {
            videoOperate2 = (OnVideoOperate2) act;
        }
        if (relyFrg instanceof OnVideoOperate2) {
            videoOperate2 = (OnVideoOperate2) relyFrg;
        }
        if (videoOperate2 != null) {
            videoOperate2.onFrgAttach(this);
        }
        setClick();
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
        boolean isUse = playerView.getUseController();
        if (isUse) {
            //控制器可用，交给控制器
            return;
        }
        Player temp = playerView.getPlayer();
        if (temp != null) {
            boolean playWhenReady = temp.getPlayWhenReady();
            temp.setPlayWhenReady(!playWhenReady);
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    protected void setPlayerViewParameter() {
        //禁止控制器在用户触摸时自动隐藏
        playerView.setControllerAutoShow(false);
        //强制启用控制器
        playerView.setUseController(true);
    }

    public CustomExoPlayer getExoPlayer() {
        return exoPlayer;
    }

    //获取播放视图
    public PlayerView getPlayerView() {
        return playerView;
    }

    //=======================初始化播放器========================
    //更新播放源
    protected void updateVideoUrl(int pageIndex, String url) {
        PlayerLog.d("视频播放Url", "计划播放 pageIndex：" + pageIndex + " url:" + url);
        if (playerView == null) {
            PlayerLog.d("未初始化", "------- index=" + pageIndex + "  " + this.pageIndex);
            return;
        }
        this.videoUrl = url;
        this.pageIndex = pageIndex;
        if (exoPlayer != null) {
            boolean isSame = exoPlayer.isEqualVideoPlay(url);
            if (isSame) {
                exoPlayer.setPlayerView(playerView);
                return;
            }
        }
        //更换 播放url
        PlayerLog.d("去初始化/更新播放源 ",
                "------- index=" + this.pageIndex + " 更新 pageIndex=" + pageIndex + " videoUrl:" + videoUrl);
        initExoPlayer();
    }

    //初始化 播放器/更换播放器地址
    @OptIn(markerClass = UnstableApi.class)
    private void initExoPlayer() {
        //
        isVideoHorizontalScreen = false;
        //
        exoPlayer = videoOperate2.getExoPlayer(pageIndex, videoUrl);
        exoPlayer.setPlayerVideo(act, videoUrl, isUseCache);
        //
        exoPlayer.setPlayerView(playerView);
        exoPlayer.prepare();
        exoPlayer.setPlayWhenReady(false);
        exoPlayer.setRepeatMode(getPlayMode());
        //立即显示控制器（需在setPlayer()之后调用）
        playerView.showController();
        if (isResume) {
            exoPlayer.addListener(playerListener);
        }
        onNewPlay();
    }

    //REPEAT_MODE_ALL：表示列表循环
    //REPEAT_MODE_ONE：表示单曲循环
    //REPEAT_MODE_OFF：不循环
    protected int getPlayMode() {
        //设置循环播放
        return Player.REPEAT_MODE_ALL;
    }

    //视频大小
    protected void onPLVideoSizeChanged() {
        if (exoPlayer == null) {
            return;
        }
        VideoSize video = exoPlayer.getVideoSize();
        if (video != null) {
            onPLVideoSizeChanged(video);
        }
    }

    protected void setShowIndexTest() {
        if (tvIndex != null) {
            var tempPlay = playerView.getPlayer();
            tvIndex.setText("页面：" + pageIndex + "\nplayer==null:" + (tempPlay == null) + "\n" + videoUrl);
        }

    }

    //=======================初始化完成回调===============================
    //一个新的播放
    protected void onNewPlay() {
    }

    //==========================生命周期=====================
    @Override
    public void onResume() {
        super.onResume();
        if (exoPlayer != null) {
            exoPlayer.addListener(playerListener);
        }
        PlayerLog.d("frg_", "onResume " + pageIndex);
    }

    @Override
    public void onPause() {
        super.onPause();
        setVideoPause();
        if (exoPlayer != null) {
            exoPlayer.removeListener(playerListener);
        }
        PlayerLog.d("frg_", "onPause  " + pageIndex + " video:" + videoUrl);
    }

    @Override
    public void onStop() {
        super.onStop();
        PlayerLog.d("frg_", "onStop " + pageIndex + " video:" + videoUrl);
    }


    @Override
    public void onDestroy() {
        if (exoPlayer != null) {
            exoPlayer.setPlayerViewRelease();
            exoPlayer = null;
        }
        super.onDestroy();
        PlayerLog.d("frg_", "onDestroy " + pageIndex + " video:" + videoUrl);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //视图不可见  Fragment实例还在
        PlayerLog.d("frg_", "onDestroyView " + pageIndex + " video:" + videoUrl);
    }

    //====================操作播放器=========================
    //设置播放
    public void setVideoPlay() {
        if (exoPlayer != null) {
            exoPlayer.setPlayContinue();
        }
    }

    //设置暂停
    public void setVideoPause() {
        if (exoPlayer != null) {
            exoPlayer.setPause();
        }
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
        if (playerView == null) {
            return;
        }
        Player temp = playerView.getPlayer();
        if (temp == null) {
            return;
        }
        int playbackState = temp.getPlaybackState();
        PlayerLog.d("播放进度", "当前播放进度   playbackState=" + playbackState);
        if (playbackState == Player.STATE_READY) {
            onPLplayerUpdate(false);
        }
    }
    //===========================监听回调======================

    //计算播放进度 isPlayEnd true:播放结束
    private void onPLplayerUpdate(boolean isPlayEnd) {
        if (exoPlayer == null) {
            return;
        }
        long currentPosition = exoPlayer.getCurrentPosition();
        long duration = exoPlayer.getDuration();
        if (isPlayEnd) {
            currentPosition = duration;
        }
        PlayerLog.d("播放进度", "当前播放进度   " + currentPosition + " / " + duration);
        onPLplayerProgress(currentPosition, duration);
    }

    //视频播放进度
    protected void onPLplayerProgress(long currentPosition, long duration) {

    }


    //视频大小回调
    public void onPLVideoSizeChanged(VideoSize videoSize) {
        int width = videoSize.width;
        int height = videoSize.height;
        if (width > height) {
            //横屏
            isVideoHorizontalScreen = true;
        } else {
            //竖屏
            isVideoHorizontalScreen = false;
        }
    }


    //视频回调播放状态
    protected void onPLIsPlayingChanged(boolean isPlay, int state) {

    }

    //准备就绪
    protected void onPLReadyCompleted() {

    }

    //播放完成 isLoop：true 是循环
    protected void onPLplayerCompleted(boolean isLoop) {
    }

    @OptIn(markerClass = UnstableApi.class)
    protected void onPLPlayerError(PlaybackException error) {
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
                    onPLReadyCompleted();
                    onPLplayerUpdate(false);
                    break;
                case Player.STATE_ENDED:
                    onPLplayerUpdate(true);
                    onPLplayerCompleted(false);
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
                    onPLplayerUpdate(true);
                    onPLplayerCompleted(true);
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
            onPLplayerUpdate(false);
        }

        @Override
        public void onIsPlayingChanged(boolean isPlaying) {
            PlayerLog.d("视频播放", "播放中/播放停止   播放中：" + isPlaying);
            Player temp = playerView.getPlayer();
            int state = -1;
            if (temp != null) {
                state = temp.getPlaybackState();
            }
            onPLIsPlayingChanged(isPlaying, state);
        }

        @Override
        public void onVideoSizeChanged(VideoSize videoSize) {
            PlayerLog.d("视频播放", "视频大小  width：" + videoSize.width + " height:" + videoSize.height);
            onPLVideoSizeChanged(videoSize);
        }

        @OptIn(markerClass = UnstableApi.class)
        @Override
        public void onPlayerError(PlaybackException error) {
            //Source error code:2007 codeName:ERROR_CODE_IO_CLEARTEXT_NOT_PERMITTED  不允许明文传输
            //发生错误：Source error code:2002 codeName:ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT 网络连接失败
            PlayerLog.d("视频播放", "发生错误：" + error.getMessage() + " code:" + error.errorCode + " codeName:" + error.getErrorCodeName());
            onPLPlayerError(error);

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


}
