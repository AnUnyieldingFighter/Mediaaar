package media.library.player.able;

import androidx.media3.exoplayer.ExoPlayer;

public interface OnVideoOperate {

    //获取一个播放实例
    ExoPlayer getExoPlayer(Integer pageIndex, String videoUrl);

    //清理这个播放器的持有者
    void setClearFrgHoldPlayer(ExoPlayer player);

    //设置滑动
    void setViewPageSlide(boolean isSlide);

    //记录观看时长
    void recordDuration(String id, int pageIndex, long pro, long total);

    //更新播放进度回调
    void updateVideoPro();
    //数据检查
    void onCheck(Object str,Object obj);
    //获取当前页面索引
    int getIndex();

}
