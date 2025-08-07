/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package library.player.act;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.viewpager2.widget.ViewPager2;

import com.images.imageselect.R;
import library.player.able.OnVideoOperate;
import library.player.manager.VideoPlaysManager;

//短视频播放
public class TestVideoPlaysAct extends AppCompatActivity implements OnVideoOperate {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_test_video_page);
        initView();

    }

    private VideoPlaysManager videoPlaysManager;

    private void initView() {
        ViewPager2 viewPagerView = findViewById(R.id.viewPager);
        videoPlaysManager = new VideoPlaysManager();
        videoPlaysManager.setView(this, viewPagerView);
    }


    @Override
    protected void onDestroy() {
        videoPlaysManager.onDestroy();
        super.onDestroy();
    }


    @Override
    protected void onResume() {
        super.onResume();
        videoPlaysManager.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        videoPlaysManager.onPause();
    }

    //给Fragment调用
    @Override
    public ExoPlayer getExoPlayer(Integer pageIndex, String videoUrl) {
        return videoPlaysManager.getExoPlayer(pageIndex, videoUrl);
    }

    @Override
    public void setClearFrgHoldPlayer(ExoPlayer player) {
        videoPlaysManager.setClearFrgHoldPlayer(player);
    }

    @Override
    public void setViewPageSlide(boolean isSlide) {
        videoPlaysManager.setViewPageSlide(isSlide);
    }

    //记录观看时长
    @Override
    public void recordDuration(String id, int pageIndex, long pro, long total) {

    }
    //横屏播放时 获取播放进度

    @Override
    public void updateVideoPro() {

    }

    @Override
    public void onCheck(Object str, Object obj) {
        videoPlaysManager.onCheck(str,obj);
    }

    @Override
    public int getIndex() {
        return videoPlaysManager.getIndex();
    }
}
