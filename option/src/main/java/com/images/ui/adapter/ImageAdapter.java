package com.images.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.images.config.ConfigBuild;
import com.images.config.entity.ImageEntity;
import com.images.imageselect.R;
import com.images.ui.view.ActionBar;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Administrator on 2016/10/17.
 */
public class ImageAdapter extends BaseAdapter {
    //所有的图片
    private List<ImageEntity> images = new ArrayList<>();
    private AbsListView.LayoutParams itemLayoutParams;
    //已经选择的图片
    private ArrayList<ImageEntity> optionImage = new ArrayList<>();
    //选中的paths
    private ArrayList<String> optionPaths = new ArrayList<>();
    //选中的不可以删除的paths（从外部传来，固定不动的）
    private ArrayList<String> optionFixationPaths = new ArrayList<>();
    private Context contex;
    private boolean isCrop, isCamera;
    private int max;
    private int imgFileIndex;

    public ImageAdapter(Context contex, int max, boolean isCrop, boolean isCamera) {
        this.contex = contex;
        this.max = max;
        this.isCamera = isCamera;
        this.isCrop = isCrop;
    }

    public void setImgFileIndex(int index) {
        imgFileIndex = index;
    }

    public int getImgFileIndex() {
        return imgFileIndex;
    }

    //显示预览数量
    private TextView previewTv;

    public void setPreviewView(TextView previewTv) {
        this.previewTv = previewTv;
    }

    //显示发送数量
    private ActionBar actionBar;
    private String optionHint;

    public void setSendView(ActionBar actionBar, String optionHint) {
        this.actionBar = actionBar;
        this.optionHint = optionHint;
    }

    //设置照片的大小
    public void setItemSize(Activity activity) {
        DisplayMetrics dm = activity.getResources().getDisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;     // 屏幕宽度（像素）
        int height = dm.heightPixels;   // 屏幕高度（像素）
        float unW = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, dm);
        int w = (int) (width - unW) / 3;
        itemLayoutParams = new GridView.LayoutParams(w, w);
    }

    public void setData(List<ImageEntity> images) {
        this.images = images;
        notifyDataSetChanged();
    }

    public List<ImageEntity> getImages() {
        return images;
    }


    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public Object getItem(int i) {
        if (i < 0 || i >= (images.size())) {
            return null;
        }
        return images.get(i);
    }

    //设置选中的图片
    public void setImages(ArrayList<ImageEntity> optionImage) {
        optionPaths = new ArrayList<>();
        for (int i = 0; i < optionImage.size(); i++) {
            ImageEntity image = optionImage.get(i);
            optionPaths.add(image.imagePathSource);
            if (image.isDelete) {
                continue;
            }
            optionFixationPaths.add(image.imagePathSource);
        }
        this.optionImage = optionImage;
        setShowHint();
        notifyDataSetChanged();
    }

    public void setOptionImage(ArrayList<ImageEntity> optionImage) {
        this.optionImage.clear();
        this.optionPaths.clear();
        if (optionImage == null) {
            notifyDataSetChanged();
            return;
        }
        for (int i = 0; i < optionImage.size(); i++) {
            ImageEntity image = optionImage.get(i);
            if (image.isOption) {
                this.optionImage.add(image);
                this.optionPaths.add(image.imagePathSource);
            }
        }
        setShowHint();
        notifyDataSetChanged();
    }

    private void addORremovePath(int index) {
        ImageEntity image = images.get(index);
        if (optionFixationPaths.contains(image.imagePathSource)) {
            //图片不能删除
            ConfigBuild.getBuild().interdictMsg(contex, image);
            return;
        }
        String path = image.imagePathSource;
        boolean isExist = false;
        int optionIndex = 0;
        for (int i = 0; i < optionImage.size(); i++) {
            isExist = path.equals(optionImage.get(i).imagePathSource);
            optionIndex = i;
            if (isExist) {
                break;
            }
        }
        if (isExist) {
            optionImage.remove(optionIndex);
            optionPaths.remove(path);
        }
        if (!isExist && optionImage.size() >= max) {
            Toast.makeText(contex, R.string.image_select_max_error, Toast.LENGTH_LONG).show();
            return;
        }
        if (!isExist) {
            optionImage.add(image);
            optionPaths.add(path);
        }
        setShowHint();
        notifyDataSetChanged();
    }

    private void setShowHint() {
        //显示预览数量
        int size = optionImage.size();
        String hint = size > 0 ? "预览(" + size + ")" : "";
        previewTv.setText(hint);
        //设置已选择数
        String sendhint = TextUtils.isEmpty(optionHint) ? "" : optionHint;
        sendhint += " ( " + optionImage.size() + "/" + max + " ) ";
        actionBar.setOptionText(sendhint);
    }

    public ArrayList<ImageEntity> getOptionImage() {
        for (int i = 0; i < optionImage.size(); i++) {
            optionImage.get(i).isOption = true;
        }
        return optionImage;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        HoldeView holdeView;
        if (view == null) {
            holdeView = new HoldeView();
            view = LayoutInflater.from(viewGroup.getContext()).inflate(
                    R.layout.item_image, null);
            holdeView.imageIv = (ImageView) view.findViewById(R.id.item_image_iv);
            holdeView.imageTv = (TextView) view.findViewById(R.id.item_image_tv);

            holdeView.selectIv = (ImageView) view.findViewById(R.id.item_image_select_iv);
            view.setLayoutParams(itemLayoutParams);
            view.setTag(holdeView);
        } else {
            holdeView = (HoldeView) view.getTag();
        }
        ImageEntity bean = images.get(i);
        if (i == 0 && isCamera) {
            holdeView.imageTv.setVisibility(View.VISIBLE);
            holdeView.imageIv.setVisibility(View.GONE);
            holdeView.imageIv.setImageResource(R.mipmap.images_select_camera);
        } else {
            holdeView.imageTv.setVisibility(View.GONE);
            holdeView.imageIv.setVisibility(View.VISIBLE);
            ConfigBuild.getBuild().setImageLoading(viewGroup.getContext(),
                    bean.imagePathSource, holdeView.imageIv);
        }
        if (isCrop) {
            holdeView.selectIv.setVisibility(View.GONE);
            return view;
        }
        if (i == 0 && isCamera) {
            holdeView.selectIv.setVisibility(View.GONE);
        } else {
            int imageId = optionPaths.contains(bean.imagePathSource) ? R.mipmap.image_select_true :
                    R.mipmap.image_select_false;
            holdeView.selectIv.setImageResource(imageId);
            holdeView.selectIv.setVisibility(View.VISIBLE);
        }
        holdeView.selectIv.setOnClickListener(new Click(i));
        return view;
    }

    class HoldeView {
        public ImageView imageIv, selectIv;
        public TextView imageTv;
    }

    //选择图片监听
    class Click implements View.OnClickListener {
        private int index;

        public Click(int index) {
            this.index = index;
        }

        @Override
        public void onClick(View v) {
            //选择了图片
            addORremovePath(index);
        }
    }
}
