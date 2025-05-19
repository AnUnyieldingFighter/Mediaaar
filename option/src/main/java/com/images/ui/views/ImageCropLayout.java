package com.images.ui.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.images.config.entity.MediaEntity;
import com.images.imageselect.R;
import com.images.photo.DataStore;
import com.images.photo.FileUtile;
import com.images.photo.MediaManager;
import com.images.ui.adapter.AdapterPreviewClickList;
import com.images.ui.adapter.MediaPreviewAdapter;
import com.images.ui.adapter.OnMediaImgIbl;
import com.images.ui.thing.crop.EnjoyCropLayout;
import com.images.ui.thing.crop.core.BaseLayerView;
import com.images.ui.thing.crop.core.clippath.ClipPathLayerView;
import com.images.ui.thing.crop.core.clippath.ClipPathSquare;
import com.images.ui.thing.crop.core.mask.ColorMask;
import com.images.unmix.BitmapUtile;
import com.images.unmix.ImageLog;

import java.io.File;
import java.util.ArrayList;

//图片裁剪
public class ImageCropLayout extends RelativeLayout {

    public ImageCropLayout(@NonNull Context context) {
        super(context);
        initView();
    }

    public ImageCropLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    protected void initView() {
        getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener);
    }

    private int outWidth, outHeight;
    private String path;

    public void setOutWH(int outWidth, int outHeight) {
        this.outWidth = outWidth;
        this.outHeight = outHeight;
    }

    private Context context;

    public void setMedias(Context context, MediaEntity media) {
        path = media.mediaPathSource;
        this.context = context;
        if (enjoyCropLayout != null) {
            setImg();
        }
    }

    public String saveImg(String filePath) {
        Bitmap bitmap = enjoyCropLayout.crop();
        File file = FileUtile.createPhotoFile(context, filePath, "");
        boolean isSave = BitmapUtile.saveBitmaps(bitmap, file);
        String path = "";
        if (isSave) {
            path = file.getPath();
        }
        return path;
    }

    private EnjoyCropLayout enjoyCropLayout;

    private void initContent() {
        enjoyCropLayout = new EnjoyCropLayout(context);
        addView(enjoyCropLayout);
        setImg();
    }

    private void setImg() {
        //设置裁剪原图片
        Bitmap bitmap = BitmapUtile.resizeBitmap(path, width, height);
        if (bitmap == null) {
            ImageLog.e("bitmap：读取失败");
            return;
        }
        //旋转图片
        Bitmap bitmapRotate = BitmapUtile.imageRotate(path, bitmap);
        enjoyCropLayout.setImage(bitmapRotate);
        defineCropParams();
    }

    private void defineCropParams() {
        int outX = outWidth;
        int outY = outHeight;
        if (outX <= 0) {
            outX = 600;
        }
        if (outY <= 0) {
            outY = 600;
        }
        //设置裁剪集成视图，这里通过一定的方式集成了遮罩层与预览框
        BaseLayerView layerView = new ClipPathLayerView(context);
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

    private int width, height;

    protected void initContextWidth() {
        int w = getWidth();
        if (w <= 0) {
            return;
        }
        this.width = w;
        this.height = getHeight();
        if (width > height) {
            int temp = height;
            width = height;
            height = temp;
        }
        getViewTreeObserver().removeOnGlobalLayoutListener(globalLayoutListener);
        initContent();
    }

    private GlobalLayoutListener globalLayoutListener = new GlobalLayoutListener();

    class GlobalLayoutListener implements ViewTreeObserver.OnGlobalLayoutListener {
        public void onGlobalLayout() {
            initContextWidth();
        }
    }
}
