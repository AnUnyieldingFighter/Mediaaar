package com.images.ui.activity.choose;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.images.config.entity.ImageEntity;
import com.images.ui.Manger.PhotoManager;
import com.guomin.app.seletcimage.R;

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

    private ArrayList<ImageEntity> getImage() {
        ArrayList<ImageEntity> iamges = new ArrayList<>();
        ImageEntity image = new ImageEntity();
        image.imagePathSource = "http://m.bookfan.cn/images/blog/userid_101_20161223135300585cbbbce9f0a.jpg";
        image.isDelete = false;
        iamges.add(image);
        ImageEntity image1 = new ImageEntity();
        image1.imagePathSource = "/storage/emulated/0/wandoujia/downloader/icon/main_tab_-1996466411.png";
        iamges.add(image1);
        ImageEntity image2 = new ImageEntity();
        image2.imagePathSource = "/storage/emulated/0/haoyisheng/1522639040658.jpg";
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



}
