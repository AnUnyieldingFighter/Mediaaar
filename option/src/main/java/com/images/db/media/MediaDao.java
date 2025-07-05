package com.images.db.media;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.images.config.entity.MediaEntity;

import java.util.List;

/**
 * 操作城市
 *
 * @Description:
 * @Author: lp
 * @CreateDate: 2021/09/14
 */
@Dao
public interface MediaDao {

    /*增 改*/
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void put(MediaEntity media);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void putList(List<MediaEntity> medias);

    @Update
    int update(MediaEntity media);

    @Delete
    void delete(MediaEntity media);


    //SELECT * FROM table_name ORDER BY column_name ASC|DESC;

    @Query("select * from tab_media ORDER BY mediaTime DESC")
    List<MediaEntity> queryAll();

    @Query("select * from tab_media where type=:type ORDER BY mediaTime DESC")
    List<MediaEntity> queryTypeAll(int type);
    @Query("select * from tab_media where type=:type AND mediaType NOT LIKE :mediaType ORDER BY mediaTime DESC")
    List<MediaEntity> queryTypeAllAndNotMediaType(int type,String mediaType);

    @Query("select * from tab_media where mediaType LIKE :mediaType ORDER BY mediaTime DESC")
    List<MediaEntity> queryAll(String mediaType);

    @Query("select * from tab_media where mediaType LIKE :mediaType OR type LIKE :type ORDER BY mediaTime DESC")
    List<MediaEntity> queryAll(String mediaType, int type);

    @Query("SELECT COUNT(*) FROM tab_media")
    int queryCount();

    @Query("delete from tab_media")
    void deleteAll();
}
