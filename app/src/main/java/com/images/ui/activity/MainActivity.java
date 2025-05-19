package com.images.ui.activity;

import android.content.Intent;
import android.os.Bundle;
 import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.guomin.app.seletcimage.R;
import com.images.ui.activity.choose.Image1Activity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.image_test1_btn).setOnClickListener(this);
        findViewById(R.id.image_test2_btn).setOnClickListener(this);
        findViewById(R.id.image_camera_btn).setOnClickListener(this);
        findViewById(R.id.video_btn).setOnClickListener(this);
          //


    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.image_test1_btn) {
            Intent it = new Intent();
            it.setClass(this, Image1Activity.class);
            startActivity(it);
            return;
        }
        if (id == R.id.image_test2_btn) {
            Intent it = new Intent();
            it.setClass(this, BillActivity.class);
            startActivity(it);
            return;
        }


    }


}
