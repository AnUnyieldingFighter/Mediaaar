package media.library.player.videoNew.act.frg;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.images.imageselect.R;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.processing.SurfaceProcessorNode;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.media3.common.Player;
import androidx.media3.ui.PlayerView;
import androidx.viewpager2.widget.ViewPager2;
import media.library.player.bean.TestVideoUrl;
import media.library.player.bean.VideoPlayVo;
import media.library.player.videoNew.able.OnVideoData2;
import media.library.player.videoNew.able.OnVideoLoading;
import media.library.player.videoNew.able.OnVideoOperate2;
import media.library.player.videoNew.frg2.VideoBaseFrg0;
import media.library.player.videoNew.frg2.VideoFrg11;
import media.library.player.videoNew.manger.ShortVideoManager2;
import media.library.player.view.CustomExoPlayer;
import media.library.player.view.VideoPageRl2;


public class VideoMainFrg extends Fragment implements OnVideoOperate2, OnVideoData2 {
    protected FragmentActivity act;
    protected Fragment relyFrg;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.media_frg_main_video, container, false);
        return view;

    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        act = getActivity();
        relyFrg = this.getParentFragment();
        setViewInit(view, savedInstanceState);
        setClick();
    }

    private ViewPager2 viewPager2;
    private VideoPageRl2 videoPageRl;
    private View viewDataLoading, tvDataDel;
    private TextView tvLoadingData, tvAudioPar;
    private View viewEmpty;
    private ShortVideoManager2 videoPlaysManager;

    private void setViewInit(@NonNull View view, @Nullable Bundle savedInstanceState) {

        viewPager2 = view.findViewById(R.id.view_pager);
        tvDataDel = view.findViewById(R.id.tv_data_del);
        viewDataLoading = view.findViewById(R.id.view_data_loading);
        viewEmpty = view.findViewById(R.id.view_video_empty);
        tvLoadingData = view.findViewById(R.id.tv_loading_data);
        tvAudioPar = view.findViewById(R.id.tv_audio_par);
        videoPageRl = view.findViewById(R.id.view_root);

        videoPlaysManager = new ShortVideoManager2();
        videoPlaysManager.setOnVideoLoading(new OnVideoLoading() {
            @Override
            public VideoBaseFrg0 getVideoPage() {
                return new VideoFrg11();
            }
        });
        videoPlaysManager.setView(this, viewPager2);
        videoPlaysManager.setUpdateDataSize(videos.size());
        viewEmpty.setVisibility(videos.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void setClick() {
        videoPageRl.setOnVerticalListener(new VideoPageRl2.OnVerticalListener() {
            @Override
            public void onVertical() {
                if (true) {
                    //先不要
                    return;
                }
                //触发加载更多
                if (isMoreReq) {
                    return;
                }
                if (videos.size() == 0) {
                    return;
                }
                int resumeIndex = videoPlaysManager.getVideoDataIndex(videoPlaysManager.getResumeIndex());
                int dataNum = (videos.size() - 1);
                if (resumeIndex != dataNum) {
                    return;
                }
                setDataMore(true);
            }
        });
        tvLoadingData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDataReq) {
                    return;
                }
                isDataReq = true;
                tvLoadingData.setText("加载中...");
                handlerData.sendEmptyMessageDelayed(0, 1 * 1000);
            }
        });
        tvAudioPar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoPlaysManager.getCursorVideoPlayer().testTrackSelector();
            }
        });
        tvDataDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //删除一个视频
                int resumeIndex = videoPlaysManager.getResumeIndex();
                if (videoPlaysManager.getCursorVideoFrg() == null) {
                    return;
                }
                int videoIndex = videoPlaysManager.getVideoDataIndex(resumeIndex);
                if (videoIndex >= 0) {
                    Log.d("删除", "索引：" + resumeIndex + "\nurl=" + videos.get(videoIndex));
                    //广告也使用列表索引，删除占位或视频后同步后面广告的位置。
                    ads.remove(videoIndex);
                    for (int i = videoIndex; i < videos.size(); i++) {
                        String adUrl = ads.remove(i + 1);
                        if (adUrl != null) {
                            ads.put(i, adUrl);
                        }
                    }
                }
                videoPlaysManager.delFrgPlay(VideoMainFrg.this);

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        videoPlaysManager.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        videoPlaysManager.onPause();
    }

    /**
     * 视图销毁时取消任务，释放管理器并清空视图引用。
     */
    @Override
    public void onDestroyView() {
        handlerData.removeCallbacksAndMessages(null);
        isDataReq = false;
        isMoreReq = false;
        if (videoPlaysManager != null) {
            videoPlaysManager.onDestroy();
            videoPlaysManager = null;
        }
        viewPager2 = null;
        videoPageRl = null;
        viewDataLoading = null;
        tvDataDel = null;
        tvLoadingData = null;
        tvAudioPar = null;
        viewEmpty = null;
        super.onDestroyView();
    }

    private boolean isMoreReq = false;

    private void setDataMore(boolean isShowView) {
        if (isMoreNot) {
            //没有更多了
            return;
        }
        if (isShowView) {
            viewDataLoading.setVisibility(View.VISIBLE);
        }
        if (isMoreReq) {
            return;
        }
        isMoreReq = true;
        //数据加载中
        handlerData.sendEmptyMessageDelayed(1, 3 * 1000);
    }

    //
    //设置页面
    private void setPageData(ArrayList<String> urls) {
        videos.clear();
        ads.clear();
        videos.addAll(urls);
        viewEmpty.setVisibility(videos.isEmpty() ? View.VISIBLE : View.GONE);
        videoPlaysManager.setUpdateDataSize(videos.size());
        videoPlaysManager.setVideoPlay(0, true);
    }

    //设置加载更多页面
    private void setPageDataMore(ArrayList<String> urls) {
        /*if (videos.contains(url)) {
        //不去重
            return;
        }*/
        videos.addAll(urls);
        viewEmpty.setVisibility(videos.isEmpty() ? View.VISIBLE : View.GONE);
        videoPlaysManager.setUpdateDataSize(videos.size());
    }

    //
    private ArrayList<String> videos = new ArrayList();
    private HashMap<Integer, String> ads = new HashMap<>();
    private HandlerData handlerData = new HandlerData();
    //true 没有更多了
    private boolean isMoreNot;
    private boolean isDataReq;

    class HandlerData extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 0:
                    //初始化加载数据
                    ArrayList<String> urls = new TestVideoUrl().buildTestVideoUrls3();
                    String video = "https://flutter.github.io/assets-for-api-docs/assets/videos/bee.mp4";
                    //播放不出来报错
                    String video2 = "https://nbc.vtnbo.com/nbc/msg/video/pro/17674882940931162.mp4";
                    //PlayerSupport.getInstance().test(act,video);
                    urls.add(video);
                    urls.add(video2);
                    isMoreNot = false;
                    isDataReq = false;
                    //
                    /*ArrayList<String> urls2 = new ArrayList<>();
                    urls2.add(video);
                    urls2.add(video2);
                    setPageData(urls2);*/
                    //
                    setPageData(urls);
                    tvLoadingData.setText("重新加载数据");
                    break;
                case 1:
                    //加载一条数据
                    isMoreReq = false;
                    isMoreNot = true;
                    viewDataLoading.setVisibility(View.GONE);
                    urls = new TestVideoUrl().buildTestVideoUrls();
                    setPageDataMore(urls);
                    break;
            }
        }
    }
    //=====================视频数据，播放 操作接口======================================

    /**
     * 获取当前显示页面的索引。
     */
    @Override
    public int getResumeIndex() {
        return videoPlaysManager.getResumeIndex();
    }

    /**
     * 根据页面对象获取播放器。
     */
    @Override
    public CustomExoPlayer getExoPlayer(Fragment frg, String videoUrl) {
        CustomExoPlayer customExoPlayer = videoPlaysManager.getExoPlayer(frg, videoUrl);
        customExoPlayer.setARB(true);
        return customExoPlayer;
    }

    /**
     * 页面销毁时移除对应关系并释放播放器。
     */
    @Override
    public void setDelFrg(Fragment frg) {
        //父视图销毁时已统一释放，随后到达的子页面销毁回调无需再次处理。
        if (videoPlaysManager != null) {
            videoPlaysManager.setDelFrg(frg);
        }
    }

    @Override
    public void setViewPageSlide(boolean isSlide) {
        videoPlaysManager.setViewPageSlide(isSlide);
    }

    @Override
    public void recordDuration(String id, int pageIndex, long pro, long total, CustomExoPlayer customExoPlayer, Player player, PlayerView playerView, int type) {

    }

    @Override
    public void setPageChange(VideoBaseFrg0 frgExit, VideoBaseFrg0 frgEnter) {
        viewEmpty.setVisibility(frgEnter == null ? View.VISIBLE : View.GONE);
        Log.d("切换 退出：" + (frgExit != null), "切换 进入" + (frgEnter != null));
    }

    //获取播放数据 重要的是视频路径  可以为空 vo可以为null
    @Override
    public VideoPlayVo getVideoPlayData(int pageIndex, boolean isPre) {
        //videos 已包含广告占位，页码无需扣除广告；无限翻页时仅循环取列表索引。
        int index = videoPlaysManager.getVideoDataIndex(pageIndex);
        //数据删除或刷新后，旧索引可能已超出范围。
        if (videos == null || index < 0 || index >= videos.size()) {
            return null;
        }
        String videoUrl = videos.get(index);
        //Fragment 会复用，实际类型以本次数据为准，空字符串从 ads 获取广告地址。
        boolean isAd = "".equals(videoUrl);
        if (isAd) {
            videoUrl = ads.get(index);
        }
        VideoPlayVo vo = new VideoPlayVo();
        vo.pageIndex = pageIndex;
        vo.url = videoUrl;
        vo.isAd = isAd;
        vo.id = "-1";
        return vo;

    }

    @Override
    public void onFrgAttach(Fragment frg) {
        videoPlaysManager.setFrgAttach(frg);
    }
    //=====================其它数据操作接口======================================

    //获取视频详情 发布者头像，昵称，点赞，收藏 等
    @Override
    public Object getVideoDetailsData(int pageIndex) {
        return null;
    }

    //1 旋转到横屏 2 旋转到竖屏
    @Override
    public void onScreenRotation(int typeScreen) {

    }

    //删除这个索引的数据
    @Override
    public void setDataDel(int index) {
        videos.remove(index);
    }

}
