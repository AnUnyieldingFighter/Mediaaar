package media.library.player.videoNew.act.frg;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.images.imageselect.R;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.widget.ViewPager2;
import media.library.player.bean.TestVideoUrl;
import media.library.player.bean.VideoPlayVo;
import media.library.player.videoNew.able.OnVideoData2;
import media.library.player.videoNew.able.OnVideoLoading;
import media.library.player.videoNew.able.OnVideoOperate2;
import media.library.player.videoNew.frg2.VideoBaseFrg0;
import media.library.player.videoNew.frg2.VideoFrg1;
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
    private View viewDataLoading;
    private TextView tvLoadingData;
    private ShortVideoManager2 videoPlaysManager;

    private void setViewInit(@NonNull View view, @Nullable Bundle savedInstanceState) {

        viewPager2 = view.findViewById(R.id.view_pager);
        viewDataLoading = view.findViewById(R.id.view_data_loading);
        tvLoadingData = view.findViewById(R.id.tv_loading_data);
        videoPageRl = view.findViewById(R.id.view_root);

        videoPlaysManager = new ShortVideoManager2();
        videoPlaysManager.setOnVideoLoading(new OnVideoLoading() {
            @Override
            public void onVideoLoadingUp(int index, int indexUp, VideoBaseFrg0 videoFrgNow, VideoBaseFrg0 videoFrgUp) {
                //预加载上一页数据
                videoFrgUp.setVideoDataPlay(indexUp);
            }

            @Override
            public void onVideoLoadingDow(int index, int indexDow, VideoBaseFrg0 videoFrgNow, VideoBaseFrg0 videoFrgDow) {
                //预加载下一页数据
                videoFrgDow.setVideoDataPlay(indexDow);
            }

            @Override
            public ArrayList<VideoBaseFrg0> getPages() {
                ArrayList pages = new ArrayList<VideoBaseFrg0>();
                pages.add(new VideoFrg1());
                pages.add(new VideoFrg1());
                pages.add(new VideoFrg1());
                pages.add(new VideoFrg1());
                pages.add(new VideoFrg1());
                pages.add(new VideoFrg1());
                pages.add(new VideoFrg1());
                return pages;
            }
        });
        videoPlaysManager.setView(this, viewPager2);
    }

    private void setClick() {
        videoPageRl.setOnVerticalListener(new VideoPageRl2.OnVerticalListener() {
            @Override
            public void onVertical() {
                if (isMoreReq) {
                    return;
                }
                if (videos.size() == 0) {
                    return;
                }
                int resumeIndex = videoPlaysManager.getResumeIndex();
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

    @Override
    public void onDestroy() {
        videoPlaysManager.onDestroy();
        super.onDestroy();
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
        videos.addAll(urls);
        videoPlaysManager.setUpdateDataSize(videos.size());
        VideoBaseFrg0 videoFrg = videoPlaysManager.setPageCurrentItem0();
        videoFrg.setVideoDataPlay(-1);
        videoFrg.setDataUpdate(0);
    }

    //设置页面
    private void setPageDataMore(ArrayList<String> urls) {
        /*if (videos.contains(url)) {
        //不去重
            return;
        }*/
        videos.addAll(urls);
        videoPlaysManager.setUpdateDataSize(videos.size());
    }

    //
    private ArrayList<String> videos = new ArrayList();
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
                    isMoreNot = false;
                    isDataReq = false;
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

    @Override
    public CustomExoPlayer getExoPlayer(Integer pageIndex, String videoUrl) {
        CustomExoPlayer customExoPlayer = videoPlaysManager.getExoPlayer(pageIndex);
        customExoPlayer.setARB(true);
        return customExoPlayer;
    }

    @Override
    public void setViewPageSlide(boolean isSlide) {
        videoPlaysManager.setViewPageSlide(isSlide);
    }

    @Override
    public void recordDuration(String id, int pageIndex, long pro, long total) {
        //进度回调

    }

    @Override
    public void onCheck(Object str, Object obj) {
        //一些检查
    }

    //获取播放数据 重要的是视频路径  可以为空 vo可以为null
    @Override
    public VideoPlayVo getVideoPlayData(int pageIndex) {
        var index = pageIndex;
        if (index == -1) {
            //当前页面
            index = videoPlaysManager.getResumeIndex();
        }
        String videoUrl = "";
        if (videos != null && videos.size() > 0) {
            videoUrl = videos.get(index);
        }
        VideoPlayVo vo = new VideoPlayVo();
        vo.pageIndex = index;
        vo.url = videoUrl;
        vo.id = "-1";
        return vo;

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

}
