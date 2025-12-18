package media.library.player.videoNew.frg2;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.NonNull;

//动画
public class VideoAnimationFrg21 extends VideoFrg1 {
    //============================动画开始==========================================
     private int animationTime = 200;

    //因为动画发生改变  1:横屏动画中  2：竖屏动画中  0：未开始
    protected int viewTypeAnimation;

    //true ：已是动画效果
    protected boolean isSmall;

    //true ：已经动画过，但响应的不是横屏动画UI
    protected boolean isUpdateHorizontalView() {
        if (isVideoHorizontalScreen && viewTypeAnimation == 2) {
            return true;
        }
        return false;
    }

    //竖屏动画
    protected void setVerticalScreenNew(int animationValue) {
        this.animationValueTotal = animationValue;
        if (isSmall) {
            animationViewClose();
            return;
        }
        animationViewShow();
    }


    private int animationValueTotal;

    protected void setHorizontalScreenNew(int animationValue) {
        this.animationValueTotal = animationValue;
        if (isSmall) {
            animationViewClose();
            return;
        }
        animationViewShow();
    }

    //视频缩小动画
    private void animationViewShow() {
        isSmall = true;
        //
        ValueAnimator animator = ValueAnimator.ofFloat(0, animationValueTotal);
        //
        animator.setDuration(animationTime);
        animator.setInterpolator(new AccelerateDecelerateInterpolator()); // 插值器（先加速后减速）
        // animator.setRepeatCount(ValueAnimator.INFINITE); // 重复次数（INFINITE 无限）
        // animator.setRepeatMode(ValueAnimator.REVERSE); // 重复模式（REVERSE 反向，RESTART 重启）
        animator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(@NonNull Animator animation) {
                onAnimation(1, true, false);
                onAnimation(1, 1, null, animationTime, animationValueTotal);
            }

            @Override
            public void onAnimationEnd(@NonNull Animator animation) {
                onAnimation(1, false, true);
                onAnimation(1, 2, null, animationTime, animationValueTotal);
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animation) {

            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animation) {
                //动画重复
            }
        });
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                //动画移动速度
                onAnimation(1, 3, animation, animationTime, animationValueTotal);
            }
        });
        animator.start();
    }

    private void animationViewClose() {
        isSmall = false;
        //
        ValueAnimator animator = ValueAnimator.ofFloat(animationValueTotal, 0);
        //
        animator.setDuration(animationTime);
        animator.setInterpolator(new AccelerateDecelerateInterpolator()); // 插值器（先加速后减速）
        // animator.setRepeatCount(ValueAnimator.INFINITE); // 重复次数（INFINITE 无限）
        // animator.setRepeatMode(ValueAnimator.REVERSE); // 重复模式（REVERSE 反向，RESTART 重启）
        animator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(@NonNull Animator animation) {
                onAnimation(2, true, false);
                onAnimation(2, 1, null, animationTime, animationValueTotal);
            }

            @Override
            public void onAnimationEnd(@NonNull Animator animation) {
                onAnimation(2, false, true);
                onAnimation(2, 2, null, animationTime, animationValueTotal);
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animation) {

            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animation) {
                //动画重复
            }
        });
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                //动画移动速度
                onAnimation(2, 3, animation, animationTime, animationValueTotal);
            }
        });

        animator.start();
    }
    //type:1 视频缩小动画  2：视频放大动画
    protected void onAnimation(int type, boolean isStart, boolean isEnd) {
        switch (type) {
            case 1:
                //视频缩小动画
                if (isStart) {
                    videoOperate2.setViewPageSlide(false);
                }
                break;
            case 2:
                //视频放大动画
                if (isEnd) {
                    viewTypeAnimation = 0;
                    videoOperate2.setViewPageSlide(true);
                }
                break;
        }
    }

    /**
     * @param type                1 视频缩小动画  2：视频放大动画
     * @param state               1 动画开始 2 动画结束 3 动画进行中
     * @param animation           3  进行中的值
     * @param animationTime       动画时间
     * @param animationValueTotal 动画总值
     */

    protected void onAnimation(int type, int state, ValueAnimator animation, int animationTime, int animationValueTotal) {

    }
    //============================动画结束==========================================
}
