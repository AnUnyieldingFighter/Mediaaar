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

    //取出一个 页面索引删除
    public void setDelExoPlayer(int pageIndex) {
        int index = pageIndex % playersMax;
        if (index >= players.size()) {
            return;
        }
        players.remove(index);
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
