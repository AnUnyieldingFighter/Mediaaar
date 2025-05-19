package com.images.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ListPopupWindow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.images.ui.adapter.ImageAdapter;
import com.images.ui.adapter.PopupAdapter;
import com.images.config.entity.ImageEntity;
import com.images.ui.bean.ImageFile;
import com.images.ui.bean.PreviewImageBean;
import com.images.config.operation.ConfigBuiledCrop;
import com.images.config.Configs;
import com.images.imageselect.R;
import com.images.photo.DataStore;
import com.images.photo.PhotoUtile;
import com.images.unmix.ImageLog;
import com.images.ui.view.ActionBar;

import java.io.File;
import java.util.ArrayList;


//选择图片
public class ImageSelectActivity extends AppCompatActivity {
    private String TAG = "ImageSelectActivity";
    protected Configs config;
    protected ImageAdapter adapter;
    protected PopupAdapter popupAdapter;
    protected ListPopupWindow fileLv;
    protected View footerRl;
    protected TextView fileNameTv, previewTv;
    protected ActionBar actionBar;
    //拍照图片路径
    protected File photoFile;
    //裁剪完成之后图片路径
    protected String cropImagePath;
    //protected ArrayList<String> media_paths = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_select);
        Intent it = getIntent();
        Bundle bundle = null;
        if (it != null) {
            bundle = it.getExtras();
        }
        if (bundle == null) {
            return;
        }
        config = (Configs) bundle.getSerializable("config");
        if (config == null) {
            return;
        }
        ImageLog.setIsDebug(config.isDebug);
        init(it);
    }


    protected void init(Intent it) {
    }

    //
    protected int fileOptionIndex;
    protected String fileOptionPath;

    //选择文件夹
    protected void onFileClick(int i) {
        ImageFile file = (ImageFile) popupAdapter.getItem(i);
        fileOptionIndex = i;
        fileOptionPath = file.filePath;
        popupAdapter.setIndex(i);
        fileNameTv.setText(file.fileName);
        adapter.setImgFileIndex(fileOptionIndex);
        adapter.setData(file.imags);
        fileLv.dismiss();
    }

    //选择照片
    protected void onImagesClick(int i) {
        if (i == 0 && config.showCamera) {
            //拍照
            photoFile = PhotoUtile.showCameraAction(this, config, fileOptionPath);
            return;
        }
        //可以多选择
        if (config.isMore) {
            //去预览
            PreviewImageBean bean = new PreviewImageBean();
            bean.type = 1;
            bean.index = i;
            bean.optionImage = adapter.getOptionImage();
            bean.imgFileIndex = adapter.getImgFileIndex();
            // bean.images = adapter.getImgFile();
            previewImages(bean);
            return;
        }
        //裁剪
        ImageEntity image = (ImageEntity) adapter.getItem(i);
        // 系统裁剪
        ConfigBuiledCrop crop = config.configBuildSingle;
        if (config.isCrop && !crop.isNotSystemCrop()) {
            File file = PhotoUtile.crop(this, config, image.imagePathSource);
            cropImagePath = file == null ? "" : file.getAbsolutePath();
            return;
        }
        // 应用裁剪
        if (config.isCrop && crop.isNotSystemCrop()) {
            appInCrop(image.imagePathSource);
            return;
        }

    }

    protected void previewImages(PreviewImageBean bean) {
        bean.config = config;
        Intent it = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable("bean", bean);
        it.putExtras(bundle);
        it.setClass(this, PreviewSelectActivity.class);
        startActivityForResult(it, PREVIEW_IMAGE_REQ);

    }

    //在应用内裁剪
    private final int IMAGE_CROP_IN_APP = 900;
    //完成
    private final int IMAGE_CROP_IN_APP_OK = 901;
    //取消
    private final int IMAGE_CROP_IN_APP_CANCEL = 902;
    //预览图片完成
    private final int PREVIEW_IMAGE_REQ = 903;
    //预览图片完成
    private final int PREVIEW_IMAGE_COMPLETE = 904;
    //预览图片发送
    private final int PREVIEW_IMAGE_SEND = 905;

    @Override

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ImageLog.e(TAG, "onActivityResult");
        //拍照完成
        if (requestCode == PhotoUtile.REQUEST_CAMERA) {
            photoResult(requestCode, resultCode, data);
            return;
        }
        if (requestCode == IMAGE_CROP_IN_APP && resultCode == IMAGE_CROP_IN_APP_OK) {
            //应用内裁剪完成
            cropImagePath = data.getStringExtra("arg0");
            setResultIntent(Configs.TASK_CROP_COMPLETE, cropImagePath);
            return;
        }
        if (requestCode == IMAGE_CROP_IN_APP && resultCode == IMAGE_CROP_IN_APP_CANCEL) {
            //应用内裁剪取消
            if (config.isOnlyPhotograph && config.isCrop) {
                //只拍照 显示拍照
                photoFile = PhotoUtile.showCameraAction(this, config, fileOptionPath);
                return;
            }
            return;
        }
        //裁剪完成
        if (requestCode == PhotoUtile.REQUEST_CROP) {
            cropResult(requestCode, resultCode, data);
            return;
        }
        if (requestCode == PREVIEW_IMAGE_REQ) {
            //预览图片
            preview(resultCode, data);
        }
    }

    //预览返回
    private void preview(int resultCode, Intent data) {
        if (data == null) {
            return;
        }
        Bundle bundle = data.getExtras();
        PreviewImageBean previewImageBean = (PreviewImageBean) bundle.get("bean");
        //预览完成
        if (resultCode == PREVIEW_IMAGE_COMPLETE) {
            adapter.setOptionImage(previewImageBean.optionImage);
            return;
        }
        //预览发送
        if (resultCode == PREVIEW_IMAGE_SEND) {
            ArrayList<ImageEntity> image = previewImageBean.optionImage;
            setResultIntent(Configs.TASK_PICTURE_COMPLETE, image);
            return;
        }
    }

    private String getPhotoPath() {
        boolean isExit = photoFile.exists();
        String photoPath = photoFile.getAbsolutePath();
        String msg = isExit ? "存在" : "不存在";
        ImageLog.e(TAG, "拍照path：" + photoPath + msg);
        if (!isExit) {
            //照片路径不存在
            photoPath = DataStore.stringGet(this, DataStore.PATH_TAKE);
            photoFile = new File(photoPath);
        }
        isExit = photoFile.exists();
        if (!isExit) {
            photoPath = "";
        }
        msg = isExit ? "存在" : "不存在";
        ImageLog.e(TAG, "拍照path：" + photoPath + msg);
        return photoPath;
    }

    //拍照完成
    private void photoResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            String photoPath = getPhotoPath();
            if (TextUtils.isEmpty(photoPath)) {
                //照片路径不存在
                return;
            }
            if (!config.isCrop && !config.isMore && config.isOnlyPhotograph) {
                //拍一张照片返回
                setResultIntent(Configs.TASK_PICTURE_COMPLETE, photoPath);
                return;
            }
            //系统裁剪
            ConfigBuiledCrop crop = config.configBuildSingle;
            if (config.isCrop && !crop.isNotSystemCrop()) {
                File file = PhotoUtile.crop(this, config, photoPath);
                cropImagePath = file == null ? "" : file.getAbsolutePath();
                return;
            }
            //应用裁剪
            if (config.isCrop && crop.isNotSystemCrop()) {
                appInCrop(photoPath);
                return;
            }
            //选择更多
            if (config.isMore && !config.isOnlyPhotograph) {
                setFile(photoFile);
                return;
            }
            setResultIntent(Configs.TASK_PICTURE_COMPLETE, photoPath);
            //完成
            return;
        }
        if (photoFile != null && photoFile.exists()) {
            photoFile.delete();
        }
        //只拍照 并且已取消拍照
        if (config.isOnlyPhotograph) {
            ImageLog.e(TAG, "取消拍照");
            setResultIntent(Configs.TASK_CANCEL, "");
            return;
        }
    }

    protected void setFile(File imageFile) {
    }

    //裁剪完成
    private void cropResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            //放弃裁剪
            ImageLog.e(TAG, "裁剪失败");
            DataStore.stringSave(this, DataStore.PATH_CROP, "");
            if (!config.isOnlyPhotograph) {
                return;
            }
            // 显示拍照
            photoFile = PhotoUtile.showCameraAction(this, config, fileOptionPath);
            return;
        }
        ImageLog.e(TAG, "裁剪完成：" + cropImagePath);
        if (TextUtils.isEmpty(cropImagePath)) {
            cropImagePath = DataStore.stringGet(this, DataStore.PATH_CROP);
            ImageLog.e(TAG, "裁剪完成：path重新获取" + cropImagePath);
        }
        setResultIntent(Configs.TASK_CROP_COMPLETE, cropImagePath);
    }

    //应用内裁剪
    protected void appInCrop(String path) {
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        int width = metric.widthPixels;     // 屏幕宽度（像素）
        int height = metric.heightPixels;   // 屏幕高度（像素）
        Intent it = new Intent();
        it.putExtra("arg0", path);
        it.putExtra("arg1", width);
        it.putExtra("arg2", height);
        Bundle bundle = new Bundle();
        bundle.putSerializable("config", config);
        it.putExtras(bundle);
        it.setClass(this, CropActivity.class);
        startActivityForResult(it, IMAGE_CROP_IN_APP);
    }

    //获取图片完成
    protected void setResultIntent(int resultCode, String path) {
        ArrayList<ImageEntity> iamges = new ArrayList<>();
        ImageEntity image = new ImageEntity();
        image.imagePathSource = path;
        iamges.add(image);
        setResultIntent(resultCode, iamges);
    }

    //获取图片完成
    protected void setResultIntent(int resultCode, ArrayList<ImageEntity> iamges) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable(Configs.TASK_COMPLETE_RESULT, iamges);
        intent.putExtras(bundle);
        setResult(resultCode, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        setResultIntent(Configs.TASK_CANCEL, "");
    }
}
