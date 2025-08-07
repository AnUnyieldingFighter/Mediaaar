package com.images.ui.activity.camera;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.location.Location;
import android.media.ExifInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import media.library.images.unmix.ImageLog;
import com.media.option.R;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ImageActivity extends Activity implements View.OnClickListener {

    private ImageView iv;
    private String type;
    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent it = getIntent();
        String type = it.getStringExtra("arg0");
        setWindow();
        setContentView(R.layout.activity_image);
        path = it.getStringExtra("arg1");
        iv = (ImageView) findViewById(R.id.iv);
        TextView msgTv = (TextView) findViewById(R.id.msg_tv);
        findViewById(R.id.jwd_tv).setOnClickListener(this);
        findViewById(R.id.xz_tv).setOnClickListener(this);

        FileInputStream f;
        try {
            f = new FileInputStream(path);
            Bitmap bm = null;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 8;//图片的长宽都是原来的1/8
            BufferedInputStream bis = new BufferedInputStream(f);
            bm = BitmapFactory.decodeStream(bis, null, options);
            //bm= ImageBitmapUtile.onBitRotate(path,bm);
            iv.setImageBitmap(bm);
            f.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            f = null;
        } catch (IOException e) {
            e.printStackTrace();
            f = null;
        }

        ExifInterface exifInterface = getExifOrientation(path);
        if (exifInterface == null) {
            return;
        }
        String msg = "路径：" + path + "\n旋转：" + getAngle(exifInterface) + "\n尺寸：" + getResolution(exifInterface) + "\n拍照gps时间：" + getTakeTime(exifInterface) + "\n创建时间：" + getCreateTime(exifInterface) + "\n修改时间：" + getUpdateTime(exifInterface) + "\n经纬度：" + getLocation(exifInterface) + "\n设备品牌：" + getMake(exifInterface) + "\n设备机型：" + getModel(exifInterface) + "\n大小：" + getSize(path);
        msgTv.setText(msg);
    }

    private void setWindow() {
        //
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
        type = "2";
        if ("1".equals(type)) {
            // 设置横屏
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        if ("2".equals(type)) {
            // 设置竖屏显示
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        // 选择支持半透明模式,在有surfaceview的activity中使用。
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
    }

    private ExifInterface getExifOrientation(String filepath) {
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(filepath);
        } catch (IOException ex) {
            ImageLog.d("", "cannot read exif" + ex);
        }
        return exif;
    }

    private int getAngle(ExifInterface exif) {
        String orientation = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
        ImageLog.d("--------", "旋转" + orientation);
        int degree = -1;
        if (orientation == null) {
            return -3;
        }
        switch (orientation) {
            case ExifInterface.ORIENTATION_UNDEFINED + "":
                degree = -2;
                break;
            case ExifInterface.ORIENTATION_ROTATE_90 + "":
                degree = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180 + "":
                degree = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270 + "":
                degree = 270;
                break;
        }
        return degree;
    }

    private String getResolution(ExifInterface exif) {
        int w = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, -1);
        int h = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, -1);
        return w + "*" + h;
    }

    //拍照gps时间
    private String getTakeTime(ExifInterface exif) {
        String gpsTimeStamp = exif.getAttribute(ExifInterface.TAG_GPS_TIMESTAMP);
        String gpsDateStamp = exif.getAttribute(ExifInterface.TAG_GPS_DATESTAMP);
        return gpsDateStamp + " " + gpsTimeStamp;
    }

    //创建时间
    private String getCreateTime(ExifInterface exif) {
        String dateTime = exif.getAttribute(ExifInterface.TAG_DATETIME);
        return dateTime;
    }

    //修改时间
    private String getUpdateTime(ExifInterface exif) {
        String dateTimeDigitized = exif.getAttribute(ExifInterface.TAG_DATETIME_DIGITIZED);
        return dateTimeDigitized;
    }

    //经纬度
    private String getLocation(ExifInterface exif) {
        String latitude = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
        String longitude = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
        String latitudeRef = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
        String longitudeRef = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
        String lat = convertToCoordinate(latitude);
        String lon = convertToCoordinate(longitude);
        ImageLog.d("-----", latitudeRef + "*" + longitudeRef);
        return lat + "*" + lon;
    }

    //设备品牌
    private String getMake(ExifInterface exif) {
        String make = exif.getAttribute(ExifInterface.TAG_MAKE);
        return make;
    }

    //设备机型
    private String getModel(ExifInterface exif) {
        String model = exif.getAttribute(ExifInterface.TAG_MODEL);
        return model;
    }

    //获取照片大小
    private String getSize(String path) {
        long sizeI = 0;
        File file = new File(path);
        if (!file.exists()) {
            return sizeI + "";
        }
        sizeI = file.length();
        ImageLog.d("-------", "大小：" + sizeI);
        String value = "B";
        double size = 0;
        if (sizeI > 1024) {
            //kb
            size = sizeI / 1024;
            value = "kb";
        }
        if (size > 1024) {
            //M
            size = size / 1024;
            value = "M";
        }
        if (size > 1024) {
            //G
            size = size / 1024;
            value = "G";
        }
        double temp = sizeI;
        if (size > 0) {
            double size1 = (int) (size * 100);
            size = size1 / 100;
            temp = size;
        }
        return temp + value;
    }
    //设置旋转方向

    /**
     * ExifInterface.ORIENTATION_UNDEFINED :
     * ExifInterface.ORIENTATION_ROTATE_90:
     * ExifInterface.ORIENTATION_ROTATE_180:
     * ExifInterface.ORIENTATION_ROTATE_270:
     *
     * @param angle
     */
    private void setAngle(int angle) {
        ExifInterface exif = getExifOrientation(path);
        if (exif == null) {
            Log.e("旋转方向", "写入失败：exif=null");
            return;
        }
        exif.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(angle));
        try {
            exif.saveAttributes();
            Log.e("旋转方向", "写入成功");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("旋转方向", "写入失败：" + e.getMessage());
        }
    }

    //设置经纬度
    private void setLocation(String longitude, String latitude) {
        ExifInterface exif = getExifOrientation(path);
        if (exif == null) {
            Log.e("经纬度", "写入失败：exif=null");
            return;
        }
        //
        exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, longitude);
        //exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, longitude > 0.0f ? "E" : "W");
        exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, latitude);
        // exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, latitude > 0.0f ? "N" : "S");
        try {
            exif.saveAttributes();
            Log.e("经纬度", "写入成功");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("经纬度", "写入失败：" + e.getMessage());
        }
    }

    private void getOther(ExifInterface exifInterface) {
        String orientation = exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION);
        String dateTime = exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
        String make = exifInterface.getAttribute(ExifInterface.TAG_MAKE);
        String model = exifInterface.getAttribute(ExifInterface.TAG_MODEL);
        String flash = exifInterface.getAttribute(ExifInterface.TAG_FLASH);
        String imageLength = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_LENGTH);
        String imageWidth = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_WIDTH);
        String latitude = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
        String longitude = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
        String latitudeRef = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
        String longitudeRef = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
        String exposureTime = exifInterface.getAttribute(ExifInterface.TAG_EXPOSURE_TIME);
        String aperture = exifInterface.getAttribute(ExifInterface.TAG_APERTURE);
        String isoSpeedRatings = exifInterface.getAttribute(ExifInterface.TAG_ISO);
        String dateTimeDigitized = exifInterface.getAttribute(ExifInterface.TAG_DATETIME_DIGITIZED);
        String subSecTime = exifInterface.getAttribute(ExifInterface.TAG_SUBSEC_TIME);
        String subSecTimeOrig = exifInterface.getAttribute(ExifInterface.TAG_SUBSEC_TIME_ORIG);
        String subSecTimeDig = exifInterface.getAttribute(ExifInterface.TAG_SUBSEC_TIME_DIG);
        String altitude = exifInterface.getAttribute(ExifInterface.TAG_GPS_ALTITUDE);
        String altitudeRef = exifInterface.getAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF);
        String gpsTimeStamp = exifInterface.getAttribute(ExifInterface.TAG_GPS_TIMESTAMP);
        String gpsDateStamp = exifInterface.getAttribute(ExifInterface.TAG_GPS_DATESTAMP);
        String whiteBalance = exifInterface.getAttribute(ExifInterface.TAG_WHITE_BALANCE);
        String focalLength = exifInterface.getAttribute(ExifInterface.TAG_FOCAL_LENGTH);
        String processingMethod = exifInterface.getAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD);

        Log.e("TAG", "## orientation=" + orientation);
        Log.e("TAG", "## dateTime=" + dateTime);
        Log.e("TAG", "## make=" + make);
        Log.e("TAG", "## model=" + model);
        Log.e("TAG", "## flash=" + flash);
        Log.e("TAG", "## imageLength=" + imageLength);
        Log.e("TAG", "## imageWidth=" + imageWidth);
        Log.e("TAG", "## latitude=" + latitude);
        Log.e("TAG", "## longitude=" + longitude);
        Log.e("TAG", "## latitudeRef=" + latitudeRef);
        Log.e("TAG", "## longitudeRef=" + longitudeRef);
        Log.e("TAG", "## exposureTime=" + exposureTime);
        Log.e("TAG", "## aperture=" + aperture);
        Log.e("TAG", "## isoSpeedRatings=" + isoSpeedRatings);
        Log.e("TAG", "## dateTimeDigitized=" + dateTimeDigitized);
        Log.e("TAG", "## subSecTime=" + subSecTime);
        Log.e("TAG", "## subSecTimeOrig=" + subSecTimeOrig);
        Log.e("TAG", "## subSecTimeDig=" + subSecTimeDig);
        Log.e("TAG", "## altitude=" + altitude);
        Log.e("TAG", "## altitudeRef=" + altitudeRef);
        Log.e("TAG", "## gpsTimeStamp=" + gpsTimeStamp);
        Log.e("TAG", "## gpsDateStamp=" + gpsDateStamp);
        Log.e("TAG", "## whiteBalance=" + whiteBalance);
        Log.e("TAG", "## focalLength=" + focalLength);
        Log.e("TAG", "## processingMethod=" + processingMethod);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.jwd_tv) {
            //设置经纬度
            //纬度
            double latitude = 31.284550;
            //经度
            double longitude = 121.080940;
            setLocation(convertToDegree(longitude), convertToDegree(latitude));
            return;
        }
        if (id == R.id.xz_tv) {
            //设置旋转方向
            setAngle(ExifInterface.ORIENTATION_ROTATE_180);
            return;
        }
    }

    /**
     * 转换纬度
     * 在转成字符串的时候，有强转为int型，会有一定精度的影响
     *
     * @param lat
     * @return
     */
    private String getLatToDMS(double lat) {
        StringBuilder sb = new StringBuilder();
        float degrees = (float) Math.floor(lat);
        float minutes = (float) Math.floor(60 * (lat - degrees));
        float seconds = (float) (3600 * (lat - degrees) - 60 * minutes);
        sb.append((long) degrees * 1000000).append("/1000000,").append((long) minutes * 100000).append("/100000,").append((long) seconds * 65540).append("/65540");
        return sb.toString();
    }

    //经纬度转度分秒
    public static String convertToDegree(double gpsInfo) {
        String dms = Location.convert(gpsInfo, Location.FORMAT_SECONDS);
        return dms;
    }

    //度分秒转经纬度
    public String convertToCoordinate(String stringDMS) {
        if (stringDMS == null) {
            return null;
        }
        String[] split = null;
        if (stringDMS.contains(":")) {
            split = stringDMS.split(":", 3);
        }
        if (split == null && stringDMS.contains(",")) {
            split = stringDMS.split(",", 3);
        }
        if (split == null || split.length < 3) {
            return stringDMS;
        }

        double value = getValue(split[0]) + getValue(split[1]) / 60 + getValue(split[2]) / 3600;
        return String.valueOf(value);
    }

    private double getValue(String value) {
        String[] vs = value.split("/");
        return Double.parseDouble(vs[0]) / Double.parseDouble(vs[1]);
    }

    /**
     * 转换经度
     *
     * @param lon
     * @return
     */
    private String getLongToDMS(double lon) {
        StringBuilder sb = new StringBuilder();
        float degrees = (float) Math.floor(lon);
        float minutes = (float) Math.floor(60 * (lon - degrees));
        float seconds = (float) (3600 * (lon - degrees) - 60 * minutes);
        sb.append((long) degrees * 100000).append("/100000,").append((long) minutes * 65540).append("/65540,").append((long) seconds * 33685504).append("/33685504");
        return sb.toString();
    }
}
