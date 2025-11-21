package media.library.player.able;

import media.library.player.bean.VideoPlayVo;
import media.library.player.view.CustomExoPlayer;

public interface OnVideoOperate2 {

    //获取一个播放实例
    CustomExoPlayer getExoPlayer(Integer pageIndex, String videoUrl);


    //设置滑动
    void setViewPageSlide(boolean isSlide);

    //记录观看时长
    void recordDuration(String id, int pageIndex, long pro, long total);

    //数据检查
    void onCheck(Object str, Object obj);

    //pageIndex -1 当前页的数据  否则就拉指定页的数据
    VideoPlayVo getVideoPlayData(int pageIndex);

}
