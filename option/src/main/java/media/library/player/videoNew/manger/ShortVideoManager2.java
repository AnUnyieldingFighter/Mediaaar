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
     * 删除当前页面的数据，处理流程见带 index 的重载。
     * 业务数据由 onDataBack.setDataDel 删除，调用前不要提前删除。
     * 删除当前页需要等待延迟处理完成，再发起下一次删除。
     *
     * @param onDataBack 同步删除业务列表中指定索引的数据，并处理业务界面更新。
     * @return true 表示已受理删除，当前页的延迟删除此时可能尚未完成。
     */
    public boolean delFrgPlay(OnVideoData2 onDataBack) {
        int index = getResumeIndex();
        return delFrgPlay(index, onDataBack);
    }

    /**
     * 删除指定索引的数据，保留删除后的当前页及前后页对应的 Fragment 和播放器。
     *
     * <p>删除当前页：先切换到下一页；没有下一页时切换到上一页。
     * 按删除前的索引取得删除后要保留的当前页和前后页，暂时禁止手势翻页，
     * 延迟 500 毫秒交给 delVideo 完成删除，再恢复手势翻页。
     * 仅剩一条数据时，直接释放全部播放器、清空适配器数量，并回调删除业务数据。
     *
     * <p>删除非当前页：保持当前视频，分三种情况选择要保留的页面。
     * 删除相邻下一页，用原下二页补为下一页，当前索引不变；
     * 删除相邻上一页，用原上二页补为上一页，当前索引减一；
     * 删除其它页，保留原前后页，只有删除位置在当前页之前时，当前索引才减一。
     *
     * <p>保留页面确定后，通过 setDelExoPlayerHold3 保留这些页面的播放器并释放其余播放器，
     * 回调同步删除业务数据，再用 setFrgLast 调整复用槽位，减少适配器数量，刷新前对齐当前页码。
     * 对齐页码时临时取消业务页面监听，保留当前 Fragment 和播放器，不重复触发页面切换和预加载。
     * setFrgLast 接收的是保留的当前页在删除前、删除后的索引，不是被删页的索引；
     * Fragment 数量不变，保留页面与播放器的对应关系也不变。
     * 非当前页删除完成后，仅为删除相邻页所露出的新邻页补预加载。
     *
     * @param index      删除前业务列表中要删除的数据索引，从 0 开始。
     * @param onDataBack 同步删除业务数据的回调；调用本方法前不要提前删除业务数据，
     *                   也不要在回调中重复减少适配器数量。业务界面更新由回调处理。
     * @return 索引无效或列表为空时返回 false；true 表示已受理，当前页删除可能尚未完成。
     */
    public boolean delFrgPlay(int index, OnVideoData2 onDataBack) {
        int size = adapter.getItemCount();
        if (size == 0 || index < 0 || index >= size) {
            return false;
        }
        //当前索引
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
            VideoBaseFrg0 frgNow = getPageFrg(currentIndex);
            VideoBaseFrg0 frgUp;
            VideoBaseFrg0 frgDown;
            int indexUpdate = currentIndex;
            //删除相邻的下一个
            if (index == currentIndex + 1) {
                frgUp = currentIndex > 0 ? getPageFrg(currentIndex - 1) : null;
                //跳过删除页，原来的下二页成为下一页。
                frgDown = currentIndex + 2 < size ? getPageFrg(currentIndex + 2) : null;
            } else if (index == currentIndex - 1) {
                //删除相邻的上一个，原来的上二页成为上一页。
                frgUp = currentIndex >= 2 ? getPageFrg(currentIndex - 2) : null;
                frgDown = currentIndex + 1 < size ? getPageFrg(currentIndex + 1) : null;
                indexUpdate = currentIndex - 1;
            } else {
                //删除其它页，保留当前页和原来的前后页。
                frgUp = currentIndex > 0 ? getPageFrg(currentIndex - 1) : null;
                frgDown = currentIndex + 1 < size ? getPageFrg(currentIndex + 1) : null;
                if (index < currentIndex) {
                    indexUpdate = currentIndex - 1;
                }
            }
            getPlayerManager().setDelExoPlayerHold3(frgNow, frgDown, frgUp);
            onDataBack.setDataDel(index);
            adapter.setFrgLast(currentIndex, indexUpdate, frgNow, frgUp, frgDown);
            adapter.removeData();
            viewPagerView.unregisterOnPageChangeCallback(onPageChange);
            try {
                if (getResumeIndex() != indexUpdate) {
                    //刷新前对齐保留页的新索引，避免按旧页码更新 Fragment 生命周期。
                    viewPagerView.setCurrentItem(indexUpdate, false);
                }
                adapter.notifyDataSetChanged();
            } finally {
                viewPagerView.registerOnPageChangeCallback(onPageChange);
            }
            //数据和槽位更新完成后，只补预加载删除相邻页后新露出的一页。
            if (index == currentIndex + 1) {
                setPageLoadNext(indexUpdate + 1);
            } else if (index == currentIndex - 1) {
                setPageLoadUp(indexUpdate - 1);
            }
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
        viewPagerView.unregisterOnPageChangeCallback(onPageChange);
        try {
            if (getResumeIndex() != videoDelBean.indexUpdate) {
                //删除后继续显示保留页，仅修正页码，不重新加载视频。
                viewPagerView.setCurrentItem(videoDelBean.indexUpdate, false);
            }
            adapter.notifyDataSetChanged();
        } finally {
            viewPagerView.registerOnPageChangeCallback(onPageChange);
        }
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
     * 一定是先添加videos里的数据，在检查frg，是插入还是单纯更换位置
     *
     * 在非当前页的位置插入广告，按删除页面时相同的思路保留页面和播放器，再调整槽位。
     * 调用前先在 videos 对应位置插入空字符串，并设置 ads；本方法将数据数量加一，
     * 因此无需提前调用 setUpdateDataSize。业务列表和广告数据均由调用方维护。
     *
     * <p>分三种情况：插在原当前索引的下一位、上一位、其它位置。
     * 相邻插入时保留原当前页、原前后页及广告，最多四页，并移出一个其它 Fragment；
     * 广告页面仅在相邻插入时通过 getFrg 创建，其它位置不创建页面。
     * 例如当前播放 C，在 D 的位置插入 Y，保留 B、C、Y、D，D 移到 Y 后面。
     * 其它位置只保留原当前页和前后页并调整槽位，广告数据后续自然加载。
     * 复用池始终为七个 Fragment，多余播放器释放，保留页面的播放器不更换。
     *
     * <p>插在当前页之前时，当前视频的新索引加一；插在之后时不变。
     * 数据数量与槽位更新后刷新，当前视频继续使用原 Fragment 和播放器。
     * 只为相邻插入的广告安排预加载，原前后页的数据与播放器继续保留。
     *
     * @param index 插入前列表中的插入位置，从 0 开始，允许追加到末尾，不允许等于当前页索引。
     */
    public void addVideoFrg(int index) {
        if (adapter == null || viewPagerView == null) {
            return;
        }
        int size = adapter.getItemCount();
        if (size == 0 || index < 0 || index > size) {
            return;
        }
        int currentIndex = getResumeIndex();
        if (index == currentIndex) {
            return;
        }

        //先取得插入前的当前页及前后页，保留已有数据和播放器。
        VideoBaseFrg0 frgNow = getPageFrg(currentIndex);
        VideoBaseFrg0 frgUp = currentIndex > 0 ? getPageFrg(currentIndex - 1) : null;
        VideoBaseFrg0 frgDown = currentIndex + 1 < size ? getPageFrg(currentIndex + 1) : null;
        VideoBaseFrg0 removed = null;
        int indexUpdate = currentIndex;
        if (index == (currentIndex - 1)) {
            //相邻的上一个
            VideoBaseFrg0 frg = getFrg();
            //原上一页、当前页及下一页都后移一位，广告放在原上一页之前。
            getPlayerManager().setDelExoPlayerHold3(frgNow, frgUp, frgDown);
            removed = adapter.addFrg(index, frg, currentIndex);
            indexUpdate = currentIndex + 1;
            if (canLoadPre) {
                setFrgPre(frg);
            }

        } else if (index == (currentIndex + 1)) {
            //相邻的下一个
            VideoBaseFrg0 frg = getFrg();
            //当前页及上一页不变，原下一页移动到广告后面。
            getPlayerManager().setDelExoPlayerHold3(frgNow, frgUp, frgDown);
            removed = adapter.addFrg(index, frg, currentIndex);
            if (canLoadPre) {
                setFrgPre(frg);
            }
        } else {
            //其它
            getPlayerManager().setDelExoPlayerHold3(frgNow, frgUp, frgDown);
            adapter.setFrgUpdate(index, currentIndex);
            if (index < currentIndex) {
                indexUpdate = currentIndex + 1;
            }
        }

        if (removed != null) {
            //释放被移出复用池的页面对应的播放器，并移除其待预加载记录。
            setDelFrg(removed);
        }
        viewPagerView.unregisterOnPageChangeCallback(onPageChange);
        try {
            if (indexUpdate != currentIndex) {
                //仅调整当前视频的页码，继续使用原 Fragment 和播放器。
                viewPagerView.setCurrentItem(indexUpdate, false);
            }
            adapter.notifyDataSetChanged();
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

    //视图创建后完成已登记的预加载；相邻插入后，也接续移到上二页或下二页的任务。
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
        } else if (index >= 2 && frg == getPageFrg(index - 2)) {
            //相邻插入后，已登记的广告或原上一页可能位于上二页。
            setPageVideoPre(frg, index - 2);
        } else if (index + 2 < size && frg == getPageFrg(index + 2)) {
            //插入下一页广告后，原下一页移动到下二页，继续完成已登记的预加载。
            setPageVideoPre(frg, index + 2);
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
