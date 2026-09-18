package com.images.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.media3.ui.PlayerView;

import com.media.option.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import media.library.camera.BaseCamera;
import media.library.camera.OperationCamera;


/**
 * CameraX 相机页面。
 *
 * <p>提供拍照、开始录像、暂停录像、继续录像和停止录像功能。
 * 点击预览画面可以对焦，双指张开或合拢可以缩放镜头。</p>
 */
public class CameraActivity extends AppCompatActivity
        implements View.OnClickListener, BaseCamera.Callback {

    private PreviewView previewView;
    private ImageView photoPreviewView;
    private PlayerView playerView;
    private View focusIndicatorView;
    private View exposureIndicatorView;
    private View exposureTrackView;
    private View exposureThumbView;
    private TextView exposureTextView;
    private TextView statusText;
    private Button recordButton;
    private Button pauseButton;
    private Button stopButton;
    private Button photoButton;
    private Button resetPreviewButton;
    private Button switchCameraButton;
    private Button flashButton;

    private OperationCamera operationCamera;
    private ScaleGestureDetector scaleGestureDetector;
    private float downX;
    private float downY;
    private boolean movedAfterDown;
    private boolean recordingPaused;
    private int focusAnimationToken;
    private boolean focusTouching;
    private boolean focusHoldingExisting;
    private boolean focusDisappearing;
    private boolean multiPointerGesture;
    private long downTime;
    private int tapTouchSlop;
    private int focusStartExposureIndex;
    private float pendingFocusX;
    private float pendingFocusY;
    private int pendingFocusToken = -1;
    private boolean pendingFocusTriggered;


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
        focusIndicatorView = findViewById(R.id.camera_focus_indicator);
        exposureIndicatorView = findViewById(R.id.camera_exposure_indicator);
        exposureTrackView = findViewById(R.id.camera_exposure_track);
        exposureThumbView = findViewById(R.id.camera_exposure_thumb);
        exposureTextView = findViewById(R.id.camera_exposure_text);
        statusText = findViewById(R.id.camera_status);
        recordButton = findViewById(R.id.camera_record);
        pauseButton = findViewById(R.id.camera_pause);
        stopButton = findViewById(R.id.camera_stop);
        photoButton = findViewById(R.id.camera_photo);
        resetPreviewButton = findViewById(R.id.camera_reset_preview);
        switchCameraButton = findViewById(R.id.camera_switch);
        flashButton = findViewById(R.id.camera_flash);
        tapTouchSlop = ViewConfiguration.get(this).getScaledTouchSlop() * 2;

        recordButton.setOnClickListener(this);
        pauseButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);
        photoButton.setOnClickListener(this);
        resetPreviewButton.setOnClickListener(this);
        switchCameraButton.setOnClickListener(this);
        flashButton.setOnClickListener(this);
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
                        if (operationCamera != null) {
                            operationCamera.zoomByScale(detector.getScaleFactor());
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
                downTime = event.getEventTime();
                movedAfterDown = false;
                multiPointerGesture = false;
                focusTouching = isFocusIndicatorShowing();
                focusHoldingExisting = focusTouching;
                if (focusTouching && operationCamera != null) {
                    focusStartExposureIndex = operationCamera.getExposureCompensationIndex();
                    if (focusDisappearing) {
                        holdExistingFocusIndicator();
                    }
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                movedAfterDown = true;
                multiPointerGesture = true;
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() > 1 || scaleGestureDetector.isInProgress()) {
                    movedAfterDown = true;
                    multiPointerGesture = true;
                    return;
                }
                if (isMoveOutsideTapSlop(event)) {
                    movedAfterDown = true;
                }
                if (focusTouching
                        && operationCamera != null
                        && Math.abs(event.getY() - downY) > tapTouchSlop) {
                    int exposureIndex = operationCamera.setExposureByVerticalDrag(
                            downY,
                            event.getY(),
                            previewView.getHeight(),
                            focusStartExposureIndex
                    );
                    updateExposureIndicator(operationCamera.getExposurePercent(exposureIndex));
                }
                break;
            case MotionEvent.ACTION_UP:
                if (isClickGesture(event) && operationCamera != null) {
                    resetFocusIndicatorForNewFocus();
                    focusTouching = true;
                    focusHoldingExisting = false;
                    focusStartExposureIndex = operationCamera.getExposureCompensationIndex();
                    int animationToken = showFocusAnimation(event.getX(), event.getY());
                    showExposureIndicator(
                            event.getX(),
                            event.getY(),
                            operationCamera.getExposurePercent(focusStartExposureIndex)
                    );
                    if (animationToken != -1) {
                        setPendingFocus(animationToken, event.getX(), event.getY());
                    }
                } else {
                    if (focusTouching && focusHoldingExisting) {
                        focusTouching = false;
                        focusHoldingExisting = false;
                        finishFocusAnimation();
                        break;
                    }
                    focusTouching = false;
                    focusHoldingExisting = false;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (focusTouching && focusHoldingExisting) {
                    finishFocusAnimation();
                }
                focusTouching = false;
                focusHoldingExisting = false;
                break;
            default:
                break;
        }
    }

    /**
     * 判断当前手势是否是短点击，只有短点击才触发聚焦和方框。
     */
    private boolean isClickGesture(MotionEvent event) {
        long duration = event.getEventTime() - downTime;
        return !movedAfterDown
                && !multiPointerGesture
                && duration < ViewConfiguration.getLongPressTimeout();
    }

    /**
     * 判断手指移动是否已经超过点击允许范围。
     */
    private boolean isMoveOutsideTapSlop(MotionEvent event) {
        float moveX = event.getX() - downX;
        float moveY = event.getY() - downY;
        return moveX * moveX + moveY * moveY > tapTouchSlop * tapTouchSlop;
    }

    /**
     * 判断聚焦框当前是否已经显示。
     */
    private boolean isFocusIndicatorShowing() {
        return focusIndicatorView != null && focusIndicatorView.getVisibility() == View.VISIBLE;
    }

    /**
     * 已有聚焦框时再次按住，接管当前动画并保持显示。
     */
    private void holdExistingFocusIndicator() {
        int animationToken = ++focusAnimationToken;
        clearPendingFocus();
        if (focusIndicatorView != null) {
            focusIndicatorView.animate().cancel();
            focusIndicatorView.setVisibility(View.VISIBLE);
            focusIndicatorView.setAlpha(1.0f);
            focusIndicatorView.setScaleX(1.0f);
            focusIndicatorView.setScaleY(1.0f);
        }
        if (exposureIndicatorView != null
                && exposureIndicatorView.getVisibility() == View.VISIBLE) {
            exposureIndicatorView.animate().cancel();
            exposureIndicatorView.setAlpha(1.0f);
            exposureIndicatorView.setScaleX(1.0f);
            exposureIndicatorView.setScaleY(1.0f);
        }
        keepFocusIndicatorVisible(animationToken);
    }

    /**
     * 新一次点击聚焦前，取消旧聚焦框和亮度指示器的动画状态。
     */
    private void resetFocusIndicatorForNewFocus() {
        focusAnimationToken++;
        clearPendingFocus();
        focusDisappearing = false;
        if (focusIndicatorView != null) {
            focusIndicatorView.animate().cancel();
            focusIndicatorView.setVisibility(View.GONE);
            focusIndicatorView.setAlpha(1.0f);
            focusIndicatorView.setScaleX(1.0f);
            focusIndicatorView.setScaleY(1.0f);
        }
        if (exposureIndicatorView != null) {
            exposureIndicatorView.animate().cancel();
            exposureIndicatorView.setVisibility(View.GONE);
            exposureIndicatorView.setAlpha(1.0f);
            exposureIndicatorView.setScaleX(1.0f);
            exposureIndicatorView.setScaleY(1.0f);
        }
    }

    /**
     * 显示点击对焦的绿色缩放框动画。
     */
    private int showFocusAnimation(float x, float y) {
        if (focusIndicatorView == null || previewView == null) {
            return -1;
        }
        final int animationToken = ++focusAnimationToken;
        focusDisappearing = false;

        int size = focusIndicatorView.getWidth();
        if (size <= 0) {
            size = dpToPx(76);
        }

        focusIndicatorView.animate().cancel();
        if (exposureIndicatorView != null) {
            exposureIndicatorView.animate().cancel();
        }
        focusIndicatorView.setVisibility(View.VISIBLE);
        focusIndicatorView.setAlpha(1.0f);
        focusIndicatorView.setScaleX(1.65f);
        focusIndicatorView.setScaleY(1.65f);
        focusIndicatorView.setX(previewView.getLeft() + x - size / 2.0f);
        focusIndicatorView.setY(previewView.getTop() + y - size / 2.0f);
        focusIndicatorView.bringToFront();

        focusIndicatorView.animate()
                .scaleX(0.82f)
                .scaleY(0.82f)
                .alpha(1.0f)
                .setStartDelay(0)
                .setDuration(180)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        if (animationToken != focusAnimationToken) {
                            return;
                        }
                        focusIndicatorView.animate()
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .alpha(1.0f)
                                .setStartDelay(0)
                                .setDuration(120)
                                .withEndAction(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (animationToken != focusAnimationToken) {
                                            return;
                                        }
                                        startFocusBlinkAnimation(animationToken, true);
                                    }
                                })
                                .start();
                    }
                })
                .start();
        return animationToken;
    }

    /**
     * 记录本次动画对应的待聚焦坐标。
     */
    private void setPendingFocus(int animationToken, float x, float y) {
        pendingFocusToken = animationToken;
        pendingFocusX = x;
        pendingFocusY = y;
        pendingFocusTriggered = false;
    }

    /**
     * 清空待聚焦状态。
     */
    private void clearPendingFocus() {
        pendingFocusToken = -1;
        pendingFocusTriggered = false;
        pendingFocusX = 0;
        pendingFocusY = 0;
    }

    /**
     * 显示聚焦框旁边的亮度指示器。
     */
    private void showExposureIndicator(float focusX, float focusY, int exposurePercent) {
        if (exposureIndicatorView == null || previewView == null) {
            return;
        }

        int focusSize = focusIndicatorView == null ? dpToPx(76) : focusIndicatorView.getWidth();
        if (focusSize <= 0) {
            focusSize = dpToPx(76);
        }
        int indicatorWidth = exposureIndicatorView.getWidth();
        if (indicatorWidth <= 0) {
            indicatorWidth = dpToPx(42);
        }
        int indicatorHeight = exposureIndicatorView.getHeight();
        if (indicatorHeight <= 0) {
            indicatorHeight = dpToPx(128);
        }

        float targetX = previewView.getLeft() + focusX + focusSize / 2.0f + dpToPx(12);
        float targetY = previewView.getTop() + focusY - indicatorHeight / 2.0f;
        float maxX = previewView.getRight() - indicatorWidth - dpToPx(8);
        float minX = previewView.getLeft() + dpToPx(8);
        float maxY = previewView.getBottom() - indicatorHeight - dpToPx(8);
        float minY = previewView.getTop() + dpToPx(8);

        if (targetX > maxX) {
            targetX = previewView.getLeft() + focusX - focusSize / 2.0f - indicatorWidth - dpToPx(12);
        }
        if (targetX < minX) {
            targetX = minX;
        }
        if (targetY < minY) {
            targetY = minY;
        }
        if (targetY > maxY) {
            targetY = maxY;
        }

        exposureIndicatorView.animate().cancel();
        exposureIndicatorView.setVisibility(View.VISIBLE);
        exposureIndicatorView.setAlpha(1.0f);
        exposureIndicatorView.setX(targetX);
        exposureIndicatorView.setY(targetY);
        exposureIndicatorView.bringToFront();
        updateExposureIndicator(exposurePercent);
    }

    /**
     * 更新亮度百分比和竖向滑块位置。
     */
    private void updateExposureIndicator(int exposurePercent) {
        if (exposureTextView != null) {
            exposureTextView.setText(exposurePercent + "%");
        }
        if (exposureTrackView == null || exposureThumbView == null) {
            return;
        }

        int trackHeight = exposureTrackView.getHeight();
        if (trackHeight <= 0) {
            trackHeight = dpToPx(92);
        }
        int thumbHeight = exposureThumbView.getHeight();
        if (thumbHeight <= 0) {
            thumbHeight = dpToPx(5);
        }

        int percent = exposurePercent;
        if (percent < 0) {
            percent = 0;
        }
        if (percent > 100) {
            percent = 100;
        }

        float movableHeight = trackHeight - thumbHeight;
        float targetTranslationY = -movableHeight * percent / 100.0f;
        exposureThumbView.setTranslationY(targetTranslationY);
    }

    /**
     * 手指离开屏幕后结束聚焦框动画。
     */
    private void finishFocusAnimation() {
        if (focusIndicatorView == null || focusIndicatorView.getVisibility() != View.VISIBLE) {
            return;
        }
        int animationToken = ++focusAnimationToken;
        clearPendingFocus();
        focusIndicatorView.animate().cancel();
        focusIndicatorView.setAlpha(1.0f);
        if (exposureIndicatorView != null
                && exposureIndicatorView.getVisibility() == View.VISIBLE) {
            exposureIndicatorView.animate().cancel();
            exposureIndicatorView.setAlpha(1.0f);
        }
        fadeOutFocusIndicator(animationToken);
    }

    /**
     * 聚焦框定格后快速闪烁，用来提示聚焦动作完成。
     */
    private void startFocusBlinkAnimation(final int animationToken, boolean disappearAfterBlink) {
        blinkFocusIndicator(animationToken, 0, disappearAfterBlink);
    }

    /**
     * 执行单次聚焦框闪烁，连续闪烁 3 次。
     */
    private void blinkFocusIndicator(
            final int animationToken,
            final int blinkCount,
            final boolean disappearAfterBlink) {
        if (animationToken != focusAnimationToken || focusIndicatorView == null) {
            return;
        }
        if (blinkCount >= 3) {
            if (disappearAfterBlink) {
                fadeOutFocusIndicator(animationToken);
            } else {
                keepFocusIndicatorVisible(animationToken);
            }
            return;
        }
        if (blinkCount == 1) {
            triggerPendingFocus(animationToken);
        }

        focusIndicatorView.animate()
                .alpha(0.35f)
                .setStartDelay(blinkCount == 0 ? 260 : 0)
                .setDuration(55)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        if (animationToken != focusAnimationToken || focusIndicatorView == null) {
                            return;
                        }
                        focusIndicatorView.animate()
                                .alpha(1.0f)
                                .setStartDelay(0)
                                .setDuration(55)
                                .withEndAction(new Runnable() {
                                    @Override
                                    public void run() {
                                        blinkFocusIndicator(
                                                animationToken,
                                                blinkCount + 1,
                                                disappearAfterBlink
                                        );
                                    }
                                })
                                .start();
                    }
                })
                .start();
    }

    /**
     * 在聚焦框第 2 次闪烁时真正触发 CameraX 聚焦。
     */
    private void triggerPendingFocus(int animationToken) {
        if (operationCamera == null
                || pendingFocusTriggered
                || pendingFocusToken != animationToken) {
            return;
        }
        pendingFocusTriggered = true;
        operationCamera.focusAt(pendingFocusX, pendingFocusY);
    }

    /**
     * 手指没有离开屏幕时，聚焦框闪烁完成后保持显示。
     */
    private void keepFocusIndicatorVisible(int animationToken) {
        if (animationToken != focusAnimationToken || focusIndicatorView == null) {
            return;
        }
        focusIndicatorView.setVisibility(View.VISIBLE);
        focusIndicatorView.setAlpha(1.0f);
        focusDisappearing = false;
        if (exposureIndicatorView != null
                && exposureIndicatorView.getVisibility() == View.VISIBLE) {
            exposureIndicatorView.setAlpha(1.0f);
        }
    }

    /**
     * 聚焦框完成提示后淡出消失。
     */
    private void fadeOutFocusIndicator(final int animationToken) {
        if (animationToken != focusAnimationToken || focusIndicatorView == null) {
            return;
        }
        if (focusHoldingExisting) {
            keepFocusIndicatorVisible(animationToken);
            return;
        }
        focusDisappearing = true;
        focusIndicatorView.animate()
                .alpha(0.0f)
                .setStartDelay(180)
                .setDuration(180)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        if (animationToken != focusAnimationToken || focusIndicatorView == null) {
                            return;
                        }
                        focusIndicatorView.setVisibility(View.GONE);
                        focusDisappearing = false;
                    }
                })
                .start();
        if (exposureIndicatorView != null
                && exposureIndicatorView.getVisibility() == View.VISIBLE) {
            exposureIndicatorView.animate()
                    .alpha(0.0f)
                    .setStartDelay(180)
                    .setDuration(180)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            if (animationToken != focusAnimationToken || exposureIndicatorView == null) {
                                return;
                            }
                            exposureIndicatorView.setVisibility(View.GONE);
                        }
                    })
                    .start();
        }
    }

    /**
     * dp 转 px。
     */
    private int dpToPx(float dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
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
        if (operationCamera != null) {
            operationCamera.release();
        }
        operationCamera = new OperationCamera(this, this);
        //开启后，拍照保存完成会直接停留在刚拍好的照片画面。
        operationCamera.setStayOnCapturedPhoto(true);
        operationCamera.bindPhotoView(photoPreviewView);
        //开启后，停止录像保存完成会直接在当前页面播放刚录好的视频。
        operationCamera.setAutoPlayRecordedVideo(true);
        operationCamera.bindPlayerView(playerView);
        operationCamera.initView(this, previewView);
        updateCameraOptionButtons();
    }

    /**
     * 处理按钮点击。
     */
    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (operationCamera == null) {
            showMessage("相机还没有准备好");
            return;
        }
        if (id == R.id.camera_record) {
            updateResetPreviewButton(false);
            operationCamera.startRecording();
            statusText.setText("正在录像");
            updateButtonState(true);
        } else if (id == R.id.camera_pause) {
            if (recordingPaused) {
                operationCamera.resumeRecording();
                statusText.setText("正在录像");
                pauseButton.setText("暂停录像");
                recordingPaused = false;
            } else {
                operationCamera.pauseRecording();
                statusText.setText("录像已暂停");
                pauseButton.setText("继续录像");
                recordingPaused = true;
            }
        } else if (id == R.id.camera_stop) {
            updateResetPreviewButton(false);
            operationCamera.stopRecording();
            statusText.setText("正在保存录像");
            updateButtonState(false);
        } else if (id == R.id.camera_photo) {
            updateResetPreviewButton(false);
            operationCamera.takePhoto();
        } else if (id == R.id.camera_reset_preview) {
            operationCamera.resetToCameraPreview();
            statusText.setText("相机已准备好");
            updateButtonState(false);
            updateResetPreviewButton(false);
        } else if (id == R.id.camera_switch) {
            boolean switched = operationCamera.switchCamera();
            updateCameraOptionButtons();
            if (switched) {
                statusText.setText(operationCamera.isBackCamera() ? "已切换到后置摄像头" : "已切换到前置摄像头");
            }
        } else if (id == R.id.camera_flash) {
            boolean enabled = operationCamera.toggleFlash();
            updateCameraOptionButtons();
            if (operationCamera.hasFlashUnit()) {
                statusText.setText(enabled ? "闪光灯已开启" : "闪光灯已关闭");
            }
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
        switchCameraButton.setEnabled(!recording);
        flashButton.setEnabled(true);
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
            switchCameraButton.setEnabled(false);
            flashButton.setEnabled(false);
        }
    }

    /**
     * 更新切换摄像头和闪光灯按钮文案。
     */
    private void updateCameraOptionButtons() {
        if (operationCamera == null) {
            switchCameraButton.setText("切前置");
            flashButton.setText("开闪光");
            return;
        }
        switchCameraButton.setText(operationCamera.isBackCamera() ? "切前置" : "切后置");
        flashButton.setText(operationCamera.isFlashEnabled() ? "关闪光" : "开闪光");
    }

    /**
     * 相机初始化完成回调。
     */
    @Override
    public void onCameraReady() {
        statusText.setText("相机已准备好");
        updateButtonState(false);
        updateResetPreviewButton(false);
        updateCameraOptionButtons();
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
        boolean recording = operationCamera != null && operationCamera.isRecording();
        updateButtonState(recording);
        if (!recording) {
            updateResetPreviewButton(false);
        }
        updateCameraOptionButtons();
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
        if (operationCamera != null) {
            operationCamera.release();
            operationCamera = null;
        }
        super.onDestroy();
    }

    private ActivityResultLauncher<Intent> activityLauncher;

    private void setActivityLauncherIntent() {
        activityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null) {
                                String value = data.getStringExtra("key");
                                // 在这里处理返回的数据
                            }
                        }
                    }
                }
        );
        //启动
        //Intent intent = new Intent(this, TargetActivity.class);
        //intent.putExtra("name", "Tom");
        //activityLauncher.launch(intent);
        //返回
        //Intent resultIntent = new Intent();
        //resultIntent.putExtra("key", "返回的数据");
        //setResult(Activity.RESULT_OK, resultIntent);
        //finish();
    }
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private Uri photoUri;
    private void setActivityLauncherIntent2() {
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                new ActivityResultCallback<Boolean>() {
                    @Override
                    public void onActivityResult(Boolean success) {
                        if (success) {
                            // 拍照成功，照片在 photoUri 里
                            //imageView.setImageURI(photoUri);
                        } else {
                            // 用户取消或拍照失败
                        }
                    }
                }
        );
        //启动 拍照
        File photoFile = new File(
                getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "photo_" + System.currentTimeMillis() + ".jpg"
        );
        photoUri = FileProvider.getUriForFile(
                this,
                getPackageName() + ".fileprovider",
                photoFile
        );
        takePictureLauncher.launch(photoUri);
    }

    private ActivityResultLauncher<Uri> captureVideoLauncher;
    private Uri videoUri;
    private void setActivityLauncherIntent3() {
        captureVideoLauncher = registerForActivityResult(
                new ActivityResultContracts.CaptureVideo(),
                new ActivityResultCallback<Boolean>() {
                    @Override
                    public void onActivityResult(Boolean success) {
                        if (success) {
                            // 录制成功，视频地址就是 videoUri
                            // 比如播放：
                            //videoView.setVideoURI(videoUri);
                            //videoView.start();
                        } else {
                            // 用户取消或录制失败
                        }
                    }
                }
        );
        //启动录制
        File videoFile = new File(
                getExternalFilesDir(Environment.DIRECTORY_MOVIES),
                "video_" + System.currentTimeMillis() + ".mp4"
        );

        videoUri = FileProvider.getUriForFile(
                this,
                getPackageName() + ".fileprovider",
                videoFile
        );
        captureVideoLauncher.launch(videoUri);
    }

}


