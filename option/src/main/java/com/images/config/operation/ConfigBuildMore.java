package com.images.config.operation;

import com.images.config.ConfigBuild;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/10/19.
 */
//多选配置
public class ConfigBuildMore implements Serializable {
    //最多可多照片的数量
    private int imageSelectMaximum;

    public ConfigBuildMore() {
    }

    public ConfigBuildMore setImageSelectMaximum(int imageSelectMaximum) {
        this.imageSelectMaximum = imageSelectMaximum;
        return this;
    }

    public ConfigBuild complete() {
        return ConfigBuild.getBuild();
    }
    //

    public int getImageSelectMaximum() {
        return imageSelectMaximum;
    }
}
