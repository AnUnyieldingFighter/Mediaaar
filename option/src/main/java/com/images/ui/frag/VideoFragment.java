package com.images.ui.frag;


import android.graphics.SurfaceTexture;
import android.os.Bundle;
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

import java.io.Serializable;

public class VideoFragment extends MediaFragment {

    private OnMediaImgIbl imgLoading;
    private MediaEntity mediaEntity;

    public void setOnImgClickListener(OnMediaImgIbl imgLoading) {
        this.imgLoading = imgLoading;
    }

    public void setData(MediaEntity mediaEntity) {
        this.mediaEntity = mediaEntity;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.media_layout_video, null);
        return view;
    }

    private TextureView textureView;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Serializable bean = getArguments().getSerializable("bean");
        if (bean instanceof MediaEntity) {
            mediaEntity = (MediaEntity) bean;
        }
        textureView = view.findViewById(R.id.texture_view);
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                // 当 SurfaceTexture 可用时执行的操作
                isAvailable = true;
                if (isSetData && manager == null) {
                    setData();
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                // SurfaceTexture 尺寸变化时的操作
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                isAvailable = false;
                return true; // 返回 true 表示 SurfaceTexture 已被销毁
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                // SurfaceTexture 更新时的操作
            }
        });
    }

    private boolean isAvailable;
    private boolean isSetData;
    private MediaPlayerManager manager;

    @Override
    public void onResume() {
        super.onResume();
        if (!isSetData) {
            isSetData = true;
            setData();
            return;
        }
        if (manager != null && manager.isStopPlay()) {
            manager.setMediaPlayerStart();
        }

    }

    private void setData() {
        if (isAvailable) {
            manager = new MediaPlayerManager();
            SurfaceTexture su = textureView.getSurfaceTexture();
            Surface surface = new Surface(su);
            manager.setDataSourcePlay(mediaEntity.url, mediaEntity.mediaPathSource);
            manager.setSurfaceView(surface);

        }

         /*if (imgLoading != null) {
            imgLoading.onImageLoading(getActivity(), mediaEntity.mediaPathSource, photoView);
        }*/

    }

    public static VideoFragment newInstance(MediaEntity mediaEntity) {
        VideoFragment frg = new VideoFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("bean", mediaEntity);
        frg.setArguments(bundle);
        return frg;
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
}
