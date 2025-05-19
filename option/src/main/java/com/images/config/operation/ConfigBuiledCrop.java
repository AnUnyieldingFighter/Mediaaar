package com.images.config.operation;

import com.images.config.ConfigBuild;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/10/19.
 */
//裁剪配置
public class ConfigBuiledCrop implements Serializable {
    private boolean isNotSystemCrop;
    private int aspectX = 60;
    private int aspectY = 60;
    private int outputX = 60;
    private int outputY = 60;


    public ConfigBuiledCrop() {
    }


    public ConfigBuiledCrop setNotSystemCrop(boolean notSystemCrop) {
        isNotSystemCrop = notSystemCrop;
        return this;
    }

    public ConfigBuiledCrop setAspect(int aspectX, int aspectY) {
        this.aspectX = aspectX;
        this.aspectY = aspectY;
        return this;
    }

    public ConfigBuiledCrop setOutput(int outputX, int outputY) {
        this.outputX = outputX;
        this.outputY = outputY;
        return this;
    }

    public ConfigBuiledCrop setAspectX(int aspectX) {
        this.aspectX = aspectX;
        return this;
    }

    public ConfigBuiledCrop setAspectY(int aspectY) {
        this.aspectY = aspectY;
        return this;
    }

    public ConfigBuiledCrop setOutputX(int outputX) {
        this.outputX = outputX;
        return this;
    }

    public ConfigBuiledCrop setOutputY(int outputY) {
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

