package com.images.config.operation;

import android.text.TextUtils;

import com.images.config.ConfigBuild;

import java.io.Serializable;

/**
 * 裁剪 导航条
 * Created by Administrator on 2016/10/19.
 */
public class ConfigBarCrop implements Serializable {
    private int titleColor, titleSize;
    private int backColor, backSize, backIconbg;
    private int optionColor, optionSize, optionIconbg;
    private String back, title, option;

    public ConfigBarCrop() {

    }

    public ConfigBarCrop setTitleColor(int titleColor) {
        this.titleColor = titleColor;
        return this;
    }

    public ConfigBarCrop setTitleSize(int titleSize) {
        this.titleSize = titleSize;
        return this;
    }

    public ConfigBarCrop setBackColor(int backColor) {
        this.backColor = backColor;
        return this;
    }

    public ConfigBarCrop setBackSize(int backSize) {
        this.backSize = backSize;
        return this;
    }

    public ConfigBarCrop setOptionColor(int optionColor) {
        this.optionColor = optionColor;
        return this;
    }

    public ConfigBarCrop setOptionSize(int optionSize) {
        this.optionSize = optionSize;
        return this;
    }

    public ConfigBarCrop setOptionIconbg(int optionIconbg) {
        this.optionIconbg = optionIconbg;
        return this;
    }

    public ConfigBarCrop setBackIconbg(int backIconbg) {
        this.backIconbg = backIconbg;
        return this;
    }

    public ConfigBarCrop setTitle(String title) {
        this.title = title;
        return this;
    }

    public ConfigBarCrop setOption(String option) {
        this.option = option;
        return this;
    }

    public ConfigBarCrop setBack(String back) {
        this.back = back;
        return this;
    }

    public ConfigBuild complete() {
        return ConfigBuild.getBuild();
    }

    public void setCommon(ConfigBarCommon common) {
        if (titleColor == 0) {
            titleColor = common.getTitleColor();
        }
        if (titleSize == 0) {
            titleSize = common.getTitleSize();
        }
        if (backColor == 0) {
            backColor = common.getBackColor();
        }
        if (backIconbg == 0) {
            backIconbg = common.getBackIconbg();
        }
        if (backSize == 0) {
            backSize = common.getBackSize();
        }
        if (optionColor == 0) {
            optionColor = common.getOptionColor();
        }
        if (optionSize == 0) {
            optionSize = common.getOptionSize();
        }
        if (optionIconbg == 0) {
            optionIconbg = common.getOptionIconbg();
        }
        if (TextUtils.isEmpty(back)) {
            back = common.getBack();
        }
        if (TextUtils.isEmpty(title)) {
            title = common.getTitle();
        }
        if (TextUtils.isEmpty(option)) {
            option = common.getOption();
        }
    }
    //

    public int getTitleColor() {
        return titleColor;
    }

    public int getTitleSize() {
        return titleSize;
    }

    public int getBackColor() {
        return backColor;
    }

    public int getBackSize() {
        return backSize;
    }

    public int getOptionColor() {
        return optionColor;
    }

    public int getOptionSize() {
        return optionSize;
    }

    public int getOptionIconbg() {
        return optionIconbg;
    }

    public String getTitle() {
        return title;
    }

    public String getOption() {
        return option;
    }

    public int getBackIconbg() {
        return backIconbg;
    }

    public String getBack() {
        return back;
    }
}
