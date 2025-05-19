package com.images.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.images.config.ConfigBuild;
import com.images.config.entity.ImageEntity;
import com.images.imageselect.R;
import com.images.unmix.ImageLog;

import java.util.ArrayList;

public class MediaOptAdapter extends RecyclerView.Adapter<MediaOptAdapter.ViewHolder> {
    private ArrayList<ImageEntity> datas = new ArrayList();
    private Context context;
    private int max;
    private int optSize = 0;
    private OnMediaImgIbl imgLoading;
    private int itemImgWidth = 0;

    public void setItemImgWidth(int itemImgWidth) {
        this.itemImgWidth = itemImgWidth;
        if (datas.size() > 0) {
            notifyDataSetChanged();
        }
    }

    public void setMediaOptMax(Context context, int max, OnMediaImgIbl imgLoading) {
        this.context = context;
        this.max = max;
        this.imgLoading = imgLoading;
    }

    public void setDatas(ArrayList<ImageEntity> datas) {
        this.datas = datas;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.media_item_img, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ImageEntity bean = datas.get(position);
        if ("-1".equals(bean.imageType)) {
            holder.ivMedia.setImageResource(R.mipmap.images_select_camera);
            holder.tvOpt.setVisibility(View.GONE);
        } else {
            boolean isOption = bean.isOption;
            if (isOption) {
                holder.tvOpt.setBackgroundResource(R.drawable.media_select_true);
                holder.tvOpt.setText("" + bean.optNumber);
            } else {
                holder.tvOpt.setBackgroundResource(R.drawable.media_select_false);
                holder.tvOpt.setText("");
            }
            holder.tvOpt.setVisibility(View.VISIBLE);
            imgLoading.onImageLoading(context, bean.imagePathSource, holder.ivMedia);
            holder.tvOpt.setOnClickListener(new Click(position));
        }
        holder.ivMedia.setOnClickListener(new Click(position));
    }

    @Override
    public int getItemCount() {
        return datas.size(); // 返回数据列表的大小
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView ivMedia;
        public TextView tvOpt;

        public ViewHolder(View itemView) {
            super(itemView);
            ivMedia = itemView.findViewById(R.id.iv_media);
            tvOpt = itemView.findViewById(R.id.tv_opt);
            if (itemImgWidth > 0 && itemImgWidth != itemView.getWidth()) {
                ViewGroup.LayoutParams lp = ivMedia.getLayoutParams();
                lp.width = itemImgWidth;
                lp.height = itemImgWidth;
                ivMedia.setLayoutParams(lp);
            }
        }
    }

    //选择图片监听
    class Click implements View.OnClickListener {
        private int index;

        public Click(int index) {
            this.index = index;
        }

        @Override
        public void onClick(View v) {
            int id = v.getId();
            ImageEntity image = datas.get(index);
            if (id == R.id.tv_opt) {
                //选择了图片
                if (!image.isOption && optSize == max) {
                    ImageLog.e("已达上限");
                    if (imgLoading != null) {
                        imgLoading.onImageSelect(null, -1);
                    }
                    return;
                }
                if (!image.isOption) {
                    optSize += 1;
                    image.isOption = true;
                    image.optNumber = optSize;
                } else {
                    optSize -= 1;
                    image.isOption = false;
                    image.optNumber = 0;
                }
                if (imgLoading != null) {
                    imgLoading.onImageSelect(image, 0);
                }
                notifyItemChanged(index);
            }
            if (id == R.id.iv_media) {
                if ("1".equals(image.imageType)) {
                    //拍照
                    return;
                }
                //查看大图
            }
        }

    }
}
