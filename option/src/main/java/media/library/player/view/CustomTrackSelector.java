package media.library.player.view;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.google.common.collect.ImmutableList;

import java.util.HashSet;
import java.util.Set;

import androidx.annotation.OptIn;
import androidx.media3.common.C;
import androidx.media3.common.Format;
import androidx.media3.common.TrackGroup;
import androidx.media3.common.TrackSelectionParameters;
import androidx.media3.common.Tracks;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.RendererCapabilities;
import androidx.media3.exoplayer.source.TrackGroupArray;
import androidx.media3.exoplayer.trackselection.AdaptiveTrackSelection;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.exoplayer.trackselection.MappingTrackSelector;
import media.library.player.manager.PlayerLog;

//轨道
public class CustomTrackSelector {
    private String tag = "轨道_";
    private BaseExoPlayer baseExoPlayer;
    private Context playerContext;

    public CustomTrackSelector(BaseExoPlayer baseExoPlayer, Context context) {
        this.baseExoPlayer = baseExoPlayer;
        this.playerContext = context;
    }

    private boolean isArb;

    public void setIsArb(boolean isArb) {
        this.isArb = isArb;
    }

    @OptIn(markerClass = UnstableApi.class)
    private DefaultTrackSelector trackSelector;

    public void setRelease() {
        trackSelector = null;
    }

    @OptIn(markerClass = UnstableApi.class)
    public DefaultTrackSelector getPlayerTrackSelector() {
        //控制多清晰度视频 怎么自动升降清晰度的。
        if (trackSelector == null) {
            // 动态码率切换（ABR）的核心组件，通过智能选择最优码率轨道来平衡播放流畅性和画质
            //minDurationForQualityIncreaseMs	切换到更高质量轨道所需的最小缓冲时长	点播：5-8k，直播：15k+
            //maxDurationForQualityDecreaseMs	当缓冲时长低于此值时触发质量降低	波动网络：15k，稳定网络：30k
            //minDurationToRetainAfterDiscardMs	切换高质量轨道时需保留的低质量缓冲最小时长	必须 > 质量提升阈值
            //bandwidthFraction	带宽利用率系数（0-1），预留余量应对波动	弱网：0.5-0.6，优质网络：0.8-0.85
            //bufferedFractionToLiveEdgeForQualityIncrease	直播场景中需缓冲至直播边缘的比例才能提升质量
            //自定义Factory实现差异化配置
            AdaptiveTrackSelection.Factory factoryTemp = new AdaptiveTrackSelection.Factory(
                    8 * 1000,  // 质量提升阈值
                    20 * 1000, // 质量降低阈值
                    25 * 1000, // 保留缓冲
                    0.8f  // 带宽利用率
            );
            //创建轨道选择器，并把这套 ABR 策略给它
            trackSelector = new DefaultTrackSelector(playerContext, factoryTemp);
        }
        applyTrackSelectorParameters();
        return trackSelector;
    }

    @OptIn(markerClass = UnstableApi.class)
    protected void applyTrackSelectorParameters() {
        if (trackSelector == null) {
            return;
        }
        DefaultTrackSelector.Parameters.Builder builder = trackSelector.buildUponParameters();
        if (isArb) {
            PlayerLog.d(tag, "开启 ABR 轨道限制：最大1080P，最大10Mbps");
            //仅能在多轨道媒体源中，筛选出分辨率和码率低于设定值的备选
            builder.setMaxVideoSize(1920, 1080);
            builder.setMaxVideoBitrate(10 * 1024 * 1024);
        } else {
            //关闭 ABR 限制时恢复为不限制视频尺寸和码率，音轨/字幕切换仍然可用
            builder.setMaxVideoSize(Integer.MAX_VALUE, Integer.MAX_VALUE);
            builder.setMaxVideoBitrate(Integer.MAX_VALUE);
        }
        trackSelector.setParameters(builder);
    }

    // 按语言优先选择音轨/视频轨道 主要作用在多音轨视频里。
    //比如一个视频里有多条音轨：
    //中文音轨 zh
    //英文音轨 en
    //日文音轨 ja
    //setPreferredAudioLanguage("zh"); 优先选择中文音轨
    @OptIn(markerClass = UnstableApi.class)
    protected void setPreferredAudioLanguage(final String... language) {
        // 语言码：中文=zh, 英文=en, 日文=ja
        runOnPlayerThread(new Runnable() {
            @Override
            public void run() {
                if (trackSelector == null) {
                    return;
                }
                //拿当前轨道选择参数，基于旧配置继续修改。
                DefaultTrackSelector.Parameters.Builder paramsBuilder = trackSelector.buildUponParameters();
                //只传一个语言
                if (language.length == 1) {
                    // 启用音画同步（默认开启）
                    paramsBuilder.setPreferredAudioLanguage(language[0]);//这句一般意义不大，可以保留，但多数情况下不会起作用。音频语言才是核心。
                    paramsBuilder.setPreferredVideoLanguage(language[0]);
                } else {
                    // 启用音画同步（默认开启）
                    paramsBuilder.setPreferredAudioLanguages(language);
                    paramsBuilder.setPreferredVideoLanguages(language);
                }
                DefaultTrackSelector.Parameters params = paramsBuilder.build();
                trackSelector.setParameters(params);
            }
        });
    }

    //禁用视频 / 音频 / 字幕轨道
    @OptIn(markerClass = UnstableApi.class)
    protected void disableTrackType(final int trackType) {
        Set<Integer> set = new HashSet<>();
        set.add(trackType);
        disableTrackType(set);
    }

    //禁用视频 / 音频 / 字幕轨道
    @OptIn(markerClass = UnstableApi.class)
    protected void disableTrackType(Set<Integer> trackTypes) {
        runOnPlayerThread(new Runnable() {
            @Override
            public void run() {
                if (trackSelector == null) {
                    return;
                }
                //基于当前参数，设置“禁用这些轨道类型 trackType”。
                TrackSelectionParameters params =
                        trackSelector.buildUponParameters()
                                .setDisabledTrackTypes(trackTypes) // 禁用指定轨道类型
                                .build();
                trackSelector.setParameters(params);
            }
        });
    }

    // “显示/隐藏字幕，并且可指定优先字幕语言”。
    @OptIn(markerClass = UnstableApi.class)
    protected void setPreferredTextTrack(final boolean showText, final String preferredLanguage) {
        runOnPlayerThread(new Runnable() {
            @Override
            public void run() {
                if (trackSelector == null) {
                    return;
                }
                Set<Integer> set = new HashSet<>(trackSelector.getParameters().disabledTrackTypes);
                if (!showText) {
                    set.add(C.TRACK_TYPE_TEXT);
                } else {
                    set.remove(C.TRACK_TYPE_TEXT);
                }
                DefaultTrackSelector.Parameters.Builder paramsBuilder =
                        trackSelector.buildUponParameters()
                                .setDisabledTrackTypes(set);  // 是否禁用字幕
                if (!TextUtils.isEmpty(preferredLanguage)) {
                    paramsBuilder.setPreferredTextLanguage(preferredLanguage); // 字幕优先语言
                }
                TrackSelectionParameters params = paramsBuilder.build();
                trackSelector.setParameters(params);
            }
        });
    }

    //限制播放器最多只能选择“码率不超过这个值”的视频轨道
    @OptIn(markerClass = UnstableApi.class)
    protected void setVideoBitrate(int maxVideoBitrate) {
        if (maxVideoBitrate <= 0) {
            return;
        }
        runOnPlayerThread(new Runnable() {
            @Override
            public void run() {
                if (trackSelector == null) {
                    return;
                }
                // 根据带宽调整ABR策略，保留20%余量
                DefaultTrackSelector.Parameters.Builder builder =
                        trackSelector.buildUponParameters()
                                .setMaxVideoBitrate(maxVideoBitrate);
                trackSelector.setParameters(builder);
            }
        });

    }
    //============================打印轨道信息==============================

    @OptIn(markerClass = UnstableApi.class)
    protected void setTrackLog() {
        runOnPlayerThread(new Runnable() {
            @Override
            public void run() {
                if (trackSelector == null) {
                    PlayerLog.d(tag, "轨道信息：trackSelector为空");
                    return;
                }
                DefaultTrackSelector.Parameters params = trackSelector.getParameters();
                MappingTrackSelector.MappedTrackInfo infos = trackSelector.getCurrentMappedTrackInfo();
                if (infos == null) {
                    PlayerLog.d(tag, "轨道信息：暂无轨道映射信息，通常是还没有prepare完成");
                    return;
                }
                PlayerLog.d(tag, "轨道选择参数：" + getTrackSelectionParamsLog(params));
                int rendererCount = infos.getRendererCount();
                PlayerLog.d(tag, "轨道渲染器数量：" + rendererCount);
                for (int rendererIndex = 0; rendererIndex < rendererCount; rendererIndex++) {
                    int rendererType = infos.getRendererType(rendererIndex);
                    int rendererSupport = infos.getRendererSupport(rendererIndex);
                    TrackGroupArray trackGroups = infos.getTrackGroups(rendererIndex);
                    PlayerLog.d(tag, "渲染器[" + rendererIndex + "]"
                            + " 名称=" + infos.getRendererName(rendererIndex)
                            + " 类型=" + getTrackTypeName(rendererType)
                            + " 支持情况=" + getRendererSupportName(rendererSupport)
                            + " 轨道组数量=" + trackGroups.length);

                    for (int groupIndex = 0; groupIndex < trackGroups.length; groupIndex++) {
                        TrackGroup group = trackGroups.get(groupIndex);
                        int adaptiveSupport = infos.getAdaptiveSupport(rendererIndex, groupIndex, false);
                        PlayerLog.d(tag, "轨道组[" + rendererIndex + "][" + groupIndex + "]"
                                + " 轨道数量=" + group.length
                                + " 自适应切换=" + getAdaptiveSupportName(adaptiveSupport));
                        for (int trackIndex = 0; trackIndex < group.length; trackIndex++) {
                            Format format = group.getFormat(trackIndex);
                            int trackSupport = infos.getTrackSupport(rendererIndex, groupIndex, trackIndex);
                            PlayerLog.d(tag, "轨道[" + rendererIndex + "][" + groupIndex + "][" + trackIndex + "]"
                                    + " 是否选中=" + isCurrentTrackSelected(format)
                                    + " 支持情况=" + getFormatSupportName(trackSupport)
                                    + " " + getFormatLog(format));
                        }
                    }
                }

                TrackGroupArray unmappedTrackGroups = infos.getUnmappedTrackGroups();
                if (unmappedTrackGroups != null && unmappedTrackGroups.length > 0) {
                    PlayerLog.d(tag, "未映射轨道组数量：" + unmappedTrackGroups.length);
                    for (int groupIndex = 0; groupIndex < unmappedTrackGroups.length; groupIndex++) {
                        TrackGroup group = unmappedTrackGroups.get(groupIndex);
                        PlayerLog.d(tag, "未映射轨道组[" + groupIndex + "] 轨道数量=" + group.length);
                        for (int trackIndex = 0; trackIndex < group.length; trackIndex++) {
                            PlayerLog.d(tag, "未映射轨道[" + groupIndex + "][" + trackIndex + "] "
                                    + getFormatLog(group.getFormat(trackIndex)));
                        }
                    }
                }
            }
        });
    }

    @OptIn(markerClass = UnstableApi.class)
    private String getTrackSelectionParamsLog(DefaultTrackSelector.Parameters params) {
        if (params == null) {
            return "空";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("禁用轨道类型=").append(params.disabledTrackTypes);
        builder.append(" 视频最大宽高=").append(formatIntLimit(params.maxVideoWidth))
                .append("x").append(formatIntLimit(params.maxVideoHeight));
        builder.append(" 视频最大码率=").append(formatBitrate(params.maxVideoBitrate));
        builder.append(" 音频最大声道=").append(formatIntLimit(params.maxAudioChannelCount));
        builder.append(" 音频最大码率=").append(formatBitrate(params.maxAudioBitrate));
        builder.append(" 首选视频语言=").append(params.preferredVideoLanguages);
        builder.append(" 首选音频语言=").append(params.preferredAudioLanguages);
        builder.append(" 首选字幕语言=").append(params.preferredTextLanguages);
        builder.append(" 强制最低码率=").append(params.forceLowestBitrate);
        builder.append(" 强制最高支持码率=").append(params.forceHighestSupportedBitrate);
        builder.append(" 字幕默认选择=").append(params.selectTextByDefault);
        builder.append(" 选择未知语言字幕=").append(params.selectUndeterminedTextLanguage);
        builder.append(" 覆盖轨道数量=").append(params.overrides == null ? 0 : params.overrides.size());
        return builder.toString();
    }

    private boolean isCurrentTrackSelected(Format format) {
        ExoPlayer player = baseExoPlayer.player;
        if (player == null || format == null) {
            return false;
        }
        Tracks currentTracks = player.getCurrentTracks();
        if (currentTracks == null) {
            return false;
        }
        ImmutableList<Tracks.Group> groups = currentTracks.getGroups();
        for (int groupIndex = 0; groupIndex < groups.size(); groupIndex++) {
            Tracks.Group group = groups.get(groupIndex);
            for (int trackIndex = 0; trackIndex < group.length; trackIndex++) {
                Format trackFormat = group.getTrackFormat(trackIndex);
                if ((format == trackFormat || format.equals(trackFormat)) && group.isTrackSelected(trackIndex)) {
                    return true;
                }
            }
        }
        return false;
    }

    @OptIn(markerClass = UnstableApi.class)
    private String getFormatLog(Format format) {
        if (format == null) {
            return "格式信息=空";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("id=").append(emptyToUnknown(format.id));
        builder.append(" label=").append(emptyToUnknown(format.label));
        builder.append(" language=").append(emptyToUnknown(format.language));
        builder.append(" containerMimeType=").append(emptyToUnknown(format.containerMimeType));
        builder.append(" sampleMimeType=").append(emptyToUnknown(format.sampleMimeType));
        builder.append(" codecs=").append(emptyToUnknown(format.codecs));
        builder.append(" bitrate=").append(formatBitrate(format.bitrate));
        builder.append(" averageBitrate=").append(formatBitrate(format.averageBitrate));
        builder.append(" peakBitrate=").append(formatBitrate(format.peakBitrate));
        builder.append(" resolution=").append(formatResolution(format.width, format.height));
        builder.append(" decodedResolution=").append(formatResolution(format.decodedWidth, format.decodedHeight));
        builder.append(" frameRate=").append(formatFrameRate(format.frameRate));
        builder.append(" rotation=").append(formatIntValue(format.rotationDegrees)).append("°");
        builder.append(" pixelRatio=").append(formatFloatValue(format.pixelWidthHeightRatio));
        builder.append(" colorInfo=").append(format.colorInfo == null ? "无" : format.colorInfo.toString());
        builder.append(" channelCount=").append(formatIntValue(format.channelCount));
        builder.append(" sampleRate=").append(formatIntValue(format.sampleRate));
        builder.append(" pcmEncoding=").append(formatIntValue(format.pcmEncoding));
        builder.append(" selectionFlags=").append(getSelectionFlagsName(format.selectionFlags));
        builder.append(" roleFlags=").append(getRoleFlagsName(format.roleFlags));
        builder.append(" drm=").append(format.drmInitData == null ? "无" : "有");
        return builder.toString();
    }

    private String getTrackTypeName(int trackType) {
        switch (trackType) {
            case C.TRACK_TYPE_NONE:
                return "无(" + trackType + ")";
            case C.TRACK_TYPE_UNKNOWN:
                return "未知(" + trackType + ")";
            case C.TRACK_TYPE_DEFAULT:
                return "默认(" + trackType + ")";
            case C.TRACK_TYPE_AUDIO:
                return "音频(" + trackType + ")";
            case C.TRACK_TYPE_VIDEO:
                return "视频(" + trackType + ")";
            case C.TRACK_TYPE_TEXT:
                return "字幕(" + trackType + ")";
            case C.TRACK_TYPE_IMAGE:
                return "图片(" + trackType + ")";
            case C.TRACK_TYPE_METADATA:
                return "元数据(" + trackType + ")";
            case C.TRACK_TYPE_CAMERA_MOTION:
                return "相机运动(" + trackType + ")";
            default:
                return "自定义/未知(" + trackType + ")";
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private String getRendererSupportName(int rendererSupport) {
        switch (rendererSupport) {
            case MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_NO_TRACKS:
                return "无轨道(" + rendererSupport + ")";
            case MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS:
                return "有轨道但不支持(" + rendererSupport + ")";
            case MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_EXCEEDS_CAPABILITIES_TRACKS:
                return "轨道超出能力(" + rendererSupport + ")";
            case MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_PLAYABLE_TRACKS:
                return "可播放(" + rendererSupport + ")";
            default:
                return "未知(" + rendererSupport + ")";
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private String getFormatSupportName(int formatSupport) {
        switch (formatSupport) {
            case C.FORMAT_HANDLED:
                return "支持播放(" + formatSupport + ")";
            case C.FORMAT_EXCEEDS_CAPABILITIES:
                return "格式类型支持但参数超出设备能力(" + formatSupport + ")";
            case C.FORMAT_UNSUPPORTED_DRM:
                return "DRM不支持(" + formatSupport + ")";
            case C.FORMAT_UNSUPPORTED_SUBTYPE:
                return "同类媒体支持但该编码/子类型不支持(" + formatSupport + ")";
            case C.FORMAT_UNSUPPORTED_TYPE:
                return "媒体类型不支持(" + formatSupport + ")";
            default:
                return "未知(" + formatSupport + ")";
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private String getAdaptiveSupportName(int adaptiveSupport) {
        switch (adaptiveSupport) {
            case RendererCapabilities.ADAPTIVE_SEAMLESS:
                return "支持无缝自适应切换(" + adaptiveSupport + ")";
            case RendererCapabilities.ADAPTIVE_NOT_SEAMLESS:
                return "支持非无缝自适应切换(" + adaptiveSupport + ")";
            case RendererCapabilities.ADAPTIVE_NOT_SUPPORTED:
                return "不支持自适应切换(" + adaptiveSupport + ")";
            default:
                return "未知(" + adaptiveSupport + ")";
        }
    }

    private String getSelectionFlagsName(int flags) {
        if (flags == 0) {
            return "无(0)";
        }
        StringBuilder builder = new StringBuilder();
        appendFlag(builder, flags, C.SELECTION_FLAG_DEFAULT, "默认");
        appendFlag(builder, flags, C.SELECTION_FLAG_FORCED, "强制");
        appendFlag(builder, flags, C.SELECTION_FLAG_AUTOSELECT, "自动选择");
        appendUnknownFlags(builder, flags, C.SELECTION_FLAG_DEFAULT | C.SELECTION_FLAG_FORCED | C.SELECTION_FLAG_AUTOSELECT);
        return builder.toString();
    }

    private String getRoleFlagsName(int flags) {
        if (flags == 0) {
            return "无(0)";
        }
        StringBuilder builder = new StringBuilder();
        appendFlag(builder, flags, C.ROLE_FLAG_MAIN, "主轨道");
        appendFlag(builder, flags, C.ROLE_FLAG_ALTERNATE, "备用");
        appendFlag(builder, flags, C.ROLE_FLAG_SUPPLEMENTARY, "补充");
        appendFlag(builder, flags, C.ROLE_FLAG_COMMENTARY, "评论音轨");
        appendFlag(builder, flags, C.ROLE_FLAG_DUB, "配音");
        appendFlag(builder, flags, C.ROLE_FLAG_EMERGENCY, "紧急");
        appendFlag(builder, flags, C.ROLE_FLAG_CAPTION, "隐藏字幕");
        appendFlag(builder, flags, C.ROLE_FLAG_SUBTITLE, "字幕");
        appendFlag(builder, flags, C.ROLE_FLAG_SIGN, "手语");
        appendFlag(builder, flags, C.ROLE_FLAG_DESCRIBES_VIDEO, "视频描述");
        appendFlag(builder, flags, C.ROLE_FLAG_DESCRIBES_MUSIC_AND_SOUND, "音乐音效描述");
        appendFlag(builder, flags, C.ROLE_FLAG_ENHANCED_DIALOG_INTELLIGIBILITY, "增强对白");
        appendFlag(builder, flags, C.ROLE_FLAG_TRANSCRIBES_DIALOG, "对白转写");
        appendFlag(builder, flags, C.ROLE_FLAG_EASY_TO_READ, "易读");
        appendFlag(builder, flags, C.ROLE_FLAG_TRICK_PLAY, "特技播放");
        appendFlag(builder, flags, C.ROLE_FLAG_AUXILIARY, "辅助");
        appendUnknownFlags(builder, flags,
                C.ROLE_FLAG_MAIN
                        | C.ROLE_FLAG_ALTERNATE
                        | C.ROLE_FLAG_SUPPLEMENTARY
                        | C.ROLE_FLAG_COMMENTARY
                        | C.ROLE_FLAG_DUB
                        | C.ROLE_FLAG_EMERGENCY
                        | C.ROLE_FLAG_CAPTION
                        | C.ROLE_FLAG_SUBTITLE
                        | C.ROLE_FLAG_SIGN
                        | C.ROLE_FLAG_DESCRIBES_VIDEO
                        | C.ROLE_FLAG_DESCRIBES_MUSIC_AND_SOUND
                        | C.ROLE_FLAG_ENHANCED_DIALOG_INTELLIGIBILITY
                        | C.ROLE_FLAG_TRANSCRIBES_DIALOG
                        | C.ROLE_FLAG_EASY_TO_READ
                        | C.ROLE_FLAG_TRICK_PLAY
                        | C.ROLE_FLAG_AUXILIARY);
        return builder.toString();
    }

    private void appendFlag(StringBuilder builder, int flags, int flag, String name) {
        if ((flags & flag) == flag) {
            if (builder.length() > 0) {
                builder.append("|");
            }
            builder.append(name);
        }
    }

    private void appendUnknownFlags(StringBuilder builder, int flags, int knownFlags) {
        int unknownFlags = flags & ~knownFlags;
        if (unknownFlags != 0) {
            if (builder.length() > 0) {
                builder.append("|");
            }
            builder.append("未知标记=").append(unknownFlags);
        }
        builder.append("(").append(flags).append(")");
    }

    private String formatResolution(int width, int height) {
        if (width == Format.NO_VALUE || height == Format.NO_VALUE) {
            return "未知";
        }
        return width + "x" + height;
    }

    private String formatBitrate(int bitrate) {
        if (bitrate == Format.NO_VALUE || bitrate == Integer.MAX_VALUE) {
            return bitrate == Integer.MAX_VALUE ? "不限制" : "未知";
        }
        return bitrate + "bps(" + String.format("%.2fMbps", (float) bitrate / 1024 / 1024) + ")";
    }

    private String formatFrameRate(float frameRate) {
        if (frameRate == Format.NO_VALUE) {
            return "未知";
        }
        return String.format("%.2ffps", frameRate);
    }

    private String formatFloatValue(float value) {
        if (value == Format.NO_VALUE) {
            return "未知";
        }
        return String.valueOf(value);
    }

    private String formatIntValue(int value) {
        if (value == Format.NO_VALUE) {
            return "未知";
        }
        return String.valueOf(value);
    }

    private String formatIntLimit(int value) {
        if (value == Integer.MAX_VALUE) {
            return "不限制";
        }
        if (value == Format.NO_VALUE) {
            return "未知";
        }
        return String.valueOf(value);
    }

    private String emptyToUnknown(String value) {
        if (TextUtils.isEmpty(value)) {
            return "未知";
        }
        return value;
    }

    //==============
    private void runOnPlayerThread(Runnable runnable) {
        baseExoPlayer.runOnPlayerThread(runnable);
    }

}
