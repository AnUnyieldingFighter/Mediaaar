package media.library.player.manager;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.view.Surface;
import android.view.SurfaceView;

import androidx.media3.exoplayer.ExoPlayer;

import media.library.player.view.CustomExoPlayer;
// exoPlayerManager = new ExoPlayerManager();
// String videoUrl = ImageLoadingUtile.getUrl(postBean.videoPath);
// exoPlayerManager.initExoPlayer().setPlayUrl(act, videoUrl);
// exoPlayerManager.setPlayerView(playerView);
// exoPlayerManager.setPlay(1);
// exoPlayerManager.setMute(isMute);
//单例播放器
public class ExoPlayerManager extends BasePlayerManager {
    private static ExoPlayerManager exoPlayerManager;

    public static ExoPlayerManager getInstance() {
        if (exoPlayerManager == null) {
            exoPlayerManager = new ExoPlayerManager();
        }
        return exoPlayerManager;
    }

    //step 1:初始化播放器

    public ExoPlayerManager initExoPlayer(Activity act) {
        super.initExoPlayers(act);
        return this;
    }

    public ExoPlayerManager initExoPlayer() {
        super.initExoPlayers();
        return this;
    }

    //step 2:设置播放url
    @Override
    public void setPlayUri(Context context, Uri uri) {
        super.setPlayUri(context, uri);
    }

    @Override
    public void setPlayUrl(Context context, String url) {
        super.setPlayUrl(context, url);
    }

    public void setMute(boolean isMute) {
        if (exoPlayer == null) {
            return;
        }
        exoPlayer.setMute(isMute);
    }


    //step 3:开始播放
    @Override
    public void setPlay() {
        super.setPlay();
    }

    //设置暂停
    @Override
    public void setPause() {
        super.setPause();
    }

    //设置进行播放
    @Override
    public void setPlayContinue() {
        super.setPlayContinue();
    }

    public CustomExoPlayer getExoPlayer() {
        return exoPlayer;
    }
    public void seekTo(long positionMs) {
        if (exoPlayer == null) {
            return;
        }
        exoPlayer.seekTo(positionMs);
    }



    public Surface getSurface() {
        return surface;
    }

    //释放资源
    public void setRelease() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            setRelease29();
        } else {
            setReleaseOther();
        }
    }

    //切换画面
    public void setReparent(SurfaceView surfaceView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            setReparent29(surfaceView);
        } else {
            setReparentOther(surfaceView);
        }
    }
}
