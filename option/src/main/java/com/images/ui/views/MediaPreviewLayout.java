package com.images.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.images.config.entity.MediaEntity;
import com.images.ui.adapter.AdapterPreviewClickList;
import com.images.ui.adapter.MediaPreviewAdapter;
import com.images.ui.adapter.OnMediaImgIbl;

import java.util.ArrayList;

//图片预览
public class MediaPreviewLayout extends ViewPager {

    public MediaPreviewLayout(@NonNull Context context) {
        super(context);
    }

    public MediaPreviewLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        try {
            return super.onTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private MediaPreviewAdapter adapter;

    public void setMedias(Context context, ArrayList<MediaEntity> medias) {
        if (adapter == null) {
            adapter = new MediaPreviewAdapter(context, medias);
            adapter.setPhotoViewClickListener(new PreviewClickList());
        }
        setAdapter(adapter);
        addOnPageChangeListener(new OnPagerChange());
    }

    public void setImageLoading(OnMediaImgIbl imgLoading) {
        adapter.setImageLoading(imgLoading);
    }

    protected void onPageOpt(int position) {
    }

    protected void OnPhotoTapListener(View view, float v, float v1) {
    }

    class PreviewClickList implements AdapterPreviewClickList {

        @Override
        public void OnPhotoTapListener(View view, float v, float v1) {
            MediaPreviewLayout.this.OnPhotoTapListener(view, v, v1);
        }
    }

    class OnPagerChange implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            onPageOpt(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }

    }
}
