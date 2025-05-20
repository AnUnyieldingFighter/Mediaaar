package com.images.ui.frag;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.images.config.entity.MediaEntity;
import com.images.imageselect.R;

import com.images.ui.adapter.OnImgClickListener;
import com.images.ui.adapter.OnMediaImgIbl;
import com.images.ui.thing.photoview.PhotoView;
import com.images.ui.thing.photoview.PhotoViewAttacher;

import java.io.Serializable;

public class ImgFragment extends MediaFragment {
    private OnImgClickListener listener;
    private OnMediaImgIbl imgLoading;
    private MediaEntity mediaEntity;

    public void setOnImgClickListener(OnImgClickListener listener, OnMediaImgIbl imgLoading) {
        this.listener = listener;
        this.imgLoading = imgLoading;
    }

    public void setData(MediaEntity mediaEntity) {
        this.mediaEntity = mediaEntity;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.media_layout_img, null);
        return view;
    }

    private PhotoView photoView;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Serializable bean = getArguments().getSerializable("bean");
        if (bean instanceof MediaEntity) {
            mediaEntity = (MediaEntity) bean;
        }
        photoView = view.findViewById(R.id.photo_view);
    }

    private boolean isSetData;

    @Override
    public void onResume() {
        super.onResume();
        if (!isSetData) {
            isSetData = true;
            setData();
        }
    }

    private void setData() {
        if (imgLoading != null) {
            imgLoading.onImageLoading(getActivity(), mediaEntity.mediaPathSource, photoView);
        }
        photoView.setOnPhotoTapListener(new PhotoTapListener());
    }

    class PhotoTapListener implements PhotoViewAttacher.OnPhotoTapListener {

        @Override
        public void onPhotoTap(View view, float x, float y) {
            if (listener == null) {
                return;
            }
            listener.OnImgTapClick(view, mediaEntity, x, y);
        }
    }

    public static ImgFragment newInstance(MediaEntity mediaEntity) {
        ImgFragment frg = new ImgFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("bean", mediaEntity);
        frg.setArguments(bundle);
        return frg;
    }

}
