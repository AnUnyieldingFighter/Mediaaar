package com.images.config.operation;

import com.images.config.ConfigBuild;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/10/19.
 */
//裁剪配置
public class ConfigBuildCrop implements Serializable {
    private boolean isNotSystemCrop;
    private int aspectX = 60;
    private int aspectY = 60;
    private int outputX = 60;
    private int outputY = 60;


    public ConfigBuildCrop() {
    }


    public ConfigBuildCrop setNotSystemCrop(boolean notSystemCrop) {
        isNotSystemCrop = notSystemCrop;
        return this;
    }

    public ConfigBuildCrop setAspect(int aspectX, int aspectY) {
        this.aspectX = aspectX;
        this.aspectY = aspectY;
        return this;
    }

    public ConfigBuildCrop setOutput(int outputX, int outputY) {
        this.outputX = outputX;
        this.outputY = outputY;
        return this;
    }

    public ConfigBuildCrop setAspectX(int aspectX) {
        this.aspectX = aspectX;
        return this;
    }

    public ConfigBuildCrop setAspectY(int aspectY) {
        this.aspectY = aspectY;
        return this;
    }

    public ConfigBuildCrop setOutputX(int outputX) {
        this.outputX = outputX;
        return this;
    }

    public ConfigBuildCrop setOutputY(int outputY) {
        this.outputY = outputY;
        return this;
    }

    public ConfigBuild complete() {
        return ConfigBuild.getBuild();
    }
    //
    public boolean isNotSystemCrop() {
        return isNotSystemCrop;
    }

    public int getAspectX() {
        return aspectX;
    }

    public int getAspectY() {
        return aspectY;
    }

    public int getOutputX() {
        return outputX;
    }

    public int getOutputY() {
        return outputY;
    }
}

