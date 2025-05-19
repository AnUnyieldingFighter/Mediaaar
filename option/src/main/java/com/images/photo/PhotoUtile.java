package com.images.photo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.images.config.Configs;
import com.images.config.operation.ConfigBuildCrop;
import com.images.unmix.ImageLog;

import java.io.File;

/**
 * Created by guom on 2016/10/18.
 */
public class PhotoUtile {
    private static String TAG = "PhotoUtile";
    //拍照
    public static final int REQUEST_CAMERA = 100;
    //裁剪
    public static final int REQUEST_CROP = 101;

    //  选择相机
    public static File showCameraAction(Activity activity, Configs config, String fileAbsolutePath) {
        File file = null;
        // 跳转到系统照相机
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(activity.getPackageManager()) != null) {
            // 设置系统相机拍照后的输出路径
            file = FileUtile.createPhotoFile(activity, config.filePath, fileAbsolutePath);
            String path = file.getAbsolutePath();
            DataStore.stringSave(activity, DataStore.PATH_TAKE, path);
            Uri uri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Context con = activity.getApplicationContext();
                String pck = con.getPackageName();
                uri = FileProvider.getUriForFile(con,
                        pck , file);
                ImageLog.e("pck", pck);
                cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                uri = Uri.fromFile(file);
            }
            ImageLog.e("url", uri.toString());
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            cameraIntent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
            activity.startActivityForResult(cameraIntent, REQUEST_CAMERA);
        } else {
            Toast.makeText(activity, "No system camera found", Toast.LENGTH_SHORT).show();
        }
        return file;
    }

    //裁剪
    public static File crop(Activity activity, Configs config, String imagePath) {
        ConfigBuildCrop crop = config.configBuildSingle;
        File file = FileUtile.createCropFile(activity, config.filePath);
        DataStore.stringSave(activity, DataStore.PATH_CROP, file.getAbsolutePath());
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(Uri.fromFile(new File(imagePath)), "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", crop.getAspectX());
        intent.putExtra("aspectY", crop.getAspectY());
        intent.putExtra("outputX", crop.getOutputX());
        intent.putExtra("outputY", crop.getOutputY());

        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        activity.startActivityForResult(intent, REQUEST_CROP);
        return file;
    }

}
