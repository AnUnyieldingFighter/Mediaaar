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
    private boolean isEnd;//true 播放器播放完成
    private String tag1 = "";//额外参数
    private String tag2 = "";//额外参数
    private String tag3 = "";//额外参数
    private String tag4 = "";//额外参数


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

    /**
     * 一次播放完成，记录这是第几次完成以及本次观看时长。
     *
     * @param endType 1：单纯播放结束；2：循环播放结束
     */
    public void setVideoEnd(int endType) {
        switch (endType) {
            case 1:
                //单纯播放结束，先停止计时
                setPlaying(false);
                if (!isEnd) {
                    videoEndNum.add(videoEndNum.size() + 1);
                    videoLookTimes.add(videoCurrentLookTime);
                    videoCurrentLookTime = 0;
                }
                isEnd = true;
                break;
            case 2:
                //循环播放结束，播放器仍在继续播放
                addLookTime();
                videoEndNum.add(videoEndNum.size() + 1);
                videoLookTimes.add(videoCurrentLookTime);
                videoCurrentLookTime = 0;
                break;
            default:
                break;
        }
    }

    public String getVideoUrl() {
        return videoUrl;
    }
    //true 正在播放 false 没有在播放

    private boolean isPlaying;


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
        //
        tag1 = "";//额外参数
        tag2 = "";//额外参数
        tag3 = "";//额外参数
        tag4 = "";//额外参数
    }

    //释放全部
    public void setRelease() {
        videoUrl = "";
        clearData();
    }
    //===============================外部用方法

    //获取总观看时长，正在播放的这一段也包含在内
    public long getVideoAllLookTime() {
        if (isPlaying) {
            return videoAllLookTime
                    + Math.max(0, SystemClock.elapsedRealtime() - videoStartLookTime);
        }
        return videoAllLookTime;
    }


    /**
     * 获取指定完成播放次数的观看时长，完成次数从 1 开始。
     * 没有对应完成记录时，返回当前这次播放截至现在的观看时长。
     */
    public long getVideoLookTime(int endNum) {
        if (endNum < 1) {
            return 0;
        }
        int index = endNum - 1;
        if (index >= 0 && index < videoLookTimes.size()) {
            return videoLookTimes.get(index);
        }
        if (isPlaying) {
            return videoCurrentLookTime
                    + Math.max(0, SystemClock.elapsedRealtime() - videoStartLookTime);
        }
        return videoCurrentLookTime;
    }

    //完成次数
    public ArrayList<Integer> getVideoEndNum() {
        return new ArrayList<>(videoEndNum);
    }

    //每次完成的观看时长，单位毫秒
    public ArrayList<Long> getVideoLookTimes() {
        return new ArrayList<>(videoLookTimes);
    }
    //====================额外参数==================

    public String getTag1() {
        return tag1;
    }

    public void setTag1(String tag1) {
        this.tag1 = tag1;
    }

    public String getTag2() {
        return tag2;
    }

    public void setTag2(String tag2) {
        this.tag2 = tag2;
    }

    public String getTag3() {
        return tag3;
    }

    public void setTag3(String tag3) {
        this.tag3 = tag3;
    }

    public String getTag4() {
        return tag4;
    }

    public void setTag4(String tag4) {
        this.tag4 = tag4;
    }


}
