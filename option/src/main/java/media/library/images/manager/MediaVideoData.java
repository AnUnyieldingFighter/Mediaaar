package media.library.images.manager;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;

import java.io.IOException;
import java.util.ArrayList;

public class MediaVideoData {
    private static MediaVideoData mediaVideoData;

    public static MediaVideoData getInstance() {
        if (mediaVideoData == null) {
            mediaVideoData = new MediaVideoData();
        }
        return mediaVideoData;
    }

    private MediaMetadataRetriever retriever;

    /**
     *
     * @param videoPath 本地视频 path
     * @return
     */
    public MediaVideoData setVideoPath(String videoPath) {
        setVideoReadEnd();
        retriever = new MediaMetadataRetriever();
        retriever.setDataSource(videoPath);
        return this;
    }

    public void setVideoReadEnd() {
        if (retriever == null) {
            return;
        }
        try {
            // 确保释放资源。
            retriever.release();
            retriever = null;
        } catch (Exception e) {

        }
    }

    /**
     * 获取视频数据
     *
     * @param isEnd 毫秒 true 结束
     * @return
     */
    public VideoDataBean getVideoData(boolean isEnd) {
        VideoDataBean videoData = new VideoDataBean();
        try {
            // 设置数据源路径
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            String width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            String height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
            String rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
            videoData.rotation = rotation;
            videoData.time = time;
            videoData.width = width;
            videoData.height = height;
            // 根据旋转角度调整宽高（如果需要）
            if ("90".equals(rotation) || "270".equals(rotation)) {
                videoData.realWidth = height;
                videoData.realHeight = width;
            } else {
                videoData.realWidth = width;
                videoData.realHeight = height;
            }
        } catch (Exception ex) {
            // 处理异常，例如文件路径无效等。
        }
        if (isEnd) {
            setVideoReadEnd();
        }
        return videoData;
    }

    /**
     * @param time  毫秒
     * @param isEnd 毫秒 true 结束
     * @return
     */
    public Bitmap getVideoBit(int time, boolean isEnd) {
        ArrayList<Bitmap> bits = getVideoBit(isEnd, true, time);
        if (bits.size() > 0) {
            return bits.get(0);
        }
        return null;
    }

    /**
     *
     * @param timeUs    时间
     * @param isPrecise true 非关键帧，而是精准时间的帧
     * @return
     */
    public ArrayList<Bitmap> getVideoBit(boolean isEnd, boolean isPrecise, long... timeUs) {
        ArrayList<Bitmap> bits = new ArrayList();
        try {
            // 设置数据源路径
            for (int i = 0; i < timeUs.length; i++) {
                int opt = MediaMetadataRetriever.OPTION_CLOSEST_SYNC;
                if (isPrecise) {
                    opt = MediaMetadataRetriever.OPTION_CLOSEST;
                }
                Bitmap bit = retriever.getFrameAtTime(timeUs[i], opt);
                if (bit != null) {
                    bits.add(bit);
                }
            }

        } catch (Exception ex) {
            // 处理异常，例如文件路径无效等。
        }
        if (isEnd) {
            setVideoReadEnd();
        }
        return bits;
    }

}
