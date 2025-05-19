package com.images.ui.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import androidx.viewpager.widget.PagerAdapter;

import com.images.config.ConfigBuild;
import com.images.config.entity.ImageEntity;
import com.images.ui.thing.photoview.PhotoView;
import com.images.ui.thing.photoview.PhotoViewAttacher;

import java.util.ArrayList;
import java.util.List;


public class ImagePageAdapter extends PagerAdapter {

    private List<ImageEntity> images = new ArrayList<>();
    //已经选择的图片
    private ArrayList<ImageEntity> optionImage = new ArrayList<>();
    private ArrayList<String> optionPaths = new ArrayList<>();
    private Activity activity;
    public AdapterPreviewClickList listener;
    //type 预览类型：1： 全局 2：只预览已选择的图片
    private int type;

    public ImagePageAdapter(Activity activity,int type,
                            List<ImageEntity> images,
                            ArrayList<ImageEntity> optionImage) {
        this.activity = activity;
        this. type = type;
        this.images =images;
        this. optionImage = optionImage;
        for (int i = 0; i < optionImage.size(); i++) {
            String imagePath = optionImage.get(i).imagePathSource;
            optionPaths.add(imagePath);
        }

    }

    //获取已经选择的图片
    public ArrayList<ImageEntity> getOptionIamge() {
        if (type == 1) {
            return optionImage;
        }
        ArrayList<ImageEntity> images = new ArrayList<>();
        for (int i = 0; i < optionImage.size(); i++) {
            ImageEntity image = optionImage.get(i);
            if (!image.isOption) {
                continue;
            }
            images.add(image);
        }
        return images;
    }

    //获取已经选择的数量
    public int getOptionSize() {
        return optionPaths.size();
    }

    //true 可以删除图片
    public boolean isDeleteImage(int index) {
        ImageEntity image = getPath(index);
        if (!image.isDelete) {
            //图片不能删除
            ConfigBuild.getBuild().interdictMsg(activity,image);
            return false;
        }
        return true;
    }

    //某张图片是否被选中
    public boolean isOptionPath(int index) {
        ImageEntity iamge = getPath(index);
        return optionPaths.contains(iamge.imagePathSource);
    }

    //选择某张图片
    public boolean clickOption(int index) {
        boolean isExist = false;
        switch (type) {
            case 1:
                ImageEntity image = images.get(index);
                String path = image.imagePathSource;
                isExist = optionPaths.contains(path);
                if (isExist) {
                    optionPaths.remove(path);
                    for (int i = 0; i < optionImage.size(); i++) {
                        ImageEntity temp = optionImage.get(i);
                        if (path.equals(temp.imagePathSource)) {
                            optionImage.remove(i);
                            break;
                        }
                    }
                    break;
                }
                image.isOption = true;
                optionImage.add(image);
                optionPaths.add(path);
                break;
            case 2:
                image = optionImage.get(index);
                path = image.imagePathSource;
                isExist = optionPaths.contains(path);
                if (isExist) {
                    image.isOption = false;
                    optionPaths.remove(path);
                    break;
                }
                image.isOption = true;
                optionPaths.add(path);
                break;

        }
        return !isExist;
    }

    private ImageEntity getPath(int index) {
        if (type == 1) {
            return images.get(index);
        }
        return optionImage.get(index);
    }

    public void setPhotoViewClickListener(AdapterPreviewClickList listener) {
        this.listener = listener;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        PhotoView photoView = new PhotoView(activity);
        String path = getPath(position).imagePathSource;
        ConfigBuild.getBuild().setImageLoading(activity, path, photoView);
        photoView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
            @Override
            public void onPhotoTap(View view, float x, float y) {
                if (listener != null) listener.OnPhotoTapListener(view, x, y);
            }
        });
        container.addView(photoView);
        return photoView;
    }

    @Override
    public int getCount() {
        if (type == 1) {
            return images.size();
        }
        return optionImage.size();
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


}
