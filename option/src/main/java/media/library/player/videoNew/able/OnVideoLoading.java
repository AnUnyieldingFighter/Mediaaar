package media.library.player.videoNew.able;


import java.util.ArrayList;

import media.library.player.videoNew.frg2.VideoBaseFrg0;

public interface OnVideoLoading {


    /**
     * 向上加载一个
     *
     * @param indexUp 上一个索引
     */
    void onVideoLoadingUp(int indexUp, VideoBaseFrg0 videoFrgUp);

    /**
     * 向下加载一个
     *
     * @param
     * @param indexDow 下一个索引
     */
    void onVideoLoadingDow(int indexDow, VideoBaseFrg0 videoFrgDow);

    ArrayList<VideoBaseFrg0> getPages();
}
