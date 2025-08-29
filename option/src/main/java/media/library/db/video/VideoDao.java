package media.library.db.video;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import media.library.player.bean.VideoEntity;

/**
 * 视频缓存
 *
 * @Description:
 * @Author: lp
 * @CreateDate: 2021/09/14
 */
@Dao
public interface VideoDao {

    /*增 改*/
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void put(VideoEntity media);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void putList(List<VideoEntity> medias);

    @Update
    int update(VideoEntity media);

    @Query("UPDATE tab_video SET pro = :pro, total = :total, lookTime = :lookTime WHERE videoUrl = :videoUrl")
    int updateVideoLookHis(String videoUrl, long pro, long total, long lookTime);

    @Query("UPDATE tab_video SET videoWidth = :videoWidth, total = :videoHeight  WHERE videoUrl = :videoUrl")
    int updateVideoSize(String videoUrl, int videoWidth, int videoHeight);

    @Delete
    void delete(VideoEntity media);

    //SELECT * FROM table_name ORDER BY column_name ASC|DESC;

    @Query("select * from tab_video ORDER BY recordTime DESC")
    List<VideoEntity> queryAll();

    //DESC 降序
    @Query("select * from tab_video where isLook=:isLook ORDER BY lookTime DESC LIMIT 100")
    List<VideoEntity> queryLookHisAll(boolean isLook);

    @Query("select * from tab_video where videoUrl=:videoUrl  ORDER BY recordTime DESC")
    List<VideoEntity> queryVideoCache(String videoUrl);

    //根据缓存地址 设置设置文件的引用情况
    @Query("UPDATE tab_video SET videoCacheType = :useType ,videoCacheTime=:time where videoCachePath=:cacheFilePath ")
    int updateCacheFile(int useType,String cacheFilePath,long time);


    @Query("UPDATE tab_video SET videoCacheType = :useType")
    int updateCacheFile(int useType);
    @Query("SELECT COUNT(*) FROM tab_video")
    int queryCount();

    @Query("SELECT COUNT(*) FROM tab_video where isLook=:isLook")
    int queryLookHisCount(boolean isLook);

    @Query("delete from tab_video")
    void deleteAll();

    @Query("delete from tab_video where videoUrl=:videoUrl")
    void deleteVideo(String videoUrl);
}
