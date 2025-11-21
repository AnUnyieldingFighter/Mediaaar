package media.library.player.able;


public interface OnVideoData2 {

    //获取视频详情
    Object getVideoDetailsData(int pageIndex);

    //Refresh true 刷新数据
    void isDataReq(boolean isRefresh);

}
