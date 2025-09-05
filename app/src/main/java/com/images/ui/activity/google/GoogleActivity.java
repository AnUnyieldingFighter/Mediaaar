package com.images.ui.activity.google;


import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.images.ui.activity.MainActivity;
import com.media.option.R;

import java.io.File;
import java.util.ArrayList;

import media.library.images.config.entity.MediaEntity;


//google 图片选择器 不需要权限
public class GoogleActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView ivVideo;
    private TextView tvMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google);
        findViewById(R.id.image_1).setOnClickListener(this);
        findViewById(R.id.image_2).setOnClickListener(this);
        findViewById(R.id.video_1).setOnClickListener(this);
        findViewById(R.id.video_2).setOnClickListener(this);
        ivVideo = (ImageView) findViewById(R.id.iv_video);
        tvMsg = (TextView) findViewById(R.id.tv_msg);
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.image_1) {
            //选择一张图片
            GoogleOptActivity.start(this, true, 2);
            return;
        }
        if (id == R.id.image_2) {
            //选择多张图片
            GoogleOptActivity.start(this, false, 2);
            return;
        }
        if (id == R.id.video_1) {
            //选择一个视频
            GoogleOptActivity.start(this, true, 3);
            return;
        }
        if (id == R.id.video_2) {
            //选择多个视频
            GoogleOptActivity.start(this, false, 3);
            return;
        }

    }

    public static ArrayList<MediaEntity> res;

    @Override
    protected void onResume() {
        super.onResume();
        if (res == null || res.size() == 0) {
            return;
        }
        tvMsg.setText("选择数量：" + res.size());
        Glide.with(GoogleActivity.this).load(res.get(0).mediaPathSource)
                .placeholder(com.images.imageselect.R.mipmap.image_select_default)
                //.centerCrop()
                .into(ivVideo);
    }


}
