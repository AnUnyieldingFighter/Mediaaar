package media.library.player.view.play;

import android.os.Handler;
import android.os.SystemClock;

import java.util.Locale;

import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DataSpec;
import androidx.media3.datasource.TransferListener;
import androidx.media3.exoplayer.analytics.AnalyticsListener;
import androidx.media3.exoplayer.source.LoadEventInfo;
import androidx.media3.exoplayer.source.MediaLoadData;

import media.library.player.manager.PlayerLog;

//同时监听播放器状态与数据源传输，统计缓冲期间的实际数据读取速度
@UnstableApi
class ExoPlayerAnalyticsListener implements AnalyticsListener, TransferListener {
    //所属播放器，加载回调通过它将事件信息交给 PlayerDB 保存
    private final BaseExoPlayer baseExoPlayer;
    //沿用所属播放器的日志标识
    private final String tag = "播放器_Analytics_";
    //当前播放源是否已接入传输监听；未接入时不启动测速，不把未知速度打印成零
    private boolean speedAvailable;
    //是否处于缓冲状态；与字节计数一起由当前监听对象的锁保护
    private boolean buffering;
    //本次进入缓冲的时间，使用单调时钟，单位毫秒
    private long bufferStartMs;
    //当前速度采样窗口的开始时间，单位毫秒
    private long speedStartMs;
    //当前采样窗口收到的网络数据量，单位字节
    private long networkBytes;
    //当前采样窗口从本地文件或缓存读取的数据量，单位字节
    private long localBytes;
    //在当前播放器所属线程定时采样，结束缓冲或释放时移除任务
    private final Handler speedHandler;
    //每秒采样一次，即使没有收到数据，也能打印零速度
    private final Runnable speedTask = new Runnable() {
        //读取并清空本次采样计数，仍在缓冲时安排下一次采样
        @Override
        public void run() {
            synchronized (ExoPlayerAnalyticsListener.this) {
                if (!buffering || !speedAvailable) {
                    return;
                }
                printBufferSpeed("缓冲中", SystemClock.elapsedRealtime());
                speedHandler.postDelayed(this, 1000);
            }
        }
    };

    //传入所属播放器，在其线程上创建采样任务的调度器
    ExoPlayerAnalyticsListener(BaseExoPlayer baseExoPlayer) {
        this.baseExoPlayer = baseExoPlayer;
        this.speedHandler = new Handler(baseExoPlayer.player.getApplicationLooper());
    }

    //===================== 数据源传输 =====================

    //开始建立连接，此时还没有传输数据
    @Override
    public void onTransferInitializing(DataSource source, DataSpec dataSpec, boolean isNetwork) {
    }

    //数据源开始读取，速度窗口从进入缓冲状态时开始计算
    @Override
    public void onTransferStart(DataSource source, DataSpec dataSpec, boolean isNetwork) {
    }

    //加载线程只累计实际字节；速度计算和日志由播放器线程的定时任务完成
    @Override
    public synchronized void onBytesTransferred(DataSource source, DataSpec dataSpec,
                                                boolean isNetwork, int bytesTransferred) {
        if (!buffering || !speedAvailable || bytesTransferred <= 0) {
            return;
        }
        if (isNetwork) {
            networkBytes += bytesTransferred;
        } else {
            localBytes += bytesTransferred;
        }
    }

    //单次传输结束不代表缓冲完成，状态由播放器回调判断
    @Override
    public void onTransferEnd(DataSource source, DataSpec dataSpec, boolean isNetwork) {
    }

    //===================== 加载任务与网络带宽监听 =====================

    //一次加载任务开始，打印播放位置和当前已缓冲时长，不代表进入缓冲状态
    @Override
    public void onLoadStarted(EventTime eventTime, LoadEventInfo loadEventInfo, MediaLoadData mediaLoadData, int retryCount) {
        PlayerLog.d(tag, "加载开始 播放位置=" + eventTime.currentPlaybackPositionMs
                + " ms，当前缓冲时长=" + eventTime.totalBufferedDurationMs + " ms");
    }

    //加载中途取消也可能已有响应头，可以读取总大小，但不把已下载字节当成总大小
    @Override
    public void onLoadCanceled(EventTime eventTime, LoadEventInfo info, MediaLoadData data) {
        baseExoPlayer.dbSaveLoadedFileSize(baseExoPlayer.playerContext,
                eventTime, info, data);
    }

    //一次加载任务完成，保存可确认的文件大小，并打印任务整体的平均加载速度
    @Override
    public void onLoadCompleted(EventTime eventTime, LoadEventInfo loadEventInfo, MediaLoadData mediaLoadData) {
        AnalyticsListener.super.onLoadCompleted(eventTime, loadEventInfo, mediaLoadData);
        baseExoPlayer.dbSaveLoadedFileSize(baseExoPlayer.playerContext,
                eventTime, loadEventInfo, mediaLoadData);
        if (loadEventInfo.loadDurationMs <= 0) {
            PlayerLog.d(tag, "加载完成 平均加载速度：未知，已加载字节="
                    + loadEventInfo.bytesLoaded + "，加载耗时=" + loadEventInfo.loadDurationMs + " ms");
            return;
        }
        //本次加载可能来自网络或本地缓存，平均加载速度不能直接当作网络带宽
        double speedKbps = loadEventInfo.bytesLoaded * 8.0 / loadEventInfo.loadDurationMs;
        PlayerLog.d(tag, String.format(Locale.US,
                "加载完成 平均加载速度=%.2f kbps，已加载字节=%d，加载耗时=%d ms",
                speedKbps, loadEventInfo.bytesLoaded, loadEventInfo.loadDurationMs));
    }

    //打印播放器提供的网络带宽估计；自适应选轨沿用原配置，不覆盖调用方的码率上限
    @Override
    public void onBandwidthEstimate(EventTime eventTime, int totalLoadTimeMs, long totalBytesLoaded, long bitrateEstimate) {
        if (bitrateEstimate <= 0) {
            PlayerLog.d(tag, "网络带宽估计：未知");
            return;
        }
        //播放器会在现有上限内自行升降清晰度
        /*if (customTrackSelector != null) {
            final int maxVideoBitrate = (int) (bitrateEstimate * 0.8);
            customTrackSelector.setVideoBitrate(maxVideoBitrate);
        }*/
        PlayerLog.d(tag, "网络带宽估计=" + bitrateEstimate / 1000.0
                + " kbps，采样传输字节=" + totalBytesLoaded + "，采样耗时=" + totalLoadTimeMs + " ms");
    }

    //===================== 监听播放器状态 =====================

    //进入缓冲时启动采样，准备就绪或停止时结束采样；暂停仍处于缓冲时继续测速
    @Override
    public synchronized void onPlaybackStateChanged(EventTime eventTime, int state) {
        long now = SystemClock.elapsedRealtime();
        if (state == Player.STATE_BUFFERING && !buffering) {
            buffering = true;
            bufferStartMs = now;
            speedStartMs = now;
            networkBytes = 0;
            localBytes = 0;
            PlayerLog.d(tag, "缓冲开始 当前缓冲时长=" + eventTime.totalBufferedDurationMs + " ms"
                    + (speedAvailable ? "" : "，测速不可用（未接入传输监听）"));
            if (speedAvailable) {
                speedHandler.postDelayed(speedTask, 1000);
            }
        } else if (state != Player.STATE_BUFFERING) {
            if (buffering) {
                String result = state == Player.STATE_READY ? "缓冲结束" : "缓冲中断";
                finishBuffering(result, eventTime);
            } else if (state == Player.STATE_READY) {
                PlayerLog.d(tag, "准备就绪 当前缓冲时长=" + eventTime.totalBufferedDurationMs + " ms");
            }
        }
    }

    //播放器释放不保证触发停止状态回调，在此收尾测速；不写入数据库
    @Override
    public synchronized void onPlayerReleased(EventTime eventTime) {
        finishBuffering("缓冲中断（播放器释放）", eventTime);
    }

    //===================== 统计缓冲期间的实际数据读取速度 =====================

    //播放源构建时设置测速能力，切换能力时清理旧采样，避免沿用上一播放源的计数
    protected synchronized void setSpeedAvailable(boolean available) {
        if (speedAvailable == available) {
            return;
        }
        speedAvailable = available;
        speedHandler.removeCallbacks(speedTask);
        networkBytes = 0;
        localBytes = 0;
        speedStartMs = SystemClock.elapsedRealtime();
        if (buffering && available) {
            speedHandler.postDelayed(speedTask, 1000);
        }
    }

    //持有当前监听对象的锁时结束缓冲统计，打印最后采样并清理定时任务和计数
    private void finishBuffering(String result, EventTime eventTime) {
        speedHandler.removeCallbacks(speedTask);
        if (buffering) {
            long now = SystemClock.elapsedRealtime();
            buffering = false;
            printBufferSpeed(result + "，最后采样速度：", now);
            PlayerLog.d(tag, result + "，耗时=" + (now - bufferStartMs)
                    + " ms，当前缓冲时长=" + eventTime.totalBufferedDurationMs + " ms");
        }
        bufferStartMs = 0;
        speedStartMs = 0;
        networkBytes = 0;
        localBytes = 0;
    }

    //持锁打印采样窗口速度；已接入测速但无数据时打印零，未接入时打印测速不可用
    private void printBufferSpeed(String state, long now) {
        long elapsedMs = now - speedStartMs;
        if (!speedAvailable) {
            PlayerLog.d(tag, state + " 测速不可用（未接入传输监听）");
        } else if (elapsedMs > 0) {
            PlayerLog.d(tag, String.format(Locale.US,
                    "%s 网络下载=%.2f KB/s，本地/缓存读取=%.2f KB/s，采样耗时=%d ms",
                    state, networkBytes * 1000.0 / (1024 * elapsedMs),
                    localBytes * 1000.0 / (1024 * elapsedMs), elapsedMs));
        } else {
            PlayerLog.d(tag, state + " 采样耗时不足 1 ms，速度未知");
        }
        networkBytes = 0;
        localBytes = 0;
        speedStartMs = now;
    }

}

