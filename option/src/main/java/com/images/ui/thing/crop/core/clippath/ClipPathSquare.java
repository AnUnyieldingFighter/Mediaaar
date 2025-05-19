package com.images.ui.thing.crop.core.clippath;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import com.images.ui.thing.crop.core.ILayer;


/**
 * Canvas.clipPath(..)方式实现的正方形
 *
 * @author Zhouztashin
 * @version 1.0
 * @created 2016/4/27.
 */
public class ClipPathSquare implements IClipPathShape {
    private int BORDER_WIDTH_DEFAULT = 6;
    private int mWidth = 0, mheight;
    private Paint mPaint;

    /**
     * @param width 宽度
     */

    public ClipPathSquare(int width) {
        mWidth = width;
        mheight = width;
        init();
    }

    public ClipPathSquare(int width, int height) {
        mWidth = width;
        mheight = height;
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(BORDER_WIDTH_DEFAULT);
        mPaint.setAntiAlias(true);
    }

    /**
     * 设置边框宽度 单位Px
     *
     * @param borderWidth
     */
    public void setBorderWidth(int borderWidth) {
        mPaint.setStrokeWidth(borderWidth);
    }

    /**
     * 设置边框颜色
     *
     * @param colorResId
     */
    public void setBorderColor(int colorResId) {
        mPaint.setColor(colorResId);
    }


    @Override
    public int width() {
        return mWidth;
    }

    @Override
    public int height() {
        return mheight;
    }

    @Override
    public Path createShapePath(ILayer layer) {
        int left = layer.width() / 2 - mWidth / 2;
        int top = layer.height() / 2 - mheight / 2;
        Path path = new Path();
        path.addRect(left, top, left + mWidth, top + mheight, Path.Direction.CW);
        return path;
    }

    @Override
    public Paint createShapePaint() {
        return mPaint;
    }


}
