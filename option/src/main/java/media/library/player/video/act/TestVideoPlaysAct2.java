/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package media.library.player.video.act;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import com.images.imageselect.R;

import media.library.player.video.able.OnVideoOperate;
import media.library.player.video.adapter.VideoPagerAdapter2;
import media.library.player.bean.TestVideoUrl;
import media.library.player.video.frg.VideoBaseFrg;
import media.library.player.video.frg.VideoFrg;
import media.library.player.manager.PlayerLog;
import media.library.player.view.CustomExoPlayer;

//短视频播放  是 TestVideoPlaysAct 的另外一种写法
public class TestVideoPlaysAct2 extends AppCompatActivity implements OnVideoOperate {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_test_video_page);
        initView();
        setClick();
    }

    private ViewPager2 viewPagerView;
    private VideoPagerAdapter2 adapter;
    private ArrayList<VideoBaseFrg> videoFrgs;
    private VideoFrg cursorVideoFrg;
    private OnPageChange onPageChange;

    protected void initView() {
        viewPagerView = findViewById(R.id.viewPager);
        adapter = new VideoPagerAdapter2(this);
        viewPagerView.setAdapter(adapter);
        //videoFrgs = getFrgs();
        videoFrgs = getFrgs2();
        adapter.setFrgs(videoFrgs);
        //设置为1时 显示第五个页面时 才销毁第第一个
        viewPagerView.setOffscreenPageLimit(1);
        onPageChange = new OnPageChange();
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

    protected void setClick() {
        viewPagerView.registerOnPageChangeCallback(onPageChange);
    }

    @Override
    protected void onDestroy() {
        setExoPlayerRelease();
        viewPagerView.unregisterOnPageChangeCallback(onPageChange);
        super.onDestroy();
    }


    //释放资源
    private void setExoPlayerRelease() {
        Set<Integer> keys = players.keySet();
        for (Integer key : keys) {
            CustomExoPlayer player = players.get(key);
            if (player != null) {
                player.release();
            }
        }
        players.clear();
    }


    private ArrayList<String> urls = new TestVideoUrl().buildTestVideoUrls();

    public String getVideoUrl(int index) {
        if (urls == null) {
            urls = new TestVideoUrl().buildTestVideoUrls();
        }
        int pos = index % urls.size();
        return urls.get(pos);
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

    //播放器个数
    private int playersMax = 7;
    //缓存数量等于 frgs 的数量
    private HashMap<Integer, CustomExoPlayer> players = new HashMap<>();
    //缓存数量等于 frgs 的数量
    private HashMap<Integer, String> urlIndexs = new HashMap<>();

    //给Fragment调用
    @Override
    public CustomExoPlayer getExoPlayer(Integer pageIndex, String videoUrl) {
        //它是0至 playersMax
        Integer urlIndex = getUrlIndex(videoUrl);
        if (urlIndex != null) {
            PlayerLog.d("播放视频", "计划通过url取ExoPlayer urlIndex=" + urlIndex + " videoUrl:" + videoUrl);
            return getExoPlayer(urlIndex);
        }
        int index = pageIndex % playersMax;
        urlIndexs.put(index, videoUrl);
        PlayerLog.d("播放视频", "更新  urlIndexs  index:" + index + " videoUrl:" + videoUrl);
        return getExoPlayer(index);
    }

    @Override
    public void setViewPageSlide(boolean isSlide) {
        viewPagerView.setUserInputEnabled(isSlide);
    }

    //记录观看时长
    @Override
    public void recordDuration(String id, int pageIndex, long pro, long total) {

    }


    @Override
    public void onCheck(Object str, Object obj) {

    }

    @Override
    public int getResumeIndex() {
        int index = viewPagerView.getCurrentItem();
        return index;
    }

    @Override
    public String getResumeVideoUrl() {
        int index = viewPagerView.getCurrentItem();
        String videoUrl = getVideoUrl(index);
        return videoUrl;
    }

    private Integer getUrlIndex(String videoUrl) {
        Set<Integer> keys = urlIndexs.keySet();
        for (Integer key : keys) {
            String url = urlIndexs.get(key);
            if (videoUrl.equals(url)) {
                return key;
            }
        }
        return null;
    }

    public CustomExoPlayer getExoPlayer(Integer frgsIndex) {
        CustomExoPlayer player = players.get(frgsIndex);
        if (player == null) {
            player = new CustomExoPlayer();
            players.put(frgsIndex, player);
            PlayerLog.d("播放视频", "取ExoPlayer  新增加 frgsIndex=" + frgsIndex);
        } else {
            PlayerLog.d("播放视频", "取ExoPlayer  取缓存：" + frgsIndex);
        }
        PlayerLog.d("播放视频", "当前缓存数量 players=" + players.size() + " urlIndexs=" + urlIndexs.size());
        return player;
    }

    @Override
    protected void onResume() {
        super.onResume();
        handlerUi.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        handlerUi.stop();
    }

    class OnPageChange extends ViewPager2.OnPageChangeCallback {
        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            // 当页面滑动结束时调用，即页面完全改变时
            int indexPage = viewPagerView.getCurrentItem();
            cursorVideoFrg = getCursorFrg(indexPage);
            //
            PlayerLog.d("页面滑动 最后结果", "index=" + indexPage);
            if (indexPage > startPage) {
                //上划 要预加载下一页
                int index = indexPage + 1;
                getCursorFrg(index).updateVideoUrl(index, getVideoUrl(index));
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
