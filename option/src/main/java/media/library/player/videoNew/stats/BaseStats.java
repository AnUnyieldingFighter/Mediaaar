package media.library.player.videoNew.stats;

import android.text.TextUtils;

import media.library.player.bean.VideoEntity;

public class BaseStats {
    private long videoDuration;//视频总时长，单位毫秒，0 表示尚未获取
    private long videoProgress;//当前播放进度，单位毫秒
    private int videoHeight;//视频高度，单位像素
    private int videoWidth;//视频宽度，单位像素
    private long sizeKb;//视频文件总大小，单位 KB（1024 字节）
    private String sizeStr = "";//文件大小展示文本
    private int indexId;//本次读取的数据库记录编号
    private String videoIds = "";//视频业务标识
    private String coverImgUrl = "";//封面地址
    private String videoName = "";//视频标题
    private String videoDescribe = "";//视频描述
    private int videoType;//1 视频，2 短视频，3 短剧
    private String videoCachePath = "";//数据库记录的缓存目录
    private int videoCacheType;//读取数据库时的缓存引用状态：0 无引用，1 使用中
    private long videoCacheTime;//数据库记录的缓存状态更新时间，时间戳毫秒
    private long videoHistoryProgress;//历史播放进度，单位毫秒
    private long recordTime;//数据库记录创建时间，时间戳毫秒
    private boolean isLook;//数据库是否已记录为看过
    private long lookTime;//数据库记录的最后观看时间，时间戳毫秒

    protected void clearVideoData() {
        videoDuration = 0;
        videoProgress = 0;
        videoHeight = 0;
        videoWidth = 0;
        sizeKb = 0;
        sizeStr = "";
        indexId = 0;
        videoIds = "";
        coverImgUrl = "";
        videoName = "";
        videoDescribe = "";
        videoType = 0;
        videoCachePath = "";
        videoCacheType = 0;
        videoCacheTime = 0;
        videoHistoryProgress = 0;
        recordTime = 0;
        isLook = false;
        lookTime = 0;
    }
    //===============================外部用方法

    //获取视频总时长，单位毫秒，尚未获取时返回 0
    public long getVideoDuration() {
        return videoDuration;
    }

    //设置有效的视频总时长，单位毫秒；未知值或 0 不覆盖已有时长，切换地址时由 clearVideoData 清空
    public void setVideoDuration(long videoDuration) {
        if (videoDuration > 0) {
            this.videoDuration = videoDuration;
        }
    }

    //获取当前播放进度，单位毫秒
    public long getVideoProgress() {
        return videoProgress;
    }

    //设置当前播放进度，单位毫秒，负数时保存为 0
    public void setVideoProgress(long videoProgress) {
        this.videoProgress = Math.max(0, videoProgress);
    }

    //获取视频高度，单位像素
    public int getVideoHeight() {
        return videoHeight;
    }

    //设置视频高度，单位像素，未知或负数时保存为 0
    public void setVideoHeight(int videoHeight) {
        this.videoHeight = Math.max(0, videoHeight);
    }

    //获取视频宽度，单位像素
    public int getVideoWidth() {
        return videoWidth;
    }

    //设置视频宽度，单位像素，未知或负数时保存为 0
    public void setVideoWidth(int videoWidth) {
        this.videoWidth = Math.max(0, videoWidth);
    }
    //====================数据库视频信息==================

    //获取视频文件总大小，单位 KB，不表示已下载或已缓存大小
    public long getSizeKb() {
        return sizeKb;
    }

    //获取文件大小展示文本
    public String getSizeStr() {
        return sizeStr;
    }

    //设置视频文件总大小，单位 KB，并同步保存用于显示的大小文本
    public void setVideoFileSize(long sizeKb, String sizeStr) {
        if (sizeKb < 0) {
            return;
        }
        this.sizeKb = sizeKb;
        this.sizeStr = sizeStr;
    }

    //获取本次读取的数据库记录编号
    public int getIndexId() {
        return indexId;
    }

    //获取视频业务标识
    public String getVideoIds() {
        return videoIds;
    }

    //获取封面地址
    public String getCoverImgUrl() {
        return coverImgUrl;
    }

    //获取视频标题
    public String getVideoName() {
        return videoName;
    }

    //获取视频描述
    public String getVideoDescribe() {
        return videoDescribe;
    }

    //获取视频业务类型：1 视频，2 短视频，3 短剧
    public int getVideoType() {
        return videoType;
    }

    //获取数据库记录的缓存目录
    public String getVideoCachePath() {
        return videoCachePath;
    }

    public void setVideoCachePath(String videoCachePath) {
        this.videoCachePath = videoCachePath;
        videoCacheType = 0;
    }

    //获取读取数据库时的缓存引用状态，不代表实时状态
    public int getVideoCacheType() {
        return videoCacheType;
    }

    //获取数据库记录的缓存状态更新时间，时间戳毫秒
    public long getVideoCacheTime() {
        return videoCacheTime;
    }

    //获取历史播放进度，单位毫秒，不代表当前播放器的位置
    public long getVideoHistoryProgress() {
        return videoHistoryProgress;
    }

    //获取数据库记录创建时间，时间戳毫秒
    public long getRecordTime() {
        return recordTime;
    }

    //获取数据库是否已记录为看过，不代表当前正在播放
    public boolean isLook() {
        return isLook;
    }

    //获取数据库记录的最后观看时间，时间戳毫秒，不是观看时长
    public long getLookTime() {
        return lookTime;
    }
    //=============读取数据库的数据

    //读取数据库的视频信息，有效总时长、宽高和文件大小用于初始化；未知规格不覆盖现有数据
    //历史进度和最后观看时间单独保存；当前地址已由 setVideoUrl 设置，此处不重置播放统计
    public void setReadDB(VideoEntity videoEntity) {
        if (videoEntity == null) {
            return;
        }
        if (videoEntity.total > 0) {
            setVideoDuration(videoEntity.total);
        }
        if (videoEntity.videoWidth > 0 && videoEntity.videoHeight > 0) {
            setVideoWidth(videoEntity.videoWidth);
            setVideoHeight(videoEntity.videoHeight);
        }
        if (videoEntity.sizeKb > 0) {
            sizeKb = videoEntity.sizeKb;
        }
        if (!TextUtils.isEmpty(videoEntity.sizeStr)) {
            sizeStr = videoEntity.sizeStr;
        }
        indexId = videoEntity.indexId;
        videoIds = videoEntity.videoIds;
        coverImgUrl = videoEntity.coverImgUrl;
        videoName = videoEntity.videoName;
        videoDescribe = videoEntity.videoDescribe;
        videoType = videoEntity.videoType;
        videoCachePath = videoEntity.videoCachePath;
        videoCacheType = videoEntity.videoCacheType;
        videoCacheTime = videoEntity.videoCacheTime;
        videoHistoryProgress = Math.max(0, videoEntity.pro);
        recordTime = videoEntity.recordTime;
        isLook = videoEntity.isLook;
        lookTime = videoEntity.lookTime;
    }

}
