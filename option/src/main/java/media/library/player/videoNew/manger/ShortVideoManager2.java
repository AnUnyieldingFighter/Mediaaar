package media.library.player.videoNew.manger;

import android.os.Handler;
import android.os.Message;
import android.view.View;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;
import media.library.player.manager.MorePlayerManager;
import media.library.player.manager.PlayerLog;
import media.library.player.videoNew.able.OnVideoData2;
import media.library.player.videoNew.able.OnVideoLoading;
import media.library.player.videoNew.able.OnVideoOperate2;
import media.library.player.videoNew.adapter.VideoPagerAdapter3;
import media.library.player.videoNew.frg2.VideoBaseFrg0;
import media.library.player.videoNew.frg2.VideoFrg1;
import media.library.player.videoNew.frg2.VideoFrg11;
import media.library.player.view.CustomExoPlayer;

//短视频播放管理类
public class ShortVideoManager2 {

    private AppCompatActivity activity;
    private Fragment fragment;
    //true 可以预加载
    private boolean canLoadPre = true;

    public void setView(AppCompatActivity activity, ViewPager2 viewPagerView) {
        this.activity = activity;
        initView(viewPagerView);
        setClick();
    }

    public void setView(Fragment fragment, ViewPager2 viewPagerView) {
        this.fragment = fragment;
        initView(viewPagerView);
        setClick();
    }

    //设置是否预加载
    public void setCanLoadPre(boolean canLoadPre) {
        this.canLoadPre = canLoadPre;
    }

    private ViewPager2 viewPagerView;
    protected VideoPagerAdapter3 adapter;

    private OnPageChange onPageChange;


    //初始化时跳转的页面
    private int pageCurrentItem = 0;

    protected void initView(ViewPager2 viewPagerView) {
        this.viewPagerView = viewPagerView;
        if (activity == null && fragment == null) {
            return;
        }
        setAdapter();
        //设置为1时 显示第五个页面时 才销毁第第一个
        viewPagerView.setOffscreenPageLimit(1);
        onPageChange = new OnPageChange();
        if (pageCurrentItem > 0) {
            viewPagerView.setCurrentItem(pageCurrentItem, false);
        }
    }

    protected void setAdapter() {
        ArrayList<VideoBaseFrg0> frgs = getFrgs();
        if (activity != null) {
            adapter = new VideoPagerAdapter3(activity, frgs.size());
        } else if (fragment != null) {
            adapter = new VideoPagerAdapter3(fragment, frgs.size());
        }
        viewPagerView.setAdapter(adapter);
        adapter.setFrgs(frgs);
    }

    //设置7个页面
    private ArrayList<VideoBaseFrg0> getFrgs() {
        ArrayList<VideoBaseFrg0> frgs = new ArrayList<>();
        frgs.add(getFrg());
        frgs.add(getFrg());
        frgs.add(getFrg());
        frgs.add(getFrg());
        frgs.add(getFrg());
        frgs.add(getFrg());
        frgs.add(getFrg());
        return frgs;
    }

    private VideoBaseFrg0 getFrg() {
        VideoBaseFrg0 frg = null;
        if (onVideoLoading != null) {
            frg = onVideoLoading.getVideoPage();
        }
        if (frg == null) {
            frg = new VideoFrg11();
        }
        return frg;
    }


    private MorePlayerManager getPlayerManager() {
        return MorePlayerManager.getInstance();
    }


    //=======================生命周期========================
    public void onResume() {
        handlerUi.start();
    }


    public void onPause() {
        handlerUi.pause();
    }

    /**
     * 停止任务，解除页面绑定并释放全部播放器。
     */
    public void onDestroy() {
        handlerUi.stop();
        frgPres.clear();
        if (viewPagerView != null) {
            viewPagerView.unregisterOnPageChangeCallback(onPageChange);
            viewPagerView.setAdapter(null);
            viewPagerView = null;
        }
        getPlayerManager().setExoPlayerRelease();
        cursorFrg = null;
    }

    //=================viewPage====================
    protected void setClick() {
        viewPagerView.registerOnPageChangeCallback(onPageChange);
    }

    //获取当前页面索引
    public int getResumeIndex() {
        int index = viewPagerView.getCurrentItem();
        return index;
    }

    //设置是否可以滑动页面 true：可以
    public void setViewPageSlide(boolean isSlide) {
        viewPagerView.setUserInputEnabled(isSlide);
    }

    //跳转指定页面，并且获取对象
    public VideoBaseFrg0 setPageCurrentItem0() {
        return setPageCurrentItem(0);
    }

    //跳转指定页面，并且获取对象
    public VideoBaseFrg0 setPageCurrentItem(int pageIndex) {
        int size = adapter.getItemCount();
        if (pageIndex < 0 || pageIndex >= size) {
            return null;
        }
        int currentItem = viewPagerView.getCurrentItem();
        VideoBaseFrg0 videoFrg = adapter.getIndexAtFrg(pageIndex);
        if (currentItem == pageIndex) {
            return videoFrg;
        }
        //调用暂停
        setVideoPause(getCursorVideoFrg());
        viewPagerView.setCurrentItem(pageIndex, false);
        return videoFrg;
    }

    //获取当前页面对象
    public VideoBaseFrg0 getCursorVideoFrg() {
        if (adapter.getItemCount() == 0) {
            return null;
        }
        int indexPage = viewPagerView.getCurrentItem();
        VideoBaseFrg0 cursorVideoFrg = getPageFrg(indexPage);
        return cursorVideoFrg;
    }

    //=================适配器=======================
    //更新列表数量，包含空字符串表示的广告占位。
    public void setUpdateDataSize(int dataSzie) {
        adapter.setDataSize(dataSzie);
    }

    /**
     * 获取页面对应的列表索引，包含广告占位，无效位置返回 -1。
     */
    public int getVideoDataIndex(int pageIndex) {
        return adapter.getVideoDataIndex(pageIndex);
    }

    //获取指定页面对象
    private VideoBaseFrg0 getPageFrg(int index) {
        return adapter.getIndexAtFrg(index);
    }

    //================播放====================================

    /**
     * 根据页面对象获取对应的播放器。
     */
    public CustomExoPlayer getExoPlayer(Fragment frg, String url) {
        return getPlayerManager().getCustomExoPlayer(frg, url);
    }

    public CustomExoPlayer getExoPlayer(Fragment frg) {
        return getPlayerManager().getCustomExoPlayer(frg, "");
    }

    //获取当前页面的播放器
    public CustomExoPlayer getCursorVideoPlayer() {
        VideoBaseFrg0 cursorVideoFrg = getCursorVideoFrg();
        if (cursorVideoFrg == null) {
            return null;
        }
        if (cursorVideoFrg instanceof VideoFrg1) {
            return ((VideoFrg1) cursorVideoFrg).getExoPlayer();
        }
        return null;
    }

    //调用暂停
    protected void setVideoPause(VideoBaseFrg0 frg) {
        if (frg != null && frg instanceof VideoFrg1) {
            ((VideoFrg1) frg).setVideoPause();
        }
    }

    /**
     * 设置视频播放
     *
     * @param index
     * @param isDataReset true 为数据重置，视图就绪时主动加载；false 由页面 onResume 加载。
     */
    public void setVideoPlay(int index, boolean isDataReset) {
        VideoBaseFrg0 frgOld = cursorFrg;
        VideoBaseFrg0 frg = setPageCurrentItem(index);
        if (frg == null) {
            return;
        }
        if (!isFrgReady(frg)) {
            //没有被初始化，走预加载逻辑
            setFrgPre(frg);
        } else if (isDataReset || frg == frgOld) {
            if (frg instanceof VideoFrg11) {
                ((VideoFrg11) frg).setVideoDataPlay();
            }
        }
        setPageLoadNext();
        if (index != 0) {
            setPageLoadUp();
        }
    }

    //调用播放
    protected void setVideoPlay(VideoBaseFrg0 frg) {
        if (frg != null && frg instanceof VideoFrg1) {
            ((VideoFrg1) frg).setVideoPlay();
        }
        setFrgPreDel(frg);
    }

    //播放下一个
    public boolean setPagePlayNext() {
        boolean isOk = false;
        int index = getResumeIndex();
        if (index + 1 < adapter.getItemCount()) {
            viewPagerView.setCurrentItem(index + 1, true);
            isOk = true;
        }
        return isOk;
    }

    //播放上一个
    public boolean setPagePlayUp() {
        boolean isOk = false;
        int index = getResumeIndex();
        if (index == 0) {
            return isOk;
        }
        viewPagerView.setCurrentItem(index - 1, true);
        isOk = true;
        return isOk;
    }

    //==================================页面视频预加载逻辑======================

    /**
     * 预加载上一页
     */
    public void setPageLoadUp() {
        // 要预加载上一页
        int indexPage = viewPagerView.getCurrentItem();
        indexPage -= 1;
        setPageLoadUp(indexPage);
    }

    /**
     * 预加载上一页
     *
     * @param indexUp 索引
     */
    public void setPageLoadUp(int indexUp) {
        if (!canLoadPre) {
            return;
        }
        //下划 要预加载上一页
        int dataSize = adapter.getItemCount();
        if (indexUp >= 0 && indexUp < dataSize) {
            VideoBaseFrg0 videoFrgUp = getPageFrg(indexUp);
            if (isFrgReady(videoFrgUp)) {
                setPageVideoPre(videoFrgUp, indexUp);
            } else {
                setFrgPre(videoFrgUp);
            }

        }
    }

    /**
     * 设置预加载下一页
     */
    public void setPageLoadNext() {
        //要预加载下一页
        int indexPage = viewPagerView.getCurrentItem();
        indexPage += 1;
        setPageLoadNext(indexPage);
    }

    /**
     * 设置预加载下一页
     *
     * @param indexNext 索引
     */
    public void setPageLoadNext(int indexNext) {
        if (!canLoadPre) {
            return;
        }
        //上划 要预加载下一页
        int dataSize = adapter.getItemCount();
        if (indexNext < 0 || indexNext >= dataSize) {
            return;
        }
        VideoBaseFrg0 videoFrgDow = getPageFrg(indexNext);
        if (isFrgReady(videoFrgDow)) {
            setPageVideoPre(videoFrgDow, indexNext);
        } else {
            setFrgPre(videoFrgDow);

        }
    }

    //设置页面视频预加载
    protected void setPageVideoPre(Fragment frg, int pageIndex) {
        if (frg instanceof VideoFrg11) {
            ((VideoFrg11) frg).setVideoDataPre(pageIndex);
        }
        setFrgPreDel(frg);
    }

    //更新播放进度
    protected void setUpdatePlayProgress(VideoBaseFrg0 frg) {
        if (frg != null && frg instanceof VideoFrg1) {
            ((VideoFrg1) frg).setUpdatePlayProgress();
        }
    }

    //=========================删除frg和播放器，以及添加frg=====================================

    /**
     *
     * 多次删除当前正在播放的当前页面（要等onDataBack回调之后 ）
     * 先跳转视频，在移除删除frg和播放器，最后删除数据
     * 调用前先删除业务数据；释放被删除页面的播放器，再恢复播放及相邻页预加载。
     */
    public boolean delFrgPlay(OnVideoData2 onDataBack) {
        int index = getResumeIndex();
        return delFrgPlay(index, onDataBack);
    }

    /**
     * index 要删除的数据索引
     * 如果是当前，先跳转视频，在移除删除frg和播放器，最后删除数据
     * 删除当前页时切换页面，删除其他页时保留当前视频。
     */
    public boolean delFrgPlay(int index, OnVideoData2 onDataBack) {
        int size = adapter.getItemCount();
        if (size == 0 || index < 0 || index >= size) {
            return false;
        }

        int currentIndex = getResumeIndex();

        //true 当前页面
        boolean isPageNow = (index == currentIndex);
        if (isPageNow) {
            VideoDelBean videoDelBean = null;


            //true: 已切换页面
            boolean isOk = false;
            //切换下一个
            boolean isNextOk = setPagePlayNext();

            if (isNextOk) {
                VideoBaseFrg0 frgNow = getPageFrg(index + 1);
                VideoBaseFrg0 frgDown = (index + 2) < size ? getPageFrg(index + 2) : null;
                VideoBaseFrg0 frgUp = (index - 1) >= 0 ? getPageFrg(index - 1) : null;//getPageFrg(index - 1);
                //
                videoDelBean = new VideoDelBean();
                videoDelBean.frgNow = frgNow;
                videoDelBean.frgDown = frgDown;
                videoDelBean.frgUp = frgUp;
                videoDelBean.indexDel = index + 1;
                videoDelBean.indexUpdate = index;
                videoDelBean.onDataBack = onDataBack;
                videoDelBean.videoIndex = index;
                //
                //adapter.setFrgLast(index + 1, index, frgHold1, frgHold3, frgHold2);
                isOk = true;
            }
            //切换上一个
            if (!isNextOk) {
                boolean isUpOk = setPagePlayUp();
                if (isUpOk) {
                    VideoBaseFrg0 frgNow = getPageFrg(index - 1);
                    VideoBaseFrg0 frgUp = (index - 2) >= 0 ? getPageFrg(index - 2) : null;//getPageFrg(index - 2);
                    //
                    videoDelBean = new VideoDelBean();
                    videoDelBean.frgNow = frgNow;
                    videoDelBean.frgDown = null;
                    videoDelBean.frgUp = frgUp;
                    videoDelBean.indexDel = index - 1;//frgNow 删除之前的索引
                    videoDelBean.indexUpdate = index - 1;//frgNow 删除之后的索引
                    videoDelBean.onDataBack = onDataBack;
                    videoDelBean.videoIndex = index;
                    //
                    //adapter.setFrgLast(index - 1, index , frgHold1, frgHold2, null);
                    isOk = true;
                }
            }

            if (isOk) {
                viewPagerView.setUserInputEnabled(false);
                Message msg = new Message();
                msg.what = 500;
                msg.obj = videoDelBean;
                handlerUi.sendMessageDelayed(msg, 500);

            } else {
                //是最后一个
                //释放全部播放器
                getPlayerManager().setExoPlayerRelease();
                adapter.removeData();
                adapter.notifyDataSetChanged();
                onDataBack.setDataDel(index);
            }
        } else {
            //删除相邻的下一个
            if ((currentIndex - index) == 1) {
                //当前
                VideoBaseFrg0 frgNow = getPageFrg(currentIndex);
                //当前上一个
                VideoBaseFrg0 frgNowUp1 = getPageFrg(currentIndex - 1);
                //当前下一个（删除）
                VideoBaseFrg0 frgNowDown1 = getPageFrg(currentIndex + 1);
                //当前下二个
                VideoBaseFrg0 frgNowDown2 = getPageFrg(currentIndex + 1);
                //
                getPlayerManager().setDelExoPlayerAdjacent(frgNow, frgNowUp1, frgNowDown1, frgNowDown2);
                setPageLoadNext();

            } else if ((index - currentIndex) == 1) {
                //删除相邻的上一个
                //当前
                VideoBaseFrg0 frgNow = getPageFrg(currentIndex);
                //当前上一个（删除）
                VideoBaseFrg0 frgNowUp1 = getPageFrg(currentIndex - 1);
                //当前上二个
                VideoBaseFrg0 frgNowUp2 = getPageFrg(currentIndex - 2);
                //当前下一个
                VideoBaseFrg0 frgNowDown1 = getPageFrg(currentIndex + 1);
                //
                getPlayerManager().setDelExoPlayerAdjacent(frgNow, frgNowDown1, frgNowUp1, frgNowUp2);
                setPageLoadUp();
            } else {
                //当前
                VideoBaseFrg0 frgNow = getPageFrg(currentIndex);
                //当前上一个
                VideoBaseFrg0 frgNowUp1 = getPageFrg(currentIndex - 1);
                //当前下一个
                VideoBaseFrg0 frgNowDown1 = getPageFrg(currentIndex + 1);
                //
                getPlayerManager().setDelExoPlayerHold3(frgNow, frgNowDown1, frgNowUp1);
            }
            onDataBack.setDataDel(index);
            adapter.removeData();
            adapter.notifyDataSetChanged();
        }
        return true;

    }

    //先切换页面完成，再删除视频和页面
    private void delVideo(VideoDelBean videoDelBean) {
        if (videoDelBean == null) {
            return;
        }
        getPlayerManager().setDelExoPlayerHold3(videoDelBean.frgNow, videoDelBean.frgDown, videoDelBean.frgUp);
        videoDelBean.onDataBack.setDataDel(videoDelBean.videoIndex);
        //
        adapter.setFrgLast(videoDelBean.indexDel, videoDelBean.indexUpdate, videoDelBean.frgNow, videoDelBean.frgUp, videoDelBean.frgDown);
        //
        adapter.removeData();
        adapter.notifyDataSetChanged();
        //预加载不需要了，因为更换了adapter里的frg的位置
        //setPageLoadNext();
        //setPageLoadUp();

    }

    /**
     * 移除页面与播放器的对应关系，并释放播放器。
     */
    public void setDelFrg(Fragment frg) {
        setFrgPreDel(frg);
        getPlayerManager().setDelExoPlayer(frg);
    }


    /**
     * 在指定位置插入广告 Fragment，同时从七个复用页面中移出一个远端页面。
     * 调用前先在 videos 对应位置插入空字符串，并设置 ads；本方法将数据数量加一，
     * 因此无需提前调用 setUpdateDataSize。业务列表和广告数据均由调用方维护。
     * index 从 0 开始，传入 getResumeIndex() + 1 即插在当前页之后，也可以追加到末尾。
     * 插入时保持当前页面和播放状态；需要删除时调用 delFrgPlay(广告当前所在的页面索引)。
     */
    public void addFrg(int index, VideoBaseFrg0 frg) {
        if (adapter == null || viewPagerView == null || frg == null) {
            return;
        }
        int size = adapter.getItemCount();
        if (index < 0 || index > size || index == Integer.MAX_VALUE
                || frg.isAdded() || adapter.containsFrg(frg)) {
            return;
        }
        int currentIndex = getResumeIndex();
        //无限页数已到 int 上限时，当前末页无法再后移，避免插入后切换到其他视频。
        if (size == Integer.MAX_VALUE && currentIndex == size - 1 && index <= currentIndex) {
            return;
        }
        viewPagerView.unregisterOnPageChangeCallback(onPageChange);
        try {
            VideoBaseFrg0 removed = adapter.addFrg(index, frg, currentIndex);
            setDelFrg(removed);
            if (size > 0 && index <= currentIndex) {
                //先对齐页码，再通知刷新，保持当前 Fragment 和播放器不变。
                viewPagerView.setCurrentItem(currentIndex + 1, false);
            }
            adapter.notifyDataSetChanged();
            if (size == 0) {
                VideoBaseFrg0 currentFrg = getCursorVideoFrg();
                setPageChange(cursorFrg, currentFrg);
                cursorFrg = currentFrg;
                setVideoPlay(0, false);
            } else {
                setPageLoadNext();
                setPageLoadUp();
            }
        } finally {
            viewPagerView.registerOnPageChangeCallback(onPageChange);
        }
    }
//======================页面状态判断=====================

    /**
     *
     * @param frg
     * @return true  已准备好
     */
    public boolean isFrgReady(Fragment frg) {
        if (frg == null) {
            return false;
        }
        boolean isAdd = frg.isAdded();
        View view = frg.getView();
        if (isAdd && view != null) {
            return true;
        }
        return false;
    }

    //frg 视图创建后的回调，只预加载当前页、前一页或后一页对应的对象。
    public void setFrgAttach(Fragment frg) {
        if (!frgPres.contains(frg)) {
            return;
        }
        int index = getResumeIndex();
        int size = adapter.getItemCount();
        if (index < 0 || index >= size) {
            return;
        }
        //按实际页码取得 Fragment 比较，避免把复用池槽位当成页面索引。
        if (frg == getPageFrg(index)) {
            setPageVideoPre(frg, index);
        } else if (index > 0 && frg == getPageFrg(index - 1)) {
            setPageVideoPre(frg, index - 1);
        } else if (index < size - 1 && frg == getPageFrg(index + 1)) {
            setPageVideoPre(frg, index + 1);
        }
    }

    //要预加载的frg对象
    private ArrayList<Fragment> frgPres = new ArrayList<>();

    protected void setFrgPre(Fragment frgPre) {
        if (frgPres.contains(frgPre)) {
            return;
        }
        frgPres.add(frgPre);
    }

    protected void setFrgPreDel(Fragment frg) {
        frgPres.remove(frg);
    }
    //====================页面切换====================

    //frgExit 退出了这个页面,frgEnter
    public void setPageChange(VideoBaseFrg0 frgExit, VideoBaseFrg0 frgEnter) {
        if (activity instanceof OnVideoOperate2) {
            ((OnVideoOperate2) activity).setPageChange(frgExit, frgEnter);
        }
        if (fragment instanceof OnVideoOperate2) {
            ((OnVideoOperate2) fragment).setPageChange(frgExit, frgEnter);
        }
    }
    //============================页面监听=====================================================

    //当前页面
    private VideoBaseFrg0 cursorFrg;

    //设置当前页面，设置切换页面
    private void setCursorFrg() {
        VideoBaseFrg0 temp = getCursorVideoFrg();
        setPageChange(cursorFrg, temp);
        cursorFrg = temp;
    }

    class OnPageChange extends ViewPager2.OnPageChangeCallback {
        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            setCursorFrg();
            setPageLoadNext();
            setPageLoadUp();

        }


        @Override
        public void onPageScrollStateChanged(int state) {
            // 当页面滚动状态改变时调用，例如开始滑动、滑动结束等
            switch (state) {
                case ViewPager.SCROLL_STATE_DRAGGING:
                    //开始滑动/手指拖动中
                    PlayerLog.d("页面滑动开始", "index=" + viewPagerView.getCurrentItem());
                    setVideoPause(getCursorVideoFrg());
                    break;
                case ViewPager.SCROLL_STATE_SETTLING:
                    // 仍在滑动但即将停止/松手
                    break;
                case ViewPager.SCROLL_STATE_IDLE:
                    //滑动结束(会调用  onPageSelected 再走这里)
                    PlayerLog.d("页面滑动结束", "当前 index=" + viewPagerView.getCurrentItem());
                    setVideoPlay(getCursorVideoFrg());
                    break;
            }

        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            // 当页面开始滑动时调用

        }
    }

    //================获取 播放进度的Handler=======================================
    private HandlerUi handlerUi = new HandlerUi();

    class HandlerUi extends Handler {
        private void start() {
            removeMessages(1);
            sendEmptyMessageDelayed(1, 1 * 300);
        }

        private void pause() {
            removeMessages(1);
        }

        private void stop() {
            removeMessages(1);
            removeMessages(500);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    setUpdatePlayProgress(getCursorVideoFrg());
                    sendEmptyMessageDelayed(1, 1 * 300);
                    break;
                case 500:
                    //删除页面
                    Object obj = msg.obj;
                    if (obj instanceof VideoDelBean) {
                        delVideo((VideoDelBean) obj);
                    }
                    viewPagerView.setUserInputEnabled(true);
                    break;
            }
        }
    }

    //================================获取视频播放页面==============================
    private OnVideoLoading onVideoLoading;

    public void setOnVideoLoading(OnVideoLoading onVideoLoading) {
        this.onVideoLoading = onVideoLoading;
    }

}
