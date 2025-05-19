package com.images.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;


;
import com.images.config.entity.MediaEntity;
import com.images.db.media.MediaDao;
import com.images.db.media.MediaDb;

/**
 * @author 86134
 */
@Database(entities = {MediaEntity.class}, version = 1, exportSchema = false)
public abstract class BaseDatabase extends RoomDatabase {
    public abstract MediaDao mediaDao();

    public MediaDb getMediaDb() {
        return new MediaDb(this);
    }

}
