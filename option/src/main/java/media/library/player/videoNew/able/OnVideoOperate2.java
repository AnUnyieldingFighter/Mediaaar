package media.library.player.videoNew.able;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.media3.common.Player;
import androidx.media3.ui.PlayerView;
import media.library.player.bean.VideoPlayVo;
import media.library.player.videoNew.frg2.VideoBaseFrg0;
import media.library.player.view.CustomExoPlayer;

public interface OnVideoOperate2 {

    /** 获取当前显示页面的索引。 */
    int getResumeIndex();

    //根据frg，获取一个播放实例
    CustomExoPlayer getExoPlayer(Fragment frg, String videoUrl);

    //删除这个frg
    void setDelFrg(Fragment frg);

    //设置滑动
    void setViewPageSlide(boolean isSlide);

    //记录观看时长
    void recordDuration(String id, int pageIndex, long pro, long total, CustomExoPlayer customExoPlayer,
                        Player player, PlayerView playerView, int type);


    /**
     * 获取指定页面的播放数据。
     * @param pageIndex 页面索引，包含独立插入的广告页。
     * @param isPre true 为预加载。
     * @param isAd true 为独立插入的广告，false 为原视频列表中的视频。
     */
    VideoPlayVo getVideoPlayData(int pageIndex, boolean isPre, boolean isAd);

    //已成功添加淘容器里
    void onFrgAttach(Fragment frg);

    //视频切换
    void setPageChange(VideoBaseFrg0 frgExit, VideoBaseFrg0 frgEnter);
}
