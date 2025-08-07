package media.library.images.ui.adapter;

import android.view.View;

import media.library.images.config.entity.MediaEntity;

/**
 * 预览图片是的点击事件
 */

public interface OnImgClickListener {

      void OnImgTapClick(View view, MediaEntity mediaEntity,float v, float v1);
}
