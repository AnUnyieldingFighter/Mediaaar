package media.library.images.unmix;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.text.TextUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Administrator on 2017/2/28.
 */

public class BitmapUtile {
    private static String TAG = "BitmapUtile";

    //======对拍摄的照片进行旋转处理===========
    public static void imageRotate(File file) {
        //1.读取图片
        Bitmap bm = decodeFile(file, 100);
        if (bm == null) {
            return;
        }
        //2.获取图片旋转的角度，然后给它旋转回来
        int degree = readPictureDegree(file.getAbsolutePath());
        //3.根据指定旋转度数进行图片旋转
        Bitmap bitmap = rotaingImageView(degree, bm);
        //4.存储旋转后图片
        saveBitmaps(bitmap, file);
    }

    //对拍摄的照片进行旋转处理
    public static Bitmap imageRotate(String imagePath, Bitmap bm) {
        if (bm == null) {
            ImageLog.d(TAG, "Bitmap 无效");
            return null;
        }
        if (TextUtils.isEmpty(imagePath)) {
            ImageLog.d(TAG, "原始图片path不能为空");
            return null;
        }
        //1.获取图片旋转的角度，然后给它旋转回来
        int degree = readPictureDegree(imagePath);
        if (degree == 0) {
            ImageLog.d(TAG, "无须处理");
            return bm;
        }
        //3.根据指定旋转度数进行图片旋转
        Bitmap bitmap = rotaingImageView(degree, bm);
        return bitmap;
    }

    private static final int DEFAULT_REQUIRED_SIZE = 70;

    /**
     * @param f
     * @param size
     * @return
     */
    private static Bitmap decodeFile(File f, int size) {
        try {
            BitmapFactory.Options option = new BitmapFactory.Options();
            /**
             inJustDecodeBounds如果将其设为true的话，在decode时将会返回null。
             通过此设置可以去查询一个bitmap的属性，比如bitmap的长和宽，而不占用内存大小.同时可避免OOM
             */
            option.inJustDecodeBounds = true;
            FileInputStream stream1 = new FileInputStream(f);
            BitmapFactory.decodeStream(stream1, null, option);
            stream1.close();
            final int REQUIRED_SIZE = size > 0 ? size : DEFAULT_REQUIRED_SIZE;
            int width_tmp = option.outWidth, height_tmp = option.outHeight;
            int scale = 1;
            while (true) {
                if (width_tmp / 2 < REQUIRED_SIZE
                        || height_tmp / 2 < REQUIRED_SIZE)
                    break;
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }
            if (scale >= 2) {
                scale /= 2;
            }
            BitmapFactory.Options option2 = new BitmapFactory.Options();
            option2.inSampleSize = scale;
            FileInputStream stream2 = new FileInputStream(f);
            Bitmap bitmap = BitmapFactory.decodeStream(stream2, null, option2);
            stream2.close();
            ImageLog.d(TAG, "读取图片成功 scale=" + scale);
            return bitmap;
        } catch (FileNotFoundException e) {
            ImageLog.d(TAG, "读取图片失败");
        } catch (IOException e) {
            ImageLog.d(TAG, "读取图片失败");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取图片信息
     *
     * @param path
     * @return degree 旋转角度
     */
    private static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
            ImageLog.d(TAG, "get degree：" + degree);
        } catch (IOException e) {
            e.printStackTrace();
            ImageLog.d(TAG, "get degree error");
        }
        return degree;

    }

    /**
     * 图片旋转
     *
     * @param angle
     * @param bitmap
     * @return
     */
    private static Bitmap rotaingImageView(int angle, Bitmap bitmap) {
        if (angle == 0) {
            return bitmap;
        }
        // 旋转图片 动作
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        ImageLog.d(TAG, "angle:" + angle + " complete");
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizedBitmap;
    }


    //----------

    /**
     * 保存bitmap
     *
     * @param newFile
     * @param bitmap
     * @return
     */
    public static boolean saveBitmaps(Bitmap bitmap, File newFile) {
        try {
            if (!newFile.exists()) {
                newFile.createNewFile();
            }
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(
                    newFile));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
            ImageLog.d(TAG, "图片保存成功");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            ImageLog.d(TAG, "图片保存失败");
        }
        return false;
    }
    //----------

    /**
     * @param bmpFilePath     图片路径
     * @param requestedWidth  需要的宽
     * @param requestedHeight 需要的高
     * @return 图片
     */
    public static Bitmap resizeBitmap(String bmpFilePath, int requestedWidth,
                                      int requestedHeight) {
        File file = new File(bmpFilePath);
        if (!file.exists()) {
            return null;
        }
        if (requestedWidth <= 0 || requestedHeight <= 0) {

            Bitmap bmp = BitmapFactory.decodeFile(bmpFilePath);

            return bmp;
        }

        final BitmapFactory.Options options = new BitmapFactory.Options();

        // 获得图片的宽高
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(bmpFilePath, options);

        //  int size = calculateInSampleSize(options, requestedWidth, requestedHeight);
        int size = (int) getScale(options.outWidth, options.outHeight,
                requestedWidth, requestedHeight);
        options.inSampleSize = size;

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        Bitmap bitmap = BitmapFactory.decodeFile(bmpFilePath, options);

        if (bitmap == null) {
            return null;
        }
        if (size <= 1) {
            return bitmap;
        }
        // 个别机型，用以上方法的压缩率不符合要求，所以再次精确压缩
        if (bitmap.getWidth() > requestedWidth
                || bitmap.getHeight() > requestedHeight) {
            bitmap = resizeBitmapInOldWay(bitmap, requestedWidth,
                    requestedHeight);
        }
        return bitmap;

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
        return calculateInSampleSize(options.outHeight, options.outWidth, reqWidth, reqHeight);
    }

    public static int calculateInSampleSize(int outWidth, int outHeight,
                                            int reqWidth, int reqHeight) {
        int height = outWidth;
        int width = outHeight;
        if (outHeight < outWidth) {
            height = outWidth;
            width = outHeight;
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
     * 统一计算缩放比例
     *
     * @param srcWidth
     * @param requestedWidth
     * @return
     */
    public static float getScale(int srcWidth, int srcHeight,
                                 int requestedWidth, int requestedHeight) {
        float scale = 1;
        if (requestedWidth <= 0 && requestedHeight <= 0) {
            // 不做任何缩放
            scale = 1;
        } else if (requestedWidth > 0 && requestedHeight > 0) {

            float scaleWidth = srcWidth * 1.0f / requestedWidth;
            float scaleHeight = srcHeight * 1.0f / requestedHeight;
            if (scaleWidth < scaleHeight) {
                scale = scaleHeight;
            } else {
                scale = scaleWidth;
            }
        } else if (requestedWidth > 0 && requestedHeight <= 0) {
            float scaleWidth = srcWidth * 1.0f / requestedWidth;
            scale = scaleWidth;
        } else if (requestedWidth <= 0 && requestedHeight > 0) {
            float scaleHeight = srcHeight * 1.0f / requestedHeight;
            scale = scaleHeight;
        }

        return scale;
    }

    private static Bitmap resizeBitmapInOldWay(Bitmap bitmap, int maxWidth,
                                               int maxHeight) {
        // 获得图片的宽高
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        // matrix的缩小参数应该为getScale的倒数(小数)
        float scale = 1 / getScale(width, height, maxWidth, maxHeight);
        //  float scale = 1 / calculateInSampleSize(width, height, maxWidth, maxHeight);
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        // 得到新的图片
        Bitmap newbm = null;
        try {
            newbm = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix,
                    true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newbm;
    }
}
