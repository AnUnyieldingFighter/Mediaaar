package com.images.ui.activity;

import android.content.Intent;
import android.os.Bundle;
 import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.media.option.R;
import com.images.ui.activity.choose.MediaActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.image_test1_btn).setOnClickListener(this);
        findViewById(R.id.image_test2_btn).setOnClickListener(this);
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



    }


}
