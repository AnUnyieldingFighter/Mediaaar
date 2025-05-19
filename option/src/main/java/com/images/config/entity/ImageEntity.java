package com.images.config.entity;

import java.io.Serializable;

public class ImageEntity implements Serializable {

    //原图片路径
    public String imagePathSource;
    //裁剪或者压缩的图片路径
    public String imagePath;
    //图片名称
    public String imageName;
    //创建时间
    public long imageTime;
    //图片id
    public String imageId;
    //相册名字
    public String imageFileName;
    //图片类型
    public String imageType;
    //图片大小
    public String imageSize;
    //相册id
    public String imageFileId;
    // 经度
    public int imageAngle;
    //true 被选中
    public boolean isOption;
    public int optNumber;
    //true 是外部预选
    public boolean isOptionFixation = true;
    //false 不可以删除
    public boolean isDelete = true;
    //true 发送的是压缩图
    public boolean optionTailor = true;
}