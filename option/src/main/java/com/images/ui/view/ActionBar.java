package com.images.ui.view;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.images.config.Configs;
import com.images.config.operation.ConfigBar;
import com.images.config.operation.ConfigBarCommon;
import com.images.config.operation.ConfigBarCrop;
import com.images.config.operation.ConfigBarPreview;
import com.images.imageselect.R;

/**
 * Created by Administrator on 2016/10/19.
 */
public class ActionBar extends RelativeLayout {

    private TextView barTitleTv;
    private TextView barBackTv;
    private TextView barOptionTv;
    private View.OnClickListener clickListener;
    public SystemBarTintManager tintManager;

    public ActionBar(Context context) {
        super(context);
        init(context);
    }

    public ActionBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ActionBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    private void init(Context context) {

        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, dm);
        //
        RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        lp1.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        barBackTv = new TextView(context);
        barBackTv.setPadding(padding, 0, 0, 0);
        barBackTv.setGravity(Gravity.CENTER);
        barBackTv.setTextSize(15);
        barBackTv.setCompoundDrawablePadding(10);
        barBackTv.setId(R.id.action_back);
        barBackTv.setLayoutParams(lp1);
        addView(barBackTv);
        //
        RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp2.addRule(RelativeLayout.CENTER_IN_PARENT);
        barTitleTv = new TextView(context);
        barTitleTv.setTextSize(18);
        barTitleTv.setId(R.id.action_title);
        barTitleTv.setLayoutParams(lp2);
        addView(barTitleTv);
        //
        int optionHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 26, dm);
        RelativeLayout.LayoutParams lp3 = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, optionHeight);
        lp3.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        lp3.addRule(RelativeLayout.CENTER_VERTICAL);

        lp3.rightMargin = padding;
        barOptionTv = new TextView(context);
        barOptionTv.setPadding(20, 5, 20, 5);
        barOptionTv.setGravity(Gravity.CENTER);
        barOptionTv.setTextSize(14);
        barOptionTv.setId(R.id.action_option);
        barOptionTv.setLayoutParams(lp3);
        addView(barOptionTv);
    }

    //设置右侧显示
    public void setTitle(String txt) {
        barTitleTv.setText(txt);
    }

    //设置右侧显示
    public void setOptionText(String txt) {
        barOptionTv.setText(txt);
    }


    public void hideView(int index) {
        TextView tv = null;
        switch (index) {
            case 1:
                tv = barBackTv;
                break;
            case 2:
                tv = barTitleTv;
                break;
            case 3:
                tv = barOptionTv;
                break;
        }
        if (tv == null) {
            return;
        }
        tv.setVisibility(View.GONE);
    }

    public void setConfigsCommon(Activity activity, Configs config, View.OnClickListener clickListener) {
        this.clickListener = clickListener;
        ConfigBar configBuileBar = config.configBar;
        setBarHeight(configBuileBar.getActionBarHeight(), configBuileBar.getActionBarColor());
        setWindowStatusBarColor(activity, configBuileBar.getStatusBarColor());
        ConfigBarCommon common = config.configBarCommon;
        //
        setTitleTv(common.getTitleSize(), common.getTitleColor(), common.getTitle());
        setOptionTv(common.getOptionIconbg(), common.getOptionSize(),
                common.getOptionColor(), common.getOption());
        setBack(common.getBackIconbg(), common.getBackSize(),
                common.getBackColor(), common.getBack());
    }

    //裁剪的activity用
    public void setConfigsCrop(Activity activity, Configs config, View.OnClickListener clickListener) {
        this.clickListener = clickListener;
        ConfigBar configBuileBar = config.configBar;
        setBarHeight(configBuileBar.getActionBarHeight(), configBuileBar.getActionBarColor());
        setWindowStatusBarColor(activity, configBuileBar.getStatusBarColor());
        //
        ConfigBarCrop crop = config.configBarCrop;
        setTitleTv(crop.getTitleSize(), crop.getTitleColor(), crop.getTitle());
        setOptionTv(crop.getOptionIconbg(), crop.getOptionSize(),
                crop.getOptionColor(), crop.getOption());
        setBack(crop.getBackIconbg(), crop.getBackSize(),
                crop.getBackColor(), crop.getBack());
    }

    //预览的选择activity用
    public void setConfigsPreview(Activity activity, Configs config, View.OnClickListener clickListener) {
        this.clickListener = clickListener;
        ConfigBar configBuileBar = config.configBar;
        setBarHeight(configBuileBar.getActionBarHeight(), configBuileBar.getActionBarColor());
        setWindowStatusBarColor(activity, configBuileBar.getStatusBarColor());
        //
        ConfigBarPreview preview = config.configPreview;
        setTitleTv(preview.getTitleSize(), preview.getTitleColor(), preview.getTitle());
        setOptionTv(preview.getOptionIconbg(), preview.getOptionSize(),
                preview.getOptionColor(), preview.getOption());
        setBack(preview.getBackIconbg(), preview.getBackSize(),
                preview.getBackColor(), preview.getBack());
    }

    //只预览的activity用
    public void setConfigsPreviewOnly(Activity activity, Configs config, View.OnClickListener clickListener) {
        this.clickListener = clickListener;
        ConfigBar configBuileBar = config.configBar;
        setBarHeight(configBuileBar.getActionBarHeight(), configBuileBar.getActionBarColor());
        setWindowStatusBarColor(activity, configBuileBar.getStatusBarColor());
        //
        ConfigBarPreview preview = config.configPreview;
        setTitleTv(preview.getTitleSize(), preview.getTitleColor(), preview.getTitle());
        setBack(preview.getBackIconbg(), preview.getBackSize(),
                preview.getBackColor(), preview.getBack());
    }

    private void setTitleTv(int size, int color, String title) {
        if (!TextUtils.isEmpty(title)) {
            barTitleTv.setText(title);
        }
        if (size != 0) {
            barTitleTv.setTextSize(size);
        }
        if (color != 0) {
            barTitleTv.setTextColor(color);
        }

    }

    private void setOptionTv(int optionBackdropId, int size, int color, String option) {
        if (!TextUtils.isEmpty(option)) {
            barOptionTv.setText(option);
            barOptionTv.setOnClickListener(clickListener);
        }
        if (size != 0) {
            barOptionTv.setTextSize(size);
        }
        if (color != 0) {
            barOptionTv.setTextColor(color);
        }
        if (optionBackdropId != 0) {
            barOptionTv.setBackgroundResource(optionBackdropId);
        }
    }

    private void setBack(int backdropId, int size, int color, String back) {
        if (!TextUtils.isEmpty(back)) {
            barBackTv.setText(back);
            barBackTv.setOnClickListener(clickListener);
        }
        if (size != 0) {
            barBackTv.setTextSize(size);
        }
        if (color != 0) {
            barBackTv.setTextColor(color);
        }
        if (backdropId != 0) {
            setText(barBackTv, backdropId, "", 0);
        }
    }

    //设置action的高度
    private void setBarHeight(int height, int color) {
        if (color != 0) {
            setBackgroundColor(color);
        }
        if (height < 80) {
            return;
        }
        setMinimumHeight(height);
        ViewGroup.LayoutParams lp = getLayoutParams();
        lp.height = height;
        setLayoutParams(lp);
    }

    public void setBarColor(boolean is) {
        if (tintManager == null) {
            return;
        }
        if (is) {
            tintManager.setStatusBarTintColor(0x00000000);
            return;
        }
        if (barcolor == 0) {
            return;
        }
        tintManager.setStatusBarTintColor(barcolor);
    }

    private int barcolor;

    //设置状态栏
    private void setWindowStatusBarColor(Activity activity, int color) {
        if (color == 0) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            ViewGroup.LayoutParams params = getLayoutParams();
            if (params instanceof LinearLayout.LayoutParams) {
                LinearLayout.LayoutParams paramsL = (LinearLayout.LayoutParams) params;
                paramsL.topMargin = getStatusHeight(activity);
            }
            if (params instanceof RelativeLayout.LayoutParams) {
                RelativeLayout.LayoutParams paramsR = (RelativeLayout.LayoutParams) params;
                paramsR.topMargin = getStatusHeight(activity);
            }
            this.setLayoutParams(params);
        }
        this.barcolor = color;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(activity, true);
        }
        tintManager = new SystemBarTintManager(activity);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarTintColor(color);  //设置上方状态栏的颜色

    }


    @TargetApi(19)
    private void setTranslucentStatus(Activity activity, boolean on) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    /**
     * 获得状态栏的高度
     */
    public int getStatusHeight(Context context) {
        int statusHeight = -1;
        try {
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField("status_bar_height").get(object).toString());
            statusHeight = context.getResources().getDimensionPixelSize(height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusHeight;
    }

    /**
     * @param tv
     * @param iconId
     * @param text
     * @param iconLocation 位置
     */
    public void setText(TextView tv, int iconId, String text, int iconLocation) {
        if (iconId != 0) {
            Drawable drawable = getContext().getResources().getDrawable(iconId);
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            switch (iconLocation) {
                case 0:
                    tv.setCompoundDrawables(drawable, null, null, null);
                    break;
                case 1:
                    tv.setCompoundDrawables(null, drawable, null, null);
                    break;
                case 2:
                    tv.setCompoundDrawables(null, null, drawable, null);
                    break;
                case 3:
                    tv.setCompoundDrawables(null, null, null, drawable);
                    break;
            }
        }
        //tv.setGravity(Gravity.CENTER_VERTICAL);
        if (TextUtils.isEmpty(text)) {
            return;
        }
        tv.setText(text);
    }
}
