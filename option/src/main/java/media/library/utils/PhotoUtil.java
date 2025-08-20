package media.library.utils;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import android.widget.Toast;

import androidx.core.content.FileProvider;

import media.library.images.config.entity.MediaEntity;
import media.library.images.manager.MediaManager;
import media.library.images.unmix.ImageLog;

import java.io.File;
import java.util.Random;

/**
 * Created by guom on 2016/10/18.
 */
public class PhotoUtil {
    //调用相机权限
    public static String take_permission = Manifest.permission.CAMERA;


    //拍照
    public static final int REQUEST_CAMERA = 100;
    private static File takeResFile;
    private static Uri imgUrl;
    private static String fileDir = "hn_earn";


    /**
     * 拍照
     *
     * @param activity
     */
    public static void showCameraAction(Activity activity) {
        Intent cameraIntent = getCameraActionIntent(activity);
        if (cameraIntent == null) {
            return;
        }
        activity.startActivityForResult(cameraIntent, REQUEST_CAMERA);
    }

    //获取跳转Intent
    public static Intent getCameraActionIntent(Activity activity) {
        takeResFile = null;
        imgUrl = null;
        // 跳转到系统照相机
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(activity.getPackageManager()) != null) {
            // 设置系统相机拍照后的输出路径
            takeResFile = FileUtil.createPhotoFile(activity);
            Uri uri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                //不需要读写权限
                ContentValues values = new ContentValues();
                Random random = new Random();
                int randomNumber = random.nextInt(90000);
                String fileName = System.currentTimeMillis() + "_" + randomNumber;//不加后缀
                values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg"); //根据文件类型修改 MIME_TYPE
                String path = Environment.DIRECTORY_PICTURES + File.separator + fileDir;
                values.put(MediaStore.Images.Media.RELATIVE_PATH, path); //设置存储路径和文件夹名
                ContentResolver contentResolver = activity.getContentResolver();
                uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Context con = activity.getApplicationContext();
                String pck = con.getPackageName();
                uri = FileProvider.getUriForFile(con, pck, takeResFile);
                ImageLog.d("pck", pck);
                cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                uri = Uri.fromFile(takeResFile);
            }
            imgUrl = uri;
            ImageLog.d("url", uri.toString());
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            cameraIntent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
            //
        } else {
            Toast.makeText(activity, "No system camera found", Toast.LENGTH_SHORT).show();
            cameraIntent = null;
        }
        return cameraIntent;
    }

    public static boolean isTakeOK(int reqCode, int resCode) {
        return (reqCode == REQUEST_CAMERA) && (resCode == Activity.RESULT_OK);
    }

    public static File getTakeResFile(Context context) {
        if ((takeResFile == null || !takeResFile.isFile())) {
            MediaEntity entity = getTakePicture(context);
            if (entity != null) {
                takeResFile = new File(entity.mediaPathSource);
            }
        }
        return takeResFile;
    }

    public static MediaEntity getTakePicture(Context context) {
        if (imgUrl == null) {
            return null;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // int flag = Intent.FLAG_GRANT_READ_URI_PERMISSION;
            // context.getContentResolver().takePersistableUriPermission(imgUrl, flag);
            MediaEntity entity = MediaManager.getInstance().getVideo(imgUrl, context);
            if (entity == null) {
                return null;
            }
            String mediaType = entity.mediaType;
            if (mediaType == null) {
                mediaType = "";
            }
            if (mediaType.contains("image") || mediaType.contains("gif")) {
                entity.type = 1;
            } else {
                entity.type = 2;
            }
        }
        return null;
    }

    //裁剪
    public static final int REQUEST_CROP = 101;
    private static File cropResFile;

    /**
     * 系统裁剪
     *
     * @param activity
     * @param imagePath
     * @param aspectX   置横向比例
     * @param aspectY   设置纵向比例
     * @param outputX   设置输出宽度
     * @param outputY   高度
     * @return
     */
    public static void crop(Activity activity, String imagePath, int aspectX, int aspectY, int outputX, int outputY) {
        cropResFile = FileUtil.createCropFile(activity);
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(Uri.fromFile(new File(imagePath)), "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", aspectX);
        intent.putExtra("aspectY", aspectY);
        intent.putExtra("outputX", outputX);
        intent.putExtra("outputY", outputY);
        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cropResFile));
        activity.startActivityForResult(intent, REQUEST_CROP);
    }

    public static void crop(Activity activity, String imagePath) {
        crop(activity, imagePath, 1, 1, 600, 600);
    }

    public static boolean isCropOK(int reqCode, int resCode) {
        return (reqCode == REQUEST_CROP) && (resCode == Activity.RESULT_OK);
    }

    public static File getCropResFile() {
        return cropResFile;
    }

}
