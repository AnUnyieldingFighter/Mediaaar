package media.library.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import media.library.db.media.MediaDb;
import media.library.db.video.VideoDb;
import media.library.images.unmix.ImageLog;


public class MediaRoom {
    private static BaseDatabase baseDB;

    public static void init(Context context) {
        baseDB = Room.databaseBuilder(context, BaseDatabase.class, "media_app.db").allowMainThreadQueries().addMigrations(new MigrationDB(1, 2)).fallbackToDestructiveMigration().build();
    }

    //
    private static MediaDb mediaDb;

    private static void initMedia(Context context) {
        if (baseDB == null) {
            init(context);
        }
        if (mediaDb == null) {
            mediaDb = baseDB.getMediaDb();
        }

    }

    public static MediaDb geMediaDb(Context context) {
        if (mediaDb == null) {
            initMedia(context);
        }
        return mediaDb;
    }

    //
    private static VideoDb videoDb;

    private static void initVideo(Context context) {
        if (baseDB == null) {
            init(context);
        }
        if (videoDb == null) {
            videoDb = baseDB.getVideoDb();
        }

    }

    public static VideoDb geVideoDb(Context context) {
        if (videoDb == null) {
            initVideo(context);
        }
        return videoDb;
    }

    //
    static class MigrationDB extends Migration {

        private int type;

        public MigrationDB(int startVersion, int endVersion) {
            super(startVersion, endVersion);
        }


        private void checkDBmedia(@NonNull SupportSQLiteDatabase database) {
            //

            String sq21 = "CREATE TABLE IF NOT EXISTS tab_media (" + "mediaId TEXT PRIMARY KEY NOT NULL," + "mediaPathSource TEXT ," + "mediaPath TEXT ," + "mediaName TEXT," + "mediaTime Long," + "mediaFileName TEXT," + "mediaType TEXT," + "mediaSize TEXT," + "mediaFileId TEXT," + "type Int," + "width Int," + "height Int," + "videoDurations Long," + "mediaDateTaken Long,"

                    + "mediaAngle TEXT" + ")";
            database.execSQL(sq21);
        }

        private void checkDBVideo(@NonNull SupportSQLiteDatabase database) {


            String sq21 = "CREATE TABLE IF NOT EXISTS tab_video ("
                    + "videoUrl TEXT ,"
                    + "indexId Int  PRIMARY KEY,"
                    + "videoIds TEXT ,"
                    + "coverImgUrl TEXT,"
                    + "videoName TEXT,"
                    + "videoDescribe TEXT,"
                    + "videoCachePath TEXT,"
                    + "pro Long,"
                    + "total Long,"


                    + "recordTime Long,"
                    + "sizeMb Long,"
                    + "sizeStr TEXT,"
                    + "videoWidth Int,"
                    + "videoHeight Int,"


                    + "isLook Boolean,"
                    + "lookTime Long,"
                    + "other1 TEXT,"
                    + "other2 TEXT,"
                    + "other3 TEXT" + ")";
            database.execSQL(sq21);
        }

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            checkDBmedia(database);
            checkDBVideo(database);
        }
    }
}
