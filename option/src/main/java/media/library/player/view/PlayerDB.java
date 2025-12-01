package media.library.player.view;

import android.content.Context;

import java.io.File;
import java.util.Date;
import java.util.List;

import androidx.media3.common.VideoSize;
import media.library.db.MediaRoom;
import media.library.player.bean.VideoEntity;

//保存历史数据
class PlayerDB {
    //更新播放进度
    protected void dbUpdateVideoPro(Context context, String videoUrl, long currentPosition, long duration) {
        long time = new Date().getTime();
        MediaRoom.geVideoDb(context).updateVideoLookHis(videoUrl, currentPosition, duration, time);

    }

    //获取视频大小
    protected List<VideoEntity> getDBVideoSize(Context context, String videoUrl) {
        List<VideoEntity> datas = MediaRoom.geVideoDb(context).queryVideoCache(videoUrl);
        return datas;
    }

    //获取缓存地址
    protected List<VideoEntity> getDBVideoCache(Context context, String videoUrl) {
        List<VideoEntity> datas = MediaRoom.geVideoDb(context).queryVideoCache(videoUrl);
        return datas;
    }

    //添加缓存地址 且设置被使用
    protected void dbVideoAdd(Context context, String videoUrl, File videoFile) {
        VideoEntity videoEntity = new VideoEntity();
        videoEntity.videoUrl = videoUrl;
        videoEntity.videoCachePath = videoFile.getPath();
        videoEntity.videoCacheType = 1;
        long time = new Date().getTime();
        videoEntity.recordTime = time;
        MediaRoom.geVideoDb(context).put(videoEntity);
    }

    //设置缓存地址  被使用
    protected void dbSetVideoCacheUse(Context context, File videoFile) {
        MediaRoom.geVideoDb(context).setCacheFileUse(videoFile.getPath());
    }

    //设置缓存地址  未被使用
    protected void dbSetVideoCacheUsable(Context context, File videoFile) {
        MediaRoom.geVideoDb(context).setCacheFileRelease(videoFile.getPath());
    }

    //设置视频大小
    protected void dbSetVideoSize(Context context, String videoUrl, VideoSize videoSize) {
        MediaRoom.geVideoDb(context).updateVideoSize(videoUrl, videoSize.width, videoSize.height);

    }
}
