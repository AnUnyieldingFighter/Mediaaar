package media.library.player.view;

import android.net.Uri;

import androidx.annotation.OptIn;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.util.UnstableApi;

import java.util.ArrayList;
import java.util.List;

/**
 * MediaItem 配置示例。
 *
 * <p>MediaItem 本身不负责播放，它只是告诉 ExoPlayer：
 * 播放地址是什么、媒体标识是什么、媒体类型是什么、有没有字幕、有没有 DRM、
 * 有没有直播参数、有没有标题封面等配置。</p>
 */
public class MediaItemConfig {

    private MediaItemConfig() {
    }

    /**
     * 最基础的配置。
     *
     * <p>只配置播放地址和媒体 ID，普通 mp4/http/file/content 地址一般这样就够了。</p>
     */
    public static MediaItem createBaseMediaItem(String videoUrl) {
        return new MediaItem.Builder()
                //真正播放的地址
                .setUri(videoUrl)
                //媒体唯一标识，方便后面通过 player.getCurrentMediaItem().mediaId 找回当前视频
                .setMediaId(videoUrl)
                .build();
    }

    /**
     * HLS/m3u8 配置示例。
     *
     * <p>确定地址是 m3u8 时，可以明确设置 APPLICATION_M3U8，避免 URL 无后缀或响应头不准时识别错误。</p>
     */
    public static MediaItem createHlsMediaItem(String m3u8Url) {
        return new MediaItem.Builder()
                .setUri(m3u8Url)
                .setMediaId(m3u8Url)
                .setMimeType(MimeTypes.APPLICATION_M3U8)
                .build();
    }

    /**
     * HTTP-FLV 配置示例。
     *
     * <p>Media3 没有单独的 FLV MediaSource，这里一般配合 ProgressiveMediaSource 使用。</p>
     */
    public static MediaItem createFlvMediaItem(String flvUrl) {
        return new MediaItem.Builder()
                .setUri(flvUrl)
                .setMediaId(flvUrl)
                .setMimeType("video/x-flv")
                .build();
    }

    /**
     * 媒体展示信息配置示例。
     *
     * <p>这些信息不会自动显示到 PlayerView 上，主要给通知栏、锁屏、车机、投屏、
     * 或者你自己的 UI 读取展示。</p>
     */
    public static MediaItem createMetadataMediaItem(
            String videoUrl,
            String title,
            String artist,
            String subtitle,
            String description,
            String albumTitle,
            String artworkUrl) {
        MediaMetadata mediaMetadata = new MediaMetadata.Builder()
                //标题
                .setTitle(title)
                //作者/艺术家
                .setArtist(artist)
                //副标题
                .setSubtitle(subtitle)
                //描述
                .setDescription(description)
                //专辑名
                .setAlbumTitle(albumTitle)
                //封面地址
                .setArtworkUri(Uri.parse(artworkUrl))
                .build();

        return new MediaItem.Builder()
                .setUri(videoUrl)
                .setMediaId(videoUrl)
                .setMediaMetadata(mediaMetadata)
                .build();
    }

    /**
     * 外挂字幕配置示例。
     *
     * <p>常见字幕 MIME：
     * vtt 使用 MimeTypes.TEXT_VTT；
     * srt 使用 MimeTypes.APPLICATION_SUBRIP；
     * ttml 使用 MimeTypes.APPLICATION_TTML。</p>
     */
    public static MediaItem createSubtitleMediaItem(String videoUrl, String subtitleUrl,
            String subtitleMimeType,
            String language) {
        List<MediaItem.SubtitleConfiguration> subtitleConfigurations = new ArrayList<>();
        MediaItem.SubtitleConfiguration subtitleConfiguration =
                new MediaItem.SubtitleConfiguration.Builder(Uri.parse(subtitleUrl))
                        //字幕类型，例如 MimeTypes.TEXT_VTT、MimeTypes.APPLICATION_SUBRIP
                        .setMimeType(subtitleMimeType)
                        //字幕语言，例如 zh、en、ja
                        .setLanguage(language)
                        //默认选中这条字幕
                        .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                        .build();
        subtitleConfigurations.add(subtitleConfiguration);

        return new MediaItem.Builder()
                .setUri(videoUrl)
                .setMediaId(videoUrl)
                .setSubtitleConfigurations(subtitleConfigurations)
                .build();
    }

    /**
     * 裁剪播放范围配置示例。
     *
     * <p>例如只播放第 10 秒到第 30 秒。</p>
     */
    public static MediaItem createClippingMediaItem(
            String videoUrl,
            long startPositionMs,
            long endPositionMs) {
        MediaItem.ClippingConfiguration clippingConfiguration =
                new MediaItem.ClippingConfiguration.Builder()
                        //开始播放位置，单位毫秒
                        .setStartPositionMs(startPositionMs)
                        //结束播放位置，单位毫秒
                        .setEndPositionMs(endPositionMs)
                        .build();

        return new MediaItem.Builder()
                .setUri(videoUrl)
                .setMediaId(videoUrl)
                .setClippingConfiguration(clippingConfiguration)
                .build();
    }

    /**
     * 直播参数配置示例。
     *
     * <p>主要用于低延迟直播，控制目标延迟、最小/最大播放速度。
     * 普通点播视频不需要配置这个。</p>
     */
    public static MediaItem createLiveMediaItem(
            String liveUrl,
            long targetOffsetMs,
            long minOffsetMs,
            long maxOffsetMs) {
        MediaItem.LiveConfiguration liveConfiguration =
                new MediaItem.LiveConfiguration.Builder()
                        //目标直播延迟，单位毫秒
                        .setTargetOffsetMs(targetOffsetMs)
                        //最小直播延迟，单位毫秒
                        .setMinOffsetMs(minOffsetMs)
                        //最大直播延迟，单位毫秒
                        .setMaxOffsetMs(maxOffsetMs)
                        //追直播边缘时允许稍微加速
                        .setMaxPlaybackSpeed(1.03f)
                        //超前或稳定时允许稍微降速
                        .setMinPlaybackSpeed(0.97f)
                        .build();

        return new MediaItem.Builder()
                .setUri(liveUrl)
                .setMediaId(liveUrl)
                .setLiveConfiguration(liveConfiguration)
                .build();
    }

    /**
     * DRM 加密视频配置示例。
     *
     * <p>常见 Widevine 加密视频需要 licenseUrl。
     * 普通 mp4、m3u8 不需要这个配置。</p>
     */
    public static MediaItem createWidevineDrmMediaItem(String videoUrl, String licenseUrl) {
        MediaItem.DrmConfiguration drmConfiguration =
                new MediaItem.DrmConfiguration.Builder(C.WIDEVINE_UUID)
                        //DRM 授权服务器地址
                        .setLicenseUri(licenseUrl)
                        .build();

        return new MediaItem.Builder()
                .setUri(videoUrl)
                .setMediaId(videoUrl)
                .setDrmConfiguration(drmConfiguration)
                .build();
    }

    /**
     * 广告配置示例。
     *
     * <p>通常配合 IMA 广告扩展使用；当前项目没有接 IMA 时，这个只是示例。</p>
     */
    public static MediaItem createAdsMediaItem(String videoUrl, String adTagUrl) {
        MediaItem.AdsConfiguration adsConfiguration =
                new MediaItem.AdsConfiguration.Builder(Uri.parse(adTagUrl))
                        .build();

        return new MediaItem.Builder()
                .setUri(videoUrl)
                .setMediaId(videoUrl)
                .setAdsConfiguration(adsConfiguration)
                .build();
    }

    /**
     * 自定义业务对象配置示例。
     *
     * <p>tag 可以塞你自己的业务对象，例如视频实体、列表位置、来源页面等。
     * 播放器不会处理 tag，只是帮你保存起来。</p>
     */
    public static MediaItem createTagMediaItem(String videoUrl, Object tag) {
        return new MediaItem.Builder()
                .setUri(videoUrl)
                .setMediaId(videoUrl)
                .setTag(tag)
                .build();
    }

    /**
     * 缓存 key 配置示例。
     *
     * <p>当播放地址带 token、签名参数，但你希望这些地址复用同一份缓存时，可以设置自定义缓存 key。</p>
     */
    @OptIn(markerClass = UnstableApi.class)
    public static MediaItem createCacheKeyMediaItem(String videoUrl, String cacheKey) {
        return new MediaItem.Builder()
                .setUri(videoUrl)
                .setMediaId(videoUrl)
                .setCustomCacheKey(cacheKey)
                .build();
    }
}
