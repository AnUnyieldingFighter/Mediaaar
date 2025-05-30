package com.images.ui.thing.crop.core.clippath;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Region;

import com.images.ui.thing.crop.core.BaseLayerView;
import com.images.unmix.ImageLog;


/**
 * 采用Canvas.clipPath(..)方式实现的预览层
 *
 * @author Zhouztashin
 * @version 1.0
 * @created 2016/4/22
 */
public class ClipPathLayerView extends BaseLayerView {

    public ClipPathLayerView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (mShape == null) {
            ImageLog.d("Warning:Shape is null");
            if (mMask != null) {
                mMask.draw(this, canvas);
            }
            return;
        }
        if (!(mShape instanceof  IClipPathShape)) {
            ImageLog.d("Shape must be subclass ofIClipPathShape ");
            return;
        }
       IClipPathShape shape = ( IClipPathShape) mShape;
        Path path = shape.createShapePath(this);
        canvas.save();
        canvas.clipPath(path, Region.Op.DIFFERENCE);
        if (mMask != null) {
            mMask.draw(this, canvas);
        }
        canvas.restore();
        canvas.drawPath(path, shape.createShapePaint());

    }
}
