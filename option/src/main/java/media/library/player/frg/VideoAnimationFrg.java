package media.library.player.frg;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.ui.AspectRatioFrameLayout;

import media.library.player.manager.PlayerLog;

//动画
public class VideoAnimationFrg extends VideoFrg {

    //============================动画开始==========================================
    private int totaltHeight, totaltWidth;
    private int animationTime = 200;
    protected boolean isSmall;
    //因为动画发生改变  1:横屏动画中  2：竖屏动画中  0：未开始
    protected int viewTypeAnimation;

    public boolean isSmall() {
        return isSmall;
    }

    //修正为响应横屏动画UI
    protected void updateHorizontalView(View viewHorizontal, int tagHeight) {
        if (isVideoHorizontalScreen && viewTypeAnimation == 2) {
            viewTypeAnimation = 1;
            tagViewHeight = viewHorizontal.getHeight();
            if (tagViewHeight == 0) {
                tagViewHeight = tagHeight;
            }
            this.tagView = viewHorizontal;
            updateZoomVideoHorizontal(1, false);

        }
    }

    //true ：已经动画过，但响应的不是横屏动画UI
    protected boolean isUpdateHorizontalView() {
        if (isVideoHorizontalScreen && viewTypeAnimation == 2) {
            return true;
        }
        return false;
    }

    //竖屏动画
    private int tagHeight, tagWidth, deviateHeight;

    private int playerResizeMode;

    //设置竖屏动画 deviateHeight 偏移高度，比如playerView到底部的距离
    @OptIn(markerClass = UnstableApi.class)
    protected void setVerticalScreen(View view, int deviateHeight) {
        playerResizeMode = playerView.getResizeMode();
        if (isSmall) {
            animationViewInVisible(view);
            return;
        }
        tagHeight = playerView.getHeight();
        tagWidth = playerView.getWidth();
        this.deviateHeight = deviateHeight;
        animationViewVisible(view);
    }

    //横屏动画 要展开和收缩的view
    private View tagView;
    private int tagViewHeight;

    //设置横屏动画 tagView：要变化的view
    protected void setHorizontalScreen(View view, View tagView) {
        if (isSmall) {
            animationViewInVisible(view);
            return;
        }
        tagViewHeight = tagView.getHeight();
        this.tagView = tagView;
        animationViewVisible(view);
    }

    //动画可见
    protected void animationViewVisible(View view) {
        if (isSmall) {
            animationViewInVisible(view);
            return;
        }
        isSmall = true;
        //
        totaltHeight = view.getHeight();
        totaltWidth = view.getWidth();
        view.setVisibility(View.INVISIBLE);
        view.setTranslationY(totaltHeight);
        view.animate().translationY(0).setDuration(animationTime) // 设置动画时长为500毫秒
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        onAnimation(1, true, false);
                        view.setVisibility(View.VISIBLE); // 动画开始时设置View为可见
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        onAnimation(1, false, true);
                    }
                }).setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                    @Override
                    public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                        float payTime = (float) animation.getCurrentPlayTime();
                        if (payTime > animationTime) {
                            payTime = animationTime;
                        }
                        float animatedValue = (float) animation.getAnimatedValue();
                        float ratio = payTime / animationTime;
                        setZoomVideo(ratio, false);
                        PlayerLog.d("动画", "动画值---animatedValue:" + animatedValue + " payTime:" + payTime);
                    }
                });
    }

    //动画不可见
    protected void animationViewInVisible(View view) {
        isSmall = false;
        totaltHeight = view.getHeight();
        totaltWidth = view.getWidth();
        view.setVisibility(View.VISIBLE);
        view.setTranslationY(0);
        view.animate().translationY(totaltHeight).setDuration(animationTime) // 设置动画时长为500毫秒
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        onAnimation(2, true, false);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        onAnimation(2, false, true);
                        view.setVisibility(View.INVISIBLE); // 动画开始时设置View为可见
                    }
                }).setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                    @Override
                    public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                        // 获取当前动画的进度值
                        float payTime = (float) animation.getCurrentPlayTime();
                        if (payTime > animationTime) {
                            payTime = animationTime;
                        }
                        float animatedValue = (float) animation.getAnimatedValue();
                        float ratio = payTime / animationTime;
                        setZoomVideo(ratio, true);
                        PlayerLog.d("动画", "动画值---animatedValue:" + animatedValue + " payTime:" + payTime);
                    }
                });
    }


    private void setZoomVideo(float ratio, boolean isExpand) {
        if (isVideoHorizontalScreen == null) {
            viewTypeAnimation = 0;
            return;
        }
        if (isVideoHorizontalScreen) {
            viewTypeAnimation = 1;
            updateZoomVideoHorizontal(ratio, isExpand);
        } else {
            viewTypeAnimation = 2;
            updateZoomVideoVertical(ratio, isExpand);
        }

    }

    //横屏偏移 isExpand true 展开 false 收缩
    @OptIn(markerClass = UnstableApi.class)
    private void updateZoomVideoHorizontal(float ratio, boolean isExpand) {
        if (tagView == null) {
            return;
        }
        if (tagView.getVisibility() != View.VISIBLE) {
            return;
        }
        ViewGroup.LayoutParams params = tagView.getLayoutParams();
        //高度总共缩小
        int temp = tagViewHeight / 2;
        float newHeight = temp * ratio;
        if (isExpand) {
            int h = temp + (int) newHeight;
            if (h > tagViewHeight) {
                h = tagViewHeight;
            }
            params.height = h;
        } else {
            int h = tagViewHeight - (int) newHeight;
            if (h < temp) {
                h = temp;
            }
            params.height = h;
        }
        tagView.setLayoutParams(params);
    }


    //竖屏缩放 isExpand true 展开 false 收缩

    @OptIn(markerClass = UnstableApi.class)
    private void updateZoomVideoVertical(float ratio, boolean isExpand) {
        int tempHeight = tagHeight + deviateHeight;
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) playerView.getLayoutParams();
        float temp = totaltHeight * ratio;
        int newHeight = (int) temp;
        if (isExpand) {
            //展开
            int h = tempHeight - totaltHeight + newHeight;
            if (h > tagHeight) {
                h = tagHeight;
            }
            if (playerResizeMode == AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH) {
                //宽度要等比缩小  即保持比例
                double wRatio = (double) h / (double) tagHeight;
                double newWidth = wRatio * tagWidth;
                if (newWidth > tagWidth) {
                    newWidth = tagWidth;
                }
                params.width = (int) newWidth;
                params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                PlayerLog.d("动画", "宽度变化展开：tagHeight=" + tagHeight + " tagWidth=" + tagWidth + "  newWidth:" + newWidth);
            }
            params.height = h;
        } else {
            //收缩
            int h = tempHeight - newHeight;
            if (h > tagHeight) {
                h = tagHeight;
            }
            if (playerResizeMode == AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH) {
                //宽度要等比缩小  即保持比例
                double wRatio = (double) h / (double) tagHeight;
                double newWidth = wRatio * tagWidth;
                if (newWidth > tagWidth) {
                    newWidth = tagWidth;
                }
                params.width = (int) newWidth;
                params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                PlayerLog.d("动画", "宽度变化收缩：tempHeight=" + tempHeight + " tagWidth=" + tagWidth + "  newWidth:" + newWidth + " h:" + h);
            }
            params.height = h;
        }

        playerView.setLayoutParams(params);
    }

    //type:1 收缩动画  2：展开动画
    protected void onAnimation(int type, boolean isStart, boolean isEnd) {
        switch (type) {
            case 1:
                //收缩动画
                if (isStart) {
                    videoOperate.setViewPageSlide(false);
                }
                break;
            case 2:
                //展开动画
                if (isEnd) {
                    viewTypeAnimation = 0;
                    videoOperate.setViewPageSlide(true);
                }
                break;
        }
    }


    //============================动画结束==========================================
}
