package com.images.config.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "tab_media")
public class MediaEntity implements Serializable {
    public int index;
    //原图片/视频路径
    public String mediaPathSource;
    //裁剪或者压缩的图片路径
    public String mediaPath;
    //图片名称
    public String mediaName;
    //创建时间
    public long mediaTime;
    @PrimaryKey()
    @NonNull
    //图片id
    public String mediaId;
    //相册名字
    public String mediaFileName;
    //图片类型 -1 表示显示添加
    public String mediaType;
    //图片大小
    public String mediaSize;
    //相册id
    public String mediaFileId;
    // 经度
    public int mediaAngle;
    public int width;
    public int height;
    //true 被选中
    //1 图片/gif 2 视频
    public int type;
    public boolean isOption;
    //false 不可以删除
    public boolean isDelete = true;
    //true 发送的是压缩图
    public boolean optionTailor = true;
    public String url;

}