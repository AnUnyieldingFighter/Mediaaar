package com.images.ui.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.media3.ui.PlayerView;

import com.media.option.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import media.library.camera.BaseCameraController;
import media.library.camera.CameraController;


/**
 * CameraX 相机页面。
 *
 * <p>提供拍照、开始录像、暂停录像、继续录像和停止录像功能。
 * 点击预览画面可以对焦，双指张开或合拢可以缩放镜头。</p>
 */
public class CameraActivity extends AppCompatActivity
        implements View.OnClickListener, BaseCameraController.Callback {

    private PreviewView previewView;
    private ImageView photoPreviewView;
    private PlayerView playerView;
    private TextView statusText;
    private Button recordButton;
    private Button pauseButton;
    private Button stopButton;
    private Button photoButton;
    private Button resetPreviewButton;

    private CameraController cameraController;
    private ScaleGestureDetector scaleGestureDetector;
    private float downX;
    private float downY;
    private boolean movedAfterDown;
    private boolean recordingPaused;


    /**
     * 创建相机页面。
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        initViews();
        initGestures();
        requestCameraPermission();
    }

    /**
     * 初始化页面控件。
     */
    private void initViews() {
        previewView = findViewById(R.id.camera_preview);
        photoPreviewView = findViewById(R.id.camera_photo_preview);
        playerView = findViewById(R.id.camera_player);
        statusText = findViewById(R.id.camera_status);
        recordButton = findViewById(R.id.camera_record);
        pauseButton = findViewById(R.id.camera_pause);
        stopButton = findViewById(R.id.camera_stop);
        photoButton = findViewById(R.id.camera_photo);
        resetPreviewButton = findViewById(R.id.camera_reset_preview);

        recordButton.setOnClickListener(this);
        pauseButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);
        photoButton.setOnClickListener(this);
        resetPreviewButton.setOnClickListener(this);
        updateButtonState(false);
    }

    /**
     * 初始化点击对焦和双指缩放手势。
     */
    private void initGestures() {
        scaleGestureDetector = new ScaleGestureDetector(
                this,
                new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    @Override
                    public boolean onScale(ScaleGestureDetector detector) {
                        movedAfterDown = true;
                        if (cameraController != null) {
                            cameraController.zoomByScale(detector.getScaleFactor());
                        }
                        return true;
                    }
                }
        );

        previewView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                scaleGestureDetector.onTouchEvent(event);
                handlePreviewTouch(event);
                return true;
            }
        });
    }

    /**
     * 处理预览画面的点击，用于单击对焦。
     */
    private void handlePreviewTouch(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                movedAfterDown = false;
                break;
            case MotionEvent.ACTION_MOVE:
                if (Math.abs(event.getX() - downX) > 20
                        || Math.abs(event.getY() - downY) > 20) {
                    movedAfterDown = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!movedAfterDown
                        && !scaleGestureDetector.isInProgress()
                        && cameraController != null) {
                    cameraController.focusAt(event.getX(), event.getY());
                    showMessage("正在对焦");
                }
                break;
            default:
                break;
        }
    }

    /**
     * 请求相机和录音权限。
     */
    private void requestCameraPermission() {
        //相机权限
        boolean cameraGranted = isPermission(Manifest.permission.CAMERA);
        //访问手机麦克风，采集音频
        boolean audioGranted = isPermission(Manifest.permission.RECORD_AUDIO);
        //Android 9 及以下写入公共相册需要存储权限
        boolean storageGranted = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                || isPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (cameraGranted) {
            bindCamera();
            List<String> permissions = new ArrayList<>();
            if (!audioGranted) {
                permissions.add(Manifest.permission.RECORD_AUDIO);
            }
            if (!storageGranted) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (!permissions.isEmpty()) {
                permissionLauncher.launch(permissions.toArray(new String[0]));
            }
            return;
        }
        List<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.CAMERA);
        permissions.add(Manifest.permission.RECORD_AUDIO);
        if (!storageGranted) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        permissionLauncher.launch(permissions.toArray(new String[0]));
    }

    /**
     * 判断是否已经有指定权限。
     */
    private boolean isPermission(String permissionName) {
        int permission = ContextCompat.checkSelfPermission(this, permissionName);
        return permission == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 请求相机和录音权限。
     */
    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    new ActivityResultCallback<Map<String, Boolean>>() {
                        @Override
                        public void onActivityResult(Map<String, Boolean> result) {
                            Boolean cameraGranted = result.get(Manifest.permission.CAMERA);
                            boolean cameraGranted2 = isPermission(Manifest.permission.CAMERA);
                            if (Boolean.TRUE.equals(cameraGranted) || cameraGranted2) {
                                bindCamera();
                            } else {
                                showMessage("需要相机权限才能使用相机");
                                finish();
                            }
                        }
                    }
            );

    /**
     * 创建并绑定相机控制器。
     */
    private void bindCamera() {
        if (cameraController != null) {
            cameraController.release();
        }
        cameraController = new CameraController(this, this);
        //开启后，拍照保存完成会直接停留在刚拍好的照片画面。
        cameraController.setStayOnCapturedPhoto(true);
        cameraController.bindPhotoView(photoPreviewView);
        //开启后，停止录像保存完成会直接在当前页面播放刚录好的视频。
        cameraController.setAutoPlayRecordedVideo(true);
        cameraController.bindPlayerView(playerView);
        cameraController.bind(this, previewView);
    }

    /**
     * 处理按钮点击。
     */
    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (cameraController == null) {
            showMessage("相机还没有准备好");
            return;
        }
        if (id == R.id.camera_record) {
            updateResetPreviewButton(false);
            cameraController.startRecording();
            statusText.setText("正在录像");
            updateButtonState(true);
        } else if (id == R.id.camera_pause) {
            if (recordingPaused) {
                cameraController.resumeRecording();
                statusText.setText("正在录像");
                pauseButton.setText("暂停录像");
                recordingPaused = false;
            } else {
                cameraController.pauseRecording();
                statusText.setText("录像已暂停");
                pauseButton.setText("继续录像");
                recordingPaused = true;
            }
        } else if (id == R.id.camera_stop) {
            updateResetPreviewButton(false);
            cameraController.stopRecording();
            statusText.setText("正在保存录像");
            updateButtonState(false);
        } else if (id == R.id.camera_photo) {
            updateResetPreviewButton(false);
            cameraController.takePhoto();
        } else if (id == R.id.camera_reset_preview) {
            cameraController.resetToCameraPreview();
            statusText.setText("相机已准备好");
            updateButtonState(false);
            updateResetPreviewButton(false);
        }
    }

    /**
     * 根据录像状态更新按钮。
     */
    private void updateButtonState(boolean recording) {
        recordButton.setEnabled(!recording);
        pauseButton.setEnabled(recording);
        stopButton.setEnabled(recording);
        photoButton.setEnabled(!recording);
        if (!recording) {
            pauseButton.setText("暂停录像");
            pauseButton.setTag(Boolean.FALSE);
            recordingPaused = false;
        }
    }

    /**
     * 根据是否处于照片/视频预览状态更新返回预览按钮。
     */
    private void updateResetPreviewButton(boolean show) {
        resetPreviewButton.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            recordButton.setEnabled(false);
            pauseButton.setEnabled(false);
            stopButton.setEnabled(false);
            photoButton.setEnabled(false);
        }
    }

    /**
     * 相机初始化完成回调。
     */
    @Override
    public void onCameraReady() {
        statusText.setText("相机已准备好");
        updateButtonState(false);
        updateResetPreviewButton(false);
    }

    @Override
    public void onPhotoSaved(Uri uri) {
        statusText.setText("照片保存成功");
        updateResetPreviewButton(true);
        showMessage("照片已保存：" + uri);
    }

    @Override
    public void onVideoSaved(Uri uri) {
        statusText.setText("视频保存成功，正在播放");
        updateButtonState(false);
        updateResetPreviewButton(true);
        showMessage("录像已保存：" + uri);
    }

    /**
     * 相机错误回调。
     */
    @Override
    public void onCameraError(String message, Throwable throwable) {
        statusText.setText(message);
        updateButtonState(false);
        updateResetPreviewButton(false);
        showMessage(message);
    }

    /**
     * 显示简短提示。
     */
    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * 页面销毁时释放相机资源。
     */
    @Override
    protected void onDestroy() {
        if (cameraController != null) {
            cameraController.release();
            cameraController = null;
        }
        super.onDestroy();
    }
}
