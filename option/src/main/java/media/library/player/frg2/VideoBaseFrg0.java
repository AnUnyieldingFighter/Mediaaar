package media.library.player.frg2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.media3.ui.PlayerView;
import media.library.player.view.CustomExoPlayer;

public class VideoBaseFrg0 extends Fragment {
    protected FragmentActivity act;
    protected Fragment relyFrg;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        int layoutId = getRootViewLayout();
        if (layoutId != 0) {
            View view = inflater.inflate(layoutId, container, false);
            return view;
        }
        return getRootView(inflater, container, savedInstanceState);
    }


    protected int getRootViewLayout() {
        return 0;
    }

    protected View getRootView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        act = getActivity();
        relyFrg = this.getParentFragment();
        setViewInit(view, savedInstanceState);
    }

    protected void setViewInit(View view, Bundle savedInstanceState) {
    }

    public CustomExoPlayer getExoPlayer() {
        return null;
    }

    //获取播放视图
    public PlayerView getPlayerView() {
        return null;
    }

    //获取页面索引
    public int getPageIndex() {
        return -1;
    }

    //设置暂停
    public void setVideoPause() {

    }

    //设置播放
    public void setVideoPlay() {

    }

    //更新播放进度
    public void setUpdatePlayProgress() {

    }

    //设置拉取播放数据   pageIndex-1:拉当前的数据（播放） 否则就拉指定页的数据（预加载）
    public void setVideoDataPlay(int pageIndex) {
    }

    public void setDataUpdate(int pageIndex) {

    }
}
