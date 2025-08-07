package media.library.images.ui.adapter;

import android.content.Context;
import android.widget.ImageView;

import media.library.images.config.entity.MediaEntity;

public interface OnMediaImgIbl {
    /**
     * 加载图片
     *
     * @param context
     * @param imageEntity
     * @param imageView
     */
    default void onImageLoading(Context context, MediaEntity imageEntity, ImageView imageView) {
    }

    /**
     * 选中了图片（选中，取消  具体看 imageEntity.isOption）
     *
     * @param imageEntity
     * @param type        -1 到达上限 -2 互斥  0 选中
     */
    default void onImageSelect(MediaEntity imageEntity, int type) {
    }


}
