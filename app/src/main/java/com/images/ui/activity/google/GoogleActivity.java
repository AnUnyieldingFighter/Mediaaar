package com.images.ui.activity.google;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ComponentCaller;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;


import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.media.option.R;

import java.util.ArrayList;
import java.util.HashMap;

import media.library.able.MediaResIbl;
import media.library.images.config.entity.MediaEntity;
import media.library.images.manager.MediaManager;
import media.library.manager.GoogleImgVideo;


//google 图片选择器 不需要权限
public class GoogleActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tvMsg;
    //多选  选择的数据保存
    public static ArrayList<MediaEntity> temp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google);


        findViewById(R.id.image_1).setOnClickListener(this);
        findViewById(R.id.image_2).setOnClickListener(this);

        findViewById(R.id.video_1).setOnClickListener(this);
        findViewById(R.id.video_2).setOnClickListener(this);
        tvMsg = (TextView) findViewById(R.id.tv_msg);
        GoogleImgVideo.getInstance().initPickMedia(this);
        GoogleImgVideo.getInstance().initPickMultipleMedia(this, 9);
        GoogleImgVideo.getInstance().setMediaResIbl(new MediaResIbl() {
            @Override
            public void onMediaRes(ArrayList<MediaEntity> res) {
                for (int i = 0; i < res.size(); i++) {
                    MediaEntity img = res.get(i);
                    Log.d("google图片选择器", "type：" + img.type + " path:" + img.mediaPathSource);
                }
                tvMsg.setText("数量：" + res.size());
            }
        });
        //initPickMedia();
        //initPickMultipleMedia(9);
    }


    @Override
    protected void onDestroy() {
        GoogleImgVideo.getInstance().onDestroy();
        super.onDestroy();
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.image_1) {
            //选择一张图片
            selectOneImg2(true, 2);
            return;
        }
        if (id == R.id.image_2) {
            //选择多张图片
            selectOneImg2(false, 2);
            return;
        }
        if (id == R.id.video_1) {
            //选择一个视频
            selectOneImg2(true, 3);
            return;
        }
        if (id == R.id.video_2) {
            //选择多个视频
            selectOneImg2(false, 3);
            return;
        }

    }
    private void selectOneImg2(boolean isOne, int type) {
        if (isOne) {
            GoogleImgVideo.getInstance().setSingleChoice(type);
        } else {
            GoogleImgVideo.getInstance().setMultipleChoice(type);
        }

    }


    //-----------------------------------
    private HashMap<String, Integer> numMap = new HashMap<>();
    private Integer reqCode = 100;

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode, @Nullable Bundle options) {
        // numMap.put("" + reqCode, requestCode);
        // requestCode = reqCode;
        super.startActivityForResult(intent, requestCode, options);
    }

    //ComponentActivity onActivityResult
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Integer num = numMap.get("" + requestCode);
        if (num != null) {
            requestCode = num;
            numMap.remove("" + requestCode);
        }
        //((ComponentActivity) this).onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data, @NonNull ComponentCaller caller) {
        Integer num = numMap.get("" + requestCode);
        if (num != null) {
            requestCode = num;
            numMap.remove("" + requestCode);
        }
        super.onActivityResult(requestCode, resultCode, data, caller);
    }

}
