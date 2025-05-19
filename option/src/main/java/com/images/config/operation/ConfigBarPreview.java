package com.images.config.operation;

import android.text.TextUtils;

import com.images.config.ConfigBuild;

import java.io.Serializable;

/**
 * 预览 导航条
 * Created by Administrator on 2016/10/19.
 */
public class ConfigBarPreview implements Serializable {
    private int titleColor, titleSize;
    private int backColor, backSize, backIconbg;
    private int optionColor, optionSize, optionIconbg;
    private String back, title, option;


    public ConfigBarPreview() {
    }

    public ConfigBarPreview setTitleColor(int titleColor) {
        this.titleColor = titleColor;
        return this;
    }

    public ConfigBarPreview setTitleSize(int titleSize) {
        this.titleSize = titleSize;
        return this;
    }

    public ConfigBarPreview setOptionColor(int optionColor) {
        this.optionColor = optionColor;
        return this;
    }

    public ConfigBarPreview setOptionSize(int optionSize) {
        this.optionSize = optionSize;
        return this;
    }

    public ConfigBarPreview setTitle(String title) {
        this.title = title;
        return this;
    }

    public ConfigBarPreview setOptionIconbg(int optionIconbg) {
        this.optionIconbg = optionIconbg;
        return this;
    }

    public ConfigBarPreview setBackIconbg(int backIconbg) {
        this.backIconbg = backIconbg;
        return this;
    }

    public ConfigBarPreview setOption(String option) {
        this.option = option;
        return this;
    }

    public ConfigBarPreview setBack(String back) {
        this.back = back;
        return this;
    }

    public ConfigBarPreview setBackSize(int backSize) {
        this.backSize = backSize;
        return this;
    }

    public ConfigBarPreview setBackColor(int backColor) {
        this.backColor = backColor;
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
            titleColor = common.getTitleSize();
        }
        if (backColor == 0) {
            backColor = common.getBackColor();
        }
        if (backSize == 0) {
            backSize = common.getBackSize();
        }
        if (backIconbg == 0) {
            backIconbg = common.getBackIconbg();
        }
        if (optionColor == 0) {
            titleColor = common.getOptionColor();
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

    public int getOptionColor() {
        return optionColor;
    }

    public int getOptionSize() {
        return optionSize;
    }

    public int getOptionIconbg() {
        return optionIconbg;
    }

    public int getBackIconbg() {
        return backIconbg;
    }

    public String getTitle() {
        return title;
    }

    public String getOption() {
        return option;
    }

    public String getBack() {
        return back;
    }

    public int getBackSize() {
        return backSize;
    }

    public int getBackColor() {
        return backColor;
    }
}
