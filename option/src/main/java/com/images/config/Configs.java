package com.images.config;

import android.text.TextUtils;

import com.images.config.operation.ConfigBuildMore;
import com.images.config.operation.ConfigBuildCrop;
import com.images.config.entity.ImageEntity;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Administrator on 2016/10/17.
 */
public class Configs implements Serializable {
    public static final int TASK_START = 200;
    public static final int TASK_PICTURE_COMPLETE = 201;
    public static final int TASK_CROP_COMPLETE = 202;
    public static final int TASK_CANCEL = 203;
    public static final int TASK_PRIVATE_DELECTE = 204;
    public static final String TASK_COMPLETE_RESULT = "result";
    //
    public String filePath = "/temp/pictures";
    //选中的图片 或者 要预览的图片
    public ArrayList<String> listImagePath;
    public ArrayList<ImageEntity> listImage;
    //多选
    public boolean isMore;
    //裁剪
    public boolean isCrop;
    //只预览 选中的图片
    public int previewIndex;
    //只拍照
    public boolean isOnlyPhotograph;
    //可开启相机
    public boolean showCamera;
    //
    public boolean isDebug;
    //多选
    public ConfigBuildMore configBuildMore;
    //裁剪
    public ConfigBuildCrop configBuildSingle;
    //
    public static Configs build;

    public ArrayList<ImageEntity> getImages() {
        if (listImage == null) {
            listImage = new ArrayList<>();
        }
        if (listImagePath == null) {
            return listImage;
        }
        for (int i = 0; i < listImagePath.size(); i++) {
            String path = listImagePath.get(i);
            ImageEntity entity = new ImageEntity();
            entity.imagePathSource = path;
            if (TextUtils.isEmpty(path)) {
                continue;
            }
            listImage.add(entity);
        }
        return listImage;
    }
}
