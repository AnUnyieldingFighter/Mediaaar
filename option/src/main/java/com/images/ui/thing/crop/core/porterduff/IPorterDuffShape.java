package com.images.ui.thing.crop.core.porterduff;


import com.images.ui.thing.crop.core.ILayer;
import com.images.ui.thing.crop.core.IShape;

/**
 * 采用PorterDuffXfermode实现的形状接口
 * @author Zhouztashin
 * @version 1.0
 * @created 2016/4/22.
 */
public interface IPorterDuffShape  extends IShape {

    /**
     * 绘画具体的形状
     * @param layer 预览层
     * @param canvasWrapper Canvas装饰类
     */
    void draw(ILayer layer, CanvasWrapper canvasWrapper);
    int width();
    int height();

}
