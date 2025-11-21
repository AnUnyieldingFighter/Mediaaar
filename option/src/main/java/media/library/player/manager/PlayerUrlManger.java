package media.library.player.manager;

import android.content.Context;
import android.text.TextUtils;

import java.util.HashMap;

import media.library.db.MediaRoom;
import media.library.player.view.CustomExoPlayer;

public class PlayerUrlManger {
    private static PlayerUrlManger playerManager;

    public static PlayerUrlManger getInstance() {
        if (playerManager == null) {
            playerManager = new PlayerUrlManger();
        }
        return playerManager;
    }

    public HashMap<Integer, String> map = new HashMap<>();

    public void addUrl(CustomExoPlayer player, String url) {
        int code = player.hashCode();
        map.put(code, url);
    }

    public void delUrl(Context context, String url, String fileName) {
        if (map.size() == 0) {
            return;
        }
        if (TextUtils.isEmpty(url)) {
            return;
        }
        if (TextUtils.isEmpty(fileName)) {
            return;
        }
        for (Integer key : map.keySet()) {
            String value = map.get(key);
            if (value == null) {
                continue;
            }
            if (url.equals(value)) {
                break;
            }
        }
        //释放缓存
        MediaRoom.geVideoDb(context).setCacheFileRelease(fileName);
    }

    public void delUrl(Context context, CustomExoPlayer player, String fileName) {
        if (map.size() == 0) {
            return;
        }
        if (player == null) {
            return;
        }
        if (TextUtils.isEmpty(fileName)) {
            return;
        }
        int code = player.hashCode();
        map.remove(code);
        //释放缓存
        MediaRoom.geVideoDb(context).setCacheFileRelease(fileName);
    }
}
