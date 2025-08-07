package library.player.view;


import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;

public class PlayView extends PlayerView {
    public PlayView(Context context) {
        super(context);
    }

    public PlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PlayView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @OptIn(markerClass = UnstableApi.class)
    private void setTest() {
        //设置线是控制器
        setUseController(true);
        //设置控制器显示时长
        setControllerShowTimeoutMs(3 * 1 * 1000);
        //是否显示缓冲视图
        setShowBuffering(SHOW_BUFFERING_ALWAYS);
        //大小调整模式
        setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);

    }
}
