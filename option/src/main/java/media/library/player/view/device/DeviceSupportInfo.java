package media.library.player.view.device;

import java.util.ArrayList;
import java.util.List;

/**
 * 视频编码支持信息。
 */
public class DeviceSupportInfo {
    //支持的视频编码 MIME 类型，例如 video/avc、video/hevc
    public final String supportedVideoMimeType;
    //支持的最小宽度
    public final int minWidth;
    //支持的最大宽度
    public final int maxWidth;
    //支持的最小高度
    public final int minHeight;
    //支持的最大高度
    public final int maxHeight;
    //该视频编码类型支持的常见分辨率
    public final List<String> commonResolutions;
    //支持该编码的解码器名称
    public final List<String> decoderNames;

    public DeviceSupportInfo(
            String supportedVideoMimeType,
            int minWidth,
            int maxWidth,
            int minHeight,
            int maxHeight,
            List<String> commonResolutions,
            List<String> decoderNames) {
        this.supportedVideoMimeType = supportedVideoMimeType;
        this.minWidth = minWidth;
        this.maxWidth = maxWidth;
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;
        this.commonResolutions = new ArrayList<>(commonResolutions);
        this.decoderNames = new ArrayList<>(decoderNames);
    }

    @Override
    public String toString() {
        return "VideoSupportInfo{"
                + "视频编码类型='" + supportedVideoMimeType + '\''
                + ", 支持的宽度=" + minWidth + "-" + maxWidth
                + ", 支持的高度=" + minHeight + "-" + maxHeight
                + ", 该视频编码类型支持的常见分辨率=" + commonResolutions
                + ", 支持该编码的解码器名称=" + decoderNames
                + '}';
    }
}
