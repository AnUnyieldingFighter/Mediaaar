package com.images.ui.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.media.option.R;

import java.util.concurrent.ExecutionException;

/**
 * 相机预览框选测试页面。
 *
 * <p>功能：
 * 1. 打开相机并显示预览。
 * 2. 预览上显示一个浮层，点击浮层任意位置可以添加正方形或长方形框。
 * 3. 点击“截取框内”按钮，从当前预览画面中裁剪框内图像，并显示在右下角。</p>
 */
public class CameraPreviewActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_CAMERA_PERMISSION = 1001;

    private PreviewView previewView;
    private media.library.camera.view.CameraPreviewOverlayView overlayView;
    private ImageView cropResultView;
    private Button rectButton;
    private Button squareButton;
    private Button captureButton;

    private ProcessCameraProvider cameraProvider;

    /**
     * 创建页面。
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_preview);
        initViews();
        requestCameraPermission();
    }

    /**
     * 释放相机资源。
     */
    @Override
    protected void onDestroy() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
        super.onDestroy();
    }

    /**
     * 初始化控件。
     */
    private void initViews() {
        previewView = findViewById(R.id.camera_preview_view);
        overlayView = findViewById(R.id.camera_overlay_view);
        cropResultView = findViewById(R.id.camera_crop_result);
        rectButton = findViewById(R.id.btn_shape_rect);
        squareButton = findViewById(R.id.btn_shape_square);
        captureButton = findViewById(R.id.btn_capture_box);

        rectButton.setOnClickListener(this);
        squareButton.setOnClickListener(this);
        captureButton.setOnClickListener(this);

        overlayView.setShapeType(media.library.camera.view.CameraPreviewOverlayView.SHAPE_SQUARE);
    }

    /**
     * 请求相机权限。
     */
    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCameraPreview();
            return;
        }
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.CAMERA},
                REQUEST_CAMERA_PERMISSION
        );
    }

    /**
     * 权限回调。
     */
    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != REQUEST_CAMERA_PERMISSION) {
            return;
        }
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCameraPreview();
        } else {
            Toast.makeText(this, "需要相机权限才能预览", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * 启动相机预览。
     */
    private void startCameraPreview() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    cameraProvider = cameraProviderFuture.get();
                    bindCameraPreview();
                } catch (ExecutionException e) {
                    Toast.makeText(CameraPreviewActivity.this, "打开相机失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    Toast.makeText(CameraPreviewActivity.this, "打开相机被中断", Toast.LENGTH_SHORT).show();
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    /**
     * 绑定 CameraX 预览。
     */
    private void bindCameraPreview() {
        if (cameraProvider == null) {
            return;
        }
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
        cameraProvider.unbindAll();
        cameraProvider.bindToLifecycle(this, cameraSelector, preview);
    }

    /**
     * 按钮点击。
     */
    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_shape_rect) {
            overlayView.setShapeType(media.library.camera.view.CameraPreviewOverlayView.SHAPE_RECTANGLE);
            Toast.makeText(this, "点击画面添加长方形框", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.btn_shape_square) {
            overlayView.setShapeType(media.library.camera.view.CameraPreviewOverlayView.SHAPE_SQUARE);
            Toast.makeText(this, "点击画面添加正方形框", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.btn_capture_box) {
            //截取框内
            captureBoxImage();
        }
    }

    /**
     * 截取当前框内图像，并显示到右下角。
     */
    private void captureBoxImage() {
        Bitmap previewBitmap = previewView.getBitmap();
        if (previewBitmap == null) {
            Toast.makeText(this, "当前预览画面还未准备好", Toast.LENGTH_SHORT).show();
            return;
        }
        Bitmap cropBitmap = overlayView.cropBitmapByViewRect(previewBitmap);
        if (cropBitmap == null) {
            Toast.makeText(this, "请先点击预览区域添加方框", Toast.LENGTH_SHORT).show();
            return;
        }
        if (cropBitmap == null) {
            Toast.makeText(this, "截取失败，方框超出预览区域", Toast.LENGTH_SHORT).show();
            return;
        }
        cropResultView.setImageBitmap(cropBitmap);
        cropResultView.setVisibility(View.VISIBLE);
    }


}
