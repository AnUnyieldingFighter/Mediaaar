package media.library.player.bean;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tab_video")
public class VideoEntity {
    @PrimaryKey(autoGenerate = true)
    public int indexId;//自增字段
    public String videoIds;
    public String coverImgUrl;//封面图
    public String videoUrl;//视频url
    public String videoName;//视频标题
    public String videoDescribe;//视频描述

    public String videoCachePath;//缓存地址

    public long pro;//视频进度
    public long total;//视频总长度
    public long recordTime;//记录时间
    public long sizeMb;//	文件大小/kb
    public String sizeStr;//	文件大小
    public int videoWidth;//	视频宽度
    public int videoHeight;//	视频高度

    public String other1;//其它1
    public String other2;//其它2
    public String other3;//其它3

    public boolean isLook;//true 看过
    public long lookTime;//观看记录时间

}
