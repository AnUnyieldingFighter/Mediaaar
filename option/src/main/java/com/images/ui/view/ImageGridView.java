package com.images.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.TextView;

import com.images.config.entity.ImageEntity;
import com.images.photo.DateUtile;

/**
 * Created by Administrator on 2017/3/6.
 */

public class ImageGridView extends GridView {
    public ImageGridView(Context context) {
        super(context);
    }

    public ImageGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private TextView timeTv;

    public void setShowView(TextView timeTv) {
        this.timeTv = timeTv;
        setOnScrollListener(new SlideListener());
    }

    class SlideListener implements AbsListView.OnScrollListener {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (scrollState == SCROLL_STATE_IDLE) {
                timeTv.setVisibility(View.GONE);
            } else if (scrollState == SCROLL_STATE_FLING) {
                timeTv.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (timeTv.getVisibility() == View.VISIBLE) {
                int index = firstVisibleItem + 1 == view.getAdapter().getCount() ? view.getAdapter().getCount() - 1 : firstVisibleItem + 1;
                ImageEntity image = (ImageEntity) view.getAdapter().getItem(index);
                if (image != null) {
                    timeTv.setText(DateUtile.formatPhotoDate(image.imagePathSource));
                }
            }
        }
    }
}
