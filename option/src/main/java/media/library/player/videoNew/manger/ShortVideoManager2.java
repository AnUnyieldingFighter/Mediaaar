package media.library.player.videoNew.manger;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;


import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;
import media.library.player.manager.MorePlayerManager;
import media.library.player.manager.PlayerLog;
import media.library.player.videoNew.able.OnVideoLoading;
import media.library.player.videoNew.adapter.VideoPagerAdapter3;
import media.library.player.videoNew.frg2.VideoBaseFrg0;
import media.library.player.videoNew.frg2.VideoFrg1;
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
        if (onVideoLoading != null) {
            return onVideoLoading.getPages();
        }
        ArrayList<VideoBaseFrg0> frgs = new ArrayList<>();
        frgs.add(new VideoFrg1());
        frgs.add(new VideoFrg1());
        frgs.add(new VideoFrg1());
        frgs.add(new VideoFrg1());
        frgs.add(new VideoFrg1());
        frgs.add(new VideoFrg1());
        frgs.add(new VideoFrg1());
        return frgs;
    }


    //无用
    public void onCheck(Object str, Object obj) {
        if (obj instanceof ExoPlayer) {
            calculateFrgHoldPlayer(str.toString(), (ExoPlayer) obj);
        }
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
        int currentItem = viewPagerView.getCurrentItem();
        VideoBaseFrg0 videoFrg = adapter.getIndexAtFrg(pageIndex);
        if (currentItem == pageIndex) {
            return videoFrg;
        }
        //调用暂停
        getCursorVideoFrg().setVideoPause();
        viewPagerView.setCurrentItem(pageIndex, false);
        return videoFrg;
    }

    //获取当前页面对象
    public VideoBaseFrg0 getCursorVideoFrg() {
        return cursorVideoFrg;
    }

    //多余的
    private VideoBaseFrg0 getCursorFrg() {
        int index = viewPagerView.getCurrentItem();
        return getCursorFrg(index);
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
        return cursorVideoFrg.getExoPlayer();
    }

    //计算持有者数量（排除问题用）
    private void calculateFrgHoldPlayer(String pageIndex, ExoPlayer player) {
        int frgSize = adapter.getFrgSize();
        for (int i = 0; i < frgSize; i++) {
            VideoBaseFrg0 frg = adapter.getIndexAtFrg(i);
            CustomExoPlayer tempPlayer = frg.getExoPlayer();
            if (tempPlayer == null) {
                continue;
            }
            if (tempPlayer == player) {
                int tempIndex = frg.getPageIndex();
                PlayerView playerView = frg.getPlayerView();
                if (playerView == null) {
                    PlayerLog.d("播放器", "页面" + pageIndex + "的播放器，被页面" + tempIndex + "所持有，但是播放视图未初始化");
                } else {
                    Player tempPlayer2 = playerView.getPlayer();
                    if (tempPlayer2 == null) {
                        PlayerLog.d("播放器", "页面" + pageIndex + "的播放器，被页面" + tempIndex + "所持有,但是播放视图未设置播放器");
                    } else {
                        PlayerLog.d("播放器", "页面" + pageIndex + "的播放器，被页面" + tempIndex + "所持有");
                    }
                }

            }
        }
    }

    //true 删除
    private boolean isDel;

    public void delFrg(int index) {
        isDel = true;
        setAdapter();
        cursorVideoFrg = null;
        PlayerLog.d("页面删除", "index=" + index);
        if (index >= 0) {
            viewPagerView.setCurrentItem(index, false);
        }
    }

    public void onResume() {
        handlerUi.start();
    }


    public void onPause() {
        handlerUi.stop();
    }

    class OnPageChange extends ViewPager2.OnPageChangeCallback {
        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            // 当页面滑动结束时调用，即页面完全改变时
            int indexPage = viewPagerView.getCurrentItem();
            cursorVideoFrg = getCursorFrg(indexPage);
            PlayerLog.d("页面滑动 选中", "index=" + indexPage + " 删除：" + isDel);
            if (isDel) {
                isDel = false;
                return;
            }
            if (indexPage > startPage) {
                //上划 要预加载下一页
                var isInfinite = adapter.isInfinite();
                int index = indexPage + 1;
                if (isInfinite) {
                    //无穷页面
                    if (onVideoLoading != null) {
                        VideoBaseFrg0 videoFrgDow = getCursorFrg(index);
                        VideoBaseFrg0 videoFrgNow = getCursorFrg(indexPage);
                        onVideoLoading.onVideoLoadingDow(indexPage, index, videoFrgNow, videoFrgDow);
                    }
                } else {
                    //
                    int dataSize = adapter.getDataSize();
                    if (index >= dataSize) {
                        return;
                    }
                    if (onVideoLoading != null) {
                        VideoBaseFrg0 videoFrgDow = getCursorFrg(index);
                        VideoBaseFrg0 videoFrgNow = getCursorFrg(indexPage);
                        onVideoLoading.onVideoLoadingDow(indexPage, index, videoFrgNow, videoFrgDow);
                    }
                }
            } else {
                //下划 要预加载上一页
                int index = indexPage - 1;
                if (index >= 0 && onVideoLoading != null) {
                    VideoBaseFrg0 videoFrgUp = getCursorFrg(index);
                    VideoBaseFrg0 videoFrgNow = getCursorFrg(indexPage);
                    onVideoLoading.onVideoLoadingUp(indexPage, index, videoFrgNow, videoFrgUp);
                }
            }

        }

        private int startPage = 0;

        @Override
        public void onPageScrollStateChanged(int state) {
            // 当页面滚动状态改变时调用，例如开始滑动、滑动结束等
            switch (state) {
                case ViewPager.SCROLL_STATE_DRAGGING:
                    startPage = viewPagerView.getCurrentItem();
                    //开始滑动
                    PlayerLog.d("页面滑动开始", "index=" + viewPagerView.getCurrentItem());
                    getCursorVideoFrg().setVideoPause();
                    break;
                case ViewPager.SCROLL_STATE_SETTLING:
                    // 仍在滑动但即将停止
                    break;
                case ViewPager.SCROLL_STATE_IDLE:
                    // 滑动结束
                    PlayerLog.d("页面滑动结束", "index=" + viewPagerView.getCurrentItem());
                    getCursorVideoFrg().setVideoPlay();
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
                    if (cursorVideoFrg != null) {
                        cursorVideoFrg.setUpdatePlayProgress();
                    }
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
