package com.images.ui.frag;


import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.images.config.entity.MediaEntity;
import com.images.imageselect.R;
import com.images.manager.MediaPlayerManager;
import com.images.ui.adapter.OnImgClickListener;
import com.images.ui.adapter.OnMediaImgIbl;
import com.images.ui.thing.photoview.PhotoView;
import com.images.ui.thing.photoview.PhotoViewAttacher;
import com.images.ui.views.VideoTextureView;

import java.io.Serializable;

public class VideoFragment extends MediaFragment implements MediaPlayerManager.OnMediaPlayerListener {

    private OnMediaImgIbl imgLoading;
    private MediaEntity mediaEntity;

    public void setOnImgClickListener(OnMediaImgIbl imgLoading) {
        this.imgLoading = imgLoading;
    }

    //无用
    public void setData(MediaEntity mediaEntity) {
        this.mediaEntity = mediaEntity;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return getView(inflater);
    }

    protected View getView(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.media_layout_video, null);
        return view;
    }

    private VideoTextureView textureView;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    protected void initView(View view) {
        Serializable bean = getArguments().getSerializable("bean");
        if (bean instanceof MediaEntity) {
            mediaEntity = (MediaEntity) bean;
            String other = mediaEntity.other;
            if (TextUtils.isEmpty(other)) {
                type = 1;
            }
            if ("1".equals(other)) {
                type = 1;
            }
            if ("2".equals(other)) {
                type = 2;
            }
        }
        textureView = view.findViewById(R.id.texture_view);
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                // 当 SurfaceTexture 可用时执行的操作
                isAvailable = true;
                if (isSetData && manager == null) {
                    initPlayer();
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                // SurfaceTexture 尺寸变化时的操作
                setPlayViewZoom(textureView);

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                isAvailable = false;
                return true; // 返回 true 表示 SurfaceTexture 已被销毁
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                // SurfaceTexture 更新时的操作
                setPlayViewZoom(textureView);
            }
        });
    }

    private boolean isAvailable;
    private boolean isSetData;
    protected MediaPlayerManager manager;

    @Override
    public void onResume() {
        super.onResume();
        setResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (manager != null) {
            manager.setMediaPlayerPause();
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        if (manager != null) {
            manager.setMediaPlayerStop();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (manager != null) {
            manager.onDestroy();
        }

    }

    protected void setResume() {
        if (!isSetData) {
            //播放初始化
            isSetData = true;
            initPlayer();
            return;
        }
        if (manager != null && manager.isStopPlay()) {
            //继续播放
            manager.setMediaPlayerStart();
        }
    }

    protected void initPlayer() {
        if (isAvailable) {
            manager = new MediaPlayerManager();
            manager.setOnMediaListener(this);
            manager.setAutoplay(true);
            SurfaceTexture su = textureView.getSurfaceTexture();
            Surface surface = new Surface(su);
            manager.setDataSourcePlay(mediaEntity.url, mediaEntity.mediaPathSource);
            manager.setSurfaceView(surface);
        }
    }

    protected int type = 1;//1：等比 2：满View播放

    private void setPlayViewZoom(VideoTextureView textureView) {
        MediaPlayer mediaPlayer = manager.getMediaPlayer();
        textureView.setType(type);
        textureView.setPlayViewZoom(mediaPlayer);


    }


    public static VideoFragment newInstance(MediaEntity mediaEntity) {
        VideoFragment frg = new VideoFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("bean", mediaEntity);
        frg.setArguments(bundle);
        return frg;
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {

    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    /**
     * @param mp
     * @param state  100:准备完成（开始播放 可以调用start）
     *               101：播放暂停
     *               102：播放完成
     *               103：停止播放
     *               104：播放进度
     *               105: 开始播放（调用start）
     *               106:设置拖动
     *               107：拖动完成
     * @param source 播放的url
     */
    @Override
    public void onPayState(MediaPlayer mp, int state, String source) {
        /*if (state == 100) {
            manager.setMediaPlayerStart();
        }*/
    }

    @Override
    public void onError(MediaPlayer mp, int what, int extra) {

    }
}
