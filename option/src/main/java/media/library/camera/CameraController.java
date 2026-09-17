package media.library.camera;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.FallbackStrategy;
import androidx.camera.video.MediaStoreOutputOptions;
import androidx.camera.video.PendingRecording;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoRecordEvent;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * CameraX 相机控制器。
 *
 * <p>负责相机预览、拍照、录像、暂停录像、继续录像、点击对焦和双指缩放。
 * Activity 只需要处理权限、按钮和结果回调。</p>
 */
public class CameraController {

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

    private final Context appContext;
    private final Callback callback;
    private final Executor mainExecutor;

    private ProcessCameraProvider cameraProvider;
    private Camera camera;
    private PreviewView previewView;
    private ImageCapture imageCapture;
    private VideoCapture<Recorder> videoCapture;
    private Recording recording;

    /**
     * 创建相机控制器。
     */
    public CameraController(Context context, Callback callback) {
        appContext = context.getApplicationContext();
        this.callback = callback;
        mainExecutor = ContextCompat.getMainExecutor(appContext);
    }

    /**
     * 绑定相机预览和拍照录像用例。
     */
    public void bind(final LifecycleOwner lifecycleOwner, final PreviewView view) {
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
        final ListenableFuture<ProcessCameraProvider> providerFuture =
                ProcessCameraProvider.getInstance(appContext);
        providerFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    cameraProvider = providerFuture.get();
                    bindUseCases(lifecycleOwner);
                } catch (Exception e) {
                    notifyError("初始化相机失败", e);
                }
            }
        }, mainExecutor);
    }

    /**
     * 绑定相机用例。
     */
    private void bindUseCases(LifecycleOwner lifecycleOwner) {
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
            notifyReady();
        } catch (Exception e) {
            notifyError("绑定相机失败", e);
        }
    }

    /**
     * 拍照并保存到系统相册。
     */
    public void takePhoto() {
        if (imageCapture == null) {
            notifyError("相机还没有准备好", null);
            return;
        }

        ContentValues contentValues = createPhotoContentValues();
        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(
                        appContext.getContentResolver(),
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        contentValues
                ).build();

        imageCapture.takePicture(
                outputOptions,
                mainExecutor,
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(
                            @NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Uri savedUri = outputFileResults.getSavedUri();
                        if (callback != null) {
                            callback.onPhotoSaved(savedUri);
                        }
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        notifyError("拍照失败", exception);
                    }
                }
        );
    }

    /**
     * 开始录像。
     *
     * <p>如果录音权限没有授予，会继续录制无声音视频。</p>
     */
    public void startRecording() {
        if (videoCapture == null) {
            notifyError("相机还没有准备好", null);
            return;
        }
        if (recording != null) {
            notifyError("当前已经在录像", null);
            return;
        }

        ContentValues contentValues = createVideoContentValues();
        MediaStoreOutputOptions outputOptions =
                new MediaStoreOutputOptions.Builder(
                        appContext.getContentResolver(),
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                ).setContentValues(contentValues).build();
        PendingRecording pendingRecording =
                videoCapture.getOutput().prepareRecording(appContext, outputOptions);

        if (ContextCompat.checkSelfPermission(appContext, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            pendingRecording = pendingRecording.withAudioEnabled();
        }

        recording = pendingRecording.start(
                mainExecutor,
                new androidx.core.util.Consumer<VideoRecordEvent>() {
                    @Override
                    public void accept(VideoRecordEvent event) {
                        if (event instanceof VideoRecordEvent.Finalize) {
                            VideoRecordEvent.Finalize finalizeEvent =
                                    (VideoRecordEvent.Finalize) event;
                            Uri outputUri = finalizeEvent.getOutputResults().getOutputUri();
                            recording = null;
                            if (finalizeEvent.hasError()) {
                                notifyError("录像失败，错误码：" + finalizeEvent.getError(), null);
                            } else if (callback != null) {
                                callback.onVideoSaved(outputUri);
                            }
                        }
                    }
                }
        );
    }

    /**
     * 暂停录像。
     */
    public void pauseRecording() {
        if (recording != null) {
            recording.pause();
        }
    }

    /**
     * 继续录像。
     */
    public void resumeRecording() {
        if (recording != null) {
            recording.resume();
        }
    }

    /**
     * 停止录像并保存文件。
     */
    public void stopRecording() {
        if (recording != null) {
            recording.stop();
        }
    }

    /**
     * 判断当前是否正在录像。
     */
    public boolean isRecording() {
        return recording != null;
    }

    /**
     * 点击预览画面时进行自动对焦和曝光测光。
     */
    public void focusAt(float x, float y) {
        if (camera == null || previewView == null) {
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
     * 根据双指手势缩放镜头。
     */
    public void zoomByScale(float scaleFactor) {
        if (camera == null || scaleFactor <= 0) {
            return;
        }
        androidx.lifecycle.LiveData<androidx.camera.core.ZoomState> zoomState =
                camera.getCameraInfo().getZoomState();
        androidx.camera.core.ZoomState state = zoomState.getValue();
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
     * 释放相机和录像资源。
     */
    public void release() {
        if (recording != null) {
            recording.stop();
            recording = null;
        }
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
            cameraProvider = null;
        }
        camera = null;
        imageCapture = null;
        videoCapture = null;
        previewView = null;
    }

    /**
     * 创建照片写入系统相册需要的媒体信息。
     */
    private ContentValues createPhotoContentValues() {
        long now = System.currentTimeMillis();
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, "IMG_" + now + ".jpg");
        values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.DATE_TAKEN, now);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + "/Mediaaar"
            );
        }
        return values;
    }

    /**
     * 创建视频写入系统相册需要的媒体信息。
     */
    private ContentValues createVideoContentValues() {
        long now = System.currentTimeMillis();
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, "VID_" + now + ".mp4");
        values.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
        values.put(MediaStore.Video.Media.DATE_TAKEN, now);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_MOVIES + "/Mediaaar"
            );
        }
        return values;
    }

    /**
     * 通知相机初始化完成。
     */
    private void notifyReady() {
        if (callback != null) {
            callback.onCameraReady();
        }
    }

    /**
     * 通知相机错误。
     */
    private void notifyError(String message, Throwable throwable) {
        if (callback != null) {
            callback.onCameraError(message, throwable);
        }
    }
}
