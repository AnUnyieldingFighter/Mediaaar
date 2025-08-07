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


    public void delete(VideoEntity data) {
        getDao().delete(data);
    }


    public void queryCount() {
        getDao().queryCount();
    }

    public void queryAll() {
        getDao().queryAll();
    }

    public void deleteAll() {
        getDao().deleteAll();
    }

    public List<VideoEntity> queryLookHisAll(boolean isLook) {
        return getDao().queryLookHisAll(isLook);
    }

    public List<VideoEntity> queryVideoCache(String videoUrl) {
        return getDao().queryVideoCache(videoUrl);
    }

    public void deleteVideo(String videoUrl) {
        getDao().deleteVideo(videoUrl);
    }

    public int queryLookHisCount(boolean isLook) {
        return getDao().queryLookHisCount(isLook);
    }
}
