package media.library.player.video.frg;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.OptIn;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.VideoSize;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.ui.PlayerView;

import com.images.imageselect.R;

import media.library.player.video.able.OnVideoOperate;
import media.library.player.manager.PlayerLog;
import media.library.player.view.CustomExoPlayer;


public class VideoFrg extends VideoBaseFrg {
    @Override
    protected int getRootViewLayout() {
        return R.layout.frg_video_play;
    }

    @Override
    public CustomExoPlayer getExoPlayer() {
        return exoPlayer;
    }

    protected CustomExoPlayer exoPlayer;
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
        PlayerLog.d("frg_", "setViewInit  " + pageIndex + " video:" + videoUrl);
    }

    private void viewInit(View view, Bundle savedInstanceState) {
        //Bundle bundle = getArguments();
        tvIndex = view.findViewById(R.id.tv_index);
        //int tempPageIndex = bundle.getInt("index", -1);
        //String tempVideoUrl = bundle.getString("url", "");
        //PlayerLog.d("视频播放", "初始化时设置播放地址 pageIndex：" + tempPageIndex
        //        + " videoUrl:" + tempVideoUrl);
        PlayerView tempPlayerView = view.findViewById(R.id.player_view);
        //
        initPlayerView(tempPlayerView);

    }

    protected void initPlayerView(PlayerView playerView) {
        this.playerView = playerView;
        //setPlayerViewParameter();
        if (act instanceof OnVideoOperate) {
            videoOperate = (OnVideoOperate) act;
        }
        setClick();
        //initExoPlayer();
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public PlayerView getPlayerView() {
        return playerView;
    }

    //step 3


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

    //初始化 播放器/更换播放器地址
    @OptIn(markerClass = UnstableApi.class)
    private void initExoPlayer() {
        //
        isVideoHorizontalScreen = false;
        //
        exoPlayer = videoOperate.getExoPlayer(pageIndex, videoUrl);
        exoPlayer.setPlayerVideo(act, videoUrl, true);
        //
        exoPlayer.setPlayerView(playerView);
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
            exoPlayer.setPlayContinue();
            //player.play();
        }

    }

    //设置暂停
    public void setVideoPause() {
        if (exoPlayer != null) {
            exoPlayer.setPause();
        }
    }

    //更新播放源
    public void updateVideoUrl(int pageIndex, String url) {
        PlayerLog.d("视频播放Url", "计划播放 pageIndex：" + pageIndex + " url:" + url);
        if (playerView == null) {
            PlayerLog.d("未初始化", "------- index=" + pageIndex + "  " + this.pageIndex);
            return;
        }
        if (exoPlayer != null) {
            boolean isSame = exoPlayer.isEqualVideoPlay(url);
            if (isSame) {
                this.pageIndex = pageIndex;
                exoPlayer.setPlayerView(playerView);
                return;
            }
        }
        this.videoUrl = url;
        this.pageIndex = pageIndex;
        //更换 播放url
        PlayerLog.d("去初始化/更新播放源 ",
                "------- index=" + this.pageIndex + " 更新 pageIndex=" + pageIndex + " videoUrl:" + videoUrl);
        initExoPlayer();
    }

    @Override
    public void onResume() {
        super.onResume();
        //拉取播放地址
        int tempIndex = videoOperate.getResumeIndex();
        String tempVideoUrl = videoOperate.getResumeVideoUrl();
        updateVideoUrl(tempIndex, tempVideoUrl);
        //
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
        setShowIndex();
        if (playerView == null) {
            return;
        }
        exoPlayer.setPlayerView(playerView);
        onVideoSize(exoPlayer);
        //
        int height = playerView.getHeight();
        int width = playerView.getWidth();

        videoOperate.onCheck(pageIndex, exoPlayer);
        int playbackState = exoPlayer.getPlaybackState();
        PlayerLog.d("视频播放", "正在播放：pageIndex" + pageIndex +
                " playbackState:" + playbackState + " height:" + height + " width:" + width + " url:" + videoUrl);
    }

    private void setShowIndex() {
        if (tvIndex != null) {
            var tempPlay = playerView.getPlayer();
            tvIndex.setText("页面：" + pageIndex + "\nplayer==null:" + (tempPlay == null) + "\n" + videoUrl);
        }

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
    protected void onVideoSize(CustomExoPlayer player) {
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
        //Bundle bundle = new Bundle();
        //bundle.putInt("index", index);
        //bundle.putString("url", url);
        //frg.setArguments(bundle);
        return frg;
    }


}
