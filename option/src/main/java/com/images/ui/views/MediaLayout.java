package com.images.ui.views;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.images.config.entity.ImageEntity;
import com.images.imageselect.R;
import com.images.photo.PhotosMnager;
import com.images.ui.adapter.MediaOptAdapter;
import com.images.ui.adapter.OnMediaImgIbl;
import com.images.ui.bean.ImageFile;

import java.util.ArrayList;

//选择照片或者视频，或者拍照
public class MediaLayout extends RelativeLayout {


    public MediaLayout(Context context) {
        super(context);
        initView();
    }

    public MediaLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public MediaLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public MediaLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    protected void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.media_layout_opt, this);
        initRecyclerView();
        getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener);
    }

    private RecyclerView rvData;
    private MediaOptAdapter adapter;


    public void setOptCount(int count, OnMediaImgIbl imgLoading) {
        if (adapter == null) {
            initRecyclerView();
        }
        adapter.setMediaOptMax(getContext(), count, imgLoading);
    }

    protected boolean isShowCamera;//true  允许拍照

    public void doRequest(Activity act, boolean isShowCamera) {
        this.isShowCamera = isShowCamera;
        PhotosMnager.getInstance().setOnLoadingListener(new LoadingListener());
        PhotosMnager.getInstance().doRequest(act, 2);
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

    //读取数据库照片监听
    class LoadingListener implements PhotosMnager.OnLoadingListener {

        @Override
        public void onLoadingFile(ArrayList<ImageFile> fils) {

        }

        @Override
        public void onLoadingMedia(ArrayList<ImageEntity> files) {
            if (isShowCamera) {
                ImageEntity img = new ImageEntity();
                img.imageType = "-1";
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
