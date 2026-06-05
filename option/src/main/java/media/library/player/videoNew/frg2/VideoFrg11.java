package media.library.player.videoNew.frg2;

import android.text.TextUtils;

import androidx.media3.common.Player;
import androidx.media3.ui.PlayerView;
import media.library.player.bean.VideoPlayVo;
import media.library.player.manager.PlayerLog;
import media.library.player.view.CustomExoPlayer;


public class VideoFrg11 extends VideoFrg1 {


    //获取页面索引
    public int getPageIndex() {
        return pageIndex;
    }

    @Override
    public void onResume() {
        super.onResume();
        setVideoDataPlay(pageIndex, false);
    }
    //设置拉取播放数据   pageIndex-1:拉当前的数据（播放） 否则就拉指定页的数据（预加载）
    //一切从这里开始

    /**
     * 设置拉取播放数据
     * 一切从这里开始
     *
     * @param pageIndex 要加载的数据索引
     * @param isPreLoad true 是预加载，false 不是预加载
     */
    public void setVideoDataPlay(int pageIndex, boolean isPreLoad) {
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
        //拉取播放地址
        updateVideoUrl(tempIndex, tempUrl);
        //
        setShowIndexTest();
        onPLVideoSizeChanged();
        if (isPreloading) {
            return;
        }
        if (!isResume) {
            return;
        }
        setVideoPlay();
    }


}
