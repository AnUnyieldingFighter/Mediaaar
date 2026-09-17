package media.library.camera;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;

import androidx.camera.core.Camera;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.ZoomState;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * 相机控制器基础类。
 *
 * <p>统一管理页面预览控件、照片停留控件、视频回放控件、相机对象和结果回调。
 * 子类只需要关心具体的拍照、录像和 CameraX 用例绑定逻辑。</p>
 */
public abstract class BaseCameraController {

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

    protected final Context appContext;
    protected final Callback callback;
    protected final Executor mainExecutor;

    protected ProcessCameraProvider cameraProvider;
    protected Camera camera;
    protected PreviewView previewView;
    protected ImageView photoView;
    protected PlayerView playerView;
    protected ExoPlayer videoPlayer;
    protected boolean stayOnCapturedPhoto;
    protected boolean autoPlayRecordedVideo;

    /**
     * 创建基础相机控制器。
     */
    public BaseCameraController(Context context, Callback callback) {
        appContext = context.getApplicationContext();
        this.callback = callback;
        mainExecutor = ContextCompat.getMainExecutor(appContext);
    }

    /**
     * 绑定相机预览和拍照录像用例。
     */
    public abstract void bind(LifecycleOwner lifecycleOwner, PreviewView view);

    /**
     * 拍照并保存。
     */
    public abstract void takePhoto();

    /**
     * 开始录像。
     */
    public abstract void startRecording();

    /**
     * 暂停录像。
     */
    public abstract void pauseRecording();

    /**
     * 继续录像。
     */
    public abstract void resumeRecording();

    /**
     * 停止录像。
     */
    public abstract void stopRecording();

    /**
     * 判断当前是否正在录像。
     */
    public abstract boolean isRecording();

    /**
     * 释放相机资源。
     */
    public abstract void release();

    /**
     * 设置录像完成后是否在当前页面自动循环播放刚录制的视频。
     */
    public void setAutoPlayRecordedVideo(boolean autoPlayRecordedVideo) {
        this.autoPlayRecordedVideo = autoPlayRecordedVideo;
        if (!autoPlayRecordedVideo) {
            stopVideoPlayback();
        }
    }

    /**
     * 设置拍照完成后是否停留在刚拍好的照片画面。
     */
    public void setStayOnCapturedPhoto(boolean stayOnCapturedPhoto) {
        this.stayOnCapturedPhoto = stayOnCapturedPhoto;
        if (!stayOnCapturedPhoto) {
            stopPhotoPreview();
        }
    }

    /**
     * 绑定当前页面用于展示拍照结果的图片控件。
     */
    public void bindPhotoView(ImageView view) {
        photoView = view;
        if (photoView != null) {
            photoView.setVisibility(View.GONE);
        }
    }

    /**
     * 绑定当前页面的视频播放器控件。
     */
    public void bindPlayerView(PlayerView view) {
        playerView = view;
        if (playerView != null) {
            playerView.setVisibility(View.GONE);
            playerView.setPlayer(getOrCreateVideoPlayer());
        }
    }

    /**
     * 恢复到相机初始预览状态。
     */
    public void resetToCameraPreview() {
        stopVideoPlayback();
        stopPhotoPreview();
    }

    /**
     * 判断当前页面是否正在播放录像回放。
     */
    public boolean isVideoPlayingInPage() {
        return videoPlayer != null && videoPlayer.isPlaying();
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
     * 如果开关打开，就在当前页面展示刚拍好的照片。
     */
    protected void showCapturedPhotoIfNeeded(Uri photoUri) {
        if (!stayOnCapturedPhoto || photoUri == null || photoView == null) {
            return;
        }
        stopVideoPlayback();
        photoView.setImageURI(photoUri);
        photoView.setVisibility(View.VISIBLE);
    }

    /**
     * 关闭当前页面的照片停留画面，恢复相机预览。
     */
    public void stopPhotoPreview() {
        if (photoView != null) {
            photoView.setImageURI(null);
            photoView.setVisibility(View.GONE);
        }
        if (previewView != null) {
            previewView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 如果开关打开，就在当前页面循环播放刚录制完成的视频。
     */
    protected void playRecordedVideoIfNeeded(Uri videoUri) {
        if (!autoPlayRecordedVideo || videoUri == null || playerView == null) {
            return;
        }
        stopPhotoPreview();
        if (previewView != null) {
            previewView.setVisibility(View.INVISIBLE);
        }
        playerView.setVisibility(View.VISIBLE);
        ExoPlayer player = getOrCreateVideoPlayer();
        player.setRepeatMode(Player.REPEAT_MODE_ONE);
        player.setMediaItem(MediaItem.fromUri(videoUri));
        player.prepare();
        player.play();
    }

    /**
     * 停止当前页面的视频回放并恢复相机预览。
     */
    public void stopVideoPlayback() {
        if (videoPlayer != null) {
            videoPlayer.stop();
            videoPlayer.clearMediaItems();
            videoPlayer.setRepeatMode(Player.REPEAT_MODE_OFF);
        }
        if (playerView != null) {
            playerView.setVisibility(View.GONE);
        }
        if (previewView != null) {
            previewView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 释放基础层持有的预览、回放和相机资源。
     */
    protected void releaseBaseResources() {
        stopPhotoPreview();
        releaseVideoPlayer();
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
            cameraProvider = null;
        }
        camera = null;
        previewView = null;
        photoView = null;
        playerView = null;
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

    /**
     * 释放当前页面的视频播放器。
     */
    private void releaseVideoPlayer() {
        if (playerView != null) {
            playerView.setPlayer(null);
            playerView.setVisibility(View.GONE);
        }
        if (videoPlayer != null) {
            videoPlayer.release();
            videoPlayer = null;
        }
    }

    /**
     * 通知相机初始化完成。
     */
    protected void notifyReady() {
        if (callback != null) {
            callback.onCameraReady();
        }
    }

    /**
     * 通知相机错误。
     */
    protected void notifyError(String message, Throwable throwable) {
        if (callback != null) {
            callback.onCameraError(message, throwable);
        }
    }
}
