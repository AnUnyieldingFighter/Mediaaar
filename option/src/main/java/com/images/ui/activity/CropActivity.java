package com.images.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.images.config.Configs;
import com.images.ui.thing.crop.EnjoyCropLayout;
import com.images.ui.thing.crop.core.BaseLayerView;
import com.images.ui.thing.crop.core.clippath.ClipPathLayerView;
import com.images.ui.thing.crop.core.clippath.ClipPathSquare;
import com.images.ui.thing.crop.core.mask.ColorMask;
import com.images.imageselect.R;
import com.images.photo.DataStore;
import com.images.photo.FileUtile;
import com.images.unmix.BitmapUtile;
import com.images.unmix.ImageLog;
import com.images.ui.view.ActionBar;

import java.io.File;


//裁剪图片
public class CropActivity extends AppCompatActivity implements View.OnClickListener {
    private EnjoyCropLayout enjoyCropLayout;
    private Configs config;
    private String path;
    private int width, height;
    private LinearLayout contentLl;
    private String TAG = "CropActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);
        Intent it = getIntent();
        // 屏幕宽度（像素）
        Bundle bundle = null;
        if (it != null) {
            path = it.getStringExtra("arg0");
            width = it.getIntExtra("arg1", 0);
            height = it.getIntExtra("arg2", 0);
            bundle = it.getExtras();
            ImageLog.e(TAG, "图片路径：" + path);
        }

        if (bundle == null) {
            ImageLog.e(TAG, "bundle：读取失败");
            onBackPressed();
            return;
        }
        config = (Configs) bundle.getSerializable("config");
        if (config == null) {
            ImageLog.e(TAG, "config：读取失败");
            onBackPressed();
            return;
        }
        if (TextUtils.isEmpty(path)) {
            ImageLog.e(TAG, "path：读取失败");
            onBackPressed();
            return;
        }
        ActionBar cctionBar = (ActionBar) findViewById(R.id.actionbar);
        cctionBar.setConfigsCrop(this, config, this);
        if (width > height) {
            int temp = height;
            width = height;
            height = temp;
        }
        contentLl = (LinearLayout) findViewById(R.id.content_ll);
        viewResumeHandler.sendEmptyMessageDelayed(1, 500);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.action_option) {
            Bitmap bitmap = enjoyCropLayout.crop();
            File file = FileUtile.createPhotoFile(this, config.filePath, "");
            boolean isSave = BitmapUtile.saveBitmaps(bitmap, file);
            if (!isSave) {
                ImageLog.e(TAG, "保存失败");
                return;
            }
            DataStore.stringSave(this, DataStore.PATH_CROP, file.getAbsolutePath());
            Intent it = new Intent();
            it.putExtra("arg0", file.getAbsolutePath());
            setResult(901, it);
            finish();
            return;
        }
        if (id == R.id.action_back) {
            onBackPressed();
            return;
        }

    }


    @Override
    public void onBackPressed() {
        setResult(902);
        super.onBackPressed();
    }

    private void initContent() {
        //enjoyCropLayout = (EnjoyCropLayout) findViewById(R.id.cl);
        enjoyCropLayout = new EnjoyCropLayout(this);
        contentLl.addView(enjoyCropLayout);
        //设置裁剪原图片
        Bitmap bitmap = BitmapUtile.resizeBitmap(path, width, height);
        if (bitmap == null) {
            ImageLog.e(TAG, "bitmap：读取失败");
            onBackPressed();
            return;
        }
        //旋转图片
        Bitmap bitmapRotate = BitmapUtile.imageRotate(path, bitmap);
        // Bitmap imageBitmap = BitmapFactory.decodeFile(path);
        // enjoyCropLayout.setImageResource(R.drawable.test2);
        enjoyCropLayout.setImage(bitmapRotate);
        defineCropParams();
        findViewById(R.id.progress_il).setVisibility(View.GONE);
    }

    private void defineCropParams() {
        int outX = config.configBuildSingle.getOutputX();
        int outY = config.configBuildSingle.getOutputY();
        if (outX <= 0) {
            outX = 100;
        }
        if (outY <= 0) {
            outY = 100;
        }
        //设置裁剪集成视图，这里通过一定的方式集成了遮罩层与预览框
        BaseLayerView layerView = new ClipPathLayerView(this);
        //设置遮罩层,这里使用半透明的遮罩层
        layerView.setMask(ColorMask.getTranslucentMask());
        //设置预览框形状
        // layerView.setShape(new ClipPathCircle(aspectX));
        layerView.setShape(new ClipPathSquare(outX, outY));
        //设置裁剪集成视图
        enjoyCropLayout.setLayerView(layerView);
        //设置边界限制，如果设置了该参数，预览框则不会超出图片
        enjoyCropLayout.setRestrict(true);
    }

    private ViewResumeHandler viewResumeHandler = new ViewResumeHandler();

    class ViewResumeHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            initContent();
        }
    }


}
