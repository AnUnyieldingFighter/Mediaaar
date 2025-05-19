package com.images.ui.activity.choose;

import android.content.Context;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.guomin.app.seletcimage.R;
import com.images.ui.adapter.OnMediaImgIbl;
import com.images.ui.views.MediaLayout;

//老板
public class MediaOptActivity extends AppCompatActivity {

    private MediaLayout mediaLayout;

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
        mediaLayout.doRequest(this, true);

    }

}
