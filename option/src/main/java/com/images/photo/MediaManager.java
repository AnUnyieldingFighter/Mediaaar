package com.images.photo;

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;


import com.images.config.entity.MediaEntity;
import com.images.db.MediaRoom;
import com.images.ui.bean.ImageFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2016/10/17.
 */
public class MediaManager {
    private HashMap<String, List<MediaEntity>> map;
    private static MediaManager manager;
    //全部照片
    private List<ImageFile> images;

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
    };


    //==================设置监听======================================
    private OnLoadingListener onLoadingListener;

    public void setOnLoadingListener(OnLoadingListener onLoadingListener) {
        this.onLoadingListener = onLoadingListener;
    }

    public interface OnLoadingListener {
        void onLoadingFile(ArrayList<ImageFile> files);

        void onLoadingMedia(ArrayList<MediaEntity> files);

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
        image.mediaSize = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[6]));
        image.mediaFileId = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[7]));
        image.mediaAngle = data.getInt(data.getColumnIndexOrThrow(IMAGE_PROJECTION[8]));
        return image;
    }

    private MediaEntity readCursorVideo(Cursor data) {
        MediaEntity image = new MediaEntity();
        image.mediaPathSource = data.getString(data.getColumnIndexOrThrow(VIDEO_PROJECTION[0]));
        image.mediaName = data.getString(data.getColumnIndexOrThrow(VIDEO_PROJECTION[1]));
        image.mediaTime = data.getLong(data.getColumnIndexOrThrow(VIDEO_PROJECTION[2]));
        image.mediaId = data.getString(data.getColumnIndexOrThrow(VIDEO_PROJECTION[3]));
        image.mediaFileName = data.getString(data.getColumnIndexOrThrow(VIDEO_PROJECTION[4]));
        image.mediaType = data.getString(data.getColumnIndexOrThrow(VIDEO_PROJECTION[5]));
        image.mediaSize = data.getString(data.getColumnIndexOrThrow(VIDEO_PROJECTION[6]));
        image.mediaFileId = data.getString(data.getColumnIndexOrThrow(VIDEO_PROJECTION[7]));
        image.mediaAngle = data.getInt(data.getColumnIndexOrThrow(VIDEO_PROJECTION[8]));
        return image;
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

    //1:全部 2:图片 3：视频
    private int resType;
    private MediaThread mediaThread;

    /**
     * 请求获取照片
     *
     * @param resType 1:全部 2:图片 3：视频
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
                    //获取所有 但是要排序
                    List<MediaEntity> datas = MediaRoom.geMediaDb(context).getDao().queryAll();
                    Message msg = new Message();
                    msg.what = 200;
                    msg.obj = datas;
                    imagesHandle.sendMessage(msg);
                    break;
                case 2:
                    //获取所有图片 但是要排序
                    datas = MediaRoom.geMediaDb(context).getDao().queryTypeAll(1);
                    msg = new Message();
                    msg.what = 200;
                    msg.obj = datas;
                    imagesHandle.sendMessage(msg);
                    break;

                case 3:
                    //获取所有视频 但是要排序
                    datas = MediaRoom.geMediaDb(context).getDao().queryTypeAll(2);
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
                        onLoadingListener.onLoadingMedia(temp);
                    }
                    break;

            }


        }
    }
}
