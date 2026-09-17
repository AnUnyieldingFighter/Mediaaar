package media.library.camera;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.video.MediaStoreOutputOptions;
import androidx.camera.video.PendingRecording;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoRecordEvent;
import androidx.core.content.ContextCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

/**
 * CameraX 相机操作类。
 *
 * <p>继承 BaseCamera，复用父类已有的相机绑定、缩放和释放能力。
 * 本类只补充拍照、录像、照片停留、视频回放等操作。</p>
 */
public class OperationCamera extends BaseCamera {

    private final Callback operationCallback;

    //=========================初始化和页面控件

    /**
     * 创建相机操作类。
     */
    public OperationCamera(Context context, Callback callback) {
        super(context, callback);
        operationCallback = callback;
    }


    /**
     * 恢复到相机初始预览状态。
     */
    public void resetToCameraPreview() {
        if (released) {
            return;
        }
        stopVideoPlayback();
        stopPhotoPreview();
        resumeCameraPreview();
    }

    //=========================拍照============================
    private ImageView photoView;
    private boolean stayOnCapturedPhoto;

    /**
     * 绑定当前页面用于展示拍照结果的图片控件。
     */
    public void bindPhotoView(ImageView view) {
        if (released) {
            return;
        }
        photoView = view;
        if (photoView != null) {
            photoView.setVisibility(View.GONE);
        }
    }

    /**
     * 设置拍照完成后是否停留在刚拍好的照片画面。
     */
    public void setStayOnCapturedPhoto(boolean stayOnCapturedPhoto) {
        if (released) {
            return;
        }
        this.stayOnCapturedPhoto = stayOnCapturedPhoto;
        if (!stayOnCapturedPhoto) {
            stopPhotoPreview();
        }
    }

    /**
     * 拍照并保存到系统相册。
     */
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
                        if (operationCallback != null) {
                            operationCallback.onPhotoSaved(savedUri);
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
     * 如果开关打开，就在当前页面展示刚拍好的照片。
     */
    private void showCapturedPhotoIfNeeded(Uri photoUri) {
        if (!stayOnCapturedPhoto || photoUri == null || photoView == null) {
            return;
        }
        stopVideoPlayback();
        photoView.setImageURI(photoUri);
        photoView.setVisibility(View.VISIBLE);
    }

    /**
     * 关闭当前页面的照片停留画面。
     */
    private void stopPhotoPreview() {
        if (photoView != null) {
            photoView.setImageURI(null);
            photoView.setVisibility(View.GONE);
        }
    }

    //=========================录像===============================
    private Recording recording;
    private boolean autoPlayRecordedVideo;


    /**
     * 设置录像完成后是否在当前页面循环播放刚录制的视频。
     */
    public void setAutoPlayRecordedVideo(boolean autoPlayRecordedVideo) {
        if (released) {
            return;
        }
        this.autoPlayRecordedVideo = autoPlayRecordedVideo;
        if (!autoPlayRecordedVideo) {
            stopVideoPlayback();
            resumeCameraPreview();
        }
    }


    /**
     * 开始录像。
     *
     * <p>如果录音权限没有授予，会继续录制无声音视频。</p>
     */
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
                                if (operationCallback != null) {
                                    operationCallback.onVideoSaved(outputUri);
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
    public void pauseRecording() {
        if (!released && recording != null) {
            recording.pause();
        }
    }

    /**
     * 继续录像。
     */
    public void resumeRecording() {
        if (!released && recording != null) {
            recording.resume();
        }
    }

    /**
     * 停止录像并保存文件。
     */
    public void stopRecording() {
        if (!released && recording != null) {
            recording.stop();
        }
    }

    /**
     * 判断当前是否正在录像。
     */
    public boolean isRecording() {
        return recording != null;
    }


    //=========================摄像头选项=========================

    /**
     * BaseCamera 未开放 CameraProvider / CameraSelector，当前类不能安全切换前后摄像头。
     */
    public boolean switchCamera() {
        notifyError("BaseCamera 未开放摄像头切换能力", null);
        return false;
    }

    /**
     * 当前 BaseCamera 固定使用后置摄像头。
     */
    public boolean isBackCamera() {
        return true;
    }

    //=========================视频回放==========================
    private PlayerView playerView;
    private ExoPlayer videoPlayer;

    /**
     * 绑定当前页面的视频播放器控件。
     */
    public void bindPlayerView(PlayerView view) {
        if (released) {
            return;
        }
        playerView = view;
        if (playerView != null) {
            playerView.setVisibility(View.GONE);
            playerView.setPlayer(getOrCreateVideoPlayer());
        }
    }

    /**
     * 如果开关打开，就在当前页面循环播放刚录制完成的视频。
     */
    private void playRecordedVideoIfNeeded(Uri videoUri) {
        if (!autoPlayRecordedVideo || videoUri == null || playerView == null) {
            return;
        }
        stopPhotoPreview();
        pauseCameraPreview();
        playerView.setVisibility(View.VISIBLE);
        ExoPlayer player = getOrCreateVideoPlayer();
        player.setRepeatMode(Player.REPEAT_MODE_ONE);
        player.setMediaItem(MediaItem.fromUri(videoUri));
        player.prepare();
        player.play();
    }

    /**
     * 停止当前页面的视频回放。
     */
    private void stopVideoPlayback() {
        if (videoPlayer != null) {
            videoPlayer.stop();
            videoPlayer.clearMediaItems();
            videoPlayer.setRepeatMode(Player.REPEAT_MODE_OFF);
        }
        if (playerView != null) {
            playerView.setVisibility(View.GONE);
        }
    }

    /**
     * 获取或创建当前页面使用的视频播放器。
     */
    private ExoPlayer getOrCreateVideoPlayer() {
        if (videoPlayer == null) {
            videoPlayer = new ExoPlayer.Builder(appContext).build();
        }
        return videoPlayer;
    }

    //=========================相册保存参数 照片========================

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
    //=========================相册保存参数 视频========================
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
