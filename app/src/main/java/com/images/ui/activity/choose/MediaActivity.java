package com.images.ui.activity.choose;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.images.config.entity.MediaEntity;
import com.images.photo.PhotoUtil;
import com.images.ui.Manger.ImageUtile;
import com.images.ui.Manger.PhotoManager;
import com.guomin.app.seletcimage.R;
import com.images.unmix.ImageLog;

import java.io.File;
import java.util.ArrayList;

//老板
public class MediaActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);
        findViewById(R.id.image_btn2).setOnClickListener(this);
        findViewById(R.id.image_btn21).setOnClickListener(this);
        findViewById(R.id.image_btn3).setOnClickListener(this);
        findViewById(R.id.image_btn31).setOnClickListener(this);
        findViewById(R.id.image_btn4).setOnClickListener(this);
        findViewById(R.id.image_btn5).setOnClickListener(this);
        findViewById(R.id.image_btn51).setOnClickListener(this);

        findViewById(R.id.image_btn6).setVisibility(View.GONE);
        findViewById(R.id.image_btn7).setVisibility(View.GONE);

        iv = (ImageView) findViewById(R.id.image_iv);
    }

    private ArrayList<String> getPaths() {
        ArrayList<String> paths = new ArrayList<>();
        paths.add("/storage/emulated/0/temp/pictures/20170302145937.png");
        paths.add("/storage/emulated/0/temp/pictures/20170302144916.png");
        paths.add("/storage/emulated/0/temp/pictures/20170302145653..png");
        paths.add("http://m.bookfan.cn/images/blog/userid_101_20161223135300585cbbbce9f0a.jpg");

        return paths;
    }

    private ArrayList<MediaEntity> getImage() {
        ArrayList<MediaEntity> iamges = new ArrayList<>();
        MediaEntity image = new MediaEntity();
        image.mediaPathSource = "http://m.bookfan.cn/images/blog/userid_101_20161223135300585cbbbce9f0a.jpg";
        image.isDelete = false;
        iamges.add(image);
        MediaEntity image1 = new MediaEntity();
        image1.mediaPathSource = "/storage/emulated/0/wandoujia/downloader/icon/main_tab_-1996466411.png";
        iamges.add(image1);
        MediaEntity image2 = new MediaEntity();
        image2.mediaPathSource = "/storage/emulated/0/haoyisheng/1522639040658.jpg";
        image2.isDelete = false;
        iamges.add(image2);
        return iamges;
    }

    PhotoManager photoManager;

    @Override
    public void onClick(View view) {
        if (photoManager == null) {
            photoManager = new PhotoManager(this);
        }
        switch (view.getId()) {
            case R.id.image_btn5:
                //多选
                Intent it = new Intent();
                it.setClass(this, MediaOptActivity.class);
                startActivity(it);
                break;
            case R.id.image_btn51:
                //相机拍照
                PhotoUtil.showCameraAction(this);
                break;


            case R.id.image_btn2:
                //只预览
                photoManager.previewImage(getPaths());
                break;
            case R.id.image_btn21:
                //预览和删除
                // photoManager.previewImageDelect(getPaths());
                photoManager.previewImageDelect2(getImage());
                break;
            case R.id.image_btn3:
                //单张图片+裁剪
                photoManager.getCrop(true);
                break;
            case R.id.image_btn31:
                //选图+系统裁剪
                //photoManager.getCrop(false);
                photoManager.crop(false);
                break;
            case R.id.image_btn4:
                //只拍照
                photoManager.getSinglePhotoConfig();
                break;


        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (PhotoUtil.isTakeOK(requestCode, resultCode)) {
            //拍照成功
            File file = PhotoUtil.getTakeResFile();
            if (file != null && file.exists()) {
                ImageLog.d("拍照成功", file.getPath());
            }

        }
    }
}
