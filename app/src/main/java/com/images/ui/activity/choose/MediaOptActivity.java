package com.images.ui.activity.choose;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.guomin.app.seletcimage.R;
import com.images.config.entity.MediaEntity;
import com.images.ui.adapter.OnMediaImgIbl;
import com.images.ui.views.MediaOPTLayout;

import java.util.ArrayList;

//选择图片
public class MediaOptActivity extends AppCompatActivity {

    private MediaOPTLayout mediaLayout;
    public static ArrayList<MediaEntity> temp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_opt);
        mediaLayout = findViewById(R.id.media_layout);
        mediaLayout.setOptCount(9, new OnMediaImgIbl() {
            @Override
            public void onImageLoading(Context context, String path, ImageView imageView) {
                Glide.with(context)
                        .load(path)
                        .placeholder(R.mipmap.image_select_default)
                        //.centerCrop()
                        .into(imageView);
            }
        });
        mediaLayout.doRequest(2, true);
        findViewById(R.id.tv_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                temp = mediaLayout.getOptData();
                Intent it = new Intent();
                it.setClass(MediaOptActivity.this, MediaPreviewActivity.class);
                startActivity(it);
            }
        });
        findViewById(R.id.tv_crop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                temp = mediaLayout.getOptData();
                Intent it = new Intent();
                it.setClass(MediaOptActivity.this, ImgCropActivity.class);
                startActivity(it);
            }
        });
    }

}
