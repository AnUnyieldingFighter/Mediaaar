package media.library.images.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.widget.ViewPager2;

import media.library.images.config.entity.MediaEntity;
import com.images.imageselect.R;
import media.library.images.ui.adapter.MediaPagerAdapter2;
import media.library.images.ui.adapter.OnImgClickListener;
import media.library.images.ui.adapter.OnMediaImgIbl;
import media.library.images.ui.frag.ImgFragment;
import media.library.images.ui.frag.MediaFragment;
import media.library.images.ui.frag.VideoFragment;

import java.util.ArrayList;

//图片预览
public class MediaPreviewLayout extends RelativeLayout {

    public MediaPreviewLayout(Context context) {
        super(context);
        initView();
    }

    public MediaPreviewLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public MediaPreviewLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public MediaPreviewLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    protected ViewPager2 viewPager;

    protected void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.media_layout_preview, this);
        viewPager = findViewById(R.id.view_pager);
    }


    private MediaPagerAdapter2 adapter;

    public void setMedias(FragmentActivity act, ArrayList<MediaEntity> medias) {
        setMedias(act, medias, 0);
    }

    /**
     * @param act
     * @param medias 数据
     * @param index  定位
     */

    public void setMedias(FragmentActivity act, ArrayList<MediaEntity> medias, int index) {
        if (adapter == null) {
            adapter = new MediaPagerAdapter2(act);
        }
        ArrayList<MediaFragment> pages = getFrag(medias);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(pages.size());
        viewPager.registerOnPageChangeCallback(new OnPagerChange());
        adapter.setData(pages);
        if (index != 0) {
            viewPager.setCurrentItem(index, false);
        }
    }

    //加载图片或者视频 page
    protected ArrayList<MediaFragment> getFrag(ArrayList<MediaEntity> medias) {
        ArrayList<MediaFragment> pages = new ArrayList<>();
        for (int i = 0; i < medias.size(); i++) {
            MediaEntity bean = medias.get(i);
            if (bean.type == 1) {
                ImgFragment frg = ImgFragment.newInstance(bean);
                //设置图片点击事件  和 加载回调
                frg.setOnImgClickListener(previewClickList, imgLoading);
                pages.add(frg);
            }
            if (bean.type == 2) {
                VideoFragment frg = VideoFragment.newInstance(bean);
                //设置加载回调
                frg.setOnImgClickListener(imgLoading);
                pages.add(frg);
            }
        }
        return pages;
    }

    //viewPager 切换回调
    protected void onPageOpt(int position) {
    }

    //图片点击事件回调
    protected void OnPhotoTapListener(View view, MediaEntity mediaEntity, float v, float v1) {
    }

    private OnMediaImgIbl imgLoading;

    //设置图片加载回调
    public void setImageLoading(OnMediaImgIbl imgLoading) {
        this.imgLoading = imgLoading;
    }

    private PreviewClickList previewClickList = new PreviewClickList();

    //图片的点击事件 回调
    class PreviewClickList implements OnImgClickListener {


        @Override
        public void OnImgTapClick(View view, MediaEntity mediaEntity, float v, float v1) {
            MediaPreviewLayout.this.OnPhotoTapListener(view, mediaEntity, v, v1);
        }
    }

    class OnPagerChange extends ViewPager2.OnPageChangeCallback {
        @Override
        public void onPageSelected(int position) {
            onPageOpt(position);
        }


    }
}
