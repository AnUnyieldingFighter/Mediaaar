/*
 * Copyright (C) 2020 The Android Open Source Project
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
package media.library.player.act;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.ui.PlayerView;

import com.images.imageselect.R;

import media.library.player.bean.TestVideoUrl;
import media.library.player.manager.ExoPlayerManager;
import media.library.player.view.CustomExoPlayer;


public final class TestVideoOneAct extends Activity {

    private static final String TAG = "MainActivity";

    private PlayerView playerView;
    private TextView tvRed;


    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_test_video_one);
        playerView = findViewById(R.id.play_view);
        tvRed = findViewById(R.id.tv_red);
        tvRed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomExoPlayer exoPlayer = ExoPlayerManager.getInstance().getExoPlayer();
                exoPlayer.readData();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ExoPlayerManager.getInstance().getExoPlayer() == null) {
            String url = new TestVideoUrl().video1;
            //Uri uri = Uri.parse(new TestVideoUrl().video1);
            ExoPlayerManager.getInstance()
                    .initExoPlayer(this)
                    .setPlayUrl(this, url);
            ExoPlayerManager.getInstance().setPlay();
        }
        ExoPlayerManager.getInstance().setPlayerView(playerView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ExoPlayerManager.getInstance().setRelease();
    }
}
