package com.images.photo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.images.unmix.ImageLog;

import java.io.File;

/**
 * Created by guom on 2016/10/18.
 */
public class PhotoUtil {
    //拍照
    public static final int REQUEST_CAMERA = 100;
    //裁剪
    public static final int REQUEST_CROP = 101;

    //  选择相机
    public static File showCameraAction(Activity activity) {
        File file = null;
        // 跳转到系统照相机
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(activity.getPackageManager()) != null) {
            // 设置系统相机拍照后的输出路径
            file = FileUtil.createPhotoFile(activity);
            String path = file.getAbsolutePath();
            DataStore.stringSave(activity, DataStore.PATH_TAKE, path);
            Uri uri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Context con = activity.getApplicationContext();
                String pck = con.getPackageName();
                uri = FileProvider.getUriForFile(con, pck, file);
                ImageLog.d("pck", pck);
                cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                uri = Uri.fromFile(file);
            }
            ImageLog.d("url", uri.toString());
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            cameraIntent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
            activity.startActivityForResult(cameraIntent, REQUEST_CAMERA);
        } else {
            Toast.makeText(activity, "No system camera found", Toast.LENGTH_SHORT).show();
        }
        return file;
    }


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
        File file = FileUtil.createCropFile(activity);
        DataStore.stringSave(activity, DataStore.PATH_CROP, file.getAbsolutePath());
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(Uri.fromFile(new File(imagePath)), "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", aspectX);
        intent.putExtra("aspectY", aspectY);
        intent.putExtra("outputX", outputX);
        intent.putExtra("outputY", outputY);
        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        activity.startActivityForResult(intent, REQUEST_CROP);
    }

    public static void crop(Activity activity, String imagePath) {
        crop(activity, imagePath, 1, 1, 600, 600);
    }
}
