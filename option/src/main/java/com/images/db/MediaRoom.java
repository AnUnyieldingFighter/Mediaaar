package com.images.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.images.db.media.MediaDb;
import com.images.unmix.ImageLog;


public class MediaRoom {
    private static BaseDatabase baseDB;

    public static void init(Context context) {
        baseDB = Room.databaseBuilder(context, BaseDatabase.class, "media_app.db").allowMainThreadQueries().addMigrations(new MigrationDB(1, 2)).fallbackToDestructiveMigration().build();
    }

    //
    private static MediaDb mediaDb;

    private static void initCity(Context context) {
        if (baseDB == null) {
            init(context);
        }
        if (mediaDb == null) {
            mediaDb = baseDB.getMediaDb();
        }

    }

    public static MediaDb geMediaDb(Context context) {
        if (mediaDb == null) {
            initCity(context);
        }
        return mediaDb;
    }

    //
    static class MigrationDB extends Migration {

        private int type;

        public MigrationDB(int startVersion, int endVersion) {
            super(startVersion, endVersion);
        }


        private void checkDBmedia(@NonNull SupportSQLiteDatabase database) {
            //检查城市数据库
            ImageLog.d("数据库 检查城市数据库");
            String sq21 = "CREATE TABLE IF NOT EXISTS tab_media (" + "mediaId TEXT PRIMARY KEY NOT NULL,"
                    + "mediaPathSource TEXT ,"
                    + "mediaPath TEXT ,"
                    + "mediaName TEXT,"
                    + "mediaTime Long,"
                    + "mediaFileName TEXT,"
                    + "mediaType TEXT,"
                    + "mediaSize TEXT,"
                    + "mediaFileId TEXT,"
                    + "type Int,"
                    + "width Int,"
                    + "height Int,"
                    + "videoDurations Long,"
                    + "mediaDateTaken Long,"

                    + "mediaAngle TEXT" + ")";
            database.execSQL(sq21);
        }

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            checkDBmedia(database);
        }
    }
}
