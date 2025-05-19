package com.images.ui.pager;

import android.content.Context;
import android.view.View;

import com.images.config.ConfigBuild;
import com.images.ui.thing.photoview.PhotoView;
import com.images.ui.thing.photoview.PhotoViewAttacher;


/**
 * Created by Administrator on 2017/3/6.
 */

public class Imagepager {
    private Context context;
    private String iamgePath;
     public Imagepager(Context context, String iamgePath) {
        this.context = context;
        this.iamgePath = iamgePath;
    }

    public String getPath() {
        return iamgePath;
    }

    View view;

    public View getView() {
        if (view == null) {
            view = onViewCreated();
        }
        return view;
    }

    public View onViewCreated() {
        PhotoView view = new PhotoView(context);
        //view = LayoutInflater.from(context).inflate(R.layout.pager_image, null);
        //ImageView iv = (ImageView) view.findViewById(R.id.preview_iv);
        ConfigBuild.getBuild().setImageLoading(context, iamgePath, view);
        view.setOnPhotoTapListener(new  PhotoViewAttacher.OnPhotoTapListener() {

            @Override
            public void onPhotoTap(View view, float x, float y) {

            }

        });
        return view;
    }
}
