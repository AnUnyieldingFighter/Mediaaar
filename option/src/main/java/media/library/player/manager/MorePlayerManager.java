package media.library.player.manager;

import androidx.fragment.app.Fragment;

import java.util.HashMap;

import media.library.player.view.CustomExoPlayer;

//保存播放器
public class MorePlayerManager {
    private final HashMap<Fragment, CustomExoPlayer> players = new HashMap<>();

    /** 获取页面对应的播放器，不存在时创建。 */
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
            player.release();
        }
        players.clear();
        customExoPlayer = null;
    }

    /** 移除页面对应的播放器并释放资源。 */
    public void setDelExoPlayer(Fragment frg) {
        CustomExoPlayer player = players.remove(frg);
        if (player != null) {
            if (customExoPlayer == player) {
                customExoPlayer = null;
            }
            player.release();
        }
    }

    //当前页面正在使用的
    private CustomExoPlayer customExoPlayer;

    public void setPlay(CustomExoPlayer customExoPlayer) {
        this.customExoPlayer = customExoPlayer;
    }

    public CustomExoPlayer getPlay() {
        return customExoPlayer;
    }
}
