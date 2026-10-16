package media.library.player.videoNew.frg2;

import android.text.TextUtils;

import media.library.player.bean.VideoPlayVo;
import media.library.player.manager.PlayerLog;


public class VideoFrg11 extends VideoFrg1 {


    /** 页面恢复时，获取当前页的数据并播放。 */
    @Override
    public void onResume() {
        super.onResume();
        setVideoDataPlay();
    }


    /**
     * 获取指定页面的数据并预加载，不开始播放。
     * @param index 仅用于获取预加载数据的页面索引。
     */
    public void setVideoDataPre(int index) {
        PlayerLog.d("视频播放 预加加载", "pageIndex=" + index);
        setVideoData(videoOperate2.getVideoPlayData(index, true), true);
    }

    /** 获取当前显示页面的数据并播放，不使用上次加载时保存的索引。 */
    public void setVideoDataPlay() {
        int index = videoOperate2.getResumeIndex();
        PlayerLog.d("视频播放 加载播放", "pageIndex=" + index);
        setVideoData(videoOperate2.getVideoPlayData(index, false), false);
    }

    /**
     * 校验播放数据，再加载对应的视频。
     *
     * @param videoPlayVo 已获取的视频数据。
     * @param isPreLoad true 是预加载，false 不是预加载
     */
    private void setVideoData(VideoPlayVo videoPlayVo, boolean isPreLoad) {
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

    //===============================================数据拉取
    //isPreloading true  是预加载
    protected void setVideoStart(VideoPlayVo videoPlayVo, boolean isPreloading) {
        String tempUrl = videoPlayVo.url;
        if (TextUtils.isEmpty(tempUrl)) {
            return;
        }
        //拉取播放地址
        updateVideoUrl(tempUrl);
        //
        onPLVideoSizeChanged();
        if (isPreloading) {
            return;
        }
        if (!isResume) {
            return;
        }
        //显示当前页信息，预加载页面不使用当前页索引展示。
        setShowIndexTest();
        setVideoPlay();
    }


}
