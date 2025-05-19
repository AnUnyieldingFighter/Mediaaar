package com.images.config.operation;

import com.images.config.ConfigBuild;

import java.io.Serializable;

/**
 * 普通 导航条
 * Created by Administrator on 2016/10/19.
 */
public class ConfigBarCommon implements Serializable {
    private int titleColor, titleSize;
    private int backColor, backSize, backIconbg;
    private int optionColor, optionSize, optionIconbg;
    private String back, title, option;

    public ConfigBarCommon() {

    }

    public ConfigBarCommon setTitleColor(int titleColor) {
        this.titleColor = titleColor;
        return this;
    }

    public ConfigBarCommon setTitleSize(int titleSize) {
        this.titleSize = titleSize;
        return this;
    }

    public ConfigBarCommon setBackColor(int backColor) {
        this.backColor = backColor;
        return this;
    }

    public ConfigBarCommon setBackSize(int backSize) {
        this.backSize = backSize;
        return this;
    }

    public ConfigBarCommon setOptionColor(int optionColor) {
        this.optionColor = optionColor;
        return this;
    }

    public ConfigBarCommon setOptionSize(int optionSize) {
        this.optionSize = optionSize;
        return this;
    }

    public ConfigBarCommon setOptionIconbg(int optionIconbg) {
        this.optionIconbg = optionIconbg;
        return this;
    }

    public ConfigBarCommon setBackIconbg(int backIconbg) {
        this.backIconbg = backIconbg;
        return this;
    }

    public ConfigBarCommon setBack(String back) {
        this.back = back;
        return this;
    }

    public ConfigBarCommon setTitle(String title) {
        this.title = title;
        return this;
    }

    public ConfigBarCommon setOption(String option) {
        this.option = option;
        return this;
    }

    public ConfigBuild complete() {
        return ConfigBuild.getBuild();
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
}
