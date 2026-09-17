package media.library.camera;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.Preview;
import androidx.camera.core.ZoomState;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.FallbackStrategy;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.VideoCapture;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

/**
 * 相机控制器基础类。
 *
 * <p>统一管理页面预览控件、照片停留控件、视频回放控件、相机对象和结果回调。
 * 子类只需要关心具体的拍照、录像和 CameraX 用例绑定逻辑。</p>
 * 播放视频
 */

public abstract class BaseCamera {
    protected final Context appContext;
    //这是一个主线程（UI 线程）执行器，调用 `mainExecutor.execute(Runnable)`，
    // 里面的 Runnable 会跑在 Android 主线程，可以直接更新 UI
    protected final Executor mainExecutor;
    //回调
    private final Callback callback;



    public BaseCamera(Context context, Callback callback) {
        appContext = context.getApplicationContext();
        this.callback = callback;
        mainExecutor = ContextCompat.getMainExecutor(appContext);
    }


    //相机的预览器
    private PreviewView previewView;
    //当前绑定相机的生命周期对象
    private LifecycleOwner boundLifecycleOwner;
    //已释放
    protected boolean released;
    //相机预览是否已经暂停
    private boolean cameraPreviewPaused;
    //闪光灯是否开启
    private boolean flashEnabled;
    //** Jetpack CameraX 的相机管理器，是整个相机能力的入口类**，
    // 用来管理手机摄像头硬件、绑定相机用例（预览、拍照、图像分析），
    // 并且**自动绑定 Activity/Fragment 生命周期**，不用手动写`onResume`/`onPause`启停相机
    private ProcessCameraProvider cameraProvider;

    /**
     * 绑定相机预览和拍照录像用例。
     */

    public void bind(final LifecycleOwner lifecycleOwner, final PreviewView view) {
        if (released) {
            return;
        }
        if (lifecycleOwner == null || view == null) {
            notifyError("相机页面参数为空", null);
            return;
        }
        if (ContextCompat.checkSelfPermission(appContext, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            notifyError("没有相机权限", null);
            return;
        }

        previewView = view;
        boundLifecycleOwner = lifecycleOwner;
        final ListenableFuture<ProcessCameraProvider> providerFuture =
                ProcessCameraProvider.getInstance(appContext);
        providerFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    if (released) {
                        return;
                    }
                    cameraProvider = providerFuture.get();
                    bindUseCases(boundLifecycleOwner);
                } catch (Exception e) {
                    notifyError("初始化相机失败", e);
                }
            }
        }, mainExecutor);
    }

    //是 **CameraX 的拍照用例 (UseCase)**，专门负责**捕获静态照片**
    protected ImageCapture imageCapture;
    // 负责接收相机视频流，把画面交给 Recorder
    protected VideoCapture<Recorder> videoCapture;
    //- 开启 / 关闭闪光灯
    //- 双指缩放 zoom
    //- 点击对焦、测光
    //- 调节曝光补偿
    //- 判断是否有闪光灯
    //- 获取当前相机朝向（前置 / 后置）
    //- 监听相机状态（打开、关闭、错误）
    private Camera camera;

    /**
     * 绑定相机用例。显示视图
     */
    private void bindUseCases(LifecycleOwner lifecycleOwner) {
        if (released) {
            return;
        }
        if (cameraProvider == null || previewView == null) {
            notifyError("相机未准备好", null);
            return;
        }

        cameraProvider.unbindAll();

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();

        QualitySelector qualitySelector = QualitySelector.fromOrderedList(
                Arrays.asList(Quality.FHD, Quality.HD, Quality.SD),
                FallbackStrategy.lowerQualityOrHigherThan(Quality.SD)
        );
        Recorder recorder = new Recorder.Builder()
                .setQualitySelector(qualitySelector)
                .build();
        videoCapture = VideoCapture.withOutput(recorder);

        try {
            camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture,
                    videoCapture
            );
            cameraPreviewPaused = false;
            applyFlashStateAfterBind();
            notifyReady();
        } catch (Exception e) {
            notifyError("绑定相机失败", e);
        }
    }

    /**
     * 暂停相机预览并释放 CameraX 用例。
     *
     * <p>这个方法不会释放整个控制器，只是临时解绑相机硬件。
     * 适合录像完成后在当前页面播放视频时使用。</p>
     */
    protected void pauseCameraPreview() {
        if (released || cameraPreviewPaused) {
            return;
        }
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
        camera = null;
        imageCapture = null;
        videoCapture = null;
        cameraPreviewPaused = true;
    }

    /**
     * 恢复之前暂停的相机预览。
     */
    protected void resumeCameraPreview() {
        if (released || !cameraPreviewPaused) {
            return;
        }
        if (cameraProvider == null || boundLifecycleOwner == null || previewView == null) {
            notifyError("相机预览不能恢复", null);
            return;
        }
        bindUseCases(boundLifecycleOwner);
    }

    /**
     * 切换闪光灯开关。
     */
    public boolean toggleFlash() {
        return setFlashEnabled(!flashEnabled);
    }

    /**
     * 设置闪光灯开关。
     *
     * <p>这里同时处理两件事：
     * 1. 通过 CameraControl 控制预览/录像时的常亮补光灯；
     * 2. 通过 ImageCapture 同步拍照时的闪光模式。</p>
     */
    public boolean setFlashEnabled(boolean enabled) {
        if (released) {
            return false;
        }
        if (camera == null) {
            notifyError("相机还没有准备好", null);
            return false;
        }
        if (!camera.getCameraInfo().hasFlashUnit()) {
            flashEnabled = false;
            applyImageCaptureFlashMode(false);
            notifyError("当前摄像头没有闪光灯", null);
            return false;
        }
        flashEnabled = enabled;
        camera.getCameraControl().enableTorch(enabled);
        applyImageCaptureFlashMode(enabled);
        return flashEnabled;
    }

    /**
     * 判断闪光灯是否开启。
     */
    public boolean isFlashEnabled() {
        return flashEnabled;
    }

    /**
     * 判断当前相机是否支持闪光灯。
     */
    public boolean hasFlashUnit() {
        return camera != null && camera.getCameraInfo().hasFlashUnit();
    }

    /**
     * 相机重新绑定后恢复闪光灯状态。
     */
    private void applyFlashStateAfterBind() {
        if (camera == null) {
            return;
        }
        if (!camera.getCameraInfo().hasFlashUnit()) {
            flashEnabled = false;
            applyImageCaptureFlashMode(false);
            return;
        }
        camera.getCameraControl().enableTorch(flashEnabled);
        applyImageCaptureFlashMode(flashEnabled);
    }

    /**
     * 同步拍照用例的闪光灯模式。
     */
    private void applyImageCaptureFlashMode(boolean enabled) {
        if (imageCapture == null) {
            return;
        }
        imageCapture.setFlashMode(enabled
                ? ImageCapture.FLASH_MODE_ON
                : ImageCapture.FLASH_MODE_OFF);
    }

    /**
     * 根据双指手势缩放镜头。
     */
    public void zoomByScale(float scaleFactor) {
        if (released || camera == null || scaleFactor <= 0) {
            return;
        }
        androidx.lifecycle.LiveData<ZoomState> zoomState = camera.getCameraInfo().getZoomState();
        ZoomState state = zoomState.getValue();
        if (state == null) {
            return;
        }
        float targetZoom = state.getZoomRatio() * scaleFactor;
        float minZoom = state.getMinZoomRatio();
        float maxZoom = state.getMaxZoomRatio();
        if (targetZoom < minZoom) {
            targetZoom = minZoom;
        }
        if (targetZoom > maxZoom) {
            targetZoom = maxZoom;
        }
        camera.getCameraControl().setZoomRatio(targetZoom);
    }

    /**
     * 根据用户点击的预览坐标进行自动对焦和测光。聚焦
     */
    public void focusAt(float x, float y) {
        if (released || camera == null || previewView == null || cameraPreviewPaused) {
            return;
        }
        if (x < 0 || y < 0 || x > previewView.getWidth() || y > previewView.getHeight()) {
            return;
        }

        MeteringPoint point = previewView.getMeteringPointFactory().createPoint(x, y);
        FocusMeteringAction action = new FocusMeteringAction.Builder(
                point,
                FocusMeteringAction.FLAG_AF | FocusMeteringAction.FLAG_AE
        ).setAutoCancelDuration(5, TimeUnit.SECONDS).build();
        camera.getCameraControl().startFocusAndMetering(action);
    }

    /**
     * 释放相机
     */
    public final void release() {
        if (released) {
            return;
        }
        released = true;
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
            cameraProvider = null;
        }
        camera = null;
        previewView = null;
        boundLifecycleOwner = null;
        imageCapture = null;
        videoCapture = null;
        flashEnabled = false;
        cameraPreviewPaused = false;

    }

    /**
     * 通知相机初始化完成。
     */
    private void notifyReady() {
        if (!released && callback != null) {
            callback.onCameraReady();
        }
    }

    /**
     * 通知相机错误。
     */
    protected void notifyError(String message, Throwable throwable) {
        if (!released && callback != null) {
            callback.onCameraError(message, throwable);
        }
    }
    /**
     * 相机结果回调。
     */
    public interface Callback {
        /**
         * 相机初始化完成。
         */
        void onCameraReady();

        /**
         * 拍照完成。
         */
        void onPhotoSaved(Uri uri);

        /**
         * 录像完成。
         */
        void onVideoSaved(Uri uri);

        /**
         * 相机发生错误。
         */
        void onCameraError(String message, Throwable throwable);
    }

}
