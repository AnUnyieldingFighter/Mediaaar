package media.library.player.manager;

import android.content.Context;

import media.library.db.MediaRoom;

public class DBManager {
    //设置缓存文件全部被释放
    public static void setCacheFileReleaseAll(Context context) {
        MediaRoom.geVideoDb(context).setCacheFileReleaseAll();
    }
}
