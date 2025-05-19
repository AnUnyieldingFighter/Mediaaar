package com.images.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.images.config.Configs;
import com.images.imageselect.R;
import com.images.ui.adapter.PreviewAdapter;
import com.images.config.entity.ImageEntity;
import com.images.ui.view.ActionBar;
import com.images.ui.view.ViewPagerFixed;

import java.util.ArrayList;

public class PreviewDeleteActivity extends PreviewActivity {

    private PreviewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        Intent it = getIntent();
        Bundle bundle = it.getExtras();
        if (bundle == null) {
            return;
        }
        config = (Configs) bundle.getSerializable("config");

        if (config == null) {
            return;
        }
        bottomRl = findViewById(R.id.bottom_rl);
        rootView = findViewById(R.id.content);
        actionBar = (ActionBar) findViewById(R.id.actionbar);
        actionBar.setOnClickListener(this);
        bottomRl.setOnClickListener(this);
        //
        ViewPagerFixed viewPager = (ViewPagerFixed) findViewById(R.id.image_vp);
        TextView completeTv = (TextView) findViewById(R.id.preview_delete_complete_tv);
        completeTv.setVisibility(View.VISIBLE);
        completeTv.setOnClickListener(this);
        //
        ArrayList<ImageEntity> images = config.getImages();
        adapter = new PreviewAdapter(this, images);
        adapter.setPhotoViewClickListener(new PreviewClickList());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new OnPagerChange());
        //
        position = config.previewIndex;
        actionBar.setConfigsPreview(this, config, this);
        viewPager.setCurrentItem(position);
        onPageSelected(position);
    }


    @Override
    protected void onClick(int id) {
        if (id == R.id.action_back) {
            onBackPressed();
            return;
        }
        if (id == R.id.preview_delete_complete_tv) {
            //预览 完成
            setResultIntent(adapter.getImages());
            return;
        }
        if (id == R.id.action_option) {
            //删除图片
            int count = adapter.getCount();
            boolean isDelect = adapter.isDeleteImage(position);
            if (!isDelect) {
                return;
            }
            if (count == 1) {
                setResultIntent(new ArrayList<ImageEntity>());
                return;
            }
            adapter.deleteImage(position);
            count -= 1;
            if (position > (count - 1)) {
                position = count;
            }
            onPageSelected(position);
            return;
        }
    }

    private void setResultIntent(ArrayList<ImageEntity> images) {
        Intent it = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable(Configs.TASK_COMPLETE_RESULT, images);
        it.putExtras(bundle);
        setResult(Configs.TASK_PRIVATE_DELECTE, it);
        finish();
    }

    @Override
    public void onBackPressed() {
        Intent it = new Intent();
        setResult(Configs.TASK_CANCEL, it);
        finish();

    }

    private int position;

    @Override
    public void onPageSelected(int position) {
        this.position = position;
        actionBar.setTitle((position + 1) + "/" + adapter.getCount());
    }
}
