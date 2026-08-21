package media.library.images.ui.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import media.library.images.config.entity.MediaEntity;
import media.library.utils.FileUtil;
import media.library.images.ui.thing.crop.EnjoyCropLayout;
import media.library.images.ui.thing.crop.core.BaseLayerView;
import media.library.images.ui.thing.crop.core.clippath.ClipPathLayerView;
import media.library.images.ui.thing.crop.core.clippath.ClipPathSquare;
import media.library.images.ui.thing.crop.core.mask.ColorMask;
import media.library.images.unmix.BitmapUtile;
import media.library.images.unmix.ImageLog;

import java.io.File;

//图片裁剪
public class ImageCropLayout extends RelativeLayout {
    private static final long ROTATION_DURATION_MS = 300L;

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
    // fitImageInside true 图片完整显示在容器内  false:图片填满容器 ，图片边缘可能超出容器。
    private boolean fitImageInside = true;
    //原图
    private Bitmap bitOriginal;

    public void setOutWH(int outWidth, int outHeight) {
        this.outWidth = outWidth;
        this.outHeight = outHeight;
    }

    private Context context;

    public void setMedias(Context context, MediaEntity media) {
        setMedias(context, media, true);
    }

    /**
     *
     * @param context        上下文
     * @param media          媒体图
     * @param fitImageInside fitImageInside true 图片完整显示在容器内  false:图片填满容器 ，图片边缘可能超出容器。
     */
    public void setMedias(Context context, MediaEntity media, boolean fitImageInside) {
        setImageCrop(context, media.mediaPathSource, fitImageInside);

    }

    //设置图片裁剪
    public void setImageCrop(Context context, String imgPatch, boolean fitImageInside) {
        this.context = context;
        this.fitImageInside = fitImageInside;
        setImageCrop(imgPatch);
    }

    //true 设置成功
    private boolean isSetInit;

    private void setImageCrop(String imgPatch) {
        path = imgPatch;
        if (enjoyCropLayout != null) {
            isSetInit = true;
            //设置裁剪原图片
            Bitmap bitmap = BitmapUtile.resizeBitmap(path, width, height);
            if (bitmap == null) {
                ImageLog.d("bitmap：读取失败");
                return;
            }
            //旋转图片
            bitOriginal = BitmapUtile.imageRotate(path, bitmap);
            enjoyCropLayout.setImage(bitOriginal, fitImageInside);
            defineCropParams();
        }
    }

    //裁剪
    public String saveImg() {
        Bitmap bitmap = enjoyCropLayout.crop();
        File file = FileUtil.createCropFile(context);
        boolean isSave = BitmapUtile.saveBitmaps(bitmap, file);
        String path = "";
        if (isSave) {
            path = file.getPath();
        }
        return path;
    }

    //获取裁剪的头像
    public Bitmap getCropImg() {
        Bitmap bitmap = enjoyCropLayout.crop();
        return bitmap;
    }

    //获取原图
    public Bitmap getOriginalImg() {
        return bitOriginal;
    }


    protected EnjoyCropLayout enjoyCropLayout;

    //更新图片
    public void updateBit(Bitmap bit) {
        bitOriginal = bit;
        enjoyCropLayout.setImage(bit, fitImageInside);
    }

    private boolean isRotating;

    /**
     * 以预览动画将图像旋转指定角度。
     *
     * @param angle 角度
     */
    public void rotateImage(int angle) {
        if (isRotating || bitOriginal == null || enjoyCropLayout == null) {
            return;
        }

        final Bitmap rotatedBitmap = BitmapUtile.rotaingImageView(angle, bitOriginal);
        if (rotatedBitmap == null) {
            return;
        }

        final View imageView = enjoyCropLayout.getImageView();
        isRotating = true;
        imageView.animate()
                .rotationBy(angle)
                .setDuration(ROTATION_DURATION_MS)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        //旋转角度重置为 0° 恢复正常方向
                        imageView.setRotation(0f);
                        updateBit(rotatedBitmap);
                        isRotating = false;
                    }
                })
                .start();
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
        //
        enjoyCropLayout = new EnjoyCropLayout(context);
        addView(enjoyCropLayout);
        //尝试重新初始化数据
        if (!isSetInit && !TextUtils.isEmpty(path)) {
            setImageCrop(path);
        }
    }

    private GlobalLayoutListener globalLayoutListener = new GlobalLayoutListener();

    class GlobalLayoutListener implements ViewTreeObserver.OnGlobalLayoutListener {
        public void onGlobalLayout() {
            initContextWidth();
        }
    }
}
