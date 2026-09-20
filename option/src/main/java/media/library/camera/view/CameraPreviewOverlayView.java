package media.library.camera.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * 相机预览浮层。
 *
 * <p>点击浮层任意位置，会在点击位置放置一个正方形或长方形框。
 * 已有框时，按住框内部拖动可以移动位置。该 View 只负责显示和记录框的位置。</p>
 */
public class CameraPreviewOverlayView extends View {

    //四周黑色遮罩
    private final Paint dimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    //中间框
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    //框内淡白色覆盖
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF boxRect = new RectF();

    //true：允许手指按住已有方框内部后拖动方框；false：方框固定，不能拖动
    private boolean canDragBox = true;
    //true：允许点击方框外任意位置重新绘制方框；false：点击方框外不改变当前方框
    private boolean canTapRedrawBox = true;
    private float lastX;
    private float lastY;

    public CameraPreviewOverlayView(Context context) {
        super(context);
        init();
    }

    public CameraPreviewOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CameraPreviewOverlayView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 初始化画笔。
     */
    protected void init() {
        setWillNotDraw(false);
        // 四周黑色遮罩
        dimPaint.setColor(0x99000000);
        // 框内淡白色覆盖
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(0x22FFFFFF);

        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(dpToPx(2));
        //中间框的颜色
        borderPaint.setColor(Color.GREEN);
    }

    /**
     * 设置要添加的框类型。
     */
    public void setShapeType(int shapeType) {
        currentShape = shapeType;
    }

    //当前这一次手指按下后，是否正在拖动框
    private boolean dragging;

    /**
     * 设置是否允许拖动已有方框。
     *
     * @param canDragBox true 允许手指按住框内拖动方框
     */
    public void setCanDragBox(boolean canDragBox) {
        this.canDragBox = canDragBox;
        if (!canDragBox) {
            dragging = false;
        }
    }

    /**
     * 设置是否允许点击任意位置重新绘制方框。
     *
     * @param canTapRedrawBox true 允许点击框外位置，在点击点重新绘制方框
     */
    public void setCanTapRedrawBox(boolean canTapRedrawBox) {
        this.canTapRedrawBox = canTapRedrawBox;
    }

    //true 已经有选框
    private boolean hasBox;

    /**
     * 是否已经有选框。
     */
    public boolean hasBox() {
        return hasBox && boxRect.width() > 0 && boxRect.height() > 0;
    }

    /**
     * 获取当前选框在 View 坐标系中的位置。
     */
    public RectF getBoxRect() {
        if (!hasBox()) {
            return null;
        }
        return new RectF(boxRect);
    }

    /**
     * 清空当前选框。
     */
    public void clearBox() {
        hasBox = false;
        dragging = false;
        boxRect.setEmpty();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!hasBox()) {
            return;
        }
        drawOutsideMask(canvas);
        canvas.drawRect(boxRect, fillPaint);
        canvas.drawRect(boxRect, borderPaint);
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        if (!hasBox && width > 0 && height > 0) {
            createBox(width / 2f, height / 2f);
        }
    }

    private int currentShape = SHAPE_SQUARE;
    //长方形
    public static final int SHAPE_RECTANGLE = 1;
    //正方形
    public static final int SHAPE_SQUARE = 2;

    /**
     * 在指定中心点创建选框。
     */
    protected void createBox(float centerX, float centerY) {
        float width;
        float height;
        if (currentShape == SHAPE_SQUARE) {
            // 正方形
            width = dpToPx(160);
            height = width;
        } else {
            // 长方形
            width = dpToPx(220);
            height = dpToPx(140);
        }
        boxRect.set(
                centerX - width / 2f,
                centerY - height / 2f,
                centerX + width / 2f,
                centerY + height / 2f
        );
        keepBoxInBounds();
        hasBox = true;
        invalidate();
    }

    /**
     * 绘制选框四周的遮罩，框内区域保持透明。
     */
    private void drawOutsideMask(Canvas canvas) {
        //上方遮罩
        canvas.drawRect(0, 0, getWidth(), boxRect.top, dimPaint);
        //下方遮罩
        canvas.drawRect(0, boxRect.bottom, getWidth(), getHeight(), dimPaint);
        //左侧遮罩
        canvas.drawRect(0, boxRect.top, boxRect.left, boxRect.bottom, dimPaint);
        //右侧遮罩
        canvas.drawRect(boxRect.right, boxRect.top, getWidth(), boxRect.bottom, dimPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                lastX = event.getX();
                lastY = event.getY();
                if (hasBox() && boxRect.contains(lastX, lastY)) {
                    //点击在已有方框内部：如果允许拖动，则进入拖动状态；否则只消费事件，不移动方框
                    dragging = canDragBox;
                } else if (canTapRedrawBox) {
                    //点击在方框外部：如果允许重新绘制，则以当前点击位置为中心创建新方框
                    createBox(lastX, lastY);
                    dragging = false;
                } else {
                    //点击在方框外部，但不允许重新绘制：保持原方框不变
                    dragging = false;
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                if (canDragBox && dragging && hasBox()) {
                    //拖动状态下，根据手指移动距离同步移动方框
                    float x = event.getX();
                    float y = event.getY();
                    moveBox(x - lastX, y - lastY);
                    lastX = x;
                    lastY = y;
                }
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                dragging = false;
                return true;
            default:
                return true;
        }
    }


    /**
     * 移动选框。
     */
    private void moveBox(float dx, float dy) {
        boxRect.offset(dx, dy);
        keepBoxInBounds();
        invalidate();
    }

    /**
     * 保证选框不会超出浮层。
     */
    private void keepBoxInBounds() {
        if (boxRect.left < 0) {
            boxRect.offset(-boxRect.left, 0);
        }
        if (boxRect.top < 0) {
            boxRect.offset(0, -boxRect.top);
        }
        if (boxRect.right > getWidth()) {
            boxRect.offset(getWidth() - boxRect.right, 0);
        }
        if (boxRect.bottom > getHeight()) {
            boxRect.offset(0, getHeight() - boxRect.bottom);
        }
    }

    /**
     * dp 转 px。
     */
    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }


    /**
     * 按浮层坐标裁剪 PreviewView 当前画面。
     */
    public Bitmap cropBitmapByViewRect(Bitmap previewBitmap) {
        RectF viewRect = getBoxRect();
        if (viewRect == null) {
            //请先点击预览区域添加方框
            return null;
        }
        int overlayWidth = getWidth();
        int overlayHeight = getHeight();
        if (overlayWidth <= 0 || overlayHeight <= 0) {
            return null;
        }

        float scaleX = previewBitmap.getWidth() / (float) overlayWidth;
        float scaleY = previewBitmap.getHeight() / (float) overlayHeight;

        int left = clamp((int) (viewRect.left * scaleX), 0, previewBitmap.getWidth() - 1);
        int top = clamp((int) (viewRect.top * scaleY), 0, previewBitmap.getHeight() - 1);
        int right = clamp((int) (viewRect.right * scaleX), left + 1, previewBitmap.getWidth());
        int bottom = clamp((int) (viewRect.bottom * scaleY), top + 1, previewBitmap.getHeight());

        int width = right - left;
        int height = bottom - top;
        if (width <= 0 || height <= 0) {
            return null;
        }
        return Bitmap.createBitmap(previewBitmap, left, top, width, height);
    }

    /**
     * 限制数值范围。
     */
    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
