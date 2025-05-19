package com.images.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.images.config.entity.ImageEntity;
import com.images.imageselect.R;
import com.images.photo.PhotosMnager;
import com.images.ui.adapter.ImagePageAdapter;
import com.images.ui.bean.PreviewImageBean;
import com.images.ui.view.ActionBar;
import com.images.ui.view.ViewPagerFixed;

import java.util.List;

public class PreviewSelectActivity extends PreviewActivity {
    private ImageView chooseIv;
    private int max;
    private ImagePageAdapter imagePageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        Intent it = getIntent();
        Bundle bundle = it.getExtras();
        PreviewImageBean previewBean = (PreviewImageBean) bundle.get("bean");
        //
        bottomRl = findViewById(R.id.bottom_rl);
        rootView = findViewById(R.id.content);
        ViewPagerFixed viewPager = (ViewPagerFixed) findViewById(R.id.image_vp);
        View chooseTv = findViewById(R.id.preview_choose_tv);
        chooseTv.setVisibility(View.VISIBLE);
        chooseTv.setOnClickListener(this);
        chooseIv = (ImageView) findViewById(R.id.preview_choose_iv);
        chooseIv.setVisibility(View.VISIBLE);
        chooseIv.setOnClickListener(this);
        actionBar = (ActionBar) findViewById(R.id.actionbar);
        actionBar.setOnClickListener(this);
        bottomRl.setOnClickListener(this);
        //
        List<ImageEntity> images = PhotosMnager.getInstance().getImgs(previewBean.imgFileIndex);
        index = previewBean.index;
        //
        if (previewBean.type == 1) {
            String imagePath = images.get(0).imagePathSource;
            if (TextUtils.isEmpty(imagePath)) {
                images = images.subList(1, images.size());
                index--;
            }
        }
        //
        imagePageAdapter = new ImagePageAdapter(this,
                previewBean.type, images, previewBean.optionImage);
        //
        imagePageAdapter.setPhotoViewClickListener(new PreviewClickList());
        viewPager.setAdapter(imagePageAdapter);
        viewPager.addOnPageChangeListener(new OnPagerChange());
        //
        config = previewBean.config;
        if (config.isMore) {
            max = config.configBuildMore.getImageSelectMaximum();
        }
        actionBar.setConfigsPreview(this, config, this);

        viewPager.setCurrentItem(index);
        //
        if (max == 0) {
            bottomRl.setVisibility(View.GONE);
        }
        onPageSelected(index);
    }

    @Override
    protected void onClick(int id) {
        if (id == R.id.preview_choose_tv || id == R.id.preview_choose_iv) {
            setSelct();
            return;
        }
        if (id == R.id.action_back) {
            onBackPressed();
            return;
        }
        //发送
        int size = imagePageAdapter.getOptionSize();
        if (id == R.id.action_option && size > 0) {
            Intent it = new Intent();
            PreviewImageBean p = new PreviewImageBean();
            p.optionImage = imagePageAdapter.getOptionIamge();
            Bundle bundle = new Bundle();
            bundle.putSerializable("bean", p);
            it.putExtras(bundle);
            setResult(905, it);
            finish();
            return;
        }
    }

    @Override
    public void onBackPressed() {
        Intent it = new Intent();
        PreviewImageBean p = new PreviewImageBean();
        p.optionImage = imagePageAdapter.getOptionIamge();
        Bundle bundle = new Bundle();
        bundle.putSerializable("bean", p);
        it.putExtras(bundle);
        setResult(904, it);
        super.onBackPressed();
    }

    //选择图片逻辑
    private void setSelct() {
        boolean isDelete = imagePageAdapter.isDeleteImage(index);
        if (!isDelete) {
            return;
        }
        boolean isExist = imagePageAdapter.isOptionPath(index);
        int optionSize = imagePageAdapter.getOptionSize();
        if (!isExist && optionSize >= max) {
            Toast.makeText(this, R.string.image_select_max_error, Toast.LENGTH_LONG).show();
            return;
        }
        isExist = imagePageAdapter.clickOption(index);
        setIcon(isExist);
    }

    //设置是否选中
    private void setIcon(boolean isExist) {
        int icon = isExist ? R.mipmap.image_select_true : R.mipmap.image_select_false;
        chooseIv.setImageResource(icon);
        setOption();
    }


    private void setOption() {
        String hint = config.configPreview.getOption();
        if (max > 0) {
            int selectSize = imagePageAdapter.getOptionSize();
            hint += "(" + selectSize + "/" + max + ")";
        }
        actionBar.setOptionText(hint);
    }

    private int index;

    @Override
    public void onPageSelected(int position) {
        index = position;
        //设置显示标题
        actionBar.setTitle((index + 1) + "/" + imagePageAdapter.getCount());
        boolean isExist = imagePageAdapter.isOptionPath(index);
        setIcon(isExist);
    }


}
