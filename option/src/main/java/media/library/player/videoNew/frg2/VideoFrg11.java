package media.library.player.videoNew.frg2;

import android.text.TextUtils;

import media.library.player.bean.VideoPlayVo;
import media.library.player.manager.PlayerLog;


public class VideoFrg11 extends VideoFrg1 {


    //获取页面索引
    public int getPageIndex() {
        return pageIndex;
    }

    //因为删除了一个frg，这个是相邻的下一个frg，索引要变根
    public void setUpdateIndex(int index) {
        pageIndex = index;
    }

    @Override
    public void onResume() {
        super.onResume();
        setVideoDataPlay(pageIndex, false);
    }


    /**
     * 预加载
     *
     * @param pageIndex
     *
     */
    public void setVideoPre(int pageIndex) {
        PlayerLog.d("视频播放 预加加载", "pageIndex=" + pageIndex);
        setVideoDataPlay(pageIndex, true);
    }

    public void setVideoPlay(int pageIndex) {
        PlayerLog.d("视频播放 加载播放", "pageIndex=" + pageIndex);
        setVideoDataPlay(pageIndex, false);
    }

    /**
     * 设置拉取播放数据
     * 一切从这里开始
     *
     * @param pageIndex 要加载的数据索引
     * @param isPreLoad true 是预加载，false 不是预加载
     */
    private void setVideoDataPlay(int pageIndex, boolean isPreLoad) {
        VideoPlayVo videoPlayVo = videoOperate2.getVideoPlayData(pageIndex);
        if (videoPlayVo == null || videoPlayVo.pageIndex < 0 ||
                TextUtils.isEmpty(videoPlayVo.url)) {
            String tempPage = "";
            String tempUrl = "";
            String tempId = "";
            if (videoPlayVo != null) {
                tempPage = "" + videoPlayVo.pageIndex;
                tempId = videoPlayVo.id;
                tempUrl = videoPlayVo.url;
            }
            PlayerLog.d("视频播放 数据异常", "pageIndex=" + tempPage
                    + " id=" + tempId
                    + " url=" + tempUrl);
            return;
        }
        setVideoStart(videoPlayVo, isPreLoad);
    }

    //设置拉取其它数据（用户头像，昵称，点赞，收藏等）
    public void setDataUpdate(int pageIndex) {

    }

    //===============================================数据拉取
    //isPreloading true  是预加载
    protected void setVideoStart(VideoPlayVo videoPlayVo, boolean isPreloading) {
        int tempIndex = videoPlayVo.pageIndex;
        String tempUrl = videoPlayVo.url;
        if (TextUtils.isEmpty(tempUrl)) {
            return;
        }
        //拉取播放地址
        updateVideoUrl(tempIndex, tempUrl);
        //
        onPLVideoSizeChanged();
        //显示页面信息
        setShowIndexTest();
        if (isPreloading) {
            return;
        }
        if (!isResume) {
            return;
        }
        setVideoPlay();
    }


}
