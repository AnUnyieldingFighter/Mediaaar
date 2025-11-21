package media.library.player.vido1.able;

import media.library.player.view.CustomExoPlayer;

public interface OnVideoOperate {

    //获取一个播放实例
    CustomExoPlayer getExoPlayer(Integer pageIndex, String videoUrl);


    //设置滑动
    void setViewPageSlide(boolean isSlide);

    //记录观看时长
    void recordDuration(String id, int pageIndex, long pro, long total);

    //数据检查
    void onCheck(Object str, Object obj);

    //获取当前页面索引
    int getResumeIndex();

    String getResumeVideoUrl();

}
