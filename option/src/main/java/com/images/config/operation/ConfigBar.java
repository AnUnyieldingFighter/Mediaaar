package com.images.config.operation;

import com.images.config.ConfigBuild;

import java.io.Serializable;

/**
 * 导航条
 * Created by Administrator on 2016/10/19.
 */
public class ConfigBar implements Serializable {
    //状态栏颜色
    private int statusBarColor;
    //背景色
    private int actionBarColor;
    //高度
    private int actionBarHeight;

    public ConfigBar() {

    }

    public ConfigBar setStatusBarColor(int statusBarColor) {
        this.statusBarColor = statusBarColor;
        return this;
    }

    public ConfigBar setActionBarColor(int actionBarColor) {
        this.actionBarColor = actionBarColor;
        return this;
    }

    public ConfigBar setActionBarHeight(int actionBarHeight) {
        this.actionBarHeight = actionBarHeight;
        return this;
    }

    public ConfigBuild complete() {
        return ConfigBuild.getBuild();
    }

    //
    public int getStatusBarColor() {
        return statusBarColor;
    }

    public int getActionBarColor() {
        return actionBarColor;
    }

    public int getActionBarHeight() {
        return actionBarHeight;
    }
}
