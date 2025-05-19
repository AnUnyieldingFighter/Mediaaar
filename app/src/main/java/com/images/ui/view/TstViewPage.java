package com.images.ui.view;

import android.content.Context;

import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;

public class TstViewPage extends ViewPager {
    public TstViewPage(Context context) {
        super(context);
    }

    public TstViewPage(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        try {
            return super.onInterceptTouchEvent(event);
        } catch (IllegalArgumentException e) {
            Log.e("发生异常", e.getMessage());
            e.printStackTrace();
        }
        return false;
    }


}
