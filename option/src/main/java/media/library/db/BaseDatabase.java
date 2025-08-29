package media.library.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import media.library.db.video.VideoDao;
import media.library.db.video.VideoDb;
import media.library.images.config.entity.MediaEntity;
import media.library.db.media.MediaDao;
import media.library.db.media.MediaDb;
import media.library.player.bean.VideoEntity;

/**
 * @author 86134
 */
@Database(entities = {MediaEntity.class, VideoEntity.class}, version = 8, exportSchema = false)
public abstract class BaseDatabase extends RoomDatabase {
    public abstract MediaDao mediaDao();

    public MediaDb getMediaDb() {
        return new MediaDb(this);
    }


    public abstract VideoDao videoDao();

    public VideoDb getVideoDb() {
        return new VideoDb(this);
    }


}
