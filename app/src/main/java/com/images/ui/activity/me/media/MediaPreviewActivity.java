package com.images.ui.activity.me.media;

import android.content.Context;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.media.option.R;
import media.library.images.config.entity.MediaEntity;
import media.library.images.ui.adapter.OnMediaImgIbl;
import media.library.images.ui.views.MediaPreviewLayout;

//照片与视频预览
public class MediaPreviewActivity extends AppCompatActivity {

    private MediaPreviewLayout mediaLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_preview);
        mediaLayout = findViewById(R.id.media_layout);

        mediaLayout.setImageLoading(new OnMediaImgIbl() {
            @Override
            public void onImageLoading(Context context, MediaEntity imageEntity, ImageView imageView) {
                Glide.with(context).load(imageEntity.mediaPathSource).placeholder(com.images.imageselect.R.mipmap.image_select_default)
                        //.centerCrop()
                        .into(imageView);
            }
        });
        mediaLayout.setMedias(this, MediaActivity.temp);
    }

}
