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

    //暂停、完成或停止时保存历史进度，未知时长不覆盖已知时长
    @Query("UPDATE tab_video SET pro = CASE WHEN :pro >= 0 THEN :pro ELSE pro END, "
            + "total = CASE WHEN :total > 0 THEN :total ELSE total END, "
            + "isLook = 1, lookTime = :lookTime WHERE videoUrl = :videoUrl")
    int updateVideoLookHis(String videoUrl, long pro, long total, long lookTime);

    //即使关闭文件缓存，也建立视频信息记录；同一地址已有记录时不重复插入
    @Query("INSERT INTO tab_video (videoUrl, recordTime, videoCacheType, videoCacheTime, "
            + "pro, total, sizeMb, videoWidth, videoHeight, videoType, isLook, lookTime) "
            + "SELECT :videoUrl, :recordTime, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 "
            + "WHERE NOT EXISTS (SELECT 1 FROM tab_video WHERE videoUrl = :videoUrl)")
    void ensureVideoRecord(String videoUrl, long recordTime);

    //单独保存总时长，不修改播放进度或观看时间
    @Query("UPDATE tab_video SET total = :total WHERE videoUrl = :videoUrl AND total != :total")
    int updateVideoDuration(String videoUrl, long total);

    //保存文件大小，sizeMb 沿用原字段约定，单位为 KB（1024 字节）
    @Query("UPDATE tab_video SET sizeMb = :sizeKb, sizeStr = :sizeStr WHERE videoUrl = :videoUrl "
            + "AND (sizeMb != :sizeKb OR sizeStr IS NULL OR sizeStr != :sizeStr)")
    int updateVideoFileSize(String videoUrl, long sizeKb, String sizeStr);

    //元数据缺失时保留原值，不用空值清除调用方已保存的业务信息
    @Query("UPDATE tab_video SET videoIds = COALESCE(:videoIds, videoIds), "
            + "videoName = COALESCE(:videoName, videoName), "
            + "videoDescribe = COALESCE(:videoDescribe, videoDescribe), "
            + "coverImgUrl = COALESCE(:coverImgUrl, coverImgUrl) WHERE videoUrl = :videoUrl")
    int updateVideoMetadata(String videoUrl, String videoIds, String videoName,
                            String videoDescribe, String coverImgUrl);

    @Query("UPDATE tab_video SET videoWidth = :videoWidth, videoHeight = :videoHeight WHERE videoUrl = :videoUrl")
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
