package media.library.images.manager;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;


import media.library.images.config.entity.MediaEntity;
import media.library.db.MediaRoom;

import java.util.ArrayList;

import java.util.List;

/**
 *
 */
public class MediaManager {
    private static MediaManager manager;

    public static MediaManager getInstance() {
        if (manager == null) {
            manager = new MediaManager();
        }
        return manager;
    }

    private final String[] IMAGE_PROJECTION = {MediaStore.Images.Media.DATA,                  //图片路径
            MediaStore.Images.Media.DISPLAY_NAME,          //图片名称
            MediaStore.Images.Media.DATE_ADDED,            //创建时间
            MediaStore.Images.Media._ID,                   //图片id
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,   //相册名字
            MediaStore.Images.Media.MIME_TYPE,             //图片类型
            MediaStore.Images.Media.SIZE,                  //图片大小
            MediaStore.Images.Media.BUCKET_ID,             //相册id
            MediaStore.Images.Media.LONGITUDE,             // 经度
            MediaStore.Images.Media.WIDTH,             // 宽度
            MediaStore.Images.Media.HEIGHT,             // 高度
            MediaStore.Images.Media.DATE_TAKEN,         // 拍摄日期

    };
    private final String[] VIDEO_PROJECTION = {MediaStore.Video.Media.DATA, //视频路径
            MediaStore.Video.Media.DISPLAY_NAME,          //视频名称
            MediaStore.Video.Media.DATE_ADDED,            //视频创建时间
            MediaStore.Video.Media._ID,                   //视频id
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,   //相册名字
            MediaStore.Video.Media.MIME_TYPE,             //视频类型
            MediaStore.Video.Media.SIZE,                  //视频大小
            MediaStore.Video.Media.BUCKET_ID,             //相册id
            MediaStore.Video.Media.LONGITUDE,             // 经度
            MediaStore.Video.Media.WIDTH,             // 宽度
            MediaStore.Video.Media.HEIGHT,             // 高度
            MediaStore.Video.Media.DATE_TAKEN,         // 拍摄日期


            MediaStore.Video.Media.DURATION,             // 持续时间


    };

    //不需要权限
    public MediaEntity getImg(Uri uri, Context context) {
        //
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(uri, IMAGE_PROJECTION, null, null, null);
        cursor.moveToFirst();
        MediaEntity image = readCursorImg(cursor);
        //image.type = 1;
        cursor.close();
        return image;

    }

    //不需要权限
    public MediaEntity getVideo(Uri uri, Context context) {
        //
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(uri, VIDEO_PROJECTION, null, null, null);
        cursor.moveToFirst();
        MediaEntity image = readCursorVideo(cursor);
        //image.type = 2;
        cursor.close();
        return image;

    }

    //==================设置监听======================================
    private OnLoadingListener onLoadingListener;

    public void setOnLoadingListener(OnLoadingListener onLoadingListener) {
        this.onLoadingListener = onLoadingListener;
    }

    public interface OnLoadingListener {
        /**
         * 搜索数据库回调
         *
         * @param resType 1:全部 2:图片 3：视频 4：GIF
         * @param files   搜索到的数据
         */
        void onLoadingMedia(int resType, ArrayList<MediaEntity> files);

        void onLoadingFail();
    }

    //==================读取游标======================================
    private MediaEntity readCursorImg(Cursor data) {
        MediaEntity image = new MediaEntity();
        image.mediaPathSource = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[0]));
        image.mediaName = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[1]));
        image.mediaTime = data.getLong(data.getColumnIndexOrThrow(IMAGE_PROJECTION[2]));
        image.mediaId = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[3]));
        image.mediaFileName = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[4]));
        image.mediaType = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[5]));
        image.mediaSize = data.getLong(data.getColumnIndexOrThrow(IMAGE_PROJECTION[6]));
        image.mediaFileId = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[7]));
        image.mediaAngle = data.getInt(data.getColumnIndexOrThrow(IMAGE_PROJECTION[8]));
        image.width = data.getInt(data.getColumnIndexOrThrow(IMAGE_PROJECTION[9]));
        image.height = data.getInt(data.getColumnIndexOrThrow(IMAGE_PROJECTION[10]));
        image.mediaDateTaken = data.getLong(data.getColumnIndexOrThrow(IMAGE_PROJECTION[11]));
        return image;
    }

    private MediaEntity readCursorVideo(Cursor data) {
        MediaEntity video = new MediaEntity();
        video.mediaPathSource = data.getString(data.getColumnIndexOrThrow(VIDEO_PROJECTION[0]));
        video.mediaName = data.getString(data.getColumnIndexOrThrow(VIDEO_PROJECTION[1]));
        video.mediaTime = data.getLong(data.getColumnIndexOrThrow(VIDEO_PROJECTION[2]));
        video.mediaId = data.getString(data.getColumnIndexOrThrow(VIDEO_PROJECTION[3]));
        video.mediaFileName = data.getString(data.getColumnIndexOrThrow(VIDEO_PROJECTION[4]));
        video.mediaType = data.getString(data.getColumnIndexOrThrow(VIDEO_PROJECTION[5]));
        video.mediaSize = data.getLong(data.getColumnIndexOrThrow(VIDEO_PROJECTION[6]));
        video.mediaFileId = data.getString(data.getColumnIndexOrThrow(VIDEO_PROJECTION[7]));
        video.mediaAngle = data.getInt(data.getColumnIndexOrThrow(VIDEO_PROJECTION[8]));
        video.width = data.getInt(data.getColumnIndexOrThrow(VIDEO_PROJECTION[9]));
        video.height = data.getInt(data.getColumnIndexOrThrow(VIDEO_PROJECTION[10]));
        video.mediaDateTaken = data.getLong(data.getColumnIndexOrThrow(VIDEO_PROJECTION[11]));
        video.videoDurations = data.getLong(data.getColumnIndexOrThrow(VIDEO_PROJECTION[12]));
        return video;
    }
    //================获取照片=====================================

    private ImagesThread run;
    private Context context;

    public void initMediaDB(Context context) {
        if (run != null && run.isRuning) {
            return;
        }
        this.context = context;
        run = new ImagesThread();
        run.start();
    }

    //1:全部 2:图片（含GIT） 3：视频 4：GIF 5 ：图片和gif  6：只有图片
    private int resType;
    private MediaThread mediaThread;

    /**
     * 请求获取照片
     *
     * @param resType 1:全部 2:图片（含GIF） 3：视频 4：GIF  5：图片和Gif  6：只有图片
     */
    public void doReq(int resType) {
        this.resType = resType;
        if (run != null && run.isRuning) {
            return;
        }
        doReqRest();
    }

    private void doReqRest() {
        if (mediaThread != null && mediaThread.isRuning) {
            return;
        }
        mediaThread = new MediaThread();
        mediaThread.run();
    }

    class ImagesThread extends Thread {
        private boolean isRuning = false;


        public ImagesThread() {

        }


        @Override
        public void run() {
            isRuning = true;
            MediaRoom.geMediaDb(context).deleteAll();
            Cursor data = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION, null, null, IMAGE_PROJECTION[2] + " DESC");
            if (data != null) {
                ArrayList<MediaEntity> imgs = setAssayImg(data);
                MediaRoom.geMediaDb(context).putList(imgs);
            }
            data = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, VIDEO_PROJECTION, null, null, IMAGE_PROJECTION[2] + " DESC");
            if (data != null) {
                ArrayList<MediaEntity> videos = setAssayVideo(data);
                MediaRoom.geMediaDb(context).putList(videos);
            }
            isRuning = false;
            imagesHandle.sendEmptyMessage(100);
        }

        //分析图片
        private ArrayList<MediaEntity> setAssayImg(Cursor data) {
            int count = data.getCount();
            ArrayList<MediaEntity> medias = new ArrayList<>();
            if (count > 0) {
                data.moveToFirst();
                do {
                    MediaEntity image = readCursorImg(data);
                    image.type = 1;
                    medias.add(image);
                } while (data.moveToNext());
                //data.close();
            }

            return medias;
        }

        //分析视频
        private ArrayList<MediaEntity> setAssayVideo(Cursor data) {
            int count = data.getCount();
            ArrayList<MediaEntity> medias = new ArrayList<>();
            if (count > 0) {
                data.moveToFirst();
                do {
                    MediaEntity image = readCursorVideo(data);
                    image.type = 2;
                    medias.add(image);
                } while (data.moveToNext());
                //data.close();
            }

            return medias;
        }

    }

    class MediaThread extends Thread {

        private boolean isRuning = false;

        @Override
        public void run() {
            isRuning = true;
            switch (resType) {
                case 1:
                    //获取所有 并且要排序
                    List<MediaEntity> datas = MediaRoom.geMediaDb(context).getDao().queryAll();
                    Message msg = new Message();
                    msg.what = 200;
                    msg.obj = datas;
                    imagesHandle.sendMessage(msg);
                    break;
                case 2:
                    //获取所有图片 并且要排序(含GIF)
                    datas = MediaRoom.geMediaDb(context).getDao().queryTypeAll(1);
                    msg = new Message();
                    msg.what = 200;
                    msg.obj = datas;
                    imagesHandle.sendMessage(msg);
                    break;

                case 3:
                    //获取所有视频 并且要排序
                    datas = MediaRoom.geMediaDb(context).getDao().queryTypeAll(2);
                    msg = new Message();
                    msg.what = 200;
                    msg.obj = datas;
                    imagesHandle.sendMessage(msg);
                    break;
                case 4:
                    //获取所有GIF 并且要排序
                    datas = MediaRoom.geMediaDb(context).getDao().queryAll("%gif%");
                    msg = new Message();
                    msg.what = 200;
                    msg.obj = datas;
                    imagesHandle.sendMessage(msg);
                    break;
                case 5:
                    //与2重叠
                    //获取所有图片，GIF 并且要排序
                    datas = MediaRoom.geMediaDb(context).getDao().queryAll("%gif%", 1);
                    msg = new Message();
                    msg.what = 200;
                    msg.obj = datas;
                    imagesHandle.sendMessage(msg);
                    break;
                case 6:
                    //获取所有图片 （只有图片）
                    datas = MediaRoom.geMediaDb(context).getDao().queryTypeAllAndNotMediaType(1, "%gif%");
                    msg = new Message();
                    msg.what = 200;
                    msg.obj = datas;
                    imagesHandle.sendMessage(msg);
                    break;
            }
            isRuning = false;
        }


    }


    private ImagesHandle imagesHandle = new ImagesHandle();

    class ImagesHandle extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case 100:
                    //重新获取数据
                    doReqRest();
                    break;

                case 200:
                    ArrayList<MediaEntity> temp = new ArrayList();
                    Object obj = msg.obj;
                    if (obj != null) {
                        temp = (ArrayList<MediaEntity>) obj;
                    }
                    if (onLoadingListener != null) {
                        onLoadingListener.onLoadingMedia(resType, temp);
                    }
                    break;

            }


        }
    }
}
