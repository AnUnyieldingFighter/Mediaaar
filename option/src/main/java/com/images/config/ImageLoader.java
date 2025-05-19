package com.images.config;

import android.content.Context;
import android.widget.ImageView;

import com.images.config.entity.MediaEntity;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/10/20.
 */
public interface ImageLoader extends Serializable {
    void imageLoading(Context context, String path, ImageView imageView);

    void interdictMsg(Context context, MediaEntity imageEntity);
}
