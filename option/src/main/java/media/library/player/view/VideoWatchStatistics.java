package media.library.player.view;

import android.os.SystemClock;
import android.text.TextUtils;

import java.util.ArrayList;

/**
 * 统计当前播放地址的实际观看时长。
 */
public final class VideoWatchStatistics {
    private String videoUrl = "";
    private ArrayList<Integer> videoEndNum = new ArrayList<>();//完成次数
    private ArrayList<Long> videoLookTimes = new ArrayList<>();//每次完成的观看时长，单位毫秒
    private long videoAllLookTime;//总观看时长，单位毫秒
    private long videoCurrentLookTime;//当前这次播放的观看时长，单位毫秒
    private long videoStartLookTime;//本段开始观看的时间
    private boolean isEnd;

    //设置播放地址，地址改变时清空原来的统计
    public void setVideoUrl(String url) {
        if (TextUtils.equals(videoUrl, url)) {
            return;
        }
        this.videoUrl = url == null ? "" : url;
        clearData();
    }

    //设置当前是否正在播放，暂停和缓冲期间不计算观看时长
    public void setPlaying(boolean playing) {
        if (isPlaying == playing) {
            return;
        }
        if (playing) {
            if (isEnd) {
                videoCurrentLookTime = 0;
                isEnd = false;
            }
            videoStartLookTime = SystemClock.elapsedRealtime();
        } else {
            addLookTime();
        }
        isPlaying = playing;
    }

    //一次播放完成，记录这是第几次完成以及本次观看时长
    public void setVideoEnd() {
        if (isEnd) {
            return;
        }
        addLookTime();
        videoEndNum.add(videoEndNum.size() + 1);
        videoLookTimes.add(videoCurrentLookTime);
        isEnd = true;
    }

    //循环播放进入下一次，保留总记录并重新统计本次观看时长
    public void setVideoNext() {
        videoCurrentLookTime = 0;
        isEnd = false;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public ArrayList<Integer> getVideoEndNum() {
        return new ArrayList<>(videoEndNum);
    }

    public ArrayList<Long> getVideoLookTimes() {
        return new ArrayList<>(videoLookTimes);
    }

    private boolean isPlaying;

    //获取总观看时长，正在播放的这一段也包含在内
    public long getVideoAllLookTime() {
        if (isPlaying) {
            return videoAllLookTime
                    + Math.max(0, SystemClock.elapsedRealtime() - videoStartLookTime);
        }
        return videoAllLookTime;
    }

    //结算本段实际播放的时间
    private void addLookTime() {
        if (!isPlaying) {
            return;
        }
        long now = SystemClock.elapsedRealtime();
        long lookTime = Math.max(0, now - videoStartLookTime);
        videoAllLookTime += lookTime;
        videoCurrentLookTime += lookTime;
        videoStartLookTime = now;
    }

    //清空当前播放地址的统计
    private void clearData() {
        videoEndNum.clear();
        videoLookTimes.clear();
        videoAllLookTime = 0;
        videoCurrentLookTime = 0;
        videoStartLookTime = 0;
        isPlaying = false;
        isEnd = false;
    }

    //释放全部
    public void setRelease() {
        videoUrl = "";
        clearData();
    }

}
