package media.library.images.ui.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import media.library.images.config.CropType;
import media.library.images.config.entity.MediaEntity;
import media.library.images.ui.thing.crop.core.clippath.ClipPathCircle;
import media.library.utils.FileUtil;
import media.library.images.ui.thing.crop.EnjoyCropLayout;
import media.library.images.ui.thing.crop.core.BaseLayerView;
import media.library.images.ui.thing.crop.core.clippath.ClipPathLayerView;
import media.library.images.ui.thing.crop.core.clippath.ClipPathSquare;
import media.library.images.ui.thing.crop.core.mask.ColorMask;
import media.library.images.ui.thing.crop.core.image.EnjoyImageView;
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


    private GlobalLayoutListener globalLayoutListener = new GlobalLayoutListener();

    class GlobalLayoutListener implements ViewTreeObserver.OnGlobalLayoutListener {
        public void onGlobalLayout() {
            initContextWidth();
        }
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

    //============================================================
    private int outWidth, outHeight;
    private String path;
    // fitImageInside true 图片完整显示在容器内  false:图片填满容器 ，图片边缘可能超出容器。
    private boolean fitImageInside = true;
    //原图
    private Bitmap bitOriginal;
    //裁剪布局
    protected EnjoyCropLayout enjoyCropLayout;

    public void setOutWH(int outWidth, int outHeight) {
        this.outWidth = outWidth;
        this.outHeight = outHeight;
    }

    //裁剪框类型，默认矩形。
    private CropType corpType = CropType.RECTANGLE;

    /**
     * 设置裁剪框类型；图片加载后调用会立即更新裁剪框。
     */
    public void setCorpType(@NonNull CropType corpType) {
        this.corpType = corpType;
        if (enjoyCropLayout != null && bitOriginal != null) {
            defineCropParams();
        }
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

    //true 初始设置成功
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

    //按裁剪框形状裁剪并保存 PNG，圆形图片的圆外区域保持透明。
    public String saveImg() {
        Bitmap bitmap = enjoyCropLayout.crop(corpType);
        File file = FileUtil.createCropFile(context);
        boolean isSave = BitmapUtile.saveBitmaps(bitmap, file, Bitmap.CompressFormat.PNG);
        String path = "";
        if (isSave) {
            path = file.getPath();
        }
        return path;
    }

    //获取裁剪结果：矩形返回矩形图片，圆形返回圆外透明的圆形图片。
    public Bitmap getCropImg() {
        Bitmap bitmap = enjoyCropLayout.crop(corpType);
        return bitmap;
    }

    //获取原图
    public Bitmap getOriginalImg() {
        return bitOriginal;
    }


    //true 正在旋转
    private boolean isRotating;

    /**
     * 旋转期间不处理裁剪手势，避免手势修改动画使用的图片矩阵。
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (isRotating) {
            return true;
        }
        return super.dispatchTouchEvent(event);
    }

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

        final EnjoyImageView imageView = (EnjoyImageView) enjoyCropLayout.getImageView();
        if (imageView.getWidth() <= 0 || imageView.getHeight() <= 0) {
            return;
        }
        //以当前图片中心旋转，同时过渡到新图片的缩放比例和居中位置。
        RectF imageRect = new RectF(0, 0, imageView.getDrawable().getIntrinsicWidth(),
                imageView.getDrawable().getIntrinsicHeight());
        imageView.getImageMatrix().mapRect(imageRect);
        float targetScale = imageView.getFitScale(rotatedBitmap, fitImageInside);
        float scaleRatio = targetScale / (float) imageView.getScale();
        imageView.setPivotX(imageRect.centerX());
        imageView.setPivotY(imageRect.centerY());
        isRotating = true;
        imageView.animate()
                .rotationBy(angle)
                .scaleX(scaleRatio)
                .scaleY(scaleRatio)
                .translationX(imageView.getWidth() / 2f - imageRect.centerX())
                .translationY(imageView.getHeight() / 2f - imageRect.centerY())
                .setDuration(ROTATION_DURATION_MS)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        //同一帧中替换图片并恢复视图属性，由图片矩阵接管最终显示状态。
                        imageView.animate().setListener(null);
                        imageView.setRotation(0f);
                        imageView.setScaleX(1f);
                        imageView.setScaleY(1f);
                        imageView.setTranslationX(0f);
                        imageView.setTranslationY(0f);
                        imageView.setPivotX(imageView.getWidth() / 2f);
                        imageView.setPivotY(imageView.getHeight() / 2f);
                        updateBit(rotatedBitmap);
                        isRotating = false;
                    }
                })
                .start();
    }

    //更新图片
    public void updateBit(Bitmap bit) {
        bitOriginal = bit;
        enjoyCropLayout.setImage(bit, fitImageInside);
    }

    //根据设置的类型更新裁剪框。
    private void defineCropParams() {
        if (corpType == CropType.CIRCLE) {
            defineCropParamsCircle();
        } else {
            defineCropParamsRectangle();
        }
    }

    //矩形裁剪框
    protected void defineCropParamsRectangle() {
        int outX = outWidth;
        int outY = outHeight;
        if (outX <= 0) {
            outX = 600;
        }
        if (outY <= 0) {
            outY = 600;
        }
        outWidth = outX;
        outHeight = outY;
        //设置裁剪集成视图，这里通过一定的方式集成了遮罩层与预览框
        BaseLayerView layerView = new ClipPathLayerView(context);
        //设置遮罩层,这里使用半透明的遮罩层
        layerView.setMask(ColorMask.getTranslucentMask());
        //设置预览框形状
        layerView.setShape(new ClipPathSquare(outX, outY));
        //设置裁剪集成视图
        enjoyCropLayout.setLayerView(layerView);
        //设置边界限制，如果设置了该参数，预览框则不会超出图片
        enjoyCropLayout.setRestrict(true);
    }

    //圆形裁剪
    protected void defineCropParamsCircle() {
        int outX = outWidth;
        int outY = outHeight;
        if (outX <= 0) {
            outX = 600;
        }
        if (outY <= 0) {
            outY = 600;
        }
        //以框的较短边作为直径，转换为半径，单位 px。
        int w = Math.min(outX, outY);
        outWidth = w;
        outHeight = w;
        int radius = w / 2;
        BaseLayerView layerView = new ClipPathLayerView(context);
        layerView.setMask(ColorMask.getTranslucentMask()); // 设置遮罩

        layerView.setShape(new ClipPathCircle(radius));
        enjoyCropLayout.setLayerView(layerView); // 放到图片上方
        enjoyCropLayout.setRestrict(true); // 限制图片边界，避免框内露出空白。
    }

    public int getOutWidth() {
        return outWidth;
    }

    public int getOutHeight() {
        return outHeight;
    }

}
