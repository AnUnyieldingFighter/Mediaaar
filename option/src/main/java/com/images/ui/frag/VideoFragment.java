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

import java.io.Serializable;

public class VideoFragment extends MediaFragment {

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
    private MediaPlayerManager manager;

    @Override
    public void onResume() {
        super.onResume();
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

    private void initPlayer() {
        if (isAvailable) {
            manager = new MediaPlayerManager();
            manager.setAutoplay(true);
            SurfaceTexture su = textureView.getSurfaceTexture();
            Surface surface = new Surface(su);
            manager.setDataSourcePlay(mediaEntity.url, mediaEntity.mediaPathSource);
            manager.setSurfaceView(surface);
        }
    }

    private int type = 1;//1：等比 2：满View播放

    private void setPlayViewZoom(TextureView textureView) {
        MediaPlayer mediaPlayer = manager.getMediaPlayer();
        if (type == 1) {
            setVideoAspect(mediaPlayer, textureView);
        }
        if (type == 2) {
            setVideoAspectFull(mediaPlayer, textureView);
        }
    }

    private int oVideoW, oViewH;
    private int oTextureW, oTureH;

    //等比播放
    private void setVideoAspect(MediaPlayer mediaPlayer, TextureView textureView) {
        //mtextureViewWidth为textureView宽，mtextureViewHeight为textureView高
        //mtextureViewWidth宽高，为什么需要用传入的，因为全屏显示时宽高不会及时更新
        Matrix matrix = new Matrix();
        //videoView为new MediaPlayer()
        int videoWidth = mediaPlayer.getVideoWidth();
        int videoHeight = mediaPlayer.getVideoHeight();

        if (videoWidth == 0 || videoHeight == 0) {
            return;
        }
        float textureWidth = (float) textureView.getWidth();
        float textureHeight = (float) textureView.getHeight();
        if (oVideoW == videoWidth && oViewH == videoHeight && oTextureW == textureWidth && oTureH == textureHeight) {
            return;
        }
        //
        oVideoW = videoWidth;
        oViewH = videoHeight;
        oTextureW = (int) textureWidth;
        oTureH = (int) textureHeight;

        //得到缩放比，从而获得最佳缩放比
        float sx = textureWidth / videoWidth;
        float sy = textureHeight / videoHeight;
        //先将视频变回原来的大小
        float sx1 = videoWidth / textureWidth;
        float sy1 = videoHeight / textureHeight;
        matrix.preScale(sx1, sy1);
        //然后判断最佳比例，满足一边能够填满
        if (sx >= sy) {
            matrix.preScale(sy, sy);
            //然后判断出左右偏移，实现居中，进入到这个判断，证明y轴是填满了的
            float leftX = (textureWidth - videoWidth * sy) / 2;
            matrix.postTranslate(leftX, 0);
        } else {
            matrix.preScale(sx, sx);
            float leftY = (textureHeight - videoHeight * sx) / 2;
            matrix.postTranslate(0, leftY);
        }
        textureView.setTransform(matrix);//将矩阵添加到textureView
        textureView.postInvalidate();//重绘视图
    }

    private int oVideoW2, oViewH2;
    private int oTextureW2, oTureH2;


    private void setVideoAspectFull(MediaPlayer mediaPlayer, TextureView textureView) {
        // 获取视频的原始宽高比
        int videoWidth = mediaPlayer.getVideoWidth();
        int videoHeight = mediaPlayer.getVideoHeight();
        if (videoWidth == 0 || videoHeight == 0) {
            return;
        }
        // 获取TextureView的宽高
        int textureWidth = textureView.getWidth();
        int textureHeight = textureView.getHeight();
        if (oVideoW2 == videoWidth && oViewH2 == videoHeight && oTextureW2 == textureWidth && oTureH2 == textureHeight) {
            return;
        }
        //
        oVideoW2 = videoWidth;
        oViewH2 = videoHeight;
        oTextureW2 = (int) textureWidth;
        oTureH2 = (int) textureHeight;
        // 计算缩放比例以适应TextureView的宽高比
        float scaleX = textureWidth / (float) videoWidth;
        float scaleY = textureHeight / (float) videoHeight;
        float scale = Math.max(scaleX, scaleY); // 选择最大的缩放比例以适应整个视图
        // 创建Matrix并设置缩放变换
        Matrix matrix = new Matrix();
        //设置缩放比例
        matrix.setScale(scale, scale);
        // 计算平移量，使视频内容居中显示
        float translateX = (textureWidth - videoWidth * scale) / 2;
        float translateY = (textureHeight - videoHeight * scale) / 2;
        matrix.postTranslate(translateX, translateY);
        //
        textureView.setTransform(matrix);
        //重绘视图
        textureView.postInvalidate();
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
