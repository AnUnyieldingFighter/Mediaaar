package com.images.ui.activity.choose;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import media.library.images.config.entity.MediaEntity;
import media.library.images.manager.MediaPlayerManager;
import media.library.images.manager.VideoDataBean;
import media.library.images.utils.PhotoUtil;

import com.media.option.R;
import media.library.images.unmix.ImageLog;

import java.io.File;
import java.util.ArrayList;

//老板
public class MediaActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView ivVideo;
    //多选  选择的数据保存
    public static ArrayList<MediaEntity> temp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);


        findViewById(R.id.image_btn5).setOnClickListener(this);
        findViewById(R.id.image_btn51).setOnClickListener(this);
        findViewById(R.id.image_btn52).setOnClickListener(this);

        findViewById(R.id.image_6).setOnClickListener(this);
        findViewById(R.id.image_7).setOnClickListener(this);
        findViewById(R.id.image_8).setOnClickListener(this);

        ivVideo = (ImageView) findViewById(R.id.iv_video);
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


    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.image_btn5) {
            //多选
            temp = null;
            Intent it = new Intent();
            it.setClass(this, MediaOptActivity.class);
            startActivity(it);
            return;
        }
        if (id == R.id.image_btn51) {
            //相机拍照
            PhotoUtil.showCameraAction(this);
            return;
        }
        if (id == R.id.image_btn52) {
            //多选(回显)  先调用 R.id.image_btn5  temp 有数据之后可回显
            Intent it = new Intent();
            it.setClass(this, MediaOptActivity.class);
            startActivity(it);
            return;
        }
        if (id == R.id.image_6) {
            //播放一个网络视频
            MediaEntity bean = new MediaEntity();
            bean.type = 2;
            bean.url = video1;
            bean.other = "2";
            temp = new ArrayList<>();
            temp.add(bean);

            Intent it = new Intent();
            it.setClass(this, MediaPreviewActivity.class);
            startActivity(it);
            return;
        }
        if (id == R.id.image_8) {
            //播放一个网络视频
            MediaEntity bean = new MediaEntity();
            bean.type = 2;
            bean.mediaPathSource = "/storage/emulated/0/DCIM/ScreenRecorder/Screenrecorder-2025-03-13-12-17-36-126.mp4";
            bean.other = "2";
            temp = new ArrayList<>();
            temp.add(bean);
            Intent it = new Intent();
            it.setClass(this, MediaPreviewActivity.class);
            startActivity(it);
            return;
        }
        if (id == R.id.image_7) {
            VideoDataBean videoData = MediaPlayerManager.getVideoData1S(video2);
            ivVideo.setImageBitmap(videoData.videoBitmap);
            return;
        }
    }

    private String video1 = "https://st.92kk.com/2021/%E8%BD%A6%E8%BD%BD%E8%A7%86%E9%A2%91/202110/20210916/[Mp4]%E4%B8%A4%E4%B8%AA%E4%B8%96%E7%95%8C-%E8%BD%A6%E8%BD%BD%E5%A4%9C%E5%BA%97%E9%9F%B3%E4%B9%90DJ%E8%A7%86%E9%A2%91[%E7%8B%AC].mp4";
    private String video2 = "https://nbc.vtnbo.com/nbc-file/file/video/beta/17484063665794360.mp4";
    private String video3 = "http://10.168.3.102:5233/chfs/shared/nbc-apk/test1.mp4";

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (PhotoUtil.isTakeOK(requestCode, resultCode)) {
            //拍照成功
            File file = PhotoUtil.getTakeResFile();
            if (file != null && file.exists()) {
                ImageLog.d("拍照成功", file.getPath());
                Glide.with(this).load(file.getPath()).placeholder(com.images.imageselect.R.mipmap.image_select_default)
                        //.centerCrop()
                        .into(ivVideo);
            }

        }
    }
}
