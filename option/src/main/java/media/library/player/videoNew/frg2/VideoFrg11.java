package media.library.player.videoNew.frg2;

import android.text.TextUtils;

import media.library.player.bean.VideoPlayVo;
import media.library.player.manager.PlayerLog;


public class VideoFrg11 extends VideoFrg1 {

    //true 表示由 addFrg 插入的广告页面；取数据时仍以 videos 中的空字符串占位为准。
    private boolean isAd;

    /** 设置插入页面的广告标记。 */
    public void setAd(boolean isAd) {
        this.isAd = isAd;
    }

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
        setVideoStart(videoOperate2.getVideoPlayData(index, true, isAd), true);
    }

    /** 获取当前显示页面的数据并播放，不使用上次加载时保存的索引。 */
    public void setVideoDataPlay() {
        int index = videoOperate2.getResumeIndex();
        PlayerLog.d("视频播放 加载播放", "pageIndex=" + index);
        setVideoStart(videoOperate2.getVideoPlayData(index, false, isAd), false);
    }

    //===============================================数据拉取
    /**
     * 预加载和正式播放的统一入口；默认不加载空数据或空地址。
     * 子类可重写此方法处理广告或空数据，再按需调用父类加载视频。
     *
     * @param videoPlayVo 获取的播放数据，可能为 null；pageIndex 不作为播放条件。
     * @param isPreloading true 为预加载，false 为正式播放。
     */
    protected void setVideoStart(VideoPlayVo videoPlayVo, boolean isPreloading) {
        if (videoPlayVo == null || TextUtils.isEmpty(videoPlayVo.url)) {
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
            //无效数据清除旧播放，避免滑动结束时再次播放上一条视频。
            setVideoPause();
            if (exoPlayer != null) {
                videoOperate2.setDelFrg(this);
                exoPlayer = null;
            }
            videoUrl = null;
            return;
        }
        String tempUrl = videoPlayVo.url;
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
