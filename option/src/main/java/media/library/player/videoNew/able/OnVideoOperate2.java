package media.library.player.videoNew.able;

import androidx.fragment.app.Fragment;
import media.library.player.bean.VideoPlayVo;
import media.library.player.view.CustomExoPlayer;

public interface OnVideoOperate2 {

    //获取一个播放实例
    CustomExoPlayer getExoPlayer(Integer pageIndex, String videoUrl);


    //设置滑动
    void setViewPageSlide(boolean isSlide);

    //记录观看时长
    void recordDuration(String id, int pageIndex, long pro, long total);


    //pageIndex  拉指定页的数据
    VideoPlayVo getVideoPlayData(int pageIndex);

    //已成功添加淘容器里
    void onFrgAttach(Fragment frg);

}
