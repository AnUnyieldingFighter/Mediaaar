package media.library.player.video2.able;


public interface OnVideoData2 {

    //获取视频详情
    Object getVideoDetailsData(int pageIndex);


    //1 旋转到横屏 2 旋转到竖屏
    void onScreenRotation(int typeScreen);
}
