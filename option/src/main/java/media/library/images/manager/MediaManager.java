package media.library.images.manager;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;


import media.library.images.config.entity.MediaEntity;
import media.library.db.MediaRoom;
import media.library.images.unmix.ImageLog;
import media.library.utils.FileUriPath;

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
    //阉割版 红米 android 13 api 33  它只能读取到这些数据
    private final String[] IMAGE_PROJECTION_2 = {MediaStore.Images.Media.DATA,                  //图片路径
            MediaStore.Images.Media.DISPLAY_NAME,          //图片名称
            // MediaStore.Images.Media.DATE_ADDED,            //创建时间
            //MediaStore.Images.Media._ID,                   //图片id
            // MediaStore.Images.Media.BUCKET_DISPLAY_NAME,   //相册名字
            MediaStore.Images.Media.MIME_TYPE,             //图片类型
            MediaStore.Images.Media.SIZE,                  //图片大小
            // MediaStore.Images.Media.BUCKET_ID,             //相册id
            //MediaStore.Images.Media.LONGITUDE,             // 经度
            //MediaStore.Images.Media.WIDTH,             // 宽度
            //MediaStore.Images.Media.HEIGHT,             // 高度
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

    /**
     * 通过 uri 获取到 MediaEntity （并且给 uri授临时读取权限）
     *
     * @param uri
     * @param context
     * @return
     */
    public MediaEntity getUriToMedia(Uri uri, Context context) {
        try {
            int flag = Intent.FLAG_GRANT_READ_URI_PERMISSION;
            context.getContentResolver().takePersistableUriPermission(uri, flag);
        } catch (Exception e) {
            ImageLog.d("授权失败：" + e.getMessage());
        }
        return getImg(uri, context);
    }

    /**
     * 通过 uri 获取到 MediaEntity
     *
     * @param uri
     * @param context
     * @return
     */
    public MediaEntity getImg(Uri uri, Context context) {
        //
        Cursor cursor = getCursorImg(uri, context);
        if (cursor == null || cursor.getCount() == 0) {
            cursor = getCursorImgAgain(uri, context);
        }
        if (cursor == null) {
            return null;
        }
        int count = cursor.getCount();
        boolean isFirst = cursor.moveToFirst();
        ImageLog.d(
                "google图片选择器", "地址---》"
                        + " 是最后一个:" + cursor.isLast()
                        + " 数量:" + count + " isFirst"
                        + " 移动到第一个:" + isFirst + " 是第一个："
                        + cursor.isFirst());
        if (count == 0) {
            return null;
        }
        MediaEntity image = null;
        try {
            image = readCursor2(cursor);
            ImageLog.d("google图片选择器", image.toString());
        } catch (Exception e) {
            ImageLog.d("google图片选择器", "错误：" + e.getMessage());
        } finally {
            cursor.close();
        }
        return image;

    }

    //content://com.android.providers.media.documents/document/image%3A1162
    //再读取一遍(华为手机 ：非本应用排的照，上面的方法读取不到照片信息)
    //这个方法只能读取到少量的信息
    //column '_data' does not exist.  Available columns:
    // [document_id, mime_type, _display_name, last_modified, flags, _size]
    private Cursor getCursorImgAgain(Uri uri, Context context) {
        Cursor cursor = context.getContentResolver().query(uri, null,
                null, null, null);
        int count = 0;
        if (cursor != null) {
            count = cursor.getCount();
        }
        ImageLog.d("google图片选择器", "再读取一遍：count=" + count);
        return cursor;
    }

    private Cursor getCursorImg(Uri uri, Context context) {
        //String path = FileUriPath.getPath(context, uri);
        //ImageLog.d("google图片选择器", "path 重新获取：" + path);
        Cursor cursor = null;
        if (isMediaDocument(uri)) {
            //Android从4.4版本(API_19)开始多了个DocumentsProvider
            //uri 是 com.android.providers.media.documents 开头才行
            // content://com.android.providers.media.documents/document/image%3A18323
            String docId = DocumentsContract.getDocumentId(uri);
            String[] split = docId.split(":");
            String type = split[0];
            ImageLog.d("google图片选择器", "获取游标 type=" + type + " docId=" + docId);
            String selection = "_id=?";
            String[] selectionArgs = new String[]{split[1]};
            Uri contentUri = null;
            if ("image".equals(type)) {
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            } else if ("video".equals(type)) {
                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            } else if ("audio".equals(type)) {
                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            }
            cursor = context.getContentResolver().query(contentUri, IMAGE_PROJECTION_2, selection, selectionArgs, null);
        } else {
            ImageLog.d("google图片选择器", "获取游标2");
            //content://media/picker/0/com.android.providers.media.photopicker/media/1000000501
            //content://media/picker/0/com.android.providers.media.photopicker/media/1000014639
            ContentResolver contentResolver = context.getContentResolver();
            cursor = contentResolver.query(uri, IMAGE_PROJECTION_2, null, null, null);
        }
        return cursor;
    }

    // Index 0 requested, with a size of 0
    private MediaEntity readCursor2(Cursor data) {
        MediaEntity video = new MediaEntity();
        int index = data.getColumnIndexOrThrow(IMAGE_PROJECTION_2[0]);
        if (index >= 0) {
            video.mediaPathSource = data.getString(index);
        }
        index = data.getColumnIndexOrThrow(IMAGE_PROJECTION_2[2]);
        if (index >= 0) {
            video.mediaType = data.getString(index);
        }
        index = data.getColumnIndexOrThrow(IMAGE_PROJECTION_2[3]);
        if (index >= 0) {
            video.mediaSize = data.getLong(index);
        }
        index = data.getColumnIndexOrThrow(IMAGE_PROJECTION_2[4]);
        if (index >= 0) {
            video.mediaDateTaken = data.getLong(index);
        }
        return video;
    }

    private boolean isMediaDocument(Uri uri) {
        String authority = uri.getAuthority();
        return "com.android.providers.media.documents".equals(authority);
    }

    private boolean isMediaPhotopicker(Uri uri) {
        String authority = uri.getAuthority();
        return "com.android.providers.media.photopicker".equals(authority);
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
