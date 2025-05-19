package com.images.ui.Manger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.TypedValue;
import android.widget.ImageView;

import com.images.config.ConfigBuild;
import com.images.config.Configs;
import com.images.config.ImageLoader;
import com.images.config.entity.ImageEntity;
import com.images.unmix.ImageLog;
import com.bumptech.glide.Glide;
import com.guomin.app.seletcimage.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/10/17.
 */
public class PhotoManager {
    private Activity activity;
    private int barHeight, optionHeight;

    public PhotoManager(Activity activity) {
        this.activity = activity;
        barHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                42, activity.getResources().getDisplayMetrics());
        optionHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                38, activity.getResources().getDisplayMetrics());
    }


    //更多选择
    public void getMoreConfig(int max, ArrayList<String> path) {
        ConfigBuild.getNewBuild()
                .setBuildBar()
                .setActionBarColor(0xffffffff)
                .setStatusBarColor(0xff7db2fd)
                .setActionBarHeight(barHeight)
                .complete()
                .setBuildBarCommon()
                .setBack("返回")
                .setTitle("选择图片")
                .setOption("完成")
                .setOptionIconbg(R.drawable.bar_send_true_bg)
                .complete()
                .setBuildBarPreview()
                .setOption("发送哈")
                .setTitle("无效")
                .complete()
                .setImagePath(path)
                .setLoading(new ImageShowType())
                .setDebug(true)
                .setBuildMore()
                .setImageSelectMaximum(max)
                .complete()
                .setShowCamera(true)
                .build(activity);
    }

    public void getMoreConfig2(int max, ArrayList<ImageEntity> images) {
        ConfigBuild.getNewBuild()
                .setBuildBar()
                .setActionBarColor(0xffffffff)
                .setStatusBarColor(0xff7db2fd)
                .setActionBarHeight(barHeight)
                .complete()
                .setBuildBarCommon()
                .setBack("返回")
                .setTitle("选择图片")
                .setOption("完成")
                .setOptionIconbg(R.drawable.bar_send_true_bg)
                .complete()
                .setBuildBarPreview()
                .setOption("发送哈")
                .setTitle("无效")
                .complete()
                .setImages(images)
                .setLoading(new ImageShowType())
                .setDebug(true)
                .setBuildMore()
                .setImageSelectMaximum(max)
                .complete()
                .setShowCamera(true)
                .build(activity);
    }

    //只拍照
    public void getSinglePhotoConfig() {
        ConfigBuild.getNewBuild()
                .setLoading(new ImageShowType())
                .setDebug(true)
                .setOnlyPhotograph(true)
                .build(activity);

    }

    //拍照+裁剪
    public void getCrop(boolean isOnlyPhotograph) {
        ConfigBuild.getNewBuild()
                .setBuildBar()
                .setActionBarColor(0xffffffff)
                .setStatusBarColor(0xff7db2fd)
                .setActionBarHeight(barHeight)
                .complete()
                .setBuildBarCommon()
                .setBack("返回")
                .setTitle("选择图片")
                .setOption("完成")
                .setOptionIconbg(R.drawable.bar_send_true_bg)
                .complete()
                .setBuildBarCrop()
                .setTitle("裁剪")
                .setOption("裁剪发送")
                .complete()
                .setBuildCrop()
                .setAspect(300, 600)
                .setOutput(300, 600)
                .setNotSystemCrop(true)
                .complete()
                .setLoading(new ImageShowType())
                .setDebug(true)
                .setOnlyPhotograph(isOnlyPhotograph)
                .build(activity);
    }

    //只预览
    public void previewImage(ArrayList<String> listImagePath) {
        ConfigBuild.getNewBuild()
                .setBuildBar()
                .setActionBarColor(0xffffffff)
                .setStatusBarColor(0xff7db2fd)
                .setActionBarHeight(barHeight)
                .complete()
                .setBuildBarPreview()
                .setBack("返回")
                .setTitle("预览")
                .complete()
                .setImagePath(listImagePath)
                .setLoading(new ImageShowType())
                .setDebug(true)
                .buildPreviewOnly(activity, 0);
    }

    //只预览可以删除
    public void previewImageDelect(ArrayList<String> listImagePath) {
        ConfigBuild.getNewBuild()
                .setBuildBar()
                .setActionBarColor(0xffffffff)
                .setStatusBarColor(0xff7db2fd)
                .setActionBarHeight(barHeight)
                .complete()
                .setBuildBarPreview()
                .setBack("返回")
                .setTitle("预览")
                .setOption("删除")
                .complete()
                .setImagePath(listImagePath)
                .setLoading(new ImageShowType())
                .setDebug(true)
                .buildPreviewDelete(activity, 0);
    }

    //只预览可以删除
    public void previewImageDelect2(ArrayList<ImageEntity> images) {
        ConfigBuild.getNewBuild()
                .setBuildBar()
                .setActionBarColor(0xffffffff)
                .setStatusBarColor(0xff7db2fd)
                .setActionBarHeight(barHeight)
                .complete()
                .setBuildBarPreview()
                .setBack("返回")
                .setTitle("预览")
                .setOption("删除")
                .complete()
                .setImages(images)
                .setLoading(new ImageShowType())
                .setDebug(true)
                .buildPreviewDelete(activity, 0);
    }

    public void crop(boolean isonlyPhotograph) {
        ConfigBuild.getNewBuild()
                .setBuildBar()
                .setActionBarColor(0xffffffff)
                .setStatusBarColor(0xff7db2fd)
                .setActionBarHeight(barHeight)
                .complete()
                .setBuildBarCommon()
                .setBack("返回")
                .setTitle("选择图片")
                .setOption("完成")
                .setOptionIconbg(R.drawable.bar_send_true_bg)
                .complete()
                .setBuildBarCrop()
                .setTitle("裁剪")
                .setOption("发送")
                .complete()
                .setBuildCrop()
                .setAspect(600, 600)
                .setOutput(600, 600)
                .setNotSystemCrop(true)
                .complete()
                .setLoading(new ImageShowType())
                .setDebug(true)
                .setShowCamera(true)
                .setOnlyPhotograph(isonlyPhotograph)
                .build(activity);
    }

    class ImageShowType implements ImageLoader {

        @Override
        public void imageLoading(Context context, String path, ImageView imageView) {
            Glide.with(context)
                    .load(path)
                    .placeholder(R.mipmap.image_select_default)
                    //.centerCrop()
                    .into(imageView);
        }

        @Override
        public void interdictMsg(Context context, ImageEntity imageEntity) {
            ImageLog.e("-------", "不能删除图片............");
        }
    }

    public List<String> onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Configs.TASK_START) {
            return null;
        }
        ArrayList<String> pathList = data.getStringArrayListExtra(Configs.TASK_COMPLETE_RESULT);
        if (pathList == null || pathList.size() == 0) {
            return null;
        }
        for (String path : pathList) {
            Log.e("ImagePathList", path);
        }
        return pathList;
    }

}
