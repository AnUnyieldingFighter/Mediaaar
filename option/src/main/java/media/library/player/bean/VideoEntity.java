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
    public int videoCacheType;//0 无引用 1  正在缓存中
    public long videoCacheTime;//去缓存时间

    public long pro;//历史播放进度，单位毫秒，只在暂停、完成或停止时更新
    public long total;//视频总时长，单位毫秒
    public long recordTime;//记录创建时间，时间戳毫秒
    public long sizeKb;//文件总大小，沿用原字段单位 KB（1024 字节），不是已下载或已缓存大小
    public String sizeStr;//文件大小展示文本，例如 12.50 MB
    public int videoWidth;//	视频宽度
    public int videoHeight;//	视频高度
    public int videoType;//1 视频  2 短视频  3 短剧

    public String other1;//其它1
    public String other2;//其它2
    public String other3;//其它3

    public boolean isLook;//true 看过
    public long lookTime;//最后观看记录时间，时间戳毫秒，不是累计观看时长

}
