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
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
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

/**
 * CameraX 相机控制器。
 *
 * <p>负责相机预览、拍照、录像、暂停录像、继续录像、点击对焦和双指缩放。
 * Activity 只需要处理权限、按钮和结果回调。</p>
 */
public class CameraController extends BaseCameraController {

    private ImageCapture imageCapture;
    private VideoCapture<Recorder> videoCapture;
    private Recording recording;

    /**
     * 创建相机控制器。
     */
    public CameraController(Context context, Callback callback) {
        super(context, callback);
    }

    /**
     * 绑定相机预览和拍照录像用例。
     */
    @Override
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
            notifyReady();
        } catch (Exception e) {
            notifyError("绑定相机失败", e);
        }
    }

    /**
     * 拍照并保存到系统相册。
     */
    @Override
    public void takePhoto() {
        if (released) {
            return;
        }
        if (imageCapture == null) {
            notifyError("相机还没有准备好", null);
            return;
        }
        stopVideoPlayback();
        stopPhotoPreview();

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
                        if (released) {
                            return;
                        }
                        Uri savedUri = outputFileResults.getSavedUri();
                        if (callback != null) {
                            callback.onPhotoSaved(savedUri);
                        }
                        showCapturedPhotoIfNeeded(savedUri);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        if (released) {
                            return;
                        }
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
    @Override
    public void startRecording() {
        if (released) {
            return;
        }
        if (videoCapture == null) {
            notifyError("相机还没有准备好", null);
            return;
        }
        if (recording != null) {
            notifyError("当前已经在录像", null);
            return;
        }
        stopVideoPlayback();
        stopPhotoPreview();

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
                            if (released) {
                                recording = null;
                                return;
                            }
                            VideoRecordEvent.Finalize finalizeEvent =
                                    (VideoRecordEvent.Finalize) event;
                            Uri outputUri = finalizeEvent.getOutputResults().getOutputUri();
                            recording = null;
                            if (finalizeEvent.hasError()) {
                                notifyError("录像失败，错误码：" + finalizeEvent.getError(), null);
                            } else {
                                if (callback != null) {
                                    callback.onVideoSaved(outputUri);
                                }
                                playRecordedVideoIfNeeded(outputUri);
                            }
                        }
                    }
                }
        );
    }

    /**
     * 暂停录像。
     */
    @Override
    public void pauseRecording() {
        if (!released && recording != null) {
            recording.pause();
        }
    }

    /**
     * 继续录像。
     */
    @Override
    public void resumeRecording() {
        if (!released && recording != null) {
            recording.resume();
        }
    }

    /**
     * 停止录像并保存文件。
     */
    @Override
    public void stopRecording() {
        if (!released && recording != null) {
            recording.stop();
        }
    }

    /**
     * 判断当前是否正在录像。
     */
    @Override
    public boolean isRecording() {
        return recording != null;
    }

    /**
     * 释放相机和录像资源。
     */
    @Override
    protected void releaseControllerResources() {
        if (recording != null) {
            recording.stop();
            recording = null;
        }
        imageCapture = null;
        videoCapture = null;
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

}
