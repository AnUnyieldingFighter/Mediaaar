package media.library.player.bean;

import java.util.ArrayList;

public class TestVideoUrl {
    public String video1 = "https://st.92kk.com/2021/%E8%BD%A6%E8%BD%BD%E8%A7%86%E9%A2%91/202110/20210916/[Mp4]%E4%B8%A4%E4%B8%AA%E4%B8%96%E7%95%8C-%E8%BD%A6%E8%BD%BD%E5%A4%9C%E5%BA%97%E9%9F%B3%E4%B9%90DJ%E8%A7%86%E9%A2%91[%E7%8B%AC].mp4";
    public String video2 = "https://nbc.vtnbo.com/nbc-file/file/video/beta/17484063665794360.mp4";
    public String video3 = "http://10.168.3.102:5233/chfs/shared/nbc-apk/test1.mp4";

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

}
