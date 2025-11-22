package media.library.player.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

public class VideoPageRl2 extends RelativeLayout {

    public VideoPageRl2(Context context) {
        super(context);
    }

    public VideoPageRl2(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoPageRl2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public VideoPageRl2(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        handleInterceptTouchEvent(ev);
        return super.onInterceptTouchEvent(ev);
    }

    private boolean isVertical = false;
    private float initialX = 0f;
    private float initialY = 0f;

    private void handleInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialX = ev.getX();
                initialY = ev.getY();
                isVertical = false;
                break;
            case MotionEvent.ACTION_MOVE:
                if (isVertical) {
                    return;
                }
                float dx = ev.getX() - initialX;
                float dy = ev.getY() - initialY;
                initialX = ev.getX();
                initialY = ev.getY();
                if (Math.abs(dy) > Math.abs(dx)) {
                    //垂直滑动
                    isVertical = true;
                    if (listener != null) {
                        listener.onVertical();
                    }

                }
                break;
        }
    }

    private OnVerticalListener listener;

    public void setOnVerticalListener(OnVerticalListener listener) {
        this.listener = listener;
    }

    public interface OnVerticalListener {
        void onVertical();
    }

}
