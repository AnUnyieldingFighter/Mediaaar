package com.images.photo;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;

import android.text.TextUtils;


import com.images.config.entity.ImageEntity;
import com.images.ui.bean.ImageFile;
import com.images.unmix.ImageLog;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by Administrator on 2016/10/17.
 */
public class PhotosMnager {
    private HashMap<String, List<ImageEntity>> map;
    private ArrayList<ImageEntity> medias;
    private static PhotosMnager manager;
    //全部照片
    private List<ImageFile> images;

    public static PhotosMnager getInstance() {
        if (manager == null) {
            manager = new PhotosMnager();
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

    public void setImgFile(List<ImageFile> images) {
        this.images = images;
    }

    public List<ImageFile> getImgFile() {
        return images;
    }

    public List<ImageEntity> getImgs(int index) {
        return images.get(index).imags;
    }

    /**
     * 通过uri 获取一张照片
     *
     * @param context
     * @param url
     * @return
     */
    public ImageEntity getImage(Context context, Uri url) {
        Cursor data = context.getContentResolver().query(url, IMAGE_PROJECTION, null, null, null);
        ImageEntity image = null;
        if (data != null) {
            data.moveToFirst();
            image = readCursor(data);
        }
        return image;
    }

    //高版本用
    public ImageEntity getImage2(Context context, Uri uri) {
        ImageEntity image = null;
        Cursor data = context.getContentResolver().query(uri, null, null, null, null);
        if (data != null) {
            data.moveToFirst();
            image = readCursor(data);
        }
        return image;
    }


    //创建一个“全部”文件夹
    public ImageFile getImageFile(List<ImageEntity> images, int type) {
        ImageFile file = new ImageFile();
        file.size = images.size();
        if (file.size != 0) {
            ImageEntity image = images.get(0);
            file.fileName = image.imageFileName;
            file.firstImagePath = image.imagePathSource;
            File imageFile = new File(image.imagePathSource);
            String filePath = imageFile.getParent();
            file.filePath = filePath;//new File(image.imagePathSource).getParentFile().getAbsolutePath();
        }
        file.imags = images;
        if (type == 0) {
            //全部
            file.fileName = "全部";
        }
        return file;
    }


    //==================设置监听======================================
    private OnLoadingListener onLoadingListener;

    public void setOnLoadingListener(OnLoadingListener onLoadingListener) {
        this.onLoadingListener = onLoadingListener;
    }

    public interface OnLoadingListener {
        void onLoadingFile(ArrayList<ImageFile> files);

        void onLoadingMedia(ArrayList<ImageEntity> files);

        void onLoadingFail();
    }

    //==================读取游标======================================
    private ImageEntity readCursor(Cursor data) {
        ImageEntity image = new ImageEntity();
        image.imagePathSource = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[0]));
        image.imageName = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[1]));
        image.imageTime = data.getLong(data.getColumnIndexOrThrow(IMAGE_PROJECTION[2]));
        image.imageId = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[3]));
        image.imageFileName = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[4]));
        image.imageType = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[5]));
        image.imageSize = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[6]));
        image.imageFileId = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[7]));
        image.imageAngle = data.getInt(data.getColumnIndexOrThrow(IMAGE_PROJECTION[8]));
        return image;
    }

    //================获取照片=====================================

    private ImagesThread run;


    /**
     * 请求获取照片
     *
     * @param activity
     * @param resType  1 分文件 2：只是全部
     */
    public void doRequest(Activity activity, int resType) {
        if (run != null && run.isRuning) {
            return;
        }
        run = new ImagesThread(activity, resType);
        run.start();
    }

    class ImagesThread extends Thread {
        private boolean isRuning = false;
        private Context context;
        private int resType;//1 分文件 2：只是全部

        public ImagesThread(Context context, int resType) {
            this.context = context;
            this.resType = resType;
        }


        @Override
        public void run() {
            isRuning = true;
            Cursor data = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION, null, null, IMAGE_PROJECTION[2] + " DESC");
            if (data == null) {
                imagesHandle.sendEmptyMessage(0);
                return;
            }
            if (resType == 1) {
                setAssayFile(data);
            }
            if (resType == 2) {
                setAssayMedia(data);
            }
            isRuning = false;
        }

        private void setAssayMedia(Cursor data) {
            int count = data.getCount();
            medias = new ArrayList<>();
            if (count > 0) {
                data.moveToFirst();
                do {
                    ImageEntity image = readCursor(data);
                    medias.add(image);
                } while (data.moveToNext());
                //data.close();
            }

            Message msg = new Message();
            msg.what = 2;
            msg.obj = medias;
            imagesHandle.sendMessage(msg);
        }

        private void setAssayFile(Cursor data) {
            int count = data.getCount();
            map = new HashMap<>();
            ArrayList<ImageEntity> imags = new ArrayList<>();
            if (count > 0) {
                data.moveToFirst();
                do {
                    ImageEntity image = readCursor(data);
                    String fileName = image.imagePathSource;
                    if (TextUtils.isEmpty(fileName)) {
                        continue;
                    }
                    if (!new File(fileName).exists()) {
                        ImageLog.e("文件不存在：" + fileName);
                        continue;
                    }
                    List<ImageEntity> list = map.get(image.imageFileId);
                    if (list == null) {
                        list = new ArrayList<>();
                    }
                    list.add(image);
                    map.put(image.imageFileId, list);
                    imags.add(image);
                } while (data.moveToNext());
                //data.close();
            }

            Set<String> keys = map.keySet();
            //文件夹
            List<ImageFile> fils = new ArrayList<>();
            //全部照片
            fils.add(getImageFile(imags, 0));
            //按文件夹分类照片
            for (String key : keys) {
                List<ImageEntity> list = map.get(key);
                fils.add(getImageFile(list, 1));
            }

            Message msg = new Message();
            msg.what = 1;
            msg.obj = fils;
            imagesHandle.sendMessage(msg);
        }
    }

    private ImagesHandle imagesHandle = new ImagesHandle();

    class ImagesHandle extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (onLoadingListener == null) {
                return;
            }
            switch (msg.what) {
                case 0:
                    //没有图片
                    onLoadingListener.onLoadingFail();
                    break;
                case 1:
                    //分析成了文件
                    ArrayList<ImageFile> fils = new ArrayList<>();
                    Object obj = msg.obj;
                    if (obj != null) {
                        fils = (ArrayList<ImageFile>) obj;
                    }

                    onLoadingListener.onLoadingFile(fils);
                    break;
                case 2:
                    //返回所有
                    ArrayList<ImageEntity> temp = new ArrayList();
                    obj = msg.obj;
                    if (obj != null) {
                        temp = (ArrayList<ImageEntity>) obj;
                    }
                    onLoadingListener.onLoadingMedia(temp);
                    break;
            }


        }
    }
}
