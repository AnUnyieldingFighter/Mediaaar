package media.library.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;


;
import media.library.images.config.entity.MediaEntity;
import media.library.db.media.MediaDao;
import media.library.db.media.MediaDb;

/**
 * @author 86134
 */
@Database(entities = {MediaEntity.class}, version = 3, exportSchema = false)
public abstract class BaseDatabase extends RoomDatabase {
    public abstract MediaDao mediaDao();

    public MediaDb getMediaDb() {
        return new MediaDb(this);
    }

}
