package media.library.player.bean;

import java.util.ArrayList;

public class TestVideoUrl {
    public String video1 = "https://vdept3.bdstatic.com/mda-pjh4avrz2rge3vy8/360p/h264/1697598240278802920/mda-pjh4avrz2rge3vy8.mp4?v_from_s=hkapp-haokan-nanjing&auth_key=1756548002-0-0-c030e626b2ed6d131d5d036593a87f76&bcevod_channel=searchbox_feed&pd=1&cr=0&cd=0&pt=3&logid=0002906616&vid=9164001368238016104&klogid=0002906616&abtest=";
    public String video2 = "https://nbc.vtnbo.com/nbc-file/file/video/beta/17484063665794360.mp4";
    public String video3 = "http://10.168.3.102:5233/chfs/shared/nbc-apk/test1.mp4";
    //
    public String video4 = "https://nbc.vtnbo.com/nbc/msg/video/pro/17623972879492546.mp4";

    public ArrayList<String> buildTestVideoUrls() {
        ArrayList<String> urls = new ArrayList<>();
        urls.add("http://vjs.zencdn.net/v/oceans.mp4");
        urls.add(video1);
        urls.add(video2);
        urls.add(video3);
        urls.add("https://vfx.mtime.cn/Video/2019/01/15/mp4/190115161611510728_480.mp4");
        urls.add("http://vjs.zencdn.net/v/oceans.mp4");
        urls.add("https://storage.googleapis.com/exoplayer-test-media-1/mkv/android-screens-lavf-56.36.100-aac-avc-main-1280x720.mkv");
        return urls;
    }

    public ArrayList<String> buildTestVideoUrls2() {
        ArrayList<String> urls = new ArrayList<>();
        urls.add(video4);
        return urls;
    }
}
