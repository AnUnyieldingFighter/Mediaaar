package media.library.db.video;


import androidx.room.Query;

import java.util.List;

import media.library.db.BaseDatabase;
import media.library.images.config.entity.MediaEntity;
import media.library.player.bean.VideoEntity;


/**
 * @author 86134
 */

public class VideoDb {
    private BaseDatabase base;

    public VideoDb(BaseDatabase base) {
        this.base = base;
    }

    public VideoDao getDao() {
        return base.videoDao();
    }

    public void put(VideoEntity data) {
        getDao().put(data);
    }

    public void putList(List<VideoEntity> datas) {
        getDao().putList(datas);
    }

    public void update(VideoEntity data) {
        getDao().update(data);
    }

    public int updateVideoLookHis(String videoUrl, long pro, long total, long lookTime) {
        return getDao().updateVideoLookHis(videoUrl, pro, total, lookTime);
    }

    public int updateVideoSize(String videoUrl, int videoWidth, int videoHeight) {
        return getDao().updateVideoSize(videoUrl, videoWidth, videoHeight);
    }

    //确保当前视频有可更新的信息记录
    public void ensureVideoRecord(String videoUrl) {
        getDao().ensureVideoRecord(videoUrl, System.currentTimeMillis());
    }

    //保存已知视频总时长，单位毫秒
    public void updateVideoDuration(String videoUrl, long duration) {
        getDao().updateVideoDuration(videoUrl, duration);
    }

    //保存文件大小及用于展示的大小文本
    public void updateVideoFileSize(String videoUrl, long sizeKb, String sizeStr) {
        getDao().updateVideoFileSize(videoUrl, sizeKb, sizeStr);
    }

    //保存有明确来源的媒体标识、标题、描述和封面地址
    public void updateVideoMetadata(String videoUrl, String videoIds, String title,
                                    String description, String artworkUrl) {
        getDao().updateVideoMetadata(videoUrl, videoIds, title, description, artworkUrl);
    }

    public void delete(VideoEntity data) {
        getDao().delete(data);
    }


    public void queryCount() {
        getDao().queryCount();
    }

    public void queryAll() {
        getDao().queryAll();
    }


    public List<VideoEntity> queryLookHisAll(boolean isLook) {
        return getDao().queryLookHisAll(isLook);
    }

    public List<VideoEntity> queryVideoCache(String videoUrl) {
        return getDao().queryVideoCache(videoUrl);
    }
    public int queryLookHisCount(boolean isLook) {
        return getDao().queryLookHisCount(isLook);
    }

    //设置缓存文件被使用
    public int setCacheFileUse(String cacheFilePath) {
        return getDao().updateCacheFile(1, cacheFilePath,System.currentTimeMillis());
    }

    //设置缓存文件被释放
    public int setCacheFileRelease(String cacheFilePath) {
        return getDao().updateCacheFile(0, cacheFilePath,System.currentTimeMillis());
    }
    //设置缓存文件全部被释放
    public int setCacheFileReleaseAll() {
        return getDao().updateCacheFile(0);
    }
    public void deleteAll() {
        getDao().deleteAll();
    }
    public void deleteVideo(String videoUrl) {
        getDao().deleteVideo(videoUrl);
    }


}
