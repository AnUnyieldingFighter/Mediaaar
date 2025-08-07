package library.player.manager;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.view.Surface;
import android.view.SurfaceView;

import androidx.media3.exoplayer.ExoPlayer;

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
        return exoPlayerManager;
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

    @Override
    public void setPlayUrl2(Context context, String url) {
        super.setPlayUrl2(context, url);
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

    public ExoPlayer getExoPlayer() {
        return exoPlayer;
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
