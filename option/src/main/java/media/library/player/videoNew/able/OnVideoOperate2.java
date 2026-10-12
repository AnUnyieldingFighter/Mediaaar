package media.library.player.videoNew.able;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.media3.common.Player;
import androidx.media3.ui.PlayerView;
import media.library.player.bean.VideoPlayVo;
import media.library.player.videoNew.frg2.VideoBaseFrg0;
import media.library.player.view.CustomExoPlayer;

public interface OnVideoOperate2 {

    //获取一个播放实例
    CustomExoPlayer getExoPlayer(Integer pageIndex, String videoUrl);


    //设置滑动
    void setViewPageSlide(boolean isSlide);

    //记录观看时长
    void recordDuration(String id, int pageIndex, long pro, long total, CustomExoPlayer customExoPlayer,
                        Player player, PlayerView playerView);


    //pageIndex  拉指定页的数据 isPre :true 预记载
    VideoPlayVo getVideoPlayData(int pageIndex, boolean isPre);

    //已成功添加淘容器里
    void onFrgAttach(Fragment frg);

    void setPageChange(VideoBaseFrg0 frgExit, VideoBaseFrg0 frgEnter);
}
