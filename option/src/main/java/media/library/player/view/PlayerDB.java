package media.library.player.view;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.text.format.Formatter;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.OptIn;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Player;
import androidx.media3.common.VideoSize;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.common.util.Util;
import androidx.media3.datasource.DataSpec;
import androidx.media3.exoplayer.source.LoadEventInfo;
import androidx.media3.exoplayer.source.MediaLoadData;
import media.library.db.MediaRoom;
import media.library.player.bean.VideoEntity;
import media.library.player.manager.PlayerLog;

//保存历史数据
class PlayerDB {


    //保存确定的视频总时长，未知值不覆盖数据库已有时长
    protected void dbSetVideoDuration(Context context, String videoUrl, long duration) {
        if (duration <= 0) {
            return;
        }
        MediaRoom.geVideoDb(context).updateVideoDuration(videoUrl, duration);
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

    //完成或停止时保存进度，播放失败不记录；释放或更换地址前由调用方先暂停或停止旧视频
    protected void dbSaveVideoHistory(Context context, String videoUrl, Player player) {
        if (player == null || player.getPlayerError() != null || TextUtils.isEmpty(videoUrl)) {
            return;
        }
        long progress = player.getCurrentPosition();
        if (progress == C.TIME_UNSET) {
            // 媒体还没加载好 / 直播流，没有进度
            progress = -1;
        }
        long duration = player.getDuration();
        if (duration == C.TIME_UNSET) {
            duration = -1;
        }
        dbUpdateVideoPro(context, videoUrl, progress, duration);
    }

    //更新播放进度
    protected void dbUpdateVideoPro(Context context, String videoUrl, long currentPosition, long duration) {
        if (duration <= 0) {
            return;
        }
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


    //============================获取视频大小

    //播放初始化时建立信息记录，并尝试获取本地文件或 content URI 的总大小
    protected void dbInitVideoInfo(Context context, String videoUrl) {
        if (context == null || TextUtils.isEmpty(videoUrl)) {
            return;
        }
        MediaRoom.geVideoDb(context).ensureVideoRecord(videoUrl);
        long size = getVideoSizeFile(context, videoUrl);
        dbSetVideoFileSize(context, videoUrl, size);
    }


    //获取本地视频总大小：支持文件路径、file URI 和 content URI；返回 KB（1024 字节），向上取整，未知返回 -1
    private long getVideoSizeFile(Context context, String videoUrl) {
        if (context == null || TextUtils.isEmpty(videoUrl)) {
            return -1;
        }
        Uri uri = Uri.parse(videoUrl);
        String scheme = uri.getScheme();
        if (scheme == null) {
            //没有 URI scheme 时，videoUrl 就是普通文件路径，不能再通过 Uri.getPath() 解析
            File file = new File(videoUrl);
            return file.isFile() ? getVideoSizeKb(file.length()) : -1;
        } else if ("file".equalsIgnoreCase(scheme)) {
            String path = uri.getPath();
            if (path != null) {
                File file = new File(path);
                if (file.isFile()) {
                    return getVideoSizeKb(file.length());
                }
            }
        } else if ("content".equalsIgnoreCase(scheme)) {
            try (AssetFileDescriptor descriptor = context.getContentResolver()
                    .openAssetFileDescriptor(uri, "r")) {
                if (descriptor != null && descriptor.getLength() >= 0) {
                    return getVideoSizeKb(descriptor.getLength());
                }
            } catch (IOException | SecurityException exception) {
                PlayerLog.d("视频数据库", "无法获取本地视频大小：" + exception.getMessage());
            }

            //部分 ContentProvider 不提供 AssetFileDescriptor 长度，改从 OpenableColumns.SIZE 查询
            try (Cursor cursor = context.getContentResolver().query(
                    uri, new String[]{OpenableColumns.SIZE}, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                    if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) {
                        return getVideoSizeKb(cursor.getLong(sizeIndex));
                    }
                }
            } catch (SecurityException | IllegalArgumentException |
                     UnsupportedOperationException exception) {
                PlayerLog.d("视频数据库", "无法查询本地视频大小：" + exception.getMessage());
            }
        }
        return -1;
    }

    //字节转换为 KB，保留原有向上取整规则；负数表示大小未知
    private long getVideoSizeKb(long sizeBytes) {
        if (sizeBytes < 0) {
            return -1;
        }
        return sizeBytes / 1024 + (sizeBytes % 1024 == 0 ? 0 : 1);
    }

    //==========网络上的视频大小===========
    //只读取普通视频文件的大小，排除直播、HLS/DASH 分片和字幕
    @OptIn(markerClass = UnstableApi.class)
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
        long size = getVideoSizeNet(info);
        dbSetVideoFileSize(context, videoUrl, size);
    }

    //获取网络视频总大小：从 HTTP 响应头读取，分段响应取 Content-Range 的总大小，完整请求才取 Content-Length
    //返回 KB（1024 字节），向上取整；不是已下载或已缓存大小，未知或压缩响应返回 -1
    @OptIn(markerClass = UnstableApi.class)
    private long getVideoSizeNet(LoadEventInfo info) {
        String encoding = getResponseHeader(info.responseHeaders, "Content-Encoding");
        if (encoding != null && !"identity".equalsIgnoreCase(encoding.trim())) {
            return -1;
        }
        try {
            String range = getResponseHeader(info.responseHeaders, "Content-Range");
            if (range != null) {
                Matcher matcher = Pattern.compile("bytes\\s+(\\d+)-(\\d+)/(\\d+)",
                        Pattern.CASE_INSENSITIVE).matcher(range.trim());
                if (!matcher.matches()) {
                    return -1;
                }
                long start = Long.parseLong(matcher.group(1));
                long end = Long.parseLong(matcher.group(2));
                long total = Long.parseLong(matcher.group(3));
                return end >= start && total > end ? getVideoSizeKb(total) : -1;
            }
            if (info.dataSpec.position != 0 || info.dataSpec.length != C.LENGTH_UNSET) {
                return -1;
            }
            for (String name : info.dataSpec.httpRequestHeaders.keySet()) {
                if ("Range".equalsIgnoreCase(name)) {
                    return -1;
                }
            }
            String length = getResponseHeader(info.responseHeaders, "Content-Length");
            return length == null ? -1 : getVideoSizeKb(Long.parseLong(length.trim()));
        } catch (NumberFormatException exception) {
            return -1;
        }
    }

    //响应头名称忽略大小写，多值或空值视为无效，避免误用分段长度
    private String getResponseHeader(Map<String, List<String>> headers, String name) {
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


    //
    //保存本地或网络视频总大小，参数和 sizeMb 均为 KB；sizeStr 按 KB 换算为字节后生成可读文本
    private void dbSetVideoFileSize(Context context, String videoUrl, long sizeKb) {
        if (sizeKb < 0) {
            return;
        }
        MediaRoom.geVideoDb(context).updateVideoFileSize(videoUrl, sizeKb,
                Formatter.formatShortFileSize(context, sizeKb * 1024));
    }
}
