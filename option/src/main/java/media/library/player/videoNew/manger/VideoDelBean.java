package media.library.player.videoNew.manger;

import media.library.player.videoNew.able.OnVideoData2;
import media.library.player.videoNew.frg2.VideoBaseFrg0;

class VideoDelBean {
    public VideoBaseFrg0 frgNow = null;
    public VideoBaseFrg0 frgDown = null;
    public VideoBaseFrg0 frgUp = null;//frgDown
    public int indexDel;//frgNow/frgHold1 在删除数据之前的页面索引，不是被删除视频的索引。
    public int indexUpdate;//frgNow/frgHold1 在删除数据之后的页面索引。
    public OnVideoData2 onDataBack;
    public int videoIndex;
}
