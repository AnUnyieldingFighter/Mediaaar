package media.library.images.config.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
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
    //拍摄时间
    public long mediaDateTaken;
    @PrimaryKey()
    @NonNull
    //图片/视频id
    public String mediaId;
    //相册名字
    public String mediaFileName;
    //图片类型 -1 表示显示添加
    public String mediaType;
    //图片/视频大小
    public long mediaSize;//字节为单位
    public double mediaSizeMb;//MB为单位

    //相册id
    public String mediaFileId;
    // 经度
    public int mediaAngle;
    public int width;
    public int height;
    //视频持续时间  毫秒
    public long videoDurations;
    //true 被选中
    //1 图片/gif 2 视频
    public int type;
    @Ignore
    public boolean isOption;
    //false 不可以删除
    @Ignore
    public boolean isDelete = true;
    //true 发送的是压缩图
    @Ignore
    public boolean optionTailor = true;
    @Ignore
    public String url;
    @Ignore
    //上传状态 0 未上传 1：上传中 2：上传成功 3：上传失败
    public int upSate;
    @Ignore
    public String other;


    @Override
    public String toString() {
        return "MediaEntity{" +
                "index=" + index +
                ", mediaPathSource='" + mediaPathSource + '\'' +
                ", mediaPath='" + mediaPath + '\'' +
                ", mediaName='" + mediaName + '\'' +
                ", mediaTime=" + mediaTime +
                ", mediaDateTaken=" + mediaDateTaken +
                ", mediaId='" + mediaId + '\'' +
                ", mediaFileName='" + mediaFileName + '\'' +
                ", mediaType='" + mediaType + '\'' +
                ", mediaSize=" + mediaSize +
                ", mediaFileId='" + mediaFileId + '\'' +
                ", mediaAngle=" + mediaAngle +
                ", width=" + width +
                ", height=" + height +
                ", videoDurations=" + videoDurations +
                ", type=" + type +
                ", isOption=" + isOption +
                ", isDelete=" + isDelete +
                ", optionTailor=" + optionTailor +
                ", url='" + url + '\'' +
                ", upSate=" + upSate +
                ", other='" + other + '\'' +
                '}';
    }
}