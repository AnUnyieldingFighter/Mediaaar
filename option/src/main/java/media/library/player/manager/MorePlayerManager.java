package media.library.player.manager;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Set;

import media.library.player.view.CustomExoPlayer;

public class MorePlayerManager {
    private HashMap<Integer, CustomExoPlayer> players = new HashMap<>();
    private int playersMax = 7;

    public int getPlayersMax() {
        return playersMax;
    }

    public CustomExoPlayer getCustomExoPlayer(Integer pageIndex) {
        int index = pageIndex % playersMax;
        CustomExoPlayer temp = players.get(index);
        if (temp == null) {
            temp = new CustomExoPlayer();
            players.put(index, temp);
        }
        return temp;
    }

    //释放全部资源
    public void setExoPlayerRelease() {
        Set<Integer> keys = players.keySet();
        for (Integer key : keys) {
            CustomExoPlayer player = players.get(key);
            if (player != null) {
                player.release();
            }
        }
        players.clear();
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
