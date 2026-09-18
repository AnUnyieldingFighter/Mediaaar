package media.library.camera;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Range;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExposureState;
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
    //LifecycleOwner 的作用是：提供生命周期。
    //Activity 如果当前类是 AppCompatActivity，就可以直接传：this
    //Fragment 传 getViewLifecycleOwner()
    private LifecycleOwner boundLifecycleOwner;
    //true 已销毁/不可再用
    protected boolean released;
    //相机预览是否已经暂停 true 暂停预览
    private boolean cameraPreviewPaused;

    //** Jetpack CameraX 的相机管理器，是整个相机能力的入口类**，
    // 用来管理手机摄像头硬件、绑定相机用例（预览、拍照、图像分析），
    // 并且**自动绑定 Activity/Fragment 生命周期**，不用手动写`onResume`/`onPause`启停相机
    private ProcessCameraProvider cameraProvider;

    /**
     * 绑定相机预览和拍照录像用例。
     */

    public void initView(final LifecycleOwner lifecycleOwner, final PreviewView view) {
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
        //ListenableFuture 异步任务
        final ListenableFuture<ProcessCameraProvider> providerFuture =
                ProcessCameraProvider.getInstance(appContext);
        //给这个异步任务加**完成监听器**：等相机管理器准备好之后，执行 run 里面代码。
        providerFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    if (released) {
                        return;
                    }
                    cameraProvider = providerFuture.get();
                    initCamera(boundLifecycleOwner);
                } catch (Exception e) {
                    notifyError("初始化相机失败", e);
                }
            }
        }, mainExecutor);
    }

    //拍照用
    protected ImageCapture imageCapture;
    //录制视频用
    protected VideoCapture<Recorder> videoCapture;

    private Camera camera;

    /**
     * 初始化相机：显示视图
     * Camera:
     * - 开启 / 关闭闪光灯
     * - 双指缩放 zoom
     * - 点击对焦、测光
     * - 调节曝光补偿
     * - 判断是否有闪光灯
     * - 获取当前相机朝向（前置 / 后置）
     * - 监听相机状态（打开、关闭、错误）
     * VideoCapture<Recorder> :
     * 录制视频用 负责接收相机视频流，把画面交给 Recorder
     * ImageCapture ：
     * 拍照用 **CameraX 的拍照用例 (UseCase)**，专门负责**捕获静态照片**
     *
     */
    private void initCamera(LifecycleOwner lifecycleOwner) {
        if (released) {
            return;
        }
        if (cameraProvider == null || previewView == null) {
            notifyError("相机未准备好", null);
            return;
        }

        cameraProvider.unbindAll();
        // 创建预览配置对象
        Preview preview = new Preview.Builder().build();
        // 把预览画面的数据源绑定到预览控件 PreviewView
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        //ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY 表示：拍照速度优先，尽可能快地完成拍摄。
        //ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY 表示：拍好点 画质优先，可能会更慢，比如做更多图像处理、降噪、优化清晰度等。
        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();
        //**CameraX 预览画质选择器**：优先按顺序尝试 FHD → HD → SD；
        // 如果连 SD 都不支持，就自动选**比 SD 更低**的可用分辨率，保证预览一定能打开。
        //Quality.UHD：4K
        //Quality.FHD：1080p
        //Quality.HD：720p
        //Quality.SD：480p
        QualitySelector qualitySelector = QualitySelector.fromOrderedList(
                Arrays.asList(Quality.FHD, Quality.HD, Quality.SD),
                FallbackStrategy.lowerQualityOrHigherThan(Quality.SD)
        );
        //Recorder 创建一个录像器，录像质量按 qualitySelector 指定的规则来
        Recorder recorder = new Recorder.Builder()
                .setQualitySelector(qualitySelector)
                .build();
        videoCapture = VideoCapture.withOutput(recorder);

        try {
            //：**打开后置摄像头，同时绑定预览、拍照、录视频三个功能用例，
            // 并且把相机的开关交给生命周期自动管理；返回的 Camera 对象代表当前打开的相机实例。**
            //CameraSelector.DEFAULT_BACK_CAMERA 后置摄像头
            //CameraSelector.DEFAULT_FRONT_CAMERA 前置摄像头
            camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,//实时预览画面（显示到 PreviewView）
                    imageCapture,//拍照
                    videoCapture//视频录制
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
        initCamera(boundLifecycleOwner);
    }

    //闪光灯是否开启
    private boolean flashEnabled;

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
        //判断当前设置有没有 闪光灯
        if (!camera.getCameraInfo().hasFlashUnit()) {
            flashEnabled = false;
            applyImageCaptureFlashMode(false);
            return;
        }
        //Torch enableTorch(true) 打开手电筒，灯会一直亮，直到你调用：enableTorch(false)
        camera.getCameraControl().enableTorch(flashEnabled);
        applyImageCaptureFlashMode(flashEnabled);
    }

    /**
     * 同步拍照用例的闪光灯模式。
     * enabled false:设置为 FLASH_MODE_OFF，拍照不闪光
     * true ：设置为 FLASH_MODE_ON，拍照瞬间闪光
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
     * > 1：放大
     * < 1：缩小
     * = 1：不变
     */
    public void zoomByScale(float scaleFactor) {
        if (released || camera == null || scaleFactor <= 0) {
            return;
        }
        //ZoomState 里有当前缩放倍数、最小缩放倍数、最大缩放倍数。
        androidx.lifecycle.LiveData<ZoomState> zoomState = camera.getCameraInfo().getZoomState();
        ZoomState state = zoomState.getValue();
        if (state == null) {
            return;
        }
        //计算目标缩放倍数。
        float targetZoom = state.getZoomRatio() * scaleFactor;
        float minZoom = state.getMinZoomRatio();
        float maxZoom = state.getMaxZoomRatio();
        //最小缩放
        if (targetZoom < minZoom) {
            targetZoom = minZoom;
        }
        //最大缩放
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
        // 创建对焦/测光动作：
        //FLAG_AF：自动对焦，focus
        //FLAG_AE：自动曝光，exposure
        //FLAG_AF | FLAG_AE：同时对焦和测光
        //setAutoCancelDuration(5, TimeUnit.SECONDS)：5 秒后自动取消这次指定区域对焦/测光，恢复正常自动模式
        MeteringPoint point = previewView.getMeteringPointFactory().createPoint(x, y);
        FocusMeteringAction action = new FocusMeteringAction.Builder(
                point,
                FocusMeteringAction.FLAG_AF | FocusMeteringAction.FLAG_AE
        ).setAutoCancelDuration(5, TimeUnit.SECONDS).build();
        //让相机开始对焦和测光。
        camera.getCameraControl().startFocusAndMetering(action);
    }

    /**
     * 获取当前曝光补偿值。
     */
    public int getExposureCompensationIndex() {
        if (released || camera == null || cameraPreviewPaused) {
            return 0;
        }
        //是否支持曝光
        ExposureState exposureState = camera.getCameraInfo().getExposureState();
        if (exposureState == null || !exposureState.isExposureCompensationSupported()) {
            return 0;
        }
        //返回曝光值
        return exposureState.getExposureCompensationIndex();
    }


    /**
     * 根据手指上下滑动调节曝光亮度。
     * <p>手指向上滑动时画面变亮，向下滑动时画面变暗。</p>
     *
     * @param startY             手指开始拖动时的 Y 坐标
     * @param currentY           当前手指位置的 Y 坐标
     * @param viewHeight         预览区域高度
     * @param startExposureIndex 开始拖动时的曝光补偿值
     * @return
     */
    public int setExposureByVerticalDrag(float startY, float currentY,
                                         int viewHeight,
                                         int startExposureIndex) {
        if (released || camera == null || cameraPreviewPaused || viewHeight <= 0) {
            return getExposureCompensationIndex();
        }

        ExposureState exposureState = camera.getCameraInfo().getExposureState();
        if (exposureState == null || !exposureState.isExposureCompensationSupported()) {
            return getExposureCompensationIndex();
        }
        //表示一个整数区间，比如：[-12, 12] 意思是曝光补偿值最小可以设为 -12，最大可以设为 12。
        Range<Integer> range = exposureState.getExposureCompensationRange();
        if (range == null) {
            return getExposureCompensationIndex();
        }

        int minIndex = range.getLower();
        int maxIndex = range.getUpper();
        int totalRange = maxIndex - minIndex;
        if (totalRange <= 0) {
            return getExposureCompensationIndex();
        }

        float dragPercent = (startY - currentY) / Math.max(1.0f, viewHeight * 0.6f);
        int targetIndex = startExposureIndex + Math.round(dragPercent * totalRange);
        if (targetIndex < minIndex) {
            targetIndex = minIndex;
        }
        if (targetIndex > maxIndex) {
            targetIndex = maxIndex;
        }

        camera.getCameraControl().setExposureCompensationIndex(targetIndex);
        return targetIndex;
    }

    /**
     * 获取最小曝光补偿值。
     */
    public int getMinExposureCompensationIndex() {
        if (released || camera == null || cameraPreviewPaused) {
            return 0;
        }
        ExposureState exposureState = camera.getCameraInfo().getExposureState();
        if (exposureState == null || !exposureState.isExposureCompensationSupported()) {
            return 0;
        }
        Range<Integer> range = exposureState.getExposureCompensationRange();
        if (range == null) {
            return 0;
        }
        return range.getLower();
    }

    /**
     * 获取最大曝光补偿值。
     */
    public int getMaxExposureCompensationIndex() {
        if (released || camera == null || cameraPreviewPaused) {
            return 0;
        }
        ExposureState exposureState = camera.getCameraInfo().getExposureState();
        if (exposureState == null || !exposureState.isExposureCompensationSupported()) {
            return 0;
        }
        Range<Integer> range = exposureState.getExposureCompensationRange();
        if (range == null) {
            return 0;
        }
        return range.getUpper();
    }

    /**
     * 把曝光补偿值转换成 0 到 100 的亮度百分比。
     */
    public int getExposurePercent(int exposureIndex) {
        int minIndex = getMinExposureCompensationIndex();
        int maxIndex = getMaxExposureCompensationIndex();
        int totalRange = maxIndex - minIndex;
        if (totalRange <= 0) {
            return 50;
        }
        int percent = Math.round((exposureIndex - minIndex) * 100.0f / totalRange);
        if (percent < 0) {
            percent = 0;
        }
        if (percent > 100) {
            percent = 100;
        }
        return percent;
    }

    /**
     * 释放相机(一次性)
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
