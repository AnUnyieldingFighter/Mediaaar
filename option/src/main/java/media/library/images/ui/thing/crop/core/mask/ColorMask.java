package media.library.images.ui.thing.crop.core.mask;

import android.graphics.Canvas;
import android.graphics.Color;

import media.library.images.ui.thing.crop.core.ILayer;
import media.library.images.ui.thing.crop.core.IMask;


/**
 * 颜色遮罩层
 * @author Zhouztashin
 * @version 1.0
 * @created 2016/3/15.
 */
public class ColorMask implements IMask {

    private int mColorId;
    public ColorMask(String color){
        mColorId = Color.parseColor(color);
    }
    public  ColorMask(int colorId){
        mColorId = colorId;
    }

    @Override
    public void draw(ILayer layer, Canvas c) {
        c.drawColor(mColorId);
    }

    /**
     * 获取半透明的遮罩层
     * @return
     */
    public static final ColorMask getTranslucentMask(){
        return new ColorMask("#90000000");
    }

}
