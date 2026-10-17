package media.library.player.manager;

import android.app.Activity;

import androidx.annotation.OptIn;
import androidx.fragment.app.Fragment;

import java.util.HashMap;

import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.ui.PlayerView;
import media.library.player.view.CustomExoPlayer;

//保存播放器
public class MorePlayerManager {
    private final HashMap<Fragment, CustomExoPlayer> players = new HashMap<>();

    /**
     * 获取页面对应的播放器，不存在时创建。
     */
    public CustomExoPlayer getCustomExoPlayer(Fragment frg) {
        CustomExoPlayer temp = players.get(frg);
        if (temp == null) {
            temp = new CustomExoPlayer();
            players.put(frg, temp);
        }
        return temp;
    }

    //释放全部资源
    public void setExoPlayerRelease() {
        for (CustomExoPlayer player : players.values()) {
            if (player != null) {
                player.setPlayerViewRelease();
                player.release();
            }
        }
        players.clear();
        customExoPlayer = null;
    }

    /**
     * 移除页面对应的播放器并释放资源。
     */
    public void setDelExoPlayer(Fragment frg) {
        CustomExoPlayer player = players.remove(frg);
        if (player != null) {
            if (customExoPlayer == player) {
                customExoPlayer = null;
            }
            player.setPlayerViewRelease();
            player.release();
        }
    }
    //==========================================
    /**
     * 设置视频预加载(在播放之前)
     *
     * @param act
     * @param frg        预加载视频所属的页面。
     * @param videoUrl   视频url
     * @param playerView 播放视图（一般没有）
     */
    @OptIn(markerClass = UnstableApi.class)
    public void setVideoLoadPre(Activity act, Fragment frg, String videoUrl, PlayerView playerView) {
        if (frg == null) {
            return;
        }
        CustomExoPlayer exoPlayer = getCustomExoPlayer(frg);
        //原本就存在要加载的Url
        boolean isSame = exoPlayer.isEqualVideoPlay(videoUrl);
        if (isSame) {
            exoPlayer.setPlayerView(playerView);
            return;
        }
        exoPlayer.setPlayerVideo(act, videoUrl, true);
        //
        exoPlayer.setPlayerView(playerView);
        exoPlayer.prepare();
        exoPlayer.setPlayWhenReady(false);//准备好 就暂停
        //循环播放
        exoPlayer.setRepeatMode(Player.REPEAT_MODE_ALL);
    }
    //==========================================
    //当前页面正在使用的
    private CustomExoPlayer customExoPlayer;

    public void setPlay(CustomExoPlayer customExoPlayer) {
        this.customExoPlayer = customExoPlayer;
    }

    public CustomExoPlayer getPlay() {
        return customExoPlayer;
    }

    //========================================
    private static MorePlayerManager morePlayerManager;

    public static MorePlayerManager getInstance() {
        if (morePlayerManager == null) {
            morePlayerManager = new MorePlayerManager();
        }
        return morePlayerManager;
    }
}
