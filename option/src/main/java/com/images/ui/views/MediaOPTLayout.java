package com.images.ui.views;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.images.config.entity.MediaEntity;
import com.images.imageselect.R;
import com.images.photo.MediaManager;
import com.images.ui.adapter.MediaOptAdapter;
import com.images.ui.adapter.OnMediaImgIbl;


import java.util.ArrayList;

//选择照片或者视频，或者拍照
public class MediaOPTLayout extends RelativeLayout {


    public MediaOPTLayout(Context context) {
        super(context);
        initView();
    }

    public MediaOPTLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public MediaOPTLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public MediaOPTLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    protected void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.media_layout_opt, this);
        initRecyclerView();
        getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener);
        MediaManager.getInstance().setOnLoadingListener(new LoadingListener());
        MediaManager.getInstance().initMediaDB(getContext());
    }

    private RecyclerView rvData;
    private MediaOptAdapter adapter;


    public void setOptCount(int count, OnMediaImgIbl imgLoading) {
        if (adapter == null) {
            initRecyclerView();
        }
        adapter.setMediaOptMax(getContext(), count, imgLoading);
    }

    protected boolean isShowCamera;//true  允许拍照 未实现

    /**
     *
     * @param resType  1:全部 2:图片 3：视频
     * @param isShowCamera
     */
    public void doRequest(int resType, boolean isShowCamera) {
        this.isShowCamera = isShowCamera;
        MediaManager.getInstance().doReq(resType);
    }

    protected void initRecyclerView() {
        int itemWidth = layoutWidth / 3;
        int itemImageWidth = itemWidth - itemSpacing - itemSpacing;
        rvData = (RecyclerView) findViewById(R.id.rv_data);
        adapter = new MediaOptAdapter();
        adapter.setItemImgWidth(itemImageWidth);
        rvData.setAdapter(adapter);
        rvData.setLayoutManager(new GridLayoutManager(getContext(), 3));
    }

    //获取选中的数据
    public ArrayList<MediaEntity> getOptData() {
        return adapter.getOptData();
    }

    //读取数据库照片监听
    class LoadingListener implements MediaManager.OnLoadingListener {

        @Override
        public void onLoadingMedia(ArrayList<MediaEntity> files) {
            if (isShowCamera) {
                MediaEntity img = new MediaEntity();
                img.mediaType = "-1";
                files.add(0, img);
            }
            adapter.setDatas(files);
        }

        @Override
        public void onLoadingFail() {

        }
    }


    private int itemSpacing = 10;

    protected void setItemSpacing(int itemSpacing) {
        this.itemSpacing = itemSpacing;
    }

    protected int getItemSpacing() {
        return itemSpacing;
    }

    protected int layoutWidth;

    protected void initContextWidth() {
        int width = getWidth();
        if (width <= 0) {
            return;
        }
        layoutWidth = width;
        if (adapter != null) {
            int itemWidth = layoutWidth / 3;
            int itemImageWidth = itemWidth - itemSpacing - itemSpacing;
            adapter.setItemImgWidth(itemImageWidth);
        }

        getViewTreeObserver().removeOnGlobalLayoutListener(globalLayoutListener);
    }

    private GlobalLayoutListener globalLayoutListener = new GlobalLayoutListener();

    class GlobalLayoutListener implements ViewTreeObserver.OnGlobalLayoutListener {
        public void onGlobalLayout() {
            initContextWidth();
        }
    }
}
