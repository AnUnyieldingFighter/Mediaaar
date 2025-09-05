package com.images.ui.activity.google;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.media.option.R;

import java.io.File;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import media.library.able.MediaResIbl;
import media.library.images.config.entity.MediaEntity;
import media.library.manager.GoogleImgVideo;


//google 图片选择器 不需要权限
public class GoogleOptActivity extends AppCompatActivity {
    private static boolean optIsOne;
    //1： 启动照片选择器，让用户选择图像和视频
    //2:启动照片选择器，让用户只选择图片和视频
    //3:启动照片选择器，让用户仅选择视频。
    //4:启动照片选择器，让用户只选择image/gif
    private static int optType;

    public static void start(Activity act, boolean isOne, int type) {
        optIsOne = isOne;
        optType = type;
        Intent it = new Intent();
        it.setClass(act, GoogleOptActivity.class);
        act.startActivity(it);
    }

    //多选  选择的数据保存
    private ImageView ivVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_opt);
        ivVideo = (ImageView) findViewById(R.id.iv_video);

        GoogleImgVideo.getInstance().initPickMedia(this);
        GoogleImgVideo.getInstance().initPickMultipleMedia(this, 9);
        GoogleImgVideo.getInstance().setMediaResIbl(new MediaResIbl() {
            @Override
            public void onMediaRes(ArrayList<MediaEntity> res) {

                for (int i = 0; i < res.size(); i++) {
                    MediaEntity img = res.get(i);
                    File file = new File(img.mediaPathSource);
                    boolean isExit = file.exists();
                    boolean isFile = file.isFile();
                    ///sdcard/.transforms/synthetic/picker/0/com.android.providers.media.photopicker/media/1000000489.png
                    Log.d("google图片选择器", "type：" + img.type + " path:"
                            + img.mediaPathSource + " isExit:" + isExit + " isFile:" + isFile);
                }
                setImg(res);
                GoogleActivity.res = res;
                //选择结果回调
                //finish();
            }

            @Override
            public void onDismiss() {
                finish();
            }
        });
        selectOneImg2(optIsOne, optType);
    }

    private void setImg(ArrayList<MediaEntity> res) {
        MediaEntity bean = null;
        if (res.size() > 0) {
            bean = res.get(0);
        }
        if (bean == null) {
            return;
        }
        String path = bean.mediaPathSource;
        File file = new File(path);
        Log.d("google图片选择器", "文件长度：" + file.length());
        Glide.with(this)
                .load(path)
                .placeholder(com.images.imageselect.R.mipmap.image_select_default)
                //.centerCrop()
                .into(ivVideo);
    }

    @Override
    protected void onDestroy() {
        GoogleImgVideo.getInstance().onDestroy();
        super.onDestroy();
    }

    private void selectOneImg2(boolean isOne, int type) {
        if (isOne) {
            GoogleImgVideo.getInstance().setSingleChoice(type);
        } else {
            GoogleImgVideo.getInstance().setMultipleChoice(type);
        }

    }


}
