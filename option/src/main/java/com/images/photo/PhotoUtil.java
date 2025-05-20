package com.images.photo;

import android.Manifest;
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
    //调用相机权限
    public static String take_permission = Manifest.permission.CAMERA;
    //写入SDK权限
    public static String write_permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    //
    //拍照
    public static final int REQUEST_CAMERA = 100;
    private static File takeResFile;

    /**
     * 拍照
     *
     * @param activity
     */
    public static void showCameraAction(Activity activity) {
        takeResFile = null;
        // 跳转到系统照相机
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(activity.getPackageManager()) != null) {
            // 设置系统相机拍照后的输出路径
            takeResFile = FileUtil.createPhotoFile(activity);
            String path = takeResFile.getAbsolutePath();
            DataStore.stringSave(activity, DataStore.PATH_TAKE, path);
            Uri uri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Context con = activity.getApplicationContext();
                String pck = con.getPackageName();
                uri = FileProvider.getUriForFile(con, pck, takeResFile);
                ImageLog.d("pck", pck);
                cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                uri = Uri.fromFile(takeResFile);
            }
            ImageLog.d("url", uri.toString());
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            cameraIntent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
            activity.startActivityForResult(cameraIntent, REQUEST_CAMERA);
        } else {
            Toast.makeText(activity, "No system camera found", Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean isTakeOK(int reqCode, int resCode) {
        return (reqCode == REQUEST_CAMERA) && (resCode == Activity.RESULT_OK);
    }

    public static File getTakeResFile() {
        return takeResFile;
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
        DataStore.stringSave(activity, DataStore.PATH_CROP, cropResFile.getAbsolutePath());
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
