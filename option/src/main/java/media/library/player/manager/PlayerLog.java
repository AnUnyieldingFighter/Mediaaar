package media.library.player.manager;

import android.util.Log;

public class PlayerLog {
    public static void d(String tag,String value){
        Log.d("PlayerLog_"+tag,value);
    }
}
