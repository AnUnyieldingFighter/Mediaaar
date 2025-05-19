package com.images.ui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListPopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.images.config.Configs;
import com.images.config.entity.ImageEntity;
import com.images.imageselect.R;
import com.images.photo.DataStore;
import com.images.photo.PhotoUtile;
import com.images.photo.PhotosMnager;
import com.images.ui.adapter.ImageAdapter;
import com.images.ui.adapter.PopupAdapter;
import com.images.ui.bean.ImageFile;
import com.images.ui.bean.PreviewImageBean;
import com.images.ui.view.ActionBar;
import com.images.ui.view.ImageGridView;
import com.images.unmix.ImageLog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class IncidentActivity extends ImageSelectActivity implements View.OnClickListener, AdapterView.OnItemClickListener {


    private String TAG = "IncidentActivity";

    protected void init(Intent it) {
        ImageLog.e(TAG, "onCreate");
        boolean isOnlyPhotograph = config.isOnlyPhotograph;
        if (isOnlyPhotograph) {
            //只拍照
            String photoPath = DataStore.stringGet(this, DataStore.PATH_TAKE);
            if (!TextUtils.isEmpty(photoPath)) {
                photoFile = new File(photoPath);
                return;
            }
            photoFile = PhotoUtile.showCameraAction(this, config, fileOptionPath);
            return;
        }
        TextView timeTv = (TextView) findViewById(R.id.time_tv);
        fileNameTv = (TextView) findViewById(R.id.file_name_tv);
        fileNameTv.setOnClickListener(this);
        previewTv = (TextView) findViewById(R.id.preview_tv);
        previewTv.setOnClickListener(this);
        footerRl = findViewById(R.id.footer_rl);
        actionBar = (ActionBar) findViewById(R.id.actionbar);
        actionBar.setConfigsCommon(this, config, this);
        actionBar.setOnClickListener(this);
        footerRl.setOnClickListener(this);
        ImageGridView imageGridView = (ImageGridView) findViewById(R.id.image_gv);
        //
        int max = 0;
        if (config.isMore) {
            max = config.configBuildMore.getImageSelectMaximum();
        }
        adapter = new ImageAdapter(this, max, config.isCrop, config.showCamera);
        adapter.setItemSize(this);
        adapter.setPreviewView(previewTv);
        adapter.setSendView(actionBar, config.configBarCommon.getOption());
        imageGridView.setAdapter(adapter);
        adapter.setImages(config.getImages());
        //
        imageGridView.setShowView(timeTv);
        imageGridView.setOnItemClickListener(this);
        if (config.isCrop) {
            //不显示预览 和 bar的OptionTv
            previewTv.setVisibility(View.GONE);
            actionBar.hideView(3);
        }
        doRequest();
    }

    private void doRequest() {
        PhotosMnager.getInstance().setOnLoadingListener(new LoadingListener());
        PhotosMnager.getInstance().doRequest(this, config.showCamera);
    }

    //照片选择监听
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (adapterView.getId() == R.id.image_gv) {
            onImagesClick(i);
        } else {
            onFileClick(i);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.action_option) {
            //更多发送
            ArrayList<ImageEntity> optionImage = adapter.getOptionImage();
            if (optionImage.size() == 0) {
                return;
            }
            setResultIntent(Configs.TASK_PICTURE_COMPLETE, optionImage);
            return;
        }
        if (id == R.id.action_back) {
            //返回
            onBackPressed();
            return;
        }
        if (id == R.id.file_name_tv) {
            //呼出照片文件夹选择
            popupWindow();
            return;
        }
        if (id == R.id.preview_tv) {
            //预览图片
            ArrayList<ImageEntity> optionImage = adapter.getOptionImage();
            if (optionImage.size() == 0) {
                return;
            }
            PreviewImageBean bean = new PreviewImageBean();
            bean.type = 2;
            bean.optionImage = optionImage;
            previewImages(bean);
            return;
        }
    }

    private void popupWindow() {
        if (fileLv == null) {
            fileLv = new ListPopupWindow(this);
            popupAdapter = new PopupAdapter();
            fileLv.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            fileLv.setAdapter(popupAdapter);
            int width = footerRl.getWidth();
            fileLv.setContentWidth(width);
            fileLv.setWidth(width);
            fileLv.setHeight((int) (width * 0.8));
            fileLv.setAnchorView(footerRl);
            fileLv.setModal(true);
            fileLv.setOnItemClickListener(this);
        }

        boolean isShow = fileLv.isShowing();
        if (isShow) {
            fileLv.dismiss();
        } else {
            List<ImageFile> imageFils = PhotosMnager.getInstance().getImgFile();
            popupAdapter.setData(imageFils);
            fileLv.show();
        }
    }

    //拍照之后...
    protected void setFile(File imageFile) {
        ImageEntity image = PhotosMnager.getInstance()
                .getImage(this, Uri.fromFile(imageFile));
        //
        if (image == null) {
            String filePath = imageFile.getParent();
            image = new ImageEntity();
            image.imagePathSource = imageFile.getPath();
            image.imageFileName = filePath;
        }
        //加入到文件夹
        boolean isAdd = false;
        int setIndex = config.showCamera ? 1 : 0;
        List<ImageFile> imageFils = PhotosMnager.getInstance().getImgFile();
        String imageFilePath = imageFile.getParent();
        for (int i = 0; i < imageFils.size(); i++) {
            ImageFile file = imageFils.get(i);
            //第一个文件夹是全部，所以要加入
            if (i == 0) {
                file.imags.add(setIndex, image);
                continue;
            }
            //是同一个文件夹
            String filePath = file.filePath;
            if (imageFilePath.equals(filePath)) {
                file.imags.add(setIndex, image);
                file.size += 1;
                isAdd = true;
                break;
            }
        }
        if (!isAdd) {
            ImageFile file = PhotosMnager.getInstance().getImageFile(new ArrayList<ImageEntity>(),
                    true, 1);
            file.imags.add(setIndex, image);
            file.size += 1;
            imageFils.add(file);
        }
        setAdapterIamge(imageFils);
    }

    //读取数据库照片监听
    class LoadingListener implements PhotosMnager.OnLoadingListener {

        @Override
        public void onLoadingListener(List<ImageFile> fils) {
            findViewById(R.id.progress_il).setVisibility(View.GONE);
            if (fils == null) {
                Toast.makeText(IncidentActivity.this, R.string.image_loading_error, Toast.LENGTH_SHORT).show();
                return;
            }
            setAdapterIamge(fils);
        }
    }

    //设置适配器显示图片
    private void setAdapterIamge(List<ImageFile> fils) {
        if (fils.size() == 1) {
            //不显示选择文件夹
            footerRl.setVisibility(View.GONE);
        }
        PhotosMnager.getInstance().setImgFile(fils);
        ImageFile file = fils.get(fileOptionIndex);
        adapter.setImgFileIndex(fileOptionIndex);
        adapter.setData(file.imags);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ImageLog.e(TAG, "onDestroy");
    }
}
