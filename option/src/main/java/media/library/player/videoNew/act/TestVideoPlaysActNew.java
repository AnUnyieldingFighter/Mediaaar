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
package media.library.player.videoNew.act;

import android.os.Bundle;

import com.images.imageselect.R;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import media.library.player.videoNew.act.frg.VideoMainFrg;

//短视频播放
public class TestVideoPlaysActNew extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_test_video_page2);
        initView();

    }

    private void initView() {
        //RelativeLayout rlContent = findViewById(R.id.rl_content);
        VideoMainFrg frg = new VideoMainFrg();
        FragmentManager managers = getSupportFragmentManager();
        FragmentTransaction transactions = managers.beginTransaction();
        transactions.replace(R.id.rl_content, frg);
        transactions.commit();
    }


}
