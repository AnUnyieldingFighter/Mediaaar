package media.library.player.manager;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.OptIn;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
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
    public CustomExoPlayer getCustomExoPlayer(Fragment frg, String url) {
        CustomExoPlayer temp = players.get(frg);
        if (temp == null && playersPre.size() > 0) {
            temp = playersPre.remove(url);
        }
        if (temp == null) {
            temp = new CustomExoPlayer();
            players.put(frg, temp);
        }
        return temp;
    }

    //释放全部资源
    public void setExoPlayerRelease() {
        if (players.size() > 0) {
            for (CustomExoPlayer player : players.values()) {
                if (player != null) {
                    player.setPlayerViewRelease();
                    player.release();
                }
                players.clear();
            }
        }

        if (playersPre.size() > 0) {
            for (CustomExoPlayer player : playersPre.values()) {
                if (player != null) {
                    player.setPlayerViewRelease();
                    player.release();
                }
            }
            playersPre.clear();
        }
        customExoPlayer = null;
        objs = null;
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
    private final HashMap<String, CustomExoPlayer> playersPre = new HashMap<>();
    private ArrayList<Object> objs;

    //预加载数据
    public void setVideoObjPre(Context context, ArrayList<Object> videos, int max) {
        this.objs = videos;
        if (videos == null || videos.size() == 0) {
            return;
        }
        for (int i = 0; i < videos.size(); i++) {
            Object video = videos.get(i);
            String url = video.toString();
            if (TextUtils.isEmpty(url)) {
                continue;
            }
            if (url.startsWith("http")) {
                setVideoLoadPre(context, url);
            }
            if (playersPre.size() > max) {
                break;
            }
        }
    }

    public ArrayList<Object> getObjs() {
        return objs;
    }

    /**
     * 设置视频预加载(在播放之前)
     *
     * @param context
     * @param
     * @param videoUrl 视频url
     */
    @OptIn(markerClass = UnstableApi.class)
    public void setVideoLoadPre(Context context, String videoUrl) {
        CustomExoPlayer exoPlayer = playersPre.get(videoUrl);
        if (exoPlayer != null) {
            return;
        }
        exoPlayer = new CustomExoPlayer();
        //原本就存在要加载的Url
        exoPlayer.setPlayerVideo(context, videoUrl, true);
        //
        exoPlayer.prepare();
        exoPlayer.setPlayWhenReady(false);//准备好 就暂停
        //循环播放
        exoPlayer.setRepeatMode(Player.REPEAT_MODE_ALL);
        playersPre.put(videoUrl, exoPlayer);
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
