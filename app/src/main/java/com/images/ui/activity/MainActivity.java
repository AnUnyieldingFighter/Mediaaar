package com.images.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.images.ui.activity.google.GoogleActivity;
import com.media.option.R;
import com.images.ui.activity.me.media.MediaActivity;

import java.io.File;
import java.util.ArrayList;

import media.library.images.config.entity.MediaEntity;
import media.library.player.video.act.TestVideoStartAct;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.image_test1_btn).setOnClickListener(this);
        findViewById(R.id.video_test1_btn).setOnClickListener(this);
        findViewById(R.id.google_btn).setOnClickListener(this);
        findViewById(R.id.image_camera_btn).setOnClickListener(this);
        findViewById(R.id.video_btn).setOnClickListener(this);


    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.image_test1_btn) {
            //图片选择
            Intent it = new Intent();
            it.setClass(this, MediaActivity.class);
            startActivity(it);
            return;
        }
        if (id == R.id.video_test1_btn) {
            //短视频
            Intent it = new Intent();
            it.setClass(this, TestVideoStartAct.class);
            startActivity(it);
            return;
        }
        if (id == R.id.google_btn) {
            //google 图片选择器
            Intent it = new Intent();
            it.setClass(this, GoogleActivity.class);
            startActivity(it);
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

        for (int i = 0; i < res.size(); i++) {
            MediaEntity img = res.get(i);
            File file = new File(img.mediaPathSource);
            boolean isExit = file.exists();
            boolean isFile = file.isFile();
            ///sdcard/.transforms/synthetic/picker/0/com.android.providers.media.photopicker/media/1000000489.png
            Log.d("google图片选择器", "type：" + img.type + " path:"
                    + img.mediaPathSource + " isExit:" + isExit + " isFile:" + isFile);
        }
    }
}
