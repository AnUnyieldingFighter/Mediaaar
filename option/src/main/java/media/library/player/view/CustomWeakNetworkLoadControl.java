package media.library.player.view;

import androidx.annotation.OptIn;
import androidx.media3.common.C;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.common.util.Util;
import androidx.media3.exoplayer.LoadControl;
import androidx.media3.exoplayer.upstream.Allocator;
import androidx.media3.exoplayer.upstream.DefaultAllocator;
//无用
@OptIn(markerClass = UnstableApi.class)
class CustomWeakNetworkLoadControl  implements LoadControl {
    private final DefaultAllocator allocator;
    private final long minBufferUs;
    private final long maxBufferUs;
    private final long bufferForPlaybackUs;
    private final long bufferForPlaybackAfterRebufferUs;
    private int targetBufferBytes;
    private boolean isLoading;


    public CustomWeakNetworkLoadControl() {
        this.allocator = new DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE);
        this.minBufferUs = Util.msToUs(30000); // 减少最小缓冲到30秒
        this.maxBufferUs = Util.msToUs(60000); // 增加最大缓冲到60秒
        this.bufferForPlaybackUs = Util.msToUs(1500); // 降低启动播放阈值
        this.bufferForPlaybackAfterRebufferUs = Util.msToUs(3000); // 降低重缓冲恢复阈值
        //this.targetBufferBytes = calculateTargetBufferBytes();
        this.allocator.setTargetBufferSize(targetBufferBytes);
    }

    @Override
    public Allocator getAllocator() {
        return null;
    }

    @Override
    public boolean shouldContinueLoading(long playbackPositionUs, long bufferedDurationUs, float playbackSpeed) {
        // 自定义加载逻辑：弱网下降低目标缓冲大小
        boolean targetBufferReached = allocator.getTotalBytesAllocated() >= targetBufferBytes;
        boolean bufferDurationReached = bufferedDurationUs >= maxBufferUs;

        // 网络波动时动态调整：缓冲不足时始终加载
        if (bufferedDurationUs < minBufferUs) {
            isLoading = true;
        } else if (bufferDurationReached || targetBufferReached) {
            isLoading = false;
        }
        return isLoading;
    }


    public boolean shouldStartPlayback(long bufferedDurationUs, float playbackSpeed, boolean rebuffering) {
        // 播放速度补偿：倍速播放时增加缓冲要求
        long requiredBufferUs = rebuffering ? bufferForPlaybackAfterRebufferUs : bufferForPlaybackUs;
        if (playbackSpeed > 1.5f) {
            requiredBufferUs *= playbackSpeed; // 倍速播放时按比例增加缓冲要求
        }
        return bufferedDurationUs >= requiredBufferUs;
    }


}
