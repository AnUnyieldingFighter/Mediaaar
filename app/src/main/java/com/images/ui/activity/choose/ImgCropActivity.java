package com.images.ui.activity.choose;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.media.option.R;
import com.images.ui.adapter.OnMediaImgIbl;
import com.images.ui.views.ImageCropLayout;
import com.images.ui.views.MediaPreviewLayout;

//裁剪
public class ImgCropActivity extends AppCompatActivity {

    private ImageCropLayout mediaLayout;
    private ImageView ivCrop;
    public static String savePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_img_crop);
        mediaLayout = findViewById(R.id.media_layout);
        ivCrop = findViewById(R.id.iv_crop);
        mediaLayout.setMedias(this,  MediaActivity.temp.get(0));
        findViewById(R.id.tv_crop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePath = mediaLayout.saveImg();
                Glide.with(ImgCropActivity.this).load(savePath).placeholder(com.images.imageselect.R.mipmap.image_select_default)
                        //.centerCrop()
                        .into(ivCrop);
            }
        });
    }

}
