package media.library.player.able;



import java.util.ArrayList;

import media.library.player.frg2.VideoBaseFrg0;

public interface OnVideoLoading {


    /**
     * 向上加载一个
     *
     * @param index   当前索引
     * @param indexUp 上一个索引
     */
    void onVideoLoadingUp(int index, int indexUp, VideoBaseFrg0 videoFrgNow, VideoBaseFrg0 videoFrgUp);


    /**
     * 向下加载一个
     *
     * @param index    当前索引
     * @param indexDow 下一个索引
     */
    void onVideoLoadingDow(int index, int indexDow, VideoBaseFrg0 videoFrgNow, VideoBaseFrg0 videoFrgDow);

    ArrayList<VideoBaseFrg0> getPages();
}
