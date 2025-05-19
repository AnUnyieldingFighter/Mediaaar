package com.images.ui.Manger;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.images.unmix.BitmapUtile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * 图形/图像处理类，专门实现图片图像的处理。 例如 拉伸、缩小、裁剪、倒影等效果
 *
 * @author PanYingYun
 */
public class ImageUtile {
    private static String TAG = "ImageUtile";

    /**
     * 获取bitmap
     *
     * @param filePath
     * @return
     */
    public static Bitmap getBitmapByPath(String filePath) {
        return getBitmapByPath(filePath, null);
    }

    public static Bitmap getBitmapByPath(String filePath,
                                         BitmapFactory.Options opts) {
        FileInputStream fis = null;
        Bitmap bitmap = null;
        try {
            File file = new File(filePath);
            fis = new FileInputStream(file);
            bitmap = BitmapFactory.decodeStream(fis, null, opts);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
            } catch (Exception e) {
            }
        }
        return bitmap;
    }

    public static Bitmap loadImgThumbnail(String filePath) {
        return getBitmapByPath(filePath);
    }

    /**
     * 放大缩小图片
     *
     * @param bitmap
     * @param w
     * @param h
     * @return
     */
    public static Bitmap zoomBitmap(Bitmap bitmap, int w, int h) {
        Bitmap newbmp = null;
        if (bitmap != null) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            Matrix matrix = new Matrix();
            float scaleWidht = ((float) w / width);
            float scaleHeight = ((float) h / height);
            matrix.postScale(scaleWidht, scaleHeight);
            newbmp = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix,
                    true);
        }
        return newbmp;
    }

    /**
     * 缩放图片
     *
     * @param bitmap
     * @param newWidth
     * @return
     */
    public static Bitmap zoomBitmap(Bitmap bitmap, int newWidth) {
        if (bitmap == null) {
            return null;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = height * scaleWidth / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
    }

    /**
     * 缩放图片
     *
     * @param drawable
     * @param newWidth
     * @return
     */
    public static Drawable zoomDrawable(Resources res, Drawable drawable,
                                        int newWidth) {
        if (drawable == null) {
            return null;
        }
        Bitmap bitmap = drawableToBitmap(drawable);
        return new BitmapDrawable(res, zoomBitmap(bitmap, newWidth));
    }

    // 创建倒影Bitmap
    public static Drawable createReflectedDrawable(Resources res,
                                                   Drawable originalDrawable) {
        Bitmap originalBitmap = drawableToBitmap(originalDrawable);
        final int reflectionGap = 1;
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();

        Matrix matrix = new Matrix();
        matrix.preScale(1, -1);

        Bitmap reflectionImage = Bitmap.createBitmap(originalBitmap, 0,
                height / 2, width, height / 2, matrix, false);

        Bitmap bitmapWithReflection = Bitmap.createBitmap(width, (height
                + height / 4 + reflectionGap), Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmapWithReflection);

        canvas.drawBitmap(originalBitmap, 0, 0, null);

        Paint deafaultPaint = new Paint();
        deafaultPaint.setColor(Color.WHITE);
        canvas.drawRect(0, height, width, height + reflectionGap, deafaultPaint);

        canvas.drawBitmap(reflectionImage, 0, height + reflectionGap, null);

        Paint paint = new Paint();
        LinearGradient shader = new LinearGradient(0,
                originalBitmap.getHeight(), 0, bitmapWithReflection.getHeight()
                + reflectionGap, 0x30ffffff, 0x00ffffff, TileMode.CLAMP);

        paint.setShader(shader);

        paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));

        canvas.drawRect(0, height, width, bitmapWithReflection.getHeight()
                + reflectionGap, paint);
        reflectionImage.recycle();
        return new BitmapDrawable(res, bitmapWithReflection);
    }

    /**
     * Drawable 转化Bitmap
     *
     * @param drawable
     * @return
     */
    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap
                .createBitmap(
                        Math.abs(drawable.getIntrinsicWidth()),
                        Math.abs(drawable.getIntrinsicHeight()),
                        drawable.getOpacity() != PixelFormat.OPAQUE ? Config.ARGB_8888
                                : Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * 图片旋转
     **/
    public static Bitmap adjustPhotoRotation(Bitmap bm,
                                             final int orientationDegree) {
        // 定义矩阵对象
        Matrix matrix = new Matrix();
        // 参数为正则向右旋转
        matrix.postRotate(orientationDegree);
        // bmp.getWidth(), 500分别表示重绘后的位图宽高
        Bitmap dstbmp = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
                bm.getHeight(), matrix, true);

        return dstbmp;
    }

    /**
     * bitmap to bytes
     *
     * @param bm
     * @return
     */
    public static byte[] bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    /**
     * 计算图片的缩放值
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        if (options.outHeight < options.outWidth) {
            height = options.outWidth;
            width = options.outHeight;
        }
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            int heightRatio = (int) ((float) height / (float) reqHeight);
            int widthRatio = (int) ((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? widthRatio : heightRatio;
            if (inSampleSize % 2 == 1) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }

    /**
     * 根据路径获得突破并压缩返回bitmap用于显示
     *
     * @param filePath
     * @return
     */
    @SuppressLint("NewApi")
    public static Bitmap getSmallBitmap(String filePath) {
        File imagePath = new File(filePath);
        Log.e("ImageUtile", "image:" + filePath);
        if (!imagePath.exists()) {
            Log.e("ImageUtile", "image:图片路径已修改");
            return null;
        }
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // options.inSampleSize = calculateInSampleSize(options, 720, 1280);
        options.inSampleSize = calculateInSampleSize(options, 480, 800);
        options.inJustDecodeBounds = false;
        // 重新读出图片
        Bitmap bimap = BitmapFactory.decodeFile(filePath, options);
        Bitmap bm = BitmapUtile.imageRotate(filePath, bimap);
        if (bm != null) {
            bimap = bm;
        }
        float sizeRow = (float) bimap.getRowBytes() / 1024;
        float sizeCount = bimap.getByteCount() / 1024;// 3168
        Log.e("sizeRow:" + sizeRow, "sizeCount:" + sizeCount);

        return bimap;
    }

}
