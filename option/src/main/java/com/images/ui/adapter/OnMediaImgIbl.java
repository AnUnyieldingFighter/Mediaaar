package com.images.ui.adapter;

import android.content.Context;
import android.widget.ImageView;

import com.images.config.entity.MediaEntity;

public interface OnMediaImgIbl {
    /**
     * 加载图片
     *
     * @param context
     * @param path
     * @param imageView
     */
    default void onImageLoading(Context context, String path, ImageView imageView) {
    }

    /**
     * 选中了图片（选中，取消  具体看 imageEntity.isOption）
     *
     * @param imageEntity
     * @param type        -1 到达上限
     */
    default void onImageSelect(MediaEntity imageEntity, int type) {
    }


}
