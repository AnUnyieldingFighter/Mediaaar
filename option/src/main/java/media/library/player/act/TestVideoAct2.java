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
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Surface;
import android.view.SurfaceControl;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;

import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.Assertions;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.common.util.Util;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.dash.DashMediaSource;
import androidx.media3.exoplayer.drm.DefaultDrmSessionManager;
import androidx.media3.exoplayer.drm.DrmSessionManager;
import androidx.media3.exoplayer.drm.FrameworkMediaDrm;
import androidx.media3.exoplayer.drm.HttpMediaDrmCallback;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.ui.PlayerControlView;

import java.util.UUID;

import com.images.imageselect.R;

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
public final class TestVideoAct2 extends Activity {

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
    private SurfaceView nonFullScreenView;//非全屏
    @Nullable
    private SurfaceView currentOutputView;//当前

    @Nullable
    private static ExoPlayer allPlayer;//播放器
    @Nullable
    private static SurfaceControl allSurfaceControl;
    @Nullable
    private static Surface allVideoSurface;


    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_test_video);
        playerControlView = findViewById(R.id.player_control_view);
        fullScreenView = findViewById(R.id.full_screen_view);
        fullScreenView.setOnClickListener(v -> {
            setCurrentOutputView(nonFullScreenView);
            Assertions.checkNotNull(fullScreenView).setVisibility(View.GONE);
        });
        attachSurfaceListener(fullScreenView);
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
                button.setOnClickListener(v -> startActivity(new Intent(TestVideoAct2.this, TestVideoAct2.class).putExtra(OWNER_EXTRA, /* value= */ false)));
            } else {
                SurfaceView surfaceView = new SurfaceView(this);
                view = surfaceView;
                attachSurfaceListener(surfaceView);
                surfaceView.setOnClickListener(v -> {
                    setCurrentOutputView(surfaceView);
                    nonFullScreenView = surfaceView;
                });
                if (nonFullScreenView == null) {
                    nonFullScreenView = surfaceView;
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

    @OptIn(markerClass = UnstableApi.class)//不稳定的api 注解
    @Override
    public void onResume() {
        super.onResume();

        if (isOwner && allPlayer == null) {
            initializePlayer();
        }

        setCurrentOutputView(nonFullScreenView);

        PlayerControlView playerControlView = Assertions.checkNotNull(this.playerControlView);
        playerControlView.setPlayer(allPlayer);
        playerControlView.show();
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void onPause() {
        super.onPause();

        Assertions.checkNotNull(playerControlView).setPlayer(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isOwner && isFinishing()) {
            if (allSurfaceControl != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    allSurfaceControl.release();
                }
                allSurfaceControl = null;
            }
            if (allVideoSurface != null) {
                allVideoSurface.release();
                allVideoSurface = null;
            }
            if (allPlayer != null) {
                allPlayer.release();
                allPlayer = null;
            }
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private void initializePlayer() {
        Intent intent = getIntent();
        String action = intent.getAction();
        Uri uri = ACTION_VIEW.equals(action) ? Assertions.checkNotNull(intent.getData()) :
                Uri.parse(url1);
        DrmSessionManager drmSessionManager;
        if (intent.hasExtra(DRM_SCHEME_EXTRA)) {
            String drmScheme = Assertions.checkNotNull(intent.getStringExtra(DRM_SCHEME_EXTRA));
            String drmLicenseUrl = Assertions.checkNotNull(intent.getStringExtra(DRM_LICENSE_URL_EXTRA));
            UUID drmSchemeUuid = Assertions.checkNotNull(Util.getDrmUuid(drmScheme));
            DataSource.Factory licenseDataSourceFactory = new DefaultHttpDataSource.Factory();
            HttpMediaDrmCallback drmCallback = new HttpMediaDrmCallback(drmLicenseUrl, licenseDataSourceFactory);
            drmSessionManager = new DefaultDrmSessionManager.Builder().setUuidAndExoMediaDrmProvider(drmSchemeUuid, FrameworkMediaDrm.DEFAULT_PROVIDER).build(drmCallback);
        } else {
            drmSessionManager = DrmSessionManager.DRM_UNSUPPORTED;
        }

        DataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(this);
        MediaSource mediaSource;
        @Nullable String fileExtension = intent.getStringExtra(EXTENSION_EXTRA);
        @C.ContentType int type = TextUtils.isEmpty(fileExtension) ? Util.inferContentType(uri) : Util.inferContentTypeForExtension(fileExtension);
        if (type == C.CONTENT_TYPE_DASH) {
            mediaSource = new DashMediaSource.Factory(dataSourceFactory).setDrmSessionManagerProvider(unusedMediaItem -> drmSessionManager).createMediaSource(MediaItem.fromUri(uri));
        } else if (type == C.CONTENT_TYPE_OTHER) {
            mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).setDrmSessionManagerProvider(unusedMediaItem -> drmSessionManager).createMediaSource(MediaItem.fromUri(uri));
        } else {
            throw new IllegalStateException();
        }
        ExoPlayer player = new ExoPlayer.Builder(getApplicationContext()).build();
        player.setMediaSource(mediaSource);
        player.prepare();
        player.play();
        player.setRepeatMode(Player.REPEAT_MODE_ALL);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            allSurfaceControl = new SurfaceControl.Builder().setName(SURFACE_CONTROL_NAME).setBufferSize(/* width= */ 0, /* height= */ 0).build();
            allVideoSurface = new Surface(allSurfaceControl);
        }
        player.setVideoSurface(allVideoSurface);
        TestVideoAct2.allPlayer = player;
    }

    private void setCurrentOutputView(@Nullable SurfaceView surfaceView) {
        currentOutputView = surfaceView;
        if (surfaceView != null && surfaceView.getHolder().getSurface() != null) {
            reparent(surfaceView);
        }
    }

    private void attachSurfaceListener(SurfaceView surfaceView) {
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                if (surfaceView == currentOutputView) {
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

    @OptIn(markerClass = UnstableApi.class)
    private static void reparent(@Nullable SurfaceView surfaceView) {
        SurfaceControl surfaceControl = Assertions.checkNotNull(TestVideoAct2.allSurfaceControl);
        if (surfaceView == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                new SurfaceControl.Transaction().reparent(surfaceControl, /* newParent= */ null)
                        .setBufferSize(surfaceControl, /* w= */ 0, /* h= */ 0)
                        .setVisibility(surfaceControl, /* visible= */ false)
                        .apply();
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                SurfaceControl newParentSurfaceControl = surfaceView.getSurfaceControl();
                new SurfaceControl.Transaction().reparent(surfaceControl, newParentSurfaceControl)
                        .setBufferSize(surfaceControl, surfaceView.getWidth(), surfaceView.getHeight())
                        .setVisibility(surfaceControl, /* visible= */ true)
                        .apply();
            }
        }
    }

}
