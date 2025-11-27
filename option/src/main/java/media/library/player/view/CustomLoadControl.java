package media.library.player.view;


import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.DefaultLoadControl;

//无用
@UnstableApi
class CustomLoadControl extends DefaultLoadControl {
    private DefaultLoadControl buff = null;

    public void init() {
        DefaultLoadControl.Builder build = new DefaultLoadControl.Builder();
        build.setBufferDurationsMs(
                15000,  // 最小缓冲时间（触发加载的阈值
                30000,  // 最大缓冲时间（停止加载的阈值）
                2500,  // 播放开始前预缓冲时间
                5000  // 重新缓冲后预缓冲时间
        );
 //        build.setTargetBufferBytes(getTargetBufferBytes());
        build.setPrioritizeTimeOverSizeThresholds(true); //优先时间阈值而非数据量阈值
        //禁止回退缓存 一般直播用
        //build.setBackBuffer(0, false);
        DefaultLoadControl loadControl = build.build();
        buff = loadControl;

    }

    @Override
    public boolean shouldContinueLoading(Parameters parameters) {
        // 示例：根据播放进度决定是否继续加载
        //player.getBufferedPosition() < player.getContentPosition();
        return super.shouldContinueLoading(parameters);
    }


}
