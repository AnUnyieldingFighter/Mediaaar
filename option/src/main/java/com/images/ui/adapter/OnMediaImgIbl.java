package com.images.ui.adapter;

import android.content.Context;
import android.widget.ImageView;

import com.images.config.entity.MediaEntity;

public interface OnMediaImgIbl {
    //加载图片
    default void onImageLoading(Context context, String path, ImageView imageView) {
    }

    /**
     *
     * @param imageEntity
     * @param type -1 到达上限
     */
    default void onImageSelect(MediaEntity imageEntity, int type) {
    }
    default void onImageClick(ImageView imageView) {
    }
}
