package media.library.player.view;

import android.text.TextUtils;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import androidx.annotation.OptIn;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.database.StandaloneDatabaseProvider;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.datasource.cache.CacheDataSource;
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor;
import androidx.media3.datasource.cache.NoOpCacheEvictor;
import androidx.media3.datasource.cache.SimpleCache;
import androidx.media3.exoplayer.drm.DrmSessionManager;
import androidx.media3.exoplayer.hls.HlsMediaSource;
import androidx.media3.exoplayer.rtsp.RtspMediaSource;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter;
import media.library.player.bean.VideoEntity;
import media.library.player.manager.PlayerLog;
import media.library.utils.FileUtil;
import media.library.utils.Md5Media;

/**
 * 设置/获取播放源。
 *
 * <p>当前支持的播放源类型：</p>
 * <ul>
 *     <li>普通视频文件/普通 HTTP 文件流：mp4、mov、webm 等，走 ProgressiveMediaSource。</li>
 *     <li>HLS/m3u8：可用于直播或点播，走 HlsMediaSource。</li>
 *     <li>RTSP：常用于监控、摄像头、实时流，走 RtspMediaSource。</li>
 *     <li>HTTP-FLV/.flv：常用于直播拉流，走 ProgressiveMediaSource，不走缓存。</li>
 * </ul>
 *
 * <p>当前不支持的播放源类型：</p>
 * <ul>
 *     <li>RTMP/RTMPS：media3-datasource-rtmp 依赖的 librtmp-jni.so 不支持 16KB page size，暂不接入。</li>
 *     <li>WebRTC：不是 ExoPlayer MediaSource 体系，需要单独接 WebRTC SDK、信令、PeerConnection。</li>
 * </ul>
 */
class BaseMediaSource extends BaseExoPlayer {
    //tru 开启缓存
    protected boolean isUseCache;

    //==========================设置播放源========================================
    @OptIn(markerClass = UnstableApi.class)
    public void setMediaSource(MediaSource mediaSource) {
        player.setMediaSource(mediaSource);
    }

    //==================================获取播放源=============================================
    private String img1 = "https://img0.baidu.com/it/u=3404601552,3434841255&fm=253&app=138&f=JPEG?w=800&h=1200";

    @OptIn(markerClass = UnstableApi.class)
    protected MediaSource getMediaSource() {
        String type = getMediaSourceType(videoUrl);
        //媒体元数据描述 不知道用法
        /*MediaMetadata mediaMetadata = new MediaMetadata.Builder()
                .setTitle("示例标题")
                .setArtist("示例艺术家")
                .setSubtitle("小标题")
                .setDescription("描述")
                .setAlbumTitle("示例专辑")
                .setArtworkUri(Uri.parse(img1))
                .build();*/
        //创建 MediaItem：播放这个资源所需的一组配置
        //根据播放源类型创建 MediaItem
        MediaItem videoItem;
        if ("m3u8".equals(type)) {
            videoItem = MediaItemConfig.createHlsMediaItem(videoUrl);
        } else if ("flv".equals(type)) {
            videoItem = MediaItemConfig.createFlvMediaItem(videoUrl);
        } else {
            videoItem = MediaItemConfig.createBaseMediaItem(videoUrl);
        }
        //告诉 ExoPlayer “这个资源要怎么读取、解析、加载
        MediaSource mediaSource;
        PlayerLog.d(tag, "播放器 创建播放源 type:" + type + " url:" + videoUrl);
        /*DefaultBandwidthMeter bandwidthMeter =
                new DefaultBandwidthMeter.Builder(playerContext).build();
        DefaultHttpDataSource.Factory httpFactory =
                new DefaultHttpDataSource.Factory()
                        .setTransferListener(bandwidthMeter);*/
        switch (type) {
            case "m3u8":
                // hls链接 里面记录了一段段 .ts 分片视频的地址，播放器按顺序逐个加载、拼接播放，实现流式播放。
                //m3u8 / HLS：可以直播，也可以点播
                DataSource.Factory factory = null;
                if (!isUseCache) {
                    factory = new DefaultDataSource.Factory(playerContext);
                    //factory = new DefaultDataSource.Factory(playerContext,httpFactory);
                } else {
                    setMediaSourceCacheRelease();
                    //构建缓存
                    simpleCache = createSimpleCache();
                    factory = new CacheDataSource.Factory()
                            .setCache(simpleCache)
                            .setUpstreamDataSourceFactory(new DefaultDataSource.Factory(playerContext))
                            //更适合网络视频
                            //.setUpstreamDataSourceFactory(new DefaultHttpDataSource.Factory().setUserAgent("Media3"))
                            //缓存出错时自动回退到原始源
                            //FLAG_BLOCK_ON_CACHE → 优先读缓存（你要的预加载生效！）
                            //FLAG_IGNORE_CACHE_ON_ERROR → 缓存坏了自动走网络，不崩溃
                            .setFlags(CacheDataSource.FLAG_BLOCK_ON_CACHE | CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR);
                }
                mediaSource = new HlsMediaSource.Factory(factory).createMediaSource(videoItem);
                break;
            case "rtsp":
                // rtsp链接 传输实时音视频流，主打直播、监控、摄像头画面。
                //常见于摄像头、监控、局域网实时视频。 大多数场景是实时流。
                mediaSource = new RtspMediaSource.Factory().createMediaSource(videoItem);
                break;
            case "flv":
                // HTTP-FLV 常用于直播拉流，本质是 HTTP 长连接传输 FLV 数据。
                // 这里使用 ProgressiveMediaSource，由 Media3 的 extractor 解析 FLV。
                mediaSource = createProgressiveMediaSource(videoItem, false);
                break;
            case "rtmp":
                //通常用于直播，尤其推流
                //rtmp链接 实时消息传输协议，早年主流的直播推 / 拉流协议 主要用于音视频直播，分为推流（主播上传画面到服务器）、拉流（观众播放）
                //mediaSource = new ProgressiveMediaSource.Factory(new RtmpDataSource.Factory()).createMediaSource(videoItem);
            default:
                // 其他链接（http开头或https开头的普通视频链接）
                mediaSource = createProgressiveMediaSource(videoItem, isUseCache);
                break;
        }
        return mediaSource;
    }


    //创建普通文件流播放源
    @OptIn(markerClass = UnstableApi.class)
    private MediaSource createProgressiveMediaSource(MediaItem videoItem, boolean useCache) {
        //ProgressiveMediaSource 处理数据源 并异步加载
        DrmSessionManager drmSessionManager = DrmSessionManager.DRM_UNSUPPORTED;
        MediaSource mediaSource;
        if (!useCache) {
            //默认的  无缓存
            DataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(playerContext);
            mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                    .setDrmSessionManagerProvider(unusedMediaItem -> drmSessionManager)
                    .createMediaSource(videoItem);

        } else {
            setMediaSourceCacheRelease();
            //构建缓存
            simpleCache = createSimpleCache();
            //自动缓存到磁盘，预加载 / 二次播放直接读本地
            CacheDataSource.Factory dataSourceFactory = new CacheDataSource.Factory()
                    .setCache(simpleCache)
                    .setUpstreamDataSourceFactory(new DefaultDataSource.Factory(playerContext))
                    //更适合网络视频
                    //.setUpstreamDataSourceFactory(new DefaultHttpDataSource.Factory().setUserAgent("Media3"))
                    //缓存出错时自动回退到原始源
                    //FLAG_BLOCK_ON_CACHE → 优先读缓存（你要的预加载生效！）
                    //FLAG_IGNORE_CACHE_ON_ERROR → 缓存坏了自动走网络，不崩溃
                    .setFlags(CacheDataSource.FLAG_BLOCK_ON_CACHE | CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR);
            //构建 MediaSource（支持 DRM + 缓存）
            mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                    .setDrmSessionManagerProvider(unusedMediaItem -> drmSessionManager)
                    .createMediaSource(videoItem);

        }
        return mediaSource;
    }

    //识别播放源类型
    private String getMediaSourceType(String url) {
        if (TextUtils.isEmpty(url)) {
            return "progressive";
        }
        String lowerUrl = url.toLowerCase(Locale.US);
        String urlWithoutQuery = lowerUrl;
        int queryIndex = urlWithoutQuery.indexOf("?");
        if (queryIndex >= 0) {
            urlWithoutQuery = urlWithoutQuery.substring(0, queryIndex);
        }
        if (urlWithoutQuery.endsWith(".m3u8") || lowerUrl.contains(".m3u8?")) {
            return "m3u8";
        }
        if (lowerUrl.startsWith("rtsp://")) {
            return "rtsp";
        }
        if (lowerUrl.startsWith("rtmp://") || lowerUrl.startsWith("rtmps://")) {
            return "rtmp";
        }
        if (urlWithoutQuery.endsWith(".flv") || lowerUrl.contains(".flv?")) {
            return "flv";
        }
        return "progressive";
    }

    @UnstableApi
    protected SimpleCache simpleCache;
    @UnstableApi
    protected File simpleCacheDir;
    protected long maxBytes = 100 * 1024 * 1024;//100M

    //构建缓存
    @OptIn(markerClass = UnstableApi.class)
    private SimpleCache createSimpleCache() {
        // 有缓存目录则复用，没有则新建
        simpleCacheDir = getCacheFile();
        SimpleCache simpleCache;
        if (maxBytes == 0) {
            simpleCache = new SimpleCache(simpleCacheDir, new NoOpCacheEvictor(), new StandaloneDatabaseProvider(playerContext));
        } else {
            simpleCache = new SimpleCache(simpleCacheDir, new LeastRecentlyUsedCacheEvictor(maxBytes), new StandaloneDatabaseProvider(playerContext));
        }
        return simpleCache;
    }

    //清理/释放缓存
    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void setMediaSourceCacheRelease() {
        super.setMediaSourceCacheRelease();
        if (TextUtils.isEmpty(videoUrl)) {
            return;
        }
        if (simpleCache != null) {
            simpleCache.release();
            simpleCache = null;
        }
        if (simpleCacheDir != null) {
            dbSetVideoCacheUsable(playerContext, simpleCacheDir);
            simpleCacheDir = null;
        }
        PlayerLog.d(tag, "播放器 释放缓存：" + videoUrl);
    }

    //获取缓存文件
    private File getCacheFile() {
        List<VideoEntity> datas = getDBVideoCache(playerContext, videoUrl);
        File cacheDir = null;
        if (datas != null && datas.size() > 0) {
            for (int i = 0; i < datas.size(); i++) {
                VideoEntity entity = datas.get(i);
                if (entity.videoCacheType == 1) {
                    continue;
                }
                if (TextUtils.isEmpty(entity.videoCachePath)) {
                    continue;
                }
                File candidate = new File(entity.videoCachePath);
                if (!candidate.exists()) {
                    candidate.mkdirs();
                }
                if (!candidate.isDirectory()) {
                    continue;
                }
                cacheDir = candidate;
                dbSetVideoCacheUse(playerContext, cacheDir);
                break;
            }
        }
        if (cacheDir == null) {
            cacheDir = createCacheDir(datas == null ? 0 : datas.size());
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            dbVideoAdd(playerContext, videoUrl, cacheDir);
            PlayerLog.d(tag + "缓存地址", "新建 url:" + videoUrl + "\npath:" + cacheDir.getPath());
        } else {
            PlayerLog.d(tag + "缓存地址", "取得 url:" + videoUrl + "\npath:" + cacheDir.getPath());
        }

        return cacheDir;
    }

    //创建缓存目录
    private File createCacheDir(int index) {
        Random random = new Random();
        int randomNumber = random.nextInt(9999999);
        String md5 = Md5Media.encode(videoUrl);
        String fileName = md5 + "_" + index + "_" + randomNumber;
        String dir = FileUtil.getVideoCacheDir(playerContext);
        File file = new File(dir, fileName);
        return file;
    }

    //读取正在播放的信息
    @OptIn(markerClass = UnstableApi.class)
    public void readData() {
        if (player == null) {
            return;
        }
        MediaItem mediaItem = player.getCurrentMediaItem();
        String mediaId = "";
        if (mediaItem != null) {
            mediaId = mediaItem.mediaId;
            MediaMetadata mediaMetadata = mediaItem.mediaMetadata;
            String str = "";
            if (mediaMetadata != null) {
                str = mediaMetadata.title + "-" + mediaMetadata.albumTitle + "-" + mediaMetadata.subtitle + "-" + mediaMetadata.description + "-" + mediaMetadata.artworkUri;
            }
            PlayerLog.d("视频播放Url", "当前播放地址 mediaId：" + mediaId + " str:" + str);
        }

    }

    //设置字幕
    public void setSubtitle(MediaItem.Builder builder) {
        //builder.setSubtitleConfigurations();
    }

}
