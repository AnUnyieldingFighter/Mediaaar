package media.library.images.ui.views;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.TextureView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class VideoTextureView extends TextureView {
    public VideoTextureView(@NonNull Context context) {
        super(context);
    }

    public VideoTextureView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoTextureView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public VideoTextureView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    protected int type = 1;//1：等比 2：满View播放

    public void setType(int type) {
        this.type = type;
    }

    public void setPlayViewZoom(MediaPlayer mediaPlayer) {

        if (type == 1) {
            setVideoAspect(mediaPlayer, this);
        }
        if (type == 2) {
            setVideoAspectFull(mediaPlayer, this);
        }
    }

    private float oVideoW, oViewH;
    private float oTextureW, oTureH;
    public Matrix matrix;
    private RectF videoRectF;
    public boolean isChange;

    /**
     * 获取变换之后的宽和高
     *
     * @return
     */
    public RectF getRectF() {
        if (videoRectF != null && matrix != null) {
            RectF transformedRect = new RectF(videoRectF);
            matrix.mapRect(transformedRect);
            return transformedRect;
        }
        return null;
    }

    //等比播放
    private void setVideoAspect(MediaPlayer mediaPlayer, TextureView textureView) {
        //mtextureViewWidth为textureView宽，mtextureViewHeight为textureView高
        //mtextureViewWidth宽高，为什么需要用传入的，因为全屏显示时宽高不会及时更新
        isChange = false;

        float videoWidth = mediaPlayer.getVideoWidth();
        float videoHeight = mediaPlayer.getVideoHeight();

        if (videoWidth == 0 || videoHeight == 0) {
            return;
        }
        float textureWidth = textureView.getWidth();
        float textureHeight = textureView.getHeight();
        if (oVideoW == videoWidth && oViewH == videoHeight && oTextureW == textureWidth && oTureH == textureHeight) {
            return;
        }

        //
        oVideoW = videoWidth;
        oViewH = videoHeight;
        oTextureW = textureWidth;
        oTureH = textureHeight;
        //
        isChange = true;
        videoRectF = new RectF(0, 0, textureWidth, textureHeight);

        //得到缩放比，从而获得最佳缩放比
        float sx = textureWidth / videoWidth;
        float sy = textureHeight / videoHeight;
        //先将视频变回原来的大小
        float sx1 = videoWidth / textureWidth;
        float sy1 = videoHeight / textureHeight;
        matrix = new Matrix();
        matrix.preScale(sx1, sy1);
        //然后判断最佳比例，满足一边能够填满
        if (sx >= sy) {
            matrix.preScale(sy, sy);
            //然后判断出左右偏移，实现居中，进入到这个判断，证明y轴是填满了的
            float leftX = (textureWidth - videoWidth * sy) / 2;
            matrix.postTranslate(leftX, 0);
        } else {
            matrix.preScale(sx, sx);
            float leftY = (textureHeight - videoHeight * sx) / 2;
            matrix.postTranslate(0, leftY);
        }
        textureView.setTransform(matrix);//将矩阵添加到textureView
        textureView.postInvalidate();//重绘视图
    }

    private float oVideoW2, oViewH2;
    private float oTextureW2, oTureH2;


    private void setVideoAspectFull(MediaPlayer mediaPlayer, TextureView textureView) {
        isChange = false;
        // 获取视频的原始宽高比
        float videoWidth = mediaPlayer.getVideoWidth();
        float videoHeight = mediaPlayer.getVideoHeight();
        if (videoWidth == 0 || videoHeight == 0) {
            return;
        }
        // 获取TextureView的宽高
        float textureWidth = textureView.getWidth();
        float textureHeight = textureView.getHeight();
        if (oVideoW2 == videoWidth && oViewH2 == videoHeight && oTextureW2 == textureWidth && oTureH2 == textureHeight) {
            return;
        }

        //
        oVideoW2 = videoWidth;
        oViewH2 = videoHeight;
        oTextureW2 = textureWidth;
        oTureH2 = textureHeight;
        //
        if (videoWidth == textureWidth && videoHeight == textureHeight) {
            return;
        }
        isChange = true;
        videoRectF = new RectF(0, 0, textureWidth, textureHeight);
        if (videoWidth > videoHeight || videoWidth == videoHeight) {
            //横屏 视频
            verticalScreen(textureView, textureWidth, textureHeight, videoWidth, videoHeight);
        }
        if (videoHeight > videoWidth) {
            //竖屏 视频
            verticalScreen(textureView, textureWidth, textureHeight, videoWidth, videoHeight);
        }

    }

    //竖屏(似乎通用)
    private void verticalScreen(TextureView textureView, float textureWidth, float textureHeight, float videoWidth, float videoHeight) {
        float scaleX = textureWidth / videoWidth;
        float scaleY = textureHeight / videoHeight;
        float scale = Math.max(scaleX, scaleY);
        //
        matrix = new Matrix();
        //第1步:把视频区移动到View区,使两者中心点重合.
        matrix.preTranslate((textureWidth - videoWidth) / 2, (textureHeight - videoHeight) / 2);
        //第2步:因为默认视频是fitXY的形式显示的,所以首先要缩放还原回来.
        matrix.preScale(videoWidth / textureWidth, videoHeight / textureHeight);
        //第3步
        matrix.postScale(scale, scale, textureWidth / 2, textureHeight / 2);

        textureView.setTransform(matrix);
        textureView.postInvalidate();


    }

    private void horizontalScreen(TextureView textureView, float textureWidth, float textureHeight, float videoWidth, float videoHeight) {
        float scaleX = textureWidth / videoWidth;
        float scaleY = textureHeight / videoHeight;
        float scale = Math.max(scaleX, scaleY);
        //创建Matrix并设置缩放变换
        matrix = new Matrix();
        //设置缩放比例
        matrix.setScale(scale, scale);
        // 计算平移量，使视频内容居中显示
        float translateX = (textureWidth - videoWidth * scale) / 2.0F;
        float translateY = (textureHeight - videoHeight * scale) / 2.0F;
        matrix.postTranslate(translateX, translateY);
        //
        textureView.setTransform(matrix);
        //重绘视图
        textureView.postInvalidate();
    }

}
