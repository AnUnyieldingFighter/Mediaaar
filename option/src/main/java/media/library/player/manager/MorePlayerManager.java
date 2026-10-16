package media.library.player.manager;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import media.library.player.view.CustomExoPlayer;

//保存播放器
public class MorePlayerManager {
    private ArrayList<CustomExoPlayer> players = new ArrayList<>();
    private int playersMax = 7;

    public int getPlayersMax() {
        return playersMax;
    }

    public CustomExoPlayer getCustomExoPlayer(Integer pageIndex) {
        int index = pageIndex % playersMax;
        CustomExoPlayer temp = null;
        if (index >= players.size()) {
            while (index >= players.size()) {
                temp = new CustomExoPlayer();
                players.add(temp);
            }
        }
        temp = players.get(index);
        return temp;
    }

    //释放全部资源
    public void setExoPlayerRelease() {
        for (int i = 0; i < players.size(); i++) {
            CustomExoPlayer player = players.get(i);
            if (player != null) {
                player.release();
            }
        }
        players.clear();
    }

    /** 删除指定页的播放器，按与页面池相同的循环顺序前移，末位补入新播放器。 */
    public void setDelExoPlayer(int pageIndex) {
        int index = pageIndex % playersMax;
        getCustomExoPlayer(playersMax - 1);
        for (int i = 0; i < playersMax - 1; i++) {
            int slot = (index + i) % playersMax;
            players.set(slot, players.get((slot + 1) % playersMax));
        }
        players.set((index + playersMax - 1) % playersMax, new CustomExoPlayer());
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
