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
package media.library.player.act;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.SurfaceControl;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;

import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.media3.common.util.Assertions;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.ui.PlayerControlView;

import com.images.imageselect.R;
import media.library.player.manager.ExoPlayerManager;

/**
 * Activity that demonstrates use of {@link SurfaceControl} with ExoPlayer.
 *
 * @deprecated com.google.android.exoplayer2 is deprecated. Please migrate to androidx.media3 (which
 * contains the same ExoPlayer code). See <a
 * href="https://developer.android.com/guide/topics/media/media3/getting-started/migration-guide">the
 * migration guide</a> for more details, including a script to help with the migration.
 */
@Deprecated
@UnstableApi
public final class TestVideoAct extends Activity {

    private static final String url1 = "https://storage.googleapis.com/exoplayer-test-media-1/mkv/android-screens-lavf-56.36.100-aac-avc-main-1280x720.mkv";
    private static final String url2 = "https://storage.googleapis.com/exoplayer-test-media-1/mkv/android-screens-lavf-56.36.100-aac-avc-main-1280x720.mkv";

    private static final String SURFACE_CONTROL_NAME = "surfacedemo";

    private static final String ACTION_VIEW = "com.google.android.exoplayer.surfacedemo.action.VIEW";
    private static final String EXTENSION_EXTRA = "extension";
    private static final String DRM_SCHEME_EXTRA = "drm_scheme";
    private static final String DRM_LICENSE_URL_EXTRA = "drm_license_url";
    private static final String OWNER_EXTRA = "owner";

    private boolean isOwner;
    @Nullable
    private PlayerControlView playerControlView;//控制器
    @Nullable
    private SurfaceView fullScreenView;//全屏
    @Nullable
    private SurfaceView surfaceView9;//九宫格 单画面（默认等于第一个）
    @Nullable
    private SurfaceView cursorSurfaceView;//当前


    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_test_video);
        playerControlView = findViewById(R.id.player_control_view);
        fullScreenView = findViewById(R.id.full_screen_view);
        fullScreenView.setOnClickListener(v -> {
            setCurrentOutputView(surfaceView9);
            Assertions.checkNotNull(fullScreenView).setVisibility(View.GONE);
        });
        //设置全屏画面 监听
        setSurfaceListener(fullScreenView);
        initIntent();
        initView();
    }

    private void initIntent() {
        isOwner = getIntent().getBooleanExtra(OWNER_EXTRA, /* defaultValue= */ true);
    }

    private void initView() {
        GridLayout gridLayout = findViewById(R.id.grid_layout);
        for (int i = 0; i < 9; i++) {
            View view;
            if (i == 0) {
                //没有画面
                Button button = new Button(/* context= */ this);
                view = button;
                button.setText(getString(R.string.no_output_label));
                button.setOnClickListener(v -> reparent(/* surfaceView= */ null));
            } else if (i == 1) {
                //全屏幕
                Button button = new Button(/* context= */ this);
                view = button;
                button.setText(getString(R.string.full_screen_label));
                button.setOnClickListener(v -> {
                    setCurrentOutputView(fullScreenView);
                    Assertions.checkNotNull(fullScreenView).setVisibility(View.VISIBLE);
                });
            } else if (i == 2) {
                //新窗口
                Button button = new Button(/* context= */ this);
                view = button;
                button.setText(getString(R.string.new_activity_label));
                button.setOnClickListener(v -> startActivity(new Intent(TestVideoAct.this, TestVideoAct.class).putExtra(OWNER_EXTRA, /* value= */ false)));
            } else {
                SurfaceView surfaceView = new SurfaceView(this);
                view = surfaceView;
                //设置9空格 单画面监听
                setSurfaceListener(surfaceView);
                //点击事件
                surfaceView.setOnClickListener(v -> {
                    setCurrentOutputView(surfaceView);
                    surfaceView9 = surfaceView;
                });
                if (surfaceView9 == null) {
                    surfaceView9 = surfaceView;
                }
            }
            gridLayout.addView(view);
            GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
            layoutParams.width = 0;
            layoutParams.height = 0;
            layoutParams.columnSpec = GridLayout.spec(i % 3, 1f);
            layoutParams.rowSpec = GridLayout.spec(i / 3, 1f);
            layoutParams.bottomMargin = 10;
            layoutParams.leftMargin = 10;
            layoutParams.topMargin = 10;
            layoutParams.rightMargin = 10;
            view.setLayoutParams(layoutParams);
        }
    }

    //设置监听
    private void setSurfaceListener(SurfaceView surfaceView) {
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                if (surfaceView == cursorSurfaceView) {
                    reparent(surfaceView);
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            }
        });
    }


    //画面点击_切换画面(先点，再切)
    private void setCurrentOutputView(@Nullable SurfaceView surfaceView) {
        cursorSurfaceView = surfaceView;
        if (surfaceView != null && surfaceView.getHolder().getSurface() != null) {
            reparent(surfaceView);
        }
    }


    //替换播放的画面
    private static void reparent(@Nullable SurfaceView surfaceView) {
        ExoPlayerManager.getInstance().setReparent(surfaceView);
    }

    @OptIn(markerClass = UnstableApi.class)//不稳定的api 注解
    @Override
    public void onResume() {
        super.onResume();
        if (isOwner && ExoPlayerManager.getInstance().getExoPlayer() == null) {
            //初始化播放器
            Uri uri = Uri.parse(url1);
            ExoPlayerManager.getInstance().initExoPlayer(this).setPlayUri(this, uri);
            ExoPlayerManager.getInstance().setPlay();
        }
        setCurrentOutputView(surfaceView9);
        //控制器 与 播放器 结合
        ExoPlayerManager.getInstance().setPlayerControlView(playerControlView);
        playerControlView.show();
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void onPause() {
        super.onPause();
        //控制器 与 播放器 脱离
        playerControlView.setPlayer(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isOwner && isFinishing()) {
            ExoPlayerManager.getInstance().setRelease();
        }
    }


}
