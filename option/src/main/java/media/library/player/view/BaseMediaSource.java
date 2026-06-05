package media.library.player.view;

import android.text.TextUtils;

import java.io.File;
import java.util.List;
import java.util.Random;

import androidx.annotation.OptIn;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.database.StandaloneDatabaseProvider;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.datasource.cache.CacheDataSource;
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor;
import androidx.media3.datasource.cache.NoOpCacheEvictor;
import androidx.media3.datasource.cache.SimpleCache;
import androidx.media3.exoplayer.drm.DrmSessionManager;
import androidx.media3.exoplayer.hls.HlsMediaSource;
import androidx.media3.exoplayer.rtsp.RtspMediaSource;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import media.library.player.bean.VideoEntity;
import media.library.player.manager.PlayerLog;
import media.library.utils.FileUtil;
import media.library.utils.Md5Media;

//设置/获取播放源
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
        String type = "";
        /* if (videoUrl.endsWith("m3u8")) {
            // hls链接
        } else if (videoUrl.startsWith("rtsp")) {
            // rtsp链接
        } else if (videoUrl.startsWith("rtmp")) {
            // rtmp链接
        } else {
        }*/
        //媒体元数据描述 不知道用法
        /*MediaMetadata mediaMetadata = new MediaMetadata.Builder()
                .setTitle("示例标题")
                .setArtist("示例艺术家")
                .setSubtitle("小标题")
                .setDescription("描述")
                .setAlbumTitle("示例专辑")
                .setArtworkUri(Uri.parse(img1))
                .build();*/
        MediaItem.Builder builder = new MediaItem.Builder()
                //.setMediaMetadata(mediaMetadata)
                .setMimeType(MimeTypes.VIDEO_MP4)// 设置较低分辨率
                .setUri(videoUrl)
                .setMediaId(videoUrl);
        //字幕
        //setSubtitle(builder);
        MediaItem videoItem = builder.build();
        MediaSource mediaSource;
        switch (type) {
            case "m3u8":
                // hls链接
                DataSource.Factory factory = new DefaultDataSource.Factory(playerContext);
                mediaSource = new HlsMediaSource.Factory(factory).createMediaSource(videoItem);
                break;
            case "rtsp":
                // rtsp链接
                mediaSource = new RtspMediaSource.Factory().createMediaSource(videoItem);
                break;
            case "rtmp":
                // rtmp链接
                //mediaSource = new ProgressiveMediaSource.Factory(new RtmpDataSource.Factory()).createMediaSource(videoItem);
                //break;
            default:
                //ProgressiveMediaSource 处理数据源 并异步加载
                DrmSessionManager drmSessionManager = DrmSessionManager.DRM_UNSUPPORTED;
                // 其他链接（http开头或https开头的普通视频链接）
                if (!isUseCache) {
                    //默认的  无缓存
                    DataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(playerContext);
                    mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).setDrmSessionManagerProvider(unusedMediaItem -> drmSessionManager).createMediaSource(videoItem);
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
                break;
        }
        return mediaSource;
    }

    @UnstableApi
    protected SimpleCache simpleCache;
    @UnstableApi
    protected File simpleCacheFile;
    protected long maxBytes = 100 * 1024 * 1024;//100M

    //构建缓存
    @OptIn(markerClass = UnstableApi.class)
    private SimpleCache createSimpleCache() {
        //有缓存的
        simpleCacheFile = getCacheFile();
        SimpleCache simpleCache;
        if (maxBytes == 0) {
            simpleCache = new SimpleCache(simpleCacheFile, new NoOpCacheEvictor(), new StandaloneDatabaseProvider(playerContext));
        } else {
            simpleCache = new SimpleCache(simpleCacheFile, new LeastRecentlyUsedCacheEvictor(maxBytes), new StandaloneDatabaseProvider(playerContext));
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
        if (simpleCacheFile != null) {
            dbSetVideoCacheUsable(playerContext, simpleCacheFile);
            simpleCacheFile = null;
        }
        PlayerLog.d(tag, "播放器 释放缓存：" + videoUrl);
    }

    //获取缓存文件
    private File getCacheFile() {
        List<VideoEntity> datas = getDBVideoCache(playerContext, videoUrl);
        int cacheSize = 0;
        File videoFile = null;
        if (datas != null && datas.size() > 0) {
            File optFile = null;
            cacheSize = datas.size();
            long time = System.currentTimeMillis();
            for (int i = 0; i < datas.size(); i++) {
                VideoEntity entity = datas.get(i);
                long videoCacheTime = entity.videoCacheTime;
                /*if ((time - videoCacheTime) < 10 * 1000) {
                    //10 秒以内 不使用这个缓存
                    continue;
                }*/
                if (entity.videoCacheType == 1) {
                    continue;
                }
                File cacheFile = new File(entity.videoCachePath);
                if (optFile == null) {
                    optFile = cacheFile;
                    continue;
                }
                if (!optFile.isFile() && cacheFile.isFile()) {
                    optFile = cacheFile;
                    continue;
                }
                if (optFile.isFile() && cacheFile.isFile() && cacheFile.length() > optFile.length()) {
                    optFile = cacheFile;
                }
            }
            videoFile = optFile;

        }
        if (videoFile == null) {
            videoFile = createFile(cacheSize);
            dbVideoAdd(playerContext, videoUrl, videoFile);
            PlayerLog.d(tag + "缓存地址", "新建 url:" + videoUrl + "\npath:" + videoFile.getPath());
        } else {
            dbSetVideoCacheUse(playerContext, videoFile);
            PlayerLog.d(tag + "缓存地址", "取得 url:" + videoUrl + "\npath:" + videoFile.getPath());
        }

        return videoFile;
    }

    //创建缓存文件
    private File createFile(int index) {
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
