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
import media.library.player.videoNew.adapter.VideoPagerAdapter3;
import media.library.player.videoNew.frg2.VideoBaseFrg0;
import media.library.player.videoNew.frg2.VideoFrg1;
import media.library.player.videoNew.frg2.VideoFrg11;
import media.library.player.view.CustomExoPlayer;

//短视频播放管理类
public class ShortVideoManager2 {

    private AppCompatActivity activity;
    private Fragment fragment;


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

    private ViewPager2 viewPagerView;
    protected VideoPagerAdapter3 adapter;
    private VideoBaseFrg0 cursorVideoFrg;
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
            cursorVideoFrg = adapter.getIndexAtFrg(pageCurrentItem);
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

    private MorePlayerManager playerManager;

    private MorePlayerManager getPlayerManager() {
        if (playerManager == null) {
            playerManager = new MorePlayerManager();
        }
        return playerManager;
    }

    protected void setClick() {
        viewPagerView.registerOnPageChangeCallback(onPageChange);
    }

    public void onDestroy() {
        getPlayerManager().setExoPlayerRelease();
        viewPagerView.unregisterOnPageChangeCallback(onPageChange);
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

    //更新数据数量
    public void setUpdateDataSize(int dataSzie) {
        adapter.setDataSize(dataSzie);

    }

    //跳转指定页面，并且获取对象
    public VideoBaseFrg0 setPageCurrentItem0() {
        return setPageCurrentItem(0);
    }

    //跳转指定页面，并且获取对象
    public VideoBaseFrg0 setPageCurrentItem(int pageIndex) {
        int size = adapter.getDataSize();
        if (pageIndex >= size) {
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


    /**
     * 设置视频播放
     *
     * @param index 第几个
     */
    public void setVideoPlay(int index) {
        VideoBaseFrg0 frg = setPageCurrentItem(index);
        if (frg == null) {
            return;
        }
        if (!isFrgReady(frg)) {
            //没有被初始化，走预加载逻辑
            setFrgPre(frg, index);
        } else {
            if (frg instanceof VideoFrg1) {
                ((VideoFrg11) frg).setVideoPlay(index);
                ((VideoFrg11) frg).setDataUpdate(index);
            }
        }
        setPageLoadNext();
        if (index != 0) {
            setPageLoadUp();
        }
    }

    //调用暂停
    protected void setVideoPause(VideoBaseFrg0 frg) {
        if (frg != null && frg instanceof VideoFrg1) {
            ((VideoFrg1) frg).setVideoPause();
        }
    }

    //调用播放
    protected void setVideoPlay(VideoBaseFrg0 frg) {
        if (frg != null && frg instanceof VideoFrg1) {
            ((VideoFrg1) frg).setVideoPlay();
        }
    }

    //设置页面视频预加载
    protected void setPageVideoPre(Fragment frg, int pageIndex) {
        if (frg instanceof VideoFrg11) {
            ((VideoFrg11) frg).setVideoPre(pageIndex);
        }
    }

    //更新播放进度
    protected void setUpdatePlayProgress(VideoBaseFrg0 frg) {
        if (frg != null && frg instanceof VideoFrg1) {
            ((VideoFrg1) frg).setUpdatePlayProgress();
        }
    }


    //获取当前页面对象
    public VideoBaseFrg0 getCursorVideoFrg() {
        return cursorVideoFrg;
    }

    //获取指定页面对象
    private VideoBaseFrg0 getCursorFrg(int index) {
        return adapter.getIndexAtFrg(index);
    }

    //获取播放器
    public CustomExoPlayer getExoPlayer(Integer pageIndex) {
        PlayerLog.d("播放视频", "更新  urlIndexs  index:" + pageIndex);
        CustomExoPlayer exoPlayer = getPlayerManager().getCustomExoPlayer(pageIndex);
        return exoPlayer;
    }

    //获取当前的播放器
    public CustomExoPlayer getCursorVideoPlayer() {
        if (cursorVideoFrg == null) {
            return null;
        }
        if (cursorVideoFrg instanceof VideoFrg1) {
            return ((VideoFrg1) cursorVideoFrg).getExoPlayer();
        }
        return null;
    }


    public void onResume() {
        handlerUi.start();
    }


    public void onPause() {
        handlerUi.stop();
    }


    /**
     * 设置视频预加载(在播放之前)
     *
     * @param act
     * @param index      第几个（最多7个）
     * @param videoUrl   视频url
     * @param playerView 播放视图（一般没有）
     */
    @OptIn(markerClass = UnstableApi.class)
    public void setVideoLoadPre(Activity act, int index, String videoUrl, PlayerView playerView) {
        CustomExoPlayer exoPlayer = getExoPlayer(index);
        //原本就存在要加载的Url
        boolean isSame = exoPlayer.isEqualVideoPlay(videoUrl);
        if (isSame) {
            exoPlayer.setPlayerView(playerView);
            return;
        }
        exoPlayer.setPlayerVideo(act, videoUrl, true);
        //
        exoPlayer.setPlayerView(playerView);
        exoPlayer.prepare();
        exoPlayer.setPlayWhenReady(false);//准备好 就暂停
        //循环播放
        exoPlayer.setRepeatMode(Player.REPEAT_MODE_ALL);
    }

    private boolean isDel;

    /**
     * 删除一个正在播放的
     * 先删除数据，再更正下一个frg的索引，再删除播放器，再删除frg
     */
    public void delData() {
        int index = getResumeIndex();
        isDel = true;
        int size = adapter.getDataSize();
        if (size == 0) {
            return;
        }
        int indexNext = index + 1;//下一个
        if (indexNext < size) {
            //不是最后一个 更改下一个 的索引
            VideoBaseFrg0 frg = getCursorFrg(indexNext);
            if (frg instanceof VideoFrg11) {
                ((VideoFrg11) frg).setUpdateIndex(index);
            }
        }
        //从列表删除这个播放器
        playerManager.setDelExoPlayer(index);
        //去掉这个frg的预加载
        setFrgPreDel(adapter.getFragmentAt(index));
        //删除一个frg再补一个frg，并且 释放播放器
        adapter.removeFrg(index, getFrg());
        //预加载
        if (indexNext == size) {
            setPageLoadUp();
        } else {
            setPageLoadNext();
        }
    }

    //播放下一个
    public void setPagePlayNext() {
        boolean isInfinite = adapter.isInfinite();
        int index = getResumeIndex();
        if (isInfinite) {
            //无限循环
            viewPagerView.setCurrentItem(index + 1, true);
        } else {
            int size = adapter.getDataSize();
            int indexNew1 = index + 1;
            if (indexNew1 < size) {
                viewPagerView.setCurrentItem(indexNew1, true);
            }
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
        //下划 要预加载上一页
        int dataSize = adapter.getDataSize();
        if (indexUp >= 0 && indexUp < dataSize) {
            VideoBaseFrg0 videoFrgUp = getCursorFrg(indexUp);
            if (isFrgReady(videoFrgUp)) {
                setPageVideoPre(videoFrgUp, indexUp);
            } else {
                setFrgPre(videoFrgUp, indexUp);
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
        //上划 要预加载下一页
        int dataSize = adapter.getDataSize();
        if (indexNext >= dataSize) {
            return;
        }
        VideoBaseFrg0 videoFrgDow = getCursorFrg(indexNext);
        if (isFrgReady(videoFrgDow)) {
            setPageVideoPre(videoFrgDow, indexNext);
        } else {
            setFrgPre(videoFrgDow, indexNext);

        }
    }

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

    //要预加载的frg对象

    private HashMap<Fragment, Integer> mapFrg = new HashMap<>();

    protected void setFrgPre(Fragment frgPre, int indexPre) {
        mapFrg.put(frgPre, indexPre);
    }

    protected void setFrgPreDel(Fragment frg) {
        mapFrg.remove(frg);
    }

    //frg 已创建时的回调
    public void setFrgAttach(Fragment frg) {
        if (!mapFrg.containsKey(frg)) {
            return;
        }
        Integer index = mapFrg.get(frg);
        setFrgPreDel(frg);
        if (index == null) {
            return;
        }
        setPageVideoPre(frg, index);
    }


    class OnPageChange extends ViewPager2.OnPageChangeCallback {
        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            if (isDel) {
                //删除 有自己的与加载逻辑
                isDel = false;
                return;
            }
            setPageLoadNext();
            setPageLoadUp();

        }


        @Override
        public void onPageScrollStateChanged(int state) {
            // 当页面滚动状态改变时调用，例如开始滑动、滑动结束等
            switch (state) {
                case ViewPager.SCROLL_STATE_DRAGGING:
                    //开始滑动
                    PlayerLog.d("页面滑动开始", "index=" + viewPagerView.getCurrentItem());
                    setVideoPause(getCursorVideoFrg());
                    break;
                case ViewPager.SCROLL_STATE_SETTLING:
                    // 仍在滑动但即将停止
                    break;
                case ViewPager.SCROLL_STATE_IDLE:
                    // 滑动结束
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

    //获取 播放进度的Handler
    private HandlerUi handlerUi = new HandlerUi();

    class HandlerUi extends Handler {
        private void start() {
            removeMessages(1);
            sendEmptyMessageDelayed(1, 1 * 1000);
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
                    sendEmptyMessageDelayed(1, 1 * 1000);
                    break;
            }
        }
    }

    private OnVideoLoading onVideoLoading;

    public void setOnVideoLoading(OnVideoLoading onVideoLoading) {
        this.onVideoLoading = onVideoLoading;
    }

}
