package com.images.ui.activity;

import android.os.Build;
import android.view.View;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.images.config.Configs;
import com.images.imageselect.R;
import com.images.ui.adapter.AdapterPreviewClickList;
import com.images.ui.view.ActionBar;

public class PreviewActivity extends AppCompatActivity implements View.OnClickListener {
    protected Configs config;
    protected ActionBar actionBar;
    protected View rootView, bottomRl;
    protected boolean isOnlyShow;

    @Override
    public void onClick(View v) {
        onClick(v.getId());
    }

    protected void onClick(int id) {

    }

    //全屏显示
    private void fullScreen() {
        switch (actionBar.getVisibility()) {
            case View.VISIBLE:
                actionBar.setAnimation(AnimationUtils.loadAnimation(this, R.anim.top_out));
                actionBar.setVisibility(View.GONE);
                if (!isOnlyShow) {
                    bottomRl.setAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_out));
                    bottomRl.setVisibility(View.GONE);
                }
                actionBar.setBarColor(true);//通知栏所需颜色
                //给最外层布局加上这个属性表示，Activity全屏显示，且状态栏被隐藏覆盖掉。
                if (Build.VERSION.SDK_INT >= 16) {
                    rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
                }
                break;
            case View.GONE:
                actionBar.setAnimation(AnimationUtils.loadAnimation(this, R.anim.top_in));
                actionBar.setVisibility(View.VISIBLE);
                if (!isOnlyShow) {
                    bottomRl.setAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
                    bottomRl.setVisibility(View.VISIBLE);
                }
                actionBar.setBarColor(false);
                //Activity全屏显示，但状态栏不会被隐藏覆盖，状态栏依然可见，Activity顶端布局部分会被状态遮住
                if (Build.VERSION.SDK_INT >= 16) {
                    rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                }
                break;
        }
    }


    class PreviewClickList implements AdapterPreviewClickList {

        @Override
        public void OnPhotoTapListener(View view, float v, float v1) {
            fullScreen();
        }
    }

    class OnPagerChange implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            PreviewActivity.this.onPageSelected(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }

    }

    public void onPageSelected(int position) {

    }
}
