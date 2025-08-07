package media.library.images.manager;

import static java.lang.System.currentTimeMillis;

import android.annotation.TargetApi;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.SparseIntArray;
import android.view.Surface;


import media.library.images.unmix.ImageLog;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 音频视频录制
 * Created by 郭敏 on 2018/3/21 0021.
 */

public class MediaRecorderManager {
    private String tag = "MediaRecorderManager";
    private static MediaRecorderManager mediaGatherRecorder;
    //视频
    private MediaRecorder mediaRecorder;
    private final int SENSOR_ORIENTATION_DEFAULT_DEGREES = 90;
    private final int SENSOR_ORIENTATION_INVERSE_DEGREES = 270;
    private SparseIntArray DEFAULT_ORIENTATIONS = new SparseIntArray();
    private SparseIntArray INVERSE_ORIENTATIONS = new SparseIntArray();
    private int videoH, videoW;
    //当前录制的音视频path
    private String recorderPath;
    private String recorderPathParent;
    //录制路径以.结尾的东西
    private String recorderType;
    //当前录制完成的音视频paths
    private ArrayList<String> recorderPaths = new ArrayList();
    //录音总时长(s) 默认10分钟
    private long recordTotalTime = 60 * 10;
    //当前录制时长(s)
    private long progressTime;
    //true 超时停止录制
    private boolean isRecorderTimeOut;
    //声音振幅的获取频率
    private int voiceAmplitude = 200;
    private RecorderHander recorderHander;
    //true 录制视频 false:录制语音
    private boolean isVideo;

    public static MediaRecorderManager getInstance() {
        if (mediaGatherRecorder == null) {
            mediaGatherRecorder = new MediaRecorderManager();
        }
        return mediaGatherRecorder;
    }

    /**
     * 设置视频输出
     *
     * @param path   输出位置
     * @param videoW 视频宽度
     * @param videoH 视频高度
     */
    public void setVideoOutput(String path, int videoW, int videoH) {
        setVideoOutput(path, null, true, videoW, videoH);
    }

    public void setVideoOutputRest(String path, int videoW, int videoH) {
        setVideoOutput(path, null, false, videoW, videoH);
    }

    public void setVideoOutput(String path, String end, boolean isInit, int videoW, int videoH) {
        isVideo = true;
        if (recorderHander == null) {
            recorderHander = new RecorderHander();
        }
        if (mediaRecorder != null) {
            DEFAULT_ORIENTATIONS.append(Surface.ROTATION_0, 90);
            DEFAULT_ORIENTATIONS.append(Surface.ROTATION_90, 0);
            DEFAULT_ORIENTATIONS.append(Surface.ROTATION_180, 270);
            DEFAULT_ORIENTATIONS.append(Surface.ROTATION_270, 180);
            //
            INVERSE_ORIENTATIONS.append(Surface.ROTATION_0, 270);
            INVERSE_ORIENTATIONS.append(Surface.ROTATION_90, 180);
            INVERSE_ORIENTATIONS.append(Surface.ROTATION_180, 90);
            INVERSE_ORIENTATIONS.append(Surface.ROTATION_270, 0);
            //
            mediaRecorder = new MediaRecorder();
        }
        // 设置音频采集方式
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        //设置视频的采集方式
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        //设置文件的输出格式
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);//aac_adif， aac_adts， output_format_rtp_avp， output_format_mpeg2ts ，webm
        //设置audio的编码格式
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        //设置video的编码格式
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        //设置录制的视频编码比特率(可以调整清晰度)
        mediaRecorder.setVideoEncodingBitRate(5 * 1920 * 1080);
        //设置录制的视频帧率(要硬件支持)
        mediaRecorder.setVideoFrameRate(30);
        //设置记录会话的最大持续时间（毫秒）
        //mediaRecorder.setMaxDuration(60 * 1000);
        this.videoH = videoH;
        this.videoW = videoW;
        if (videoW != 0 && videoH != 0) {
            mediaRecorder.setVideoSize(videoW, videoH);
        }
        recorderPath = path;
        if (isInit) {
            initPath(end);
        }
        mediaRecorder.setOutputFile(path);
    }

    private int recorderOutputFormat = MediaRecorder.OutputFormat.AMR_NB;
    private int recorderAudioEncoder = MediaRecorder.AudioEncoder.AMR_NB;

    public void setAudioOutput(String path, int recorderOutputFormat, int recorderAudioEncoder) {
        this.recorderOutputFormat = recorderOutputFormat;
        this.recorderAudioEncoder = recorderAudioEncoder;
        setAudioOutput(path);
    }

    /**
     * 设置语音输出
     *
     * @param path 输出位置
     */
    public void setAudioOutput(String path) {
        setAudioOutput(path, null, true);
    }

    private void setAudioOutputRest(String path) {
        setAudioOutput(path, null, false);
    }

    //end 结尾
    public void setAudioOutput(String path, String end, boolean isInit) {
        isVideo = false;
        if (recorderHander == null) {
            recorderHander = new RecorderHander();
        }
        if (mediaRecorder == null) {
            mediaRecorder = new MediaRecorder();
        }
        mediaRecorder.reset();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(recorderOutputFormat);
        mediaRecorder.setAudioEncoder(recorderAudioEncoder);
        recorderPath = path;
        if (isInit) {
            initPath(end);
        }
        mediaRecorder.setOutputFile(path);
    }

    //停止
    private void stopMedia() {
        if (mediaRecorder != null) {
            mediaRecorder.setOnErrorListener(null);
            mediaRecorder.setPreviewDisplay(null);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isPause) {
                mediaRecorder.resume();
                isPause = false;
            }
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
        }
        if (recorderHander != null) {
            recorderHander.stopHander();
        }
    }

    private void initPath(String end) {
        recorderPathParent = new File(recorderPath).getParent();
        if (recorderPath.endsWith(".mp3")) {
            recorderType = ".mp3";
        }
        if (recorderPath.endsWith(".mp4")) {
            recorderType = ".mp4";
        }
        if (recorderPath.endsWith(".3gp")) {
            recorderType = ".3gp";
        }
        if (recorderPath.endsWith(".amr")) {
            recorderType = ".amr";
        }
        if (!TextUtils.isEmpty(end)) {
            recorderType = end;
        }

    }


    //获取录制对象
    public MediaRecorder getMediaRecorder() {
        return mediaRecorder;
    }

    public void setRecordTotalTime(long recordTotalTime) {
        this.recordTotalTime = recordTotalTime;
    }

    public void setVoiceAmplitude(int voiceAmplitude) {
        this.voiceAmplitude = voiceAmplitude;
    }

    /**
     * 设置录制预览旋转
     *
     * @param rotate 旋转角度
     */
    public void setPreviewRotate(int rotate) {
        mediaRecorder.setOrientationHint(rotate);
    }

    /**
     * 设置录制预览旋转
     *
     * @param isCameraFront     true 前置相机
     * @param rotation          手机方向
     * @param cameraOrientation 相机旋转角度
     */
    public void setPreviewRotate(boolean isCameraFront, int rotation, Integer cameraOrientation) {
        d("设置角度 屏幕rotation:" + rotation + " 相机cameraOrientation" + cameraOrientation);
        if (!isCameraFront) {
            setCameraBack(rotation, cameraOrientation);
        } else {
            setCameraFront(rotation, cameraOrientation);
        }
    }

    //前置摄像头
    private void setCameraFront(int rotation, int cameraOrientation) {
        //正常（貌似不可以倒屏）
        if (rotation == 0 && cameraOrientation == 270) {
            mediaRecorder.setOrientationHint(0);
        }
        //左横屏
        if (rotation == 1 && cameraOrientation == 90) {
            mediaRecorder.setOrientationHint(0);
        }
        //右横屏（测试无效，录制的视频是倒的）
        if (rotation == 3 && cameraOrientation == 90) {
            mediaRecorder.setOrientationHint(270);
        }
    }

    //后置摄像头
    private void setCameraBack(int rotation, Integer cameraOrientation) {
        switch (cameraOrientation) {
            case SENSOR_ORIENTATION_DEFAULT_DEGREES:
                int rotate = DEFAULT_ORIENTATIONS.get(rotation);
                mediaRecorder.setOrientationHint(rotate);
                break;
            case SENSOR_ORIENTATION_INVERSE_DEGREES:
                rotate = INVERSE_ORIENTATIONS.get(rotation);
                MediaRecorderManager.getInstance().setPreviewRotate(rotate);
                break;
        }

    }

    //开始录制
    public void setMediaStart() {
        progressTime = 0;
        isRecorderTimeOut = false;
        recorderPaths.clear();
        setMediaWork(0);
        setMediaWork(1);
    }

    //暂停录制
    public void setMediaPause() {
        setMediaWork(2);
    }

    //继续录制
    public void setMediaResume() {
        setMediaWork(3);
    }

    //停止录制
    public void setMediaStop() {
        setMediaWork(4);
    }

    //关闭录制
    public void setMediaClose() {
        setMediaWork(5);
    }

    //删除文件
    public void onDeleteRecorderFile() {
        deleteRecorderFiles(recorderPaths);
    }

    //保存播放状态
    private int workType;
    //true 暂停调用了pause
    private boolean isPause;

    /**
     * 媒体开始工作
     *
     * @param type 工作类型
     */
    public void setMediaWork(int type) {
        d("状态workType：" + workType + "  type:" + type);
        if (isRecorderTimeOut && type == 0) {
            return;
        }
        if (isRecorderTimeOut && type == 1) {
            return;
        }
        if (isRecorderTimeOut && type == 2) {
            return;
        }
        if (isRecorderTimeOut && type == 3) {
            return;
        }
        if (workType == -1 && type != 0) {
            d("发生错误，要重新开始录音");
            return;
        }
        try {
            switch (type) {
                case 0:
                    //准备
                    mediaRecorder.prepare();
                    break;
                case 1:
                    //开始
                    mediaRecorder.start();
                    recorderHander.start();
                    onBackListener(1);
                    break;
                case 2:
                    //暂停
                    onBackListener(2);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        isPause = true;
                        recorderHander.stopHander();
                        mediaRecorder.pause();
                        break;
                    }
                    //如果暂停不了就会停止
                    setMediaWork(4);
                    break;
                case 3:
                    //继续
                    onBackListener(3);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        isPause = false;
                        mediaRecorder.resume();
                        recorderHander.start();
                        break;
                    }
                    //如果继续不了就会重新录音
                    long time = System.currentTimeMillis();
                    String path = recorderPathParent + File.separator + time + recorderType;
                    if (isVideo) {
                        setVideoOutputRest(path, videoW, videoH);
                    } else {
                        setAudioOutputRest(path);
                    }
                    mediaRecorder.prepare();
                    mediaRecorder.start();
                    recorderHander.start();
                    break;
                case 4:
                    //停止
                    if (workType == 4) {
                        break;
                    }
                    stopMedia();
                    recorderPaths.add(recorderPath);
                    onBackListener(4);
                    break;
                case 5:
                    //关闭录音
                    if (workType == 5) {
                        break;
                    }
                    stopMedia();
                    onBackListener(5);
                    break;
            }
        } catch (IllegalStateException e) {
            if (mediaRecorder != null) {
                mediaRecorder.release();
                mediaRecorder = null;
            }
            if (recorderHander != null) {
                recorderHander.stopHander();
            }
            isPause = false;
            type = -1;
            d("状态：" + type + " 错误：" + e.getMessage());
            d("请重新录制");
        } catch (IOException e) {
            e.printStackTrace();
            onBackListener(-1);
            type = -1;
            recorderHander.stopHander();
            d("状态：" + type + " 错误：" + e.getMessage());
        }
        workType = type;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public Surface getSurfaceView() {
        Surface surface = mediaRecorder.getSurface();
        return surface;
    }

    /**
     * 声音录制的合并
     *
     * @param paths   要合并的声音
     * @param pathNew 新声音的路径
     * @return
     */
    public boolean onRecorderMerge(List<String> paths, String pathNew) {
        // 最后合成的音频文件
        boolean isSave = false;
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(pathNew));
            for (int i = 0; i < paths.size(); i++) {
                File file = new File(paths.get(i));
                FileInputStream fileInputStream = new FileInputStream(file);
                byte[] mByte = new byte[fileInputStream.available()];
                int length = mByte.length;
                //第二个文件以上是要就减去6个byte
                int skip = 0;
                if (i > 0) {
                    skip = 6;
                }
                while (fileInputStream.read(mByte) != -1) {
                    bos.write(mByte, skip, length - skip);
                    skip = 0;
                }
                bos.flush();
                fileInputStream.close();
            }
            bos.close();
            isSave = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            d("写入合并异常" + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            d("写入合并异常" + e.getMessage());
        }
        // 不管合成是否成功、删除录音片段
        deleteRecorderFiles(paths);
        return isSave;
    }

    //删除录制的文件
    public void deleteRecorderFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }

    public ArrayList<String> getRecorderPaths() {
        return recorderPaths;
    }

    //删除录制的文件
    private void deleteRecorderFiles(List<String> paths) {
        if (paths.size() == 0) {
            return;
        }
        for (int i = 0; i < paths.size(); i++) {
            deleteRecorderFile(paths.get(i));
        }
    }

    /**
     * 获取声音强度
     * 计算出的分贝值正常值域为0 dB 到90.3 dB
     *
     * @param grade 等级数
     * @return 等级（>1<=grade）
     */
    public int getAudioAmplitude(int grade) {
        double m = 0;
        double amplitude = 0;
        if (mediaRecorder != null) {
            m = (double) mediaRecorder.getMaxAmplitude();
            amplitude = m / 7000;
        }
        int g = (int) (grade * amplitude);
        if (g < 1) {
            g = 1;
        }
        return g;
    }

    //获取录音文件地址 合并之后的储存路径会有一个新的地址
    public String getPath() {
        String newPath = "";
        if (isPause) {
            setMediaWork(4);
        } else {
            stopMedia();
        }
        if (recorderPaths.size() > 1) {
            long time = currentTimeMillis();
            newPath = recorderPathParent + File.separator + "merge_" + time + recorderType;
            onRecorderMerge(recorderPaths, newPath);
        }
        if (!TextUtils.isEmpty(newPath)) {
            return newPath;
        }
        if (recorderPaths.size() == 1) {
            newPath = recorderPaths.get(0);
        }
        return newPath;
    }


    private OnMediaRecorderListener listener;

    /**
     * 添加监听
     *
     * @param onRecorderListener
     */
    public void setOnRecorderListener(OnMediaRecorderListener onRecorderListener) {
        if (listener == onRecorderListener) {
            return;
        }
        this.listener = onRecorderListener;
    }

    public void onDestroy() {
        stopMedia();
        listener = null;
    }

    //播放进度
    class RecorderHander extends Handler {
        private boolean isRun;

        public void start() {
            if (isRun) {
                return;
            }
            isRun = true;
            //录制时长
            sendEmptyMessageDelayed(1, 1 * 1000);
            //声音振幅
            sendEmptyMessageDelayed(2, voiceAmplitude);

        }

        public void stopHander() {
            if (!isRun) {
                return;
            }
            isRun = false;
            removeMessages(1);
            removeMessages(2);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (isRecorderTimeOut) {
                return;
            }
            if (progressTime >= recordTotalTime) {
                isRecorderTimeOut = true;
                setMediaWork(4);
                onBackListener(6);
                d("录音超时......");
                return;
            }
            int what = msg.what;
            switch (what) {
                case 1:
                    progressTime += 1;
                    sendEmptyMessageDelayed(1, 1 * 1000);
                    onBackListener(7);
                    break;
                case 2:
                    sendEmptyMessageDelayed(2, voiceAmplitude);
                    onBackListener(8);
                    break;
            }

        }
    }

    private void onBackListener(int type) {
        OnMediaRecorderListener back = listener;
        if (back == null) {
            return;
        }
        switch (type) {
            case -1:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                back.onRecorderState(type, progressTime);
                break;
            case 6:
                //录音超时
                back.onRecorderTimeOut(recordTotalTime, progressTime);
                break;
            case 7:
                //录音计时
                back.onRecorderTime(recordTotalTime, progressTime);
                break;
            case 8:
                //声音振幅
                back.onVoiceAmplitude();
                break;
        }
    }

    //播放监听
    public interface OnMediaRecorderListener {
        /**
         * 录制状态 -1:发生错误 1：录制开始 2：录制暂停 3：录制继续 4：录制完成
         * 5:关闭录音
         *
         * @param state
         */
        void onRecorderState(int state, long progressTime);

        /**
         * 录音计时
         *
         * @param recordTotalTime 可以录制的时长
         * @param progressTime    当前录制时长
         */
        void onRecorderTime(long recordTotalTime, long progressTime);

        /**
         * 声音振幅
         */
        void onVoiceAmplitude();

        /**
         * 录制超时
         *
         * @param recordTotalTime 可以录制的时长
         * @param progressTime    当前录制时长
         */

        void onRecorderTimeOut(long recordTotalTime, long progressTime);
    }

    private void d(String value) {
        ImageLog.d(tag, value);
    }
}
