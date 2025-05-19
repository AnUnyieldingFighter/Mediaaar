package com.images.config;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.images.config.operation.ConfigBar;
import com.images.config.operation.ConfigBarCommon;
import com.images.config.operation.ConfigBarCrop;
import com.images.config.operation.ConfigBarPreview;
import com.images.config.operation.ConfigBuildMore;
import com.images.config.operation.ConfigBuiledCrop;
import com.images.photo.DataStore;
import com.images.ui.activity.IncidentActivity;
import com.images.ui.activity.PreviewDeleteActivity;
import com.images.ui.activity.PreviewOnlyActivity;
import com.images.config.entity.ImageEntity;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Administrator on 2016/10/17.
 */
public class ConfigBuild implements Serializable {

    private static ConfigBuild configBuild;
    private Configs configs;
    private ImageLoader imageLoader;

    public ConfigBuild() {
        configs = new Configs();
    }

    public static ConfigBuild getNewBuild() {
        configBuild = new ConfigBuild();
        return configBuild;
    }

    public static ConfigBuild getBuild() {
        if (configBuild == null) {
            configBuild = new ConfigBuild();
        }
        return configBuild;
    }


    //设置导航条
    public ConfigBar setBuildBar() {
        configs.configBar = new ConfigBar();
        return configs.configBar;
    }

    //设置普通导航条
    public ConfigBarCommon setBuildBarCommon() {
        configs.configBarCommon = new ConfigBarCommon();
        return configs.configBarCommon;
    }

    //设置裁剪导航条
    public ConfigBarCrop setBuildBarCrop() {
        configs.configBarCrop = new ConfigBarCrop();
        return configs.configBarCrop;
    }

    //设置预览导航条
    public ConfigBarPreview setBuildBarPreview() {
        configs.configPreview = new ConfigBarPreview();
        return configs.configPreview;
    }

    //设置多选
    public ConfigBuildMore setBuildMore() {
        configs.configBuildMore = new ConfigBuildMore();
        return configs.configBuildMore;
    }

    //设置裁剪
    public ConfigBuiledCrop setBuildCrop() {
        configs.configBuildSingle = new ConfigBuiledCrop();
        return configs.configBuildSingle;
    }

    //设置文件路径
    public ConfigBuild setFilePath(String filePath) {
        configs.filePath = filePath;
        return this;
    }

    //设置显示相机
    public ConfigBuild setShowCamera(boolean showCamera) {
        configs.showCamera = showCamera;
        return this;
    }

    //设置只拍照
    public ConfigBuild setOnlyPhotograph(boolean isOnlyPhotograph) {
        configs.isOnlyPhotograph = isOnlyPhotograph;
        return this;
    }

    //设置已选择或者要预览的图片
    public ConfigBuild setImagePath(ArrayList<String> listImagePath) {
        configs.listImagePath = listImagePath;
        return this;
    }

    //设置已选择或者要预览的图片
    public ConfigBuild setImages(ArrayList<ImageEntity> listImage) {
        configs.listImage = listImage;
        return this;
    }


    //设置打印信息
    public ConfigBuild setDebug(boolean isDebug) {
        configs.isDebug = isDebug;
        return this;
    }

    public ConfigBuild setLoading(ImageLoader imageLoader) {
        this.imageLoader = imageLoader;
        return this;
    }


    public void setImageLoading(Context context, String path, ImageView imageView) {
        if (imageLoader == null) {
            return;
        }
        imageLoader.imageLoading(context, path, imageView);
    }
    public void interdictMsg(Context context,ImageEntity imageEntity) {
        if (imageLoader == null) {
            return;
        }
        imageLoader.interdictMsg(context,imageEntity);
    }
    public void build(Activity activity) {
        if (configs.configBuildSingle != null) {
            configs.configBuildMore = null;
            configs.isMore = false;
            configs.isCrop = true;
        }
        if (configs.configBuildMore != null) {
            configs.isMore = true;
        }
        if (configs.configBarCrop != null) {
            configs.configBarCrop.setCommon(configs.configBarCommon);
        }
        if (configs.configPreview != null) {
            configs.configPreview.setCommon(configs.configBarCommon);
        }
        DataStore.restData(activity);
        Intent it = new Intent(activity, IncidentActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("config", configs);
        it.putExtras(bundle);
        activity.startActivityForResult(it, Configs.TASK_START);
    }

    public void buildPreviewOnly(Activity activity, int index) {
        configs.previewIndex = index;
        DataStore.restData(activity);
        Intent it = new Intent(activity, PreviewOnlyActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("config", configs);
        it.putExtras(bundle);
        activity.startActivityForResult(it, Configs.TASK_START);
    }

    public void buildPreviewDelete(Activity activity, int index) {
        configs.previewIndex = index;
        DataStore.restData(activity);
        Intent it = new Intent(activity, PreviewDeleteActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("config", configs);
        it.putExtras(bundle);
        activity.startActivityForResult(it, Configs.TASK_START);
    }
}
