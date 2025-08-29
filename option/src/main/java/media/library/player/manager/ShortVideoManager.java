package media.library.player.manager;

import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.HashMap;

import media.library.player.adapter.VideoPagerAdapter2;
import media.library.player.bean.TestVideoUrl;
import media.library.player.frg.VideoBaseFrg;
import media.library.player.frg.VideoFrg;
import media.library.player.view.CustomExoPlayer;

//短视频播放管理类
public class ShortVideoManager {
    private AppCompatActivity activity;

    public void setData(ArrayList<VideoBaseFrg> videoFrgs, ArrayList<String> urls) {
        this.videoFrgs = videoFrgs;
        this.urls = urls;
    }

    public void setPageMax(int pageMax) {
        this.pageMax = pageMax;
    }

    public void setPageCurrentItem(int pageCurrentItem) {
        this.pageCurrentItem = pageCurrentItem;

    }

    public void setView(AppCompatActivity activity, ViewPager2 viewPagerView) {
        this.activity = activity;
        initView(viewPagerView);
        setClick();
    }

    private ViewPager2 viewPagerView;
    private VideoPagerAdapter2 adapter;
    private ArrayList<VideoBaseFrg> videoFrgs;
    private ArrayList<String> urls;
    private VideoFrg cursorVideoFrg;
    private OnPageChange onPageChange;


    //page页数量0阈值，超过这个值 将显示无限
    private int pageMax = 5;
    //初始化时跳转的页面
    private int pageCurrentItem = 0;

    protected void initView(ViewPager2 viewPagerView) {
        this.viewPagerView = viewPagerView;
        adapter = new VideoPagerAdapter2(activity, pageMax);
        viewPagerView.setAdapter(adapter);
        //videoFrgs = getFrgs();
        if (videoFrgs == null) {
            videoFrgs = getFrgs2();
        }
        adapter.setFrgs(videoFrgs);
        //设置为1时 显示第五个页面时 才销毁第第一个
        viewPagerView.setOffscreenPageLimit(1);
        onPageChange = new OnPageChange();
        if (pageCurrentItem > 0) {
            viewPagerView.setCurrentItem(pageCurrentItem, false);
        }
        if (pageCurrentItem >= 0) {
            cursorVideoFrg = (VideoFrg) videoFrgs.get(pageCurrentItem);
        }
    }

    public VideoFrg getCursorVideoFrg() {
        return cursorVideoFrg;
    }

    public void setViewPageSlide(boolean isSlide) {
        viewPagerView.setUserInputEnabled(isSlide);
    }

    public void onCheck(Object str, Object obj) {
        if (obj instanceof ExoPlayer) {
            calculateFrgHoldPlayer(str.toString(), (ExoPlayer) obj);
        }
    }

    private ArrayList<VideoBaseFrg> getFrgs2() {
        ArrayList<VideoBaseFrg> frgs = new ArrayList<>();
        frgs.add(VideoFrg.newInstance(0, getVideoUrl(0)));
        frgs.add(VideoFrg.newInstance(1, getVideoUrl(1)));
        frgs.add(VideoFrg.newInstance(2, getVideoUrl(2)));
        frgs.add(VideoFrg.newInstance(3, getVideoUrl(3)));
        frgs.add(VideoFrg.newInstance(4, getVideoUrl(4)));
        frgs.add(VideoFrg.newInstance(5, getVideoUrl(5)));
        frgs.add(VideoFrg.newInstance(6, getVideoUrl(6)));
        return frgs;
    }

    private ArrayList<VideoBaseFrg> getFrgs() {
        ArrayList<VideoBaseFrg> frgs = new ArrayList<>();
        frgs.add(new VideoFrg());
        frgs.add(new VideoFrg());
        frgs.add(new VideoFrg());
        frgs.add(new VideoFrg());
        frgs.add(new VideoFrg());
        frgs.add(new VideoFrg());
        frgs.add(new VideoFrg());
        return frgs;
    }

    public String getVideoUrl(int index) {
        if (urls == null) {
            urls = new TestVideoUrl().buildTestVideoUrls();
        }
        int pos = index % urls.size();
        return urls.get(pos);
    }

    protected void setClick() {
        viewPagerView.registerOnPageChangeCallback(onPageChange);
    }

    public void onDestroy() {
        getPlayerManager().setExoPlayerRelease();
        viewPagerView.unregisterOnPageChangeCallback(onPageChange);
    }

    public int getResumeIndex() {
        int index = viewPagerView.getCurrentItem();
        return index;
    }

    public String getResumeVideoUrl() {
        int index = viewPagerView.getCurrentItem();
        String videoUrl = getVideoUrl(index);
        return videoUrl;
    }

    private VideoFrg getCursorFrg() {
        int index = viewPagerView.getCurrentItem();
        return getCursorFrg(index);
    }

    private VideoFrg getCursorFrg(int index) {
        int pos = index % videoFrgs.size();
        PlayerLog.d("页面滑动 换取索引", "index=" + index + " pos=" + pos);
        return (VideoFrg) videoFrgs.get(pos);
    }


    //缓存数量等于 frgs 的数量
    private HashMap<Integer, String> urlIndexs = new HashMap<>();

    public CustomExoPlayer getExoPlayer(Integer pageIndex, String videoUrl) {
        int playersMax = getPlayerManager().getPlayersMax();
        int index = pageIndex % playersMax;
        urlIndexs.put(index, videoUrl);
        PlayerLog.d("播放视频", "更新  urlIndexs  index:" + index + " videoUrl:" + videoUrl);
        CustomExoPlayer exoPlayer = getPlayerManager().getCustomExoPlayer(index);
        return exoPlayer;
    }

    private MorePlayerManager playerManager;

    private MorePlayerManager getPlayerManager() {
        if (playerManager == null) {
            playerManager = new MorePlayerManager();
        }
        return playerManager;
    }


    //计算持有者数量（排除问题用）
    private void calculateFrgHoldPlayer(String pageIndex, ExoPlayer player) {
        for (int i = 0; i < videoFrgs.size(); i++) {
            VideoFrg frg = (VideoFrg) videoFrgs.get(i);
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
            PlayerLog.d("页面滑动 最后结果", "index=" + indexPage);
            if (indexPage > startPage) {
                //上划 要预加载下一页
                var isInfinite = adapter.isInfinite();
                int index = indexPage + 1;
                if (isInfinite) {
                    //无穷页面
                    getCursorFrg(index).updateVideoUrl(index, getVideoUrl(index));
                } else {
                    //
                    if (index >= videoFrgs.size()) {
                        return;
                    }
                    getCursorFrg(index).updateVideoUrl(index, getVideoUrl(index));
                }
            } else {
                //下划 要预加载上一页
                int index = indexPage - 1;
                if (index >= 0) {
                    getCursorFrg(index).updateVideoUrl(index, getVideoUrl(index));
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
                    getCursorFrg().setVideoPause();
                    break;
                case ViewPager.SCROLL_STATE_SETTLING:
                    // 仍在滑动但即将停止
                    break;
                case ViewPager.SCROLL_STATE_IDLE:
                    // 滑动结束
                    PlayerLog.d("页面滑动结束", "index=" + viewPagerView.getCurrentItem());
                    getCursorFrg().setVideoPlay();
                    break;
            }

        }


        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            // 当页面开始滑动时调用

        }
    }


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
}
