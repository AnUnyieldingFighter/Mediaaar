package media.library.player.view.device;

import java.util.ArrayList;
import java.util.List;

/**
 * 单个视频地址的媒体信息和本设备支持结果。
 */
public class PlayerVideoInfo {

    //视频地址
    public String videoUrl;
    //是否成功读取媒体信息
    public boolean parseSuccess;
    //读取失败原因
    public String errorMessage;
    //容器 MIME，例如 video/mp4
    public String containerMimeType;
    //视频总时长，单位毫秒
    public long durationMs = -1;
    //轨道总数
    public int trackCount;

    //是否有视频轨道
    public boolean hasVideo;
    //视频轨道数量
    public int videoTrackCount;
    //第一个视频轨道编码 MIME，例如 video/avc、video/hevc
    public String videoMimeType;
    //第一个视频轨道宽度
    public int videoWidth = -1;
    //第一个视频轨道高度
    public int videoHeight = -1;
    //第一个视频轨道旋转角度
    public int videoRotation;
    //第一个视频轨道帧率
    public int videoFrameRate = -1;
    //第一个视频轨道码率
    public int videoBitrate = -1;
    //第一个视频轨道本设备是否支持
    public boolean videoSupported;
    //第一个视频轨道命中的解码器名称
    public String videoDecoderName;
    //第一个视频轨道支持说明
    public String videoSupportMessage;
    //是否至少有一个视频轨道被设备支持
    public boolean hasSupportedVideoTrack;

    //是否有音频轨道
    public boolean hasAudio;
    //音频轨道数量
    public int audioTrackCount;
    //第一个音频轨道编码 MIME，例如 audio/mp4a-latm
    public String audioMimeType;
    //第一个音频轨道声道数
    public int audioChannelCount = -1;
    //第一个音频轨道采样率
    public int audioSampleRate = -1;
    //第一个音频轨道码率
    public int audioBitrate = -1;
    //第一个音频轨道语言
    public String audioLanguage;
    //第一个音频轨道本设备是否支持
    public boolean audioSupported;
    //第一个音频轨道命中的解码器名称
    public String audioDecoderName;
    //第一个音频轨道支持说明
    public String audioSupportMessage;
    //是否至少有一个音频轨道被设备支持
    public boolean hasSupportedAudioTrack;

    //是否有字幕轨道
    public boolean hasSubtitle;
    //字幕轨道数量
    public int subtitleTrackCount;

    //所有视频轨道信息
    public final List<String> videoTrackInfos = new ArrayList<>();
    //所有音频轨道信息
    public final List<String> audioTrackInfos = new ArrayList<>();
    //所有字幕轨道信息
    public final List<String> subtitleTrackInfos = new ArrayList<>();
    //其他轨道信息
    public final List<String> otherTrackInfos = new ArrayList<>();

    //本设备是否支持播放
    public boolean deviceSupportPlay;
    //本设备播放支持说明
    public String deviceSupportMessage;

    @Override
    public String toString() {
        return "PlayerVideoInfo{"
                + "视频地址='" + videoUrl + '\''
                + "\n是否成功读取媒体信息=" + parseSuccess
                + "\n读取失败原因='" + errorMessage + '\''
                + "\n容器类型='" + containerMimeType + '\''
                + "\n视频总时长ms=" + durationMs
                + "\n轨道总数=" + trackCount
                + "\n是否有视频轨道=" + hasVideo
                + "\n视频轨道数量=" + videoTrackCount
                + "\n视频编码='" + videoMimeType + '\''
                + "\n视频分辨率=" + videoWidth + "x" + videoHeight
                + "\n视频旋转角度=" + videoRotation
                + "\n视频帧率=" + videoFrameRate
                + "\n视频码率=" + videoBitrate
                + "\n视频是否支持=" + videoSupported
                + "\n视频解码器名称='" + videoDecoderName + '\''
                + "\n视频支持说明='" + videoSupportMessage + '\''
                + "\n是否有音频轨道=" + hasAudio
                + "\n音频轨道数量=" + audioTrackCount
                + "\n音频编码='" + audioMimeType + '\''
                + "\n音频声道数=" + audioChannelCount
                + "\n音频采样率=" + audioSampleRate
                + "\n音频码率=" + audioBitrate
                + "\n音频语言='" + audioLanguage + '\''
                + "\n音频是否支持=" + audioSupported
                + "\n音频解码器名称='" + audioDecoderName + '\''
                + "\n音频支持说明='" + audioSupportMessage + '\''
                + "\n是否有字幕轨道=" + hasSubtitle
                + "\n字幕轨道数量=" + subtitleTrackCount
                + "\n视频轨道明细=" + videoTrackInfos
                + "\n音频轨道明细=" + audioTrackInfos
                + "\n字幕轨道明细=" + subtitleTrackInfos
                + "\n其他轨道明细=" + otherTrackInfos
                + "\n本设备是否支持播放=" + deviceSupportPlay
                + "\n本设备播放支持说明='" + deviceSupportMessage + '\''
                + '}';
    }
}
