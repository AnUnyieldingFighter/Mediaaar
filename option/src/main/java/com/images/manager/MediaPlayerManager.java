package com.images.manager;

import static androidx.core.content.ContextCompat.getSystemService;

import android.app.Service;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.Surface;

import com.images.unmix.ImageLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

/**
 * 音频视频播放
 * Created by 郭敏 on 2018/3/22 0022.
 */

public class MediaPlayerManager {
    private static MediaPlayerManager mediaPlayerManager;
    private PlayProgressHander playProgressHander;
    private MediaPlayer mediaPlayer;
    private String tag = "MediaPlayerManager";
    //播放源
    private String source;
    //播放源类型 1：本地 2：网络 3：源错误
    private int sourceType;
    //true:播放完可以重播
    //private boolean isRebroadcast;
    //true 准备完成之后自动播放 (本地视频 一般直接播放)
    private boolean isAutoplay;
    //true ：准备完成
    private boolean isPrepareComplete;

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public static MediaPlayerManager getInstance() {
        if (mediaPlayerManager == null) {
            mediaPlayerManager = new MediaPlayerManager();
        }
        return mediaPlayerManager;
    }

    public void setAutoplay(boolean isAutoplay) {
        this.isAutoplay = isAutoplay;
    }

    //播放初始化
    private void mediaPlayerInit() {
        if (mediaPlayer != null) {
            setMediaWork(3);
        }
        playProgressHander = new PlayProgressHander();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
            @Override
            public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                setListenerBack(1, 0, width, height, 0, 0);
            }
        });
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                d("准备完成");
                isInPrepare = false;
                setListenerBack(2);
                if (isAutoplay) {
                    setMediaPlayerStart();
                }
            }
        });

        mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                setListenerBack(3, percent, 0, 0, 0, 0);
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                //播放完成
                setListenerBack(4);
                playProgressHander.stop();
            }
        });

        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                setListenerBack(5, 0, 0, 0, what, extra);
                playProgressHander.stop();
                return false;
            }
        });
        //拖动播放进度
        mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer mp) {
                setListenerBack(11);
            }
        });
        // 设置循环播放
        //mediaPlayer.setLooping(true);
    }

    public static VideoDataBean getVideoData1S(String videoPath) {
        //timeUs 这里设置为1秒处的帧
        return getVideoDataNS(1, videoPath);
    }

    /**
     * @param time      单位秒
     * @param videoPath
     * @return
     */
    public static VideoDataBean getVideoDataNS(int time, String videoPath) {
        long timeUs = time * 1000000;
        return getVideoData(timeUs, videoPath);
    }

    /**
     * 获取视频数据
     *
     * @param timeUs    获取第一帧图像 (微秒)
     * @param videoPath 视频链接
     * @return
     */
    public static VideoDataBean getVideoData(long timeUs, String videoPath) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(videoPath); // 设置数据源路径
            // 获取第一帧图像，参数为时间微秒，这里设置为1秒处的帧
            Bitmap bitmap = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            String width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            String height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
            retriever.release(); // 释放资源
            VideoDataBean videoData = new VideoDataBean();
            videoData.videoBitmap = bitmap;
            videoData.time = time;
            videoData.width = width;
            videoData.height = height;
            return videoData;
        } catch (IllegalArgumentException ex) {
            // 处理异常，例如文件路径无效等。
        } catch (IOException e) {

        } finally {
            if (retriever != null) {
                try {
                    // 确保释放资源。
                    retriever.release();
                } catch (Exception e) {

                }
            }
        }
        return null;
    }

    /**
     * 比较视频播放地址
     *
     * @param url 视频播放地址
     * @return
     */
    public boolean isSamePlayUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        return url.equals(source);
    }

    /**
     * 设置播放下一个视频的url
     *
     * @param url
     */
    public void setNextPlayUrl(String url) {
        if (TextUtils.isEmpty(source)) {
            return;
        }
        if (source.equals(url)) {
            return;
        }
        if (mediaPlayer != null) {
            setMediaWork(3);
        } else {
            setListenerBack(8);
        }
    }

    /**
     * 设置播放源
     *
     * @param url  网络
     * @param path 本地
     */
    public void setDataSourcePlay(String url, String path) {
        if ((isSamePlayUrl(url) || isSamePlayUrl(path)) && isPrepareComplete) {
            setListenerBack(2);
            return;
        }
        setDataSource(url, path);
        //准备好之后会去播放，因此调用此方法后面  不要再调用 setMediaPlayerStart
        setMediaWork(0);
    }

    private void setDataSource(String url, String path) {
        isPrepareComplete = false;
        if (!TextUtils.isEmpty(path)) {
            setPayPath(path);
            return;
        }
        setPayUrl(url);
    }

    /**
     * 播放源是网络
     *
     * @param url 源地址
     */
    private void setPayUrl(String url) {
        mediaPlayerInit();
        sourceType = 2;
        try {
            source = url;
            mediaPlayer.setDataSource(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 播放源是本地的
     *
     * @param path 源地址
     */
    private void setPayPath(String path) {
        mediaPlayerInit();
        sourceType = 1;
        File file = new File(path);
        if (!file.exists()) {
            sourceType = 3;
            return;
        }
        try {
            source = path;
            FileInputStream fis = new FileInputStream(file);
            mediaPlayer.setDataSource(fis.getFD());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    /**
     * 播放视频要设置
     *
     * @param surface
     */
    public void setSurfaceView(Surface surface) {
        mediaPlayer.setSurface(surface);
    }

    /**
     * 播放拖动
     *
     * @param seekTo
     * @return
     */
    public boolean seekToPay(int seekTo) {
        if (mediaPlayer == null) {
            return false;
        }
        setListenerBack(10);
        mediaPlayer.seekTo(seekTo);
        return true;
    }

    //设置进度
    public int getCurrentPosition(String url) {
        int position = -1;
        if (!isSamePlayUrl(url)) {
            return position;
        }
        if (mediaPlayer == null) {
            position = mediaPlayer.getCurrentPosition();

        }
        return position;
    }


    /**
     * 设置禁音(setVolume方法放到play(m.start())之后)
     *
     * @param isSilence true 静音
     * @param context
     */
    public void setSilence(boolean isSilence, Context context) {
        if (mediaPlayer == null) {
            return;
        }
        //在onPrepared（准备完成）后调用
        if (isSilence) {
            seVolume(0, 0);
        } else {
            //mediaPlayer.setVolume(1, 1);
            AudioManager audioManager = (AudioManager) context.getSystemService(Service.AUDIO_SERVICE);
            //mediaPlayer.setAudioStreamType(AudioManager.STREAM_SYSTEM);
            seVolume(audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM), audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM));
        }

    }

    /**
     * 设位置音量(设置禁音(setVolume方法放到play(m.start())之后))
     *
     * @param leftVolume
     * @param rightVolume
     */
    public void seVolume(float leftVolume, float rightVolume) {
        if (mediaPlayer == null) {
            return;
        }
        //在onPrepared（准备完成）后调用
        mediaPlayer.setVolume(leftVolume, rightVolume);
    }

    //暂停播放
    public void setMediaPlayerPause() {
        setMediaWork(1);
    }

    //继续播放
    public void setMediaPlayerStart() {
        setMediaWork(2);
    }

    //停止播放
    public void setMediaPlayerStop() {
        setMediaWork(3);
    }

    //重置播放器
    public void setMediaPlayerRest() {
        setMediaWork(4);
    }

    //关闭播放器
    public void setMediaPlayerClose() {
        setMediaWork(5);
    }


    //true 没有在播放
    public boolean isStopPlay() {
        if (mediaPlayer == null) {
            return true;
        }
        return !mediaPlayer.isPlaying();
    }

    //true 准备中
    private boolean isInPrepare;

    /**
     * 媒体开始工作
     *
     * @param type 工作类型（0：准备；1：暂停；2：继续播放 或者 播放；3：停止,重置播放器  4 停止与清除数据）
     */
    public void setMediaWork(int type) {
        d("状态--：" + type + "  type:" + type);
        switch (type) {
            case 0:
                //准备
                if (sourceType == 3) {
                    setListenerBack(5);
                    break;
                }
                if (mediaPlayer != null) {
                    isInPrepare = true;
                    mediaPlayer.prepareAsync();
                }
                break;
            case 1:
                //暂停
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    playProgressHander.stop();
                } else {
                    //如果是在准备中，就停止掉
                    if (mediaPlayer != null && isInPrepare) {
                        mediaPlayer.stop();
                        mediaPlayer.reset();
                    }
                }
                setListenerBack(7);
                break;
            case 2:
                //上一个播放状态 :是暂停 或者 准备
                //
                // MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.sound_file_1);
                // mediaPlayer.start();
                //播放  使用create()方法创建 MediaPlayer对象不需要调用prepare()方法，直接调用start()方法
                if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                    playProgressHander.start();
                    setListenerBack(9);
                }
                break;
            case 3:
                //停止
                if (playProgressHander != null) {
                    playProgressHander.stop();
                }
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                }
                setListenerBack(8);
                break;
            case 4:
                setMediaWork(3);
                isInPrepare = false;
                source = "";
                break;
            case 5:
                //关闭播放 并且释放播放器
                source = "";
                if (mediaPlayer == null) {
                    break;
                }
                setMediaWork(3);
                mediaPlayer.release();
                mediaPlayer = null;
                break;
        }
        d("状态：" + type);

    }

    //播放进度
    class PlayProgressHander extends Handler {
        private boolean isRun;

        public void start() {
            if (isRun) {
                return;
            }
            isRun = true;
            sendEmptyMessageDelayed(1, 1 * 1000);
        }

        public void stop() {
            if (!isRun) {
                return;
            }
            isRun = false;
            removeMessages(1);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            setListenerBack(6);
            sendEmptyMessageDelayed(1, 1 * 1000);
        }
    }

    //监听集合
    private ArrayList<OnMediaPlayerListener> listeners = new ArrayList<>();
    //单个监听
    private OnMediaPlayerListener listener;

    public OnMediaPlayerListener getListener() {
        if (listener != null) {
            return listener;
        }
        int size = listeners.size();
        if (size == 0) {
            return null;
        }
        return listeners.get(size - 1);
    }

    //关闭activitty
    public void onDestroy() {
        //移除监听
        if (listener != null) {
            listener = null;
            return;
        }
        int size = listeners.size();
        if (size == 0) {
            return;
        }
        listeners.remove(size - 1);
    }

    /**
     * 添加监听（添加监听集合 会 移除单个监听）
     *
     * @param onMediaListener
     */
    public void setAddOnMediaListener(OnMediaPlayerListener onMediaListener) {
        if (listener != null) {
            listener = null;
        }
        listeners.add(onMediaListener);
    }

    /**
     * 添加单个监听
     *
     * @param onMediaListener
     */
    public void setOnMediaListener(OnMediaPlayerListener onMediaListener) {
        if (listener == onMediaListener) {
            return;
        }
        this.listener = onMediaListener;
    }

    //监听调用
    private void setListenerBack(int type) {
        setListenerBack(type, 0, 0, 0, 0, 0);
    }

    /**
     * @param type    回调类型（1：获取到视频大小；2：准备完成(播放准备就绪)；3：缓存；
     *                4：播放完成；5：播放错误； 6：播放进度；7：播放暂停 8:重置播放器（播放停止）
     *                9：开始播放 调用了start  10:设置拖动 11：拖动回调（拖动完成）
     * @param percent 缓存进度
     * @param width   视频宽
     * @param height  视频高
     * @param what    播放错误
     * @param extra   播放错误
     */
    private void setListenerBack(int type, int percent, int width, int height, int what, int extra) {
        OnMediaPlayerListener listener = getListener();

        switch (type) {
            case 1:
                //获取到视频大小
                if (listener != null) {
                    listener.onVideoSizeChanged(mediaPlayer, width, height);
                }
                break;
            case 2:
                //准备完成
                isPrepareComplete = true;
                if (listener != null) {
                    listener.onPayState(mediaPlayer, 100, source);
                }
                break;
            case 3:
                //缓存
                if (listener != null) {
                    listener.onBufferingUpdate(mediaPlayer, percent);
                }
                break;
            case 4:
                //播放完成
                if (listener != null) {
                    listener.onPayState(mediaPlayer, 102, source);
                }
                break;
            case 5:
                //播放错误
                if (listener != null) {
                    listener.onError(mediaPlayer, what, extra);
                }
                isPrepareComplete = false;
                break;
            case 6:
                //播放进度
                if (listener != null) {
                    listener.onPayState(mediaPlayer, 104, source);
                }

                break;
            case 7:
                //播放暂停
                if (listener != null) {
                    listener.onPayState(mediaPlayer, 101, source);
                }
                break;
            case 8:
                //播放停止
                isPrepareComplete = true;
                if (listener != null) {
                    listener.onPayState(mediaPlayer, 103, source);
                }
                break;
            case 9:
                //开始播放 调用了start
                if (listener != null) {
                    listener.onPayState(mediaPlayer, 105, source);
                }
                break;
            case 10:
                if (listener != null) {
                    listener.onPayState(mediaPlayer, 106, source);
                }
                break;
            case 11:
                if (listener != null) {
                    listener.onPayState(mediaPlayer, 107, source);
                }
                break;
        }
    }

    //播放监听
    public interface OnMediaPlayerListener {

        //播放视频大小
        void onVideoSizeChanged(MediaPlayer mp, int width, int height);

        //播放缓冲
        void onBufferingUpdate(MediaPlayer mp, int percent);

        /**
         * @param mp
         * @param state  100:准备完成（播放就绪）
         *               101：播放暂停
         *               102：播放完成
         *               103：停止播放
         *               104：播放进度
         *               105: 开始播放（调用start）
         *               106:设置拖动
         *               107：拖动完成
         * @param source 播放的url
         */
        void onPayState(MediaPlayer mp, int state, String source);


        //播放错误
        void onError(MediaPlayer mp, int what, int extra);

    }

    private void d(String value) {
        ImageLog.d(tag, value);
    }

}
