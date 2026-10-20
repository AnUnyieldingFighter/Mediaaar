package media.library.player.manager;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.OptIn;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import media.library.player.view.CustomExoPlayer;

//保存播放器
public class MorePlayerManager {
    private final HashMap<Fragment, CustomExoPlayer> players = new HashMap<>();

    /**
     * 获取页面对应的播放器，不存在时创建。
     */
    public CustomExoPlayer getCustomExoPlayer(Fragment frg, String url) {
        CustomExoPlayer temp = null;
        if (temp == null && playersPre.size() > 0) {
            temp = playersPre.remove(url);
            if (temp != null) {
                players.put(frg, temp);
            }
        }
        if (temp == null) {
            temp = players.get(frg);
        }
        if (temp == null) {
            temp = new CustomExoPlayer();
            players.put(frg, temp);
        }
        return temp;
    }

    public void setPlayerRelease() {
        if (players.size() > 0) {
            for (CustomExoPlayer player : players.values()) {
                setPlayerRelease(player);
            }
            players.clear();
        }

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

    private final HashMap<Fragment, CustomExoPlayer> playerChang = new HashMap<>();

    /**
     * 移除frg之外的页面对应的播放器并释放资源。
     *
     * @param frg
     * @param frgOld1 旧关系
     * @param frgOld2
     * @param frgNew1 新关系
     * @param frgNew2
     */
    public void setDelExoPlayerReverse(Fragment frg,
                                       Fragment frgOld1, Fragment frgOld2,
                                       Fragment frgNew1, Fragment frgNew2) {
        CustomExoPlayer exoPlayerNow = players.remove(frg);
        playerChang.put(frg, exoPlayerNow);
        CustomExoPlayer exoPlayer1 = players.remove(frgOld1);
        CustomExoPlayer exoPlayer2 = players.remove(frgOld2);
        if (frgNew1 != null && exoPlayer1 != null) {
            playerChang.put(frgNew1, exoPlayer1);
        } else {
            setPlayerRelease(exoPlayer1);
        }
        if (frgNew2 != null && exoPlayer2 != null) {
            playerChang.put(frgNew2, exoPlayer2);
        } else {
            setPlayerRelease(exoPlayer2);
        }
        if (players.size() > 0) {
            for (Map.Entry<Fragment, CustomExoPlayer> entry : players.entrySet()) {
                Fragment key = entry.getKey();
                if (key == frg) {
                    continue;
                }
                CustomExoPlayer player = entry.getValue();
                setPlayerRelease(player);
            }
            players.clear();
        }
        if (playerChang.size() > 0) {
            for (Map.Entry<Fragment, CustomExoPlayer> entry : playerChang.entrySet()) {
                Fragment key = entry.getKey();
                CustomExoPlayer player = entry.getValue();
                players.put(key, player);
            }
            playerChang.clear();
        }
    }

    //删除相邻的(正在播放的下一个)
    public void setDelExoPlayerAdjacent(Fragment frgHold1, Fragment frgHold2,
                                        Fragment frgDel, Fragment frgUpdate) {
        CustomExoPlayer exoPlayerHold1 = players.remove(frgHold1);
        if (frgHold1 != null && exoPlayerHold1 != null) {
            playerChang.put(frgHold1, exoPlayerHold1);
        }

        CustomExoPlayer exoPlayerHold2 = players.remove(frgHold2);
        if (frgHold2 != null && exoPlayerHold2 != null) {
            playerChang.put(frgHold2, exoPlayerHold2);
        } else {
            setPlayerRelease(exoPlayerHold2);
        }
        //删除
        CustomExoPlayer exoPlayerDel = players.remove(frgDel);
        if (exoPlayerDel != null) {
            setPlayerRelease(exoPlayerDel);
        }
        //更新（把它的播放器给frgDel）
        CustomExoPlayer exoPlayerUpdate = players.remove(frgUpdate);
        if (frgUpdate != null && exoPlayerUpdate != null) {
            playerChang.put(frgDel, exoPlayerUpdate);
        } else {
            setPlayerRelease(exoPlayerUpdate);
        }
        if (players.size() > 0) {
            for (Map.Entry<Fragment, CustomExoPlayer> entry : players.entrySet()) {
                CustomExoPlayer player = entry.getValue();
                setPlayerRelease(player);
            }
            players.clear();
        }
        if (playerChang.size() > 0) {
            for (Map.Entry<Fragment, CustomExoPlayer> entry : playerChang.entrySet()) {
                Fragment key = entry.getKey();
                CustomExoPlayer player = entry.getValue();
                players.put(key, player);
            }
            playerChang.clear();
        }

    }

    //保持3个 其余删除
    public void setDelExoPlayerHold3(Fragment frgHold1, Fragment frgHold2,
                                     Fragment frgHold3) {
        CustomExoPlayer exoPlayerHold1 = players.remove(frgHold1);
        if (frgHold1 != null && exoPlayerHold1 != null) {
            playerChang.put(frgHold1, exoPlayerHold1);
        }

        CustomExoPlayer exoPlayerHold2 = players.remove(frgHold2);
        if (frgHold2 != null && exoPlayerHold2 != null) {
            playerChang.put(frgHold2, exoPlayerHold2);
        } else {
            setPlayerRelease(exoPlayerHold2);
        }
        CustomExoPlayer exoPlayerHold3 = players.remove(frgHold3);
        if (frgHold3 != null && exoPlayerHold3 != null) {
            playerChang.put(frgHold3, exoPlayerHold3);
        } else {
            setPlayerRelease(exoPlayerHold3);
        }

        if (players.size() > 0) {
            for (Map.Entry<Fragment, CustomExoPlayer> entry : players.entrySet()) {
                CustomExoPlayer player = entry.getValue();
                setPlayerRelease(player);
            }
            players.clear();
        }
        if (playerChang.size() > 0) {
            for (Map.Entry<Fragment, CustomExoPlayer> entry : playerChang.entrySet()) {
                Fragment key = entry.getKey();
                CustomExoPlayer player = entry.getValue();
                players.put(key, player);
            }
            playerChang.clear();
        }

    }

    public void setPlayerChangeRelease() {
        if (playerChang.size() > 0) {
            for (CustomExoPlayer player : playerChang.values()) {
                setPlayerRelease(player);
            }
            playerChang.clear();
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
            if (playersPre.size() >= max) {
                break;
            }
            Object video = videos.get(i);
            String url = video.toString();
            if (TextUtils.isEmpty(url)) {
                continue;
            }
            if (url.startsWith("http")) {
                setVideoLoadPre(context, url);
            }
        }
    }

    public HashMap getPlayersPres() {
        return playersPre;
    }

    public ArrayList<Object> getObjs() {
        return objs;
    }

    public void setPlayerReleasePre() {
        if (playersPre.size() > 0) {
            for (CustomExoPlayer player : playersPre.values()) {
                setPlayerRelease(player);
            }
            playersPre.clear();
        }
        objs = null;
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
        //
        exoPlayer.setVideoPre(true);
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

    //释放全部资源
    public void setExoPlayerRelease() {
        setPlayerRelease();
        setPlayerReleasePre();
        setPlayerChangeRelease();
    }

    private void setPlayerRelease(CustomExoPlayer player) {
        if (player != null) {
            player.setPlayerViewRelease();
            player.release();
        }
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
