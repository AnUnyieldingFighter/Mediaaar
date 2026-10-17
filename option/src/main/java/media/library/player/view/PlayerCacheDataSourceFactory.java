package media.library.player.view;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DataSpec;
import androidx.media3.datasource.TransferListener;
import androidx.media3.datasource.cache.SimpleCache;

/** 缓存数据源工厂：所有已打开的数据源关闭后，才允许释放缓存。 */
@UnstableApi
final class PlayerCacheDataSourceFactory extends PlayerDB implements DataSource.Factory {
    private final Context context;
    private final File cacheDir;
    //保存实际的磁盘缓存对象，负责缓存文件读写。等数据源全部关闭后，调用它的 cache.release() 释放资源。
    private final SimpleCache cache;
    //保存数据源工厂，用来通过 factory.createDataSource() 创建具体的数据源，读取网络或缓存中的视频数据
    private final DataSource.Factory factory;
    //包含正在打开、读取和关闭的数据源，打开失败也要等 close() 收尾。
    private int openCount;
    //申请释放后，不再允许旧播放源开始新的缓存读取。
    private boolean releaseRequested;
    private boolean released;

    /** 保存本次缓存对应的目录和上下文，避免切源后使用新视频的信息。 */
    PlayerCacheDataSourceFactory(Context context, File cacheDir, SimpleCache cache,
                                 DataSource.Factory factory) {
        this.context = context;
        this.cacheDir = cacheDir;
        this.cache = cache;
        this.factory = factory;
    }

    /** 申请释放；正在使用的缓存保留到最后一个数据源关闭。 */
    synchronized void release() {
        releaseRequested = true;
        releaseIfIdle();
    }

    /** 持锁检查引用数量，缓存真正释放后才将目录标记为可复用。 */
    private void releaseIfIdle() {
        if (releaseRequested && openCount == 0 && !released) {
            released = true;
            cache.release();
            dbSetVideoCacheUsable(context, cacheDir);
        }
    }

    /** 包装每个数据源，将引用范围覆盖到缓存提交和关闭操作结束。 */
    @Override
    public DataSource createDataSource() {
        return new DataSource() {
            private final DataSource source = factory.createDataSource();
            private boolean opened;

            /** 透传传输监听，保留播放器原有的测速和带宽统计。 */
            @Override
            public void addTransferListener(TransferListener transferListener) {
                source.addTransferListener(transferListener);
            }

            /** 先占用缓存，再打开数据源；旧源释放后到达的打开请求直接取消。 */
            @Override
            public long open(DataSpec dataSpec) throws IOException {
                synchronized (PlayerCacheDataSourceFactory.this) {
                    if (releaseRequested) {
                        throw new IOException("旧播放源的缓存已申请释放");
                    }
                    if (opened) {
                        throw new IOException("数据源尚未关闭");
                    }
                    opened = true;
                    openCount++;
                }
                return source.open(dataSpec);
            }

            /** 透传读取，读取期间缓存引用一直保留。 */
            @Override
            public int read(byte[] buffer, int offset, int length) throws IOException {
                return source.read(buffer, offset, length);
            }

            /** 返回原数据源的实际地址。 */
            @Override
            public Uri getUri() {
                return source.getUri();
            }

            /** 透传响应头，保留现有文件大小读取逻辑。 */
            @Override
            public Map<String, List<String>> getResponseHeaders() {
                return source.getResponseHeaders();
            }

            /** 先完成缓存提交和数据源关闭，再归还缓存引用；重复关闭不重复计数。 */
            @Override
            public void close() throws IOException {
                if (!opened) {
                    return;
                }
                try {
                    source.close();
                } finally {
                    synchronized (PlayerCacheDataSourceFactory.this) {
                        opened = false;
                        openCount--;
                        releaseIfIdle();
                    }
                }
            }
        };
    }
}
