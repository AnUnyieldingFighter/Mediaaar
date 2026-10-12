package media.library.player.view;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.text.TextUtils;
import android.text.format.Formatter;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Player;
import androidx.media3.common.VideoSize;
import androidx.media3.common.util.Util;
import androidx.media3.datasource.DataSpec;
import androidx.media3.exoplayer.source.LoadEventInfo;
import androidx.media3.exoplayer.source.MediaLoadData;
import media.library.db.MediaRoom;
import media.library.player.bean.VideoEntity;
import media.library.player.manager.PlayerLog;

//保存历史数据
class PlayerDB {
    //播放初始化时建立信息记录，并尝试获取本地文件或 content URI 的总大小
    protected void dbInitVideoInfo(Context context, String videoUrl) {
        if (context == null || TextUtils.isEmpty(videoUrl)) {
            return;
        }
        MediaRoom.geVideoDb(context).ensureVideoRecord(videoUrl);
        Uri uri = Uri.parse(videoUrl);
        String scheme = uri.getScheme();
        if (scheme == null || "file".equalsIgnoreCase(scheme)) {
            String path = uri.getPath();
            if (path != null) {
                File file = new File(path);
                if (file.isFile()) {
                    dbSetVideoFileSize(context, videoUrl, file.length());
                }
            }
        } else if ("content".equalsIgnoreCase(scheme)) {
            try (AssetFileDescriptor descriptor = context.getContentResolver()
                    .openAssetFileDescriptor(uri, "r")) {
                if (descriptor != null && descriptor.getLength() >= 0) {
                    dbSetVideoFileSize(context, videoUrl, descriptor.getLength());
                }
            } catch (IOException | SecurityException exception) {
                PlayerLog.d("视频数据库", "无法获取本地视频大小：" + exception.getMessage());
            }
        }
    }

    //保存确定的视频总时长，未知值不覆盖数据库已有时长
    protected void dbSetVideoDuration(Context context, String videoUrl, long duration) {
        if (duration <= 0) {
            return;
        }
        MediaRoom.geVideoDb(context).updateVideoDuration(videoUrl, duration);
    }

    //保存文件总大小，sizeMb 按 KB 存储，sizeStr 生成可读文本
    protected void dbSetVideoFileSize(Context context, String videoUrl, long sizeBytes) {
        if (sizeBytes < 0) {
            return;
        }
        long sizeKb = sizeBytes / 1024 + (sizeBytes % 1024 == 0 ? 0 : 1);
        MediaRoom.geVideoDb(context).updateVideoFileSize(videoUrl, sizeKb,
                Formatter.formatShortFileSize(context, sizeBytes));
    }

    //只读取普通视频文件的大小，排除直播、HLS/DASH 分片和字幕
    protected void dbSaveLoadedFileSize(Context context, String videoUrl, Player player,
                                        LoadEventInfo info, MediaLoadData data) {
        if (player == null || player.isCurrentMediaItemLive()
                || data.dataType != C.DATA_TYPE_MEDIA
                || info.dataSpec.httpMethod != DataSpec.HTTP_METHOD_GET) {
            return;
        }
        MediaItem item = player.getCurrentMediaItem();
        if (item == null || item.localConfiguration == null
                || Util.inferContentType(item.localConfiguration.uri,
                item.localConfiguration.mimeType) != C.CONTENT_TYPE_OTHER) {
            return;
        }
        String scheme = info.dataSpec.uri.getScheme();
        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            return;
        }
        dbSetVideoFileSize(context, videoUrl, dbGetFileSize(info));
    }

    //分段响应取 Content-Range 中的总大小，完整请求才取 Content-Length；未知返回 -1
    private long dbGetFileSize(LoadEventInfo info) {
        String encoding = dbGetResponseHeader(info.responseHeaders, "Content-Encoding");
        if (encoding != null && !"identity".equalsIgnoreCase(encoding.trim())) {
            return -1;
        }
        try {
            String range = dbGetResponseHeader(info.responseHeaders, "Content-Range");
            if (range != null) {
                Matcher matcher = Pattern.compile("bytes\\s+(\\d+)-(\\d+)/(\\d+)",
                        Pattern.CASE_INSENSITIVE).matcher(range.trim());
                if (!matcher.matches()) {
                    return -1;
                }
                long start = Long.parseLong(matcher.group(1));
                long end = Long.parseLong(matcher.group(2));
                long total = Long.parseLong(matcher.group(3));
                return end >= start && total > end ? total : -1;
            }
            if (info.dataSpec.position != 0 || info.dataSpec.length != C.LENGTH_UNSET) {
                return -1;
            }
            for (String name : info.dataSpec.httpRequestHeaders.keySet()) {
                if ("Range".equalsIgnoreCase(name)) {
                    return -1;
                }
            }
            String length = dbGetResponseHeader(info.responseHeaders, "Content-Length");
            return length == null ? -1 : Long.parseLong(length.trim());
        } catch (NumberFormatException exception) {
            return -1;
        }
    }

    //响应头名称忽略大小写，多值或空值视为无效，避免误用分段长度
    private String dbGetResponseHeader(Map<String, List<String>> headers, String name) {
        if (headers != null) {
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                if (name.equalsIgnoreCase(entry.getKey())) {
                    List<String> values = entry.getValue();
                    return values != null && values.size() == 1 && values.get(0) != null
                            ? values.get(0) : "";
                }
            }
        }
        return null;
    }

    //保存播放器提供的元数据，自定义 mediaId 才作为业务标识保存
    protected void dbSetVideoMetadata(Context context, String videoUrl, String mediaId,
                                      MediaMetadata metadata) {
        String id = TextUtils.isEmpty(mediaId) || TextUtils.equals(mediaId, videoUrl) ? null : mediaId;
        String title = metadata.title == null ? null : metadata.title.toString();
        String description = metadata.description == null ? null : metadata.description.toString();
        String artworkUrl = metadata.artworkUri == null ? null : metadata.artworkUri.toString();
        title = TextUtils.isEmpty(title) ? null : title;
        description = TextUtils.isEmpty(description) ? null : description;
        artworkUrl = TextUtils.isEmpty(artworkUrl) ? null : artworkUrl;
        if (id == null && title == null && description == null && artworkUrl == null) {
            return;
        }
        MediaRoom.geVideoDb(context).updateVideoMetadata(videoUrl, id, title, description, artworkUrl);
    }

    //完成或停止时保存进度；释放或更换地址前由调用方先暂停或停止旧视频
    protected void dbSaveVideoHistory(Context context, String videoUrl, Player player) {
        if (player == null || TextUtils.isEmpty(videoUrl)) {
            return;
        }
        long progress = player.getCurrentPosition();
        if (progress == C.TIME_UNSET) {
            // 媒体还没加载好 / 直播流，没有进度
            progress = -1;
        }
        dbUpdateVideoPro(context, videoUrl, progress, player.getDuration());
    }

    //更新播放进度
    protected void dbUpdateVideoPro(Context context, String videoUrl, long currentPosition, long duration) {
        long time = new Date().getTime();
        MediaRoom.geVideoDb(context).updateVideoLookHis(videoUrl, currentPosition, duration, time);

    }

    //获取视频大小
    protected List<VideoEntity> dbGetVideoSize(Context context, String videoUrl) {
        List<VideoEntity> datas = MediaRoom.geVideoDb(context).queryVideoCache(videoUrl);
        return datas;
    }

    //获取缓存地址
    protected List<VideoEntity> dbGetVideoCache(Context context, String videoUrl) {
        List<VideoEntity> datas = MediaRoom.geVideoDb(context).queryVideoCache(videoUrl);
        return datas;
    }

    //添加缓存地址 且设置被使用
    protected void dbVideoAdd(Context context, String videoUrl, File videoFile) {
        //新增缓存目录记录时沿用已有视频信息，保留原有记录及其缓存目录
        List<VideoEntity> datas = dbGetVideoCache(context, videoUrl);
        VideoEntity videoEntity = datas == null || datas.isEmpty() ? new VideoEntity() : datas.get(0);
        videoEntity.indexId = 0;
        videoEntity.videoUrl = videoUrl;
        videoEntity.videoCachePath = videoFile.getPath();
        videoEntity.videoCacheType = 1;
        long time = new Date().getTime();
        videoEntity.recordTime = time;
        MediaRoom.geVideoDb(context).put(videoEntity);
    }

    //设置缓存地址  被使用
    protected void dbSetVideoCacheUse(Context context, File videoFile) {
        MediaRoom.geVideoDb(context).setCacheFileUse(videoFile.getPath());
    }

    //设置缓存地址  未被使用
    protected void dbSetVideoCacheUsable(Context context, File videoFile) {
        MediaRoom.geVideoDb(context).setCacheFileRelease(videoFile.getPath());
    }

    //设置视频高宽
    protected void dbSetVideoSize(Context context, String videoUrl, VideoSize videoSize) {
        if (videoSize.width <= 0 || videoSize.height <= 0) {
            return;
        }
        MediaRoom.geVideoDb(context).updateVideoSize(videoUrl, videoSize.width, videoSize.height);

    }
}
