package media.library.player.videoNew.manger;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.view.View;


import java.util.ArrayList;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.ui.PlayerView;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;
import media.library.player.manager.MorePlayerManager;
import media.library.player.manager.PlayerLog;
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
        handlerUi.stop();
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
        VideoBaseFrg0 cursorVideoFrg = getCursorFrg(indexPage);
        return cursorVideoFrg;
    }

    //=================适配器=======================
    //更新视频数据数量，不包含独立插入的广告页。
    public void setUpdateDataSize(int dataSzie) {
        adapter.setDataSize(dataSzie);
    }

    /**
     * 获取页面对应的原视频列表索引，广告页或无效位置返回 -1。
     */
    public int getVideoDataIndex(int pageIndex) {
        return adapter.getVideoDataIndex(pageIndex);
    }

    //获取指定页面对象
    private VideoBaseFrg0 getCursorFrg(int index) {
        return adapter.getIndexAtFrg(index);
    }

    //================播放====================================

    /**
     * 根据页面对象获取对应的播放器。
     */
    public CustomExoPlayer getExoPlayer(Fragment frg) {
        return getPlayerManager().getCustomExoPlayer(frg);
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

    /**
     * 根据页面索引获取播放器，索引无效时返回 null。
     */
    public CustomExoPlayer getExoPlayer(Integer pageIndex) {
        if (pageIndex == null || adapter == null
                || pageIndex < 0 || pageIndex >= adapter.getItemCount()) {
            return null;
        }
        PlayerLog.d("播放视频", "更新  urlIndexs  index:" + pageIndex);
        return getExoPlayer(getCursorFrg(pageIndex));
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
     * @param index 第几个
     * @param isDataReset true 为数据重置，视图就绪时主动加载；false 由页面 onResume 加载。
     */
    public void setVideoPlay(int index, boolean isDataReset) {
        VideoBaseFrg0 frg = setPageCurrentItem(index);
        if (frg == null) {
            return;
        }
        if (!isFrgReady(frg)) {
            //没有被初始化，走预加载逻辑
            setFrgPre(frg);
        } else if (isDataReset && frg instanceof VideoFrg11) {
            ((VideoFrg11) frg).setVideoDataPlay();
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
    public void setPagePlayNext() {
        int index = getResumeIndex();
        if (index + 1 < adapter.getItemCount()) {
            viewPagerView.setCurrentItem(index + 1, true);
        }
    }

    //播放上一个
    public void setPagePlayUp() {
        int index = getResumeIndex();
        if (index == 0) {
            return;
        }
        viewPagerView.setCurrentItem(index - 1, true);
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
            VideoBaseFrg0 videoFrgUp = getCursorFrg(indexUp);
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
        VideoBaseFrg0 videoFrgDow = getCursorFrg(indexNext);
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
     * 删除当前正在播放的
     * 调用前先删除业务数据；释放被删除页面的播放器，再恢复播放及相邻页预加载。
     */
    public void delFrgPlay() {
        int index = getResumeIndex();
        delFrgPlay(index);
    }

    /**
     * 删除指定页；视频页需先删除业务数据，广告页只删除页面，不操作原视频列表。
     * 删除当前页时切换页面，删除其他页时保留当前视频。
     */
    public void delFrgPlay(int index) {
        int size = adapter.getItemCount();
        if (size == 0 || index < 0 || index >= size) {
            return;
        }
        int currentIndex = getResumeIndex();
        boolean isLoopVideo = adapter.isInfinite() && !adapter.isInsertedFrg(index);
        //循环列表中，同一条视频可能出现在不同页面；删除该数据时当前页也需要切换。
        boolean deleteCurrent = index == currentIndex || (isLoopVideo
                && adapter.getVideoDataIndex(index) == adapter.getVideoDataIndex(currentIndex));
        //调整页面映射期间暂停本管理器的切页回调，完成后统一刷新预加载。
        viewPagerView.unregisterOnPageChangeCallback(onPageChange);
        try {
            if (!deleteCurrent) {
                //非当前frg
                if (adapter.isInsertedFrg(index)) {
                    setDelFrg(getCursorFrg(index));
                }
                if (isLoopVideo) {
                    currentIndex = adapter.removeLoopVideo(index, currentIndex, null);
                } else {
                    adapter.removeData(index, currentIndex);
                    if (index < currentIndex) {
                        currentIndex -= 1;
                    }
                }
                viewPagerView.setCurrentItem(currentIndex, false);
                //先对齐页码，再通知刷新，避免适配器按旧页码切换 Fragment 生命周期。
                adapter.notifyDataSetChanged();
                setPageLoadNext();
                setPageLoadUp();
                return;
            }
            //删除当前的frg
            VideoBaseFrg0 frg = getCursorFrg(currentIndex);
            setVideoPause(frg);
            setDelFrg(frg);
            if (isLoopVideo) {
                currentIndex = adapter.removeLoopVideo(index, currentIndex, getFrg());
            } else if (adapter.isInsertedFrg(index)) {
                //广告页直接移除，不调整视频复用池。
                adapter.removeData(index, currentIndex);
            } else {
                //按循环顺序前移，使下一视频页对应删除后的索引。
                adapter.removeFrg(index, getFrg());
            }
            if (adapter.getItemCount() == 0) {
                adapter.notifyDataSetChanged();
                setPageChange(cursorFrg, null);
                cursorFrg = null;
                return;
            }
            //还有下一条时保持原索引；删除末条时转到上一条。
            currentIndex = Math.min(currentIndex, adapter.getItemCount() - 1);
            viewPagerView.setCurrentItem(currentIndex, false);
            adapter.notifyDataSetChanged();
            VideoBaseFrg0 currentFrg = getCursorVideoFrg();
            if (cursorFrg != currentFrg) {
                setPageChange(cursorFrg, currentFrg);
                cursorFrg = currentFrg;
            }
            setVideoPlay(currentIndex, false);
        } finally {
            viewPagerView.registerOnPageChangeCallback(onPageChange);
        }
    }

    /**
     * 移除页面与播放器的对应关系，并释放播放器。
     */
    public void setDelFrg(Fragment frg) {
        setFrgPreDel(frg);
        getPlayerManager().setDelExoPlayer(frg);
    }


    /**
     * 在指定页面位置插入独立 Fragment，例如广告，不修改原视频列表，不参与七个视频页的复用。
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
            if (frg instanceof VideoFrg11) {
                ((VideoFrg11) frg).setAd(true);
            }
            adapter.addFrg(index, frg);
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
        if (frg == getCursorFrg(index)) {
            setPageVideoPre(frg, index);
        } else if (index > 0 && frg == getCursorFrg(index - 1)) {
            setPageVideoPre(frg, index - 1);
        } else if (index < size - 1 && frg == getCursorFrg(index + 1)) {
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

    class OnPageChange extends ViewPager2.OnPageChangeCallback {
        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            VideoBaseFrg0 temp = getCursorVideoFrg();
            setPageChange(cursorFrg, temp);
            cursorFrg = temp;
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

        private void stop() {
            removeMessages(1);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    setUpdatePlayProgress(getCursorVideoFrg());
                    sendEmptyMessageDelayed(1, 1 * 300);
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
