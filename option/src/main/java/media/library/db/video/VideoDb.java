package media.library.db.video;


import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.cache.SimpleCache;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import media.library.db.BaseDatabase;
import media.library.player.bean.VideoEntity;
import media.library.player.manager.PlayerLog;
import media.library.utils.FileUtil;


/**
 * @author 86134
 */

public class VideoDb {
    private BaseDatabase base;

    public VideoDb(BaseDatabase base) {
        this.base = base;
    }

    public VideoDao getDao() {
        return base.videoDao();
    }

    public void put(VideoEntity data) {
        getDao().put(data);
    }

    public void putList(List<VideoEntity> datas) {
        getDao().putList(datas);
    }

    public void update(VideoEntity data) {
        getDao().update(data);
    }

    public int updateVideoLookHis(String videoUrl, long pro, long total, long lookTime) {
        return getDao().updateVideoLookHis(videoUrl, pro, total, lookTime);
    }

    public int updateVideoSize(String videoUrl, int videoWidth, int videoHeight) {
        return getDao().updateVideoSize(videoUrl, videoWidth, videoHeight);
    }

    //确保当前视频有可更新的信息记录
    public void ensureVideoRecord(String videoUrl) {
        getDao().ensureVideoRecord(videoUrl, System.currentTimeMillis());
    }

    //保存已知视频总时长，单位毫秒
    public void updateVideoDuration(String videoUrl, long duration) {
        getDao().updateVideoDuration(videoUrl, duration);
    }

    //保存文件大小及用于展示的大小文本
    public void updateVideoFileSize(String videoUrl, long sizeKb, String sizeStr) {
        getDao().updateVideoFileSize(videoUrl, sizeKb, sizeStr);
    }

    //保存有明确来源的媒体标识、标题、描述和封面地址
    public void updateVideoMetadata(String videoUrl, String videoIds, String title,
                                    String description, String artworkUrl) {
        getDao().updateVideoMetadata(videoUrl, videoIds, title, description, artworkUrl);
    }

    public void delete(VideoEntity data) {
        getDao().delete(data);
    }


    public void queryCount() {
        getDao().queryCount();
    }

    public void queryAll() {
        getDao().queryAll();
    }


    public List<VideoEntity> queryLookHisAll(boolean isLook) {
        return getDao().queryLookHisAll(isLook);
    }

    public List<VideoEntity> queryVideoCache(String videoUrl) {
        return getDao().queryVideoCache(videoUrl);
    }
    public int queryLookHisCount(boolean isLook) {
        return getDao().queryLookHisCount(isLook);
    }

    //设置缓存文件被使用
    public int setCacheFileUse(String cacheFilePath) {
        return getDao().updateCacheFile(1, cacheFilePath,System.currentTimeMillis());
    }

    //设置缓存文件被释放
    public int setCacheFileRelease(String cacheFilePath) {
        return getDao().updateCacheFile(0, cacheFilePath,System.currentTimeMillis());
    }
    //设置缓存文件全部被释放
    public int setCacheFileReleaseAll() {
        return getDao().updateCacheFile(0);
    }
    public void deleteAll() {
        getDao().deleteAll();
    }
    public void deleteVideo(String videoUrl) {
        getDao().deleteVideo(videoUrl);
    }

    //========================= 视频缓存清理 =========================

    /**
     * 清理视频缓存及对应记录，请在子线程、播放器初始化前调用。
     * 同一视频地址按 lookTime（最后观看时间）保留最新一条，相同时间保留 indexId 较大的一条。
     * 删除超过一个月未观看的记录；未观看时 lookTime 默认等于 recordTime（创建时间）。
     * 清除缓存目录中没有保留记录的文件或目录。
     * 正在使用的记录和缓存暂不清理，释放后下次再处理；只清理视频缓存根目录内的文件。
     */
    @OptIn(markerClass = UnstableApi.class)
    public void dbClearVideoCache(Context context) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        long expireTime = calendar.getTimeInMillis();
        Set<String> videoUrls = new HashSet<>();
        Set<String> retainedPaths = new HashSet<>();
        try {
            File cacheRoot = new File(FileUtil.getVideoCacheDir(context)).getCanonicalFile();
            //查询结果按最后观看时间倒序，同一地址第一次出现的就是最新记录。
            for (VideoEntity entity : getDao().queryAll()) {
                boolean latest = videoUrls.add(entity.videoUrl);
                File cacheFile = TextUtils.isEmpty(entity.videoCachePath)
                        ? null : new File(entity.videoCachePath).getCanonicalFile();
                boolean inUse = entity.videoCacheType == 1
                        || (cacheFile != null && SimpleCache.isCacheFolderLocked(cacheFile));
                if (inUse || (latest && entity.lookTime >= expireTime)) {
                    if (cacheFile != null) {
                        retainedPaths.add(cacheFile.getPath());
                    }
                } else {
                    getDao().delete(entity);
                }
            }
            //统一删除无记录、重复记录以及过期记录对应的缓存，保留根目录本身。
            File[] cacheFiles = cacheRoot.listFiles();
            if (cacheFiles == null) {
                return;
            }
            for (File cacheFile : cacheFiles) {
                if (retainedPaths.contains(cacheFile.getCanonicalPath())
                        || SimpleCache.isCacheFolderLocked(cacheFile)) {
                    continue;
                }
                if (!dbDeleteCacheFile(cacheRoot, cacheFile)) {
                    PlayerLog.d("视频缓存清理", "删除失败，下次重试：" + cacheFile.getPath());
                }
            }
        } catch (IOException exception) {
            PlayerLog.d("视频缓存清理", "清理失败：" + exception.getMessage());
        }
    }

    /** 递归删除视频缓存根目录内的文件，不删除根目录，也不进入符号链接指向的目录。 */
    private boolean dbDeleteCacheFile(File cacheRoot, File file) throws IOException {
        String rootPrefix = cacheRoot.getPath() + File.separator;
        if (!file.getAbsolutePath().startsWith(rootPrefix)) {
            return false;
        }
        if (Files.isSymbolicLink(file.toPath())) {
            return file.delete();
        }
        if (!file.getCanonicalPath().startsWith(rootPrefix)) {
            return false;
        }
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children == null) {
                return false;
            }
            for (File child : children) {
                if (!dbDeleteCacheFile(cacheRoot, child)) {
                    return false;
                }
            }
        }
        return file.delete();
    }


}
