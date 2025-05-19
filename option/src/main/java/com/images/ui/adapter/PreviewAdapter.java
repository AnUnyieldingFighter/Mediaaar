package com.images.ui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.viewpager.widget.PagerAdapter;

import com.images.config.ConfigBuild;
import com.images.config.entity.ImageEntity;
import com.images.ui.thing.photoview.PhotoView;
import com.images.ui.thing.photoview.PhotoViewAttacher;

import java.util.ArrayList;


public class PreviewAdapter extends PagerAdapter {
    //预览的图片
    private ArrayList<ImageEntity> images = new ArrayList<>();
    private Context context;
    public AdapterPreviewClickList listener;

    public PreviewAdapter(Context context, ArrayList<ImageEntity> paths) {
        this.context = context;
        this.images = paths;
    }

    public void setPhotoViewClickListener(AdapterPreviewClickList listener) {
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return images.size();
    }

    //true 可以删除图片
    public boolean isDeleteImage(int index) {
        ImageEntity image = images.get(index);
        if (!image.isDelete) {
            //图片不能删除
            ConfigBuild.getBuild().interdictMsg(context,image);
            return false;
        }
        return true;
    }

    public void deleteImage(int index) {
        images.remove(index);
        notifyDataSetChanged();
    }

    public ArrayList<ImageEntity> getImages() {
        return images;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        PhotoView photoView = new PhotoView(context);
        ImageEntity entity = images.get(position);
        ConfigBuild.getBuild().setImageLoading(context, entity.imagePathSource, photoView);
        photoView.setOnPhotoTapListener(new PhotoTapListener());
        container.addView(photoView);
        return photoView;
    }


    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    class PhotoTapListener implements PhotoViewAttacher.OnPhotoTapListener {

        @Override
        public void onPhotoTap(View view, float x, float y) {
            if (listener == null) {
                return;
            }
            listener.OnPhotoTapListener(view, x, y);
        }
    }
}
