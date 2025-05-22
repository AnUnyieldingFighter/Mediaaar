package com.images.ui.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.images.config.entity.MediaEntity;
import com.images.imageselect.R;
import com.images.unmix.ImageLog;

import java.util.ArrayList;

/**
 * 图片视频选择
 */
public class MediaOptAdapter extends RecyclerView.Adapter<MediaOptAdapter.ViewHolder> {
    private ArrayList<MediaEntity> datas = new ArrayList();
    private Context context;
    //选择媒体数量，选择视频数量 （0 无限制）
    private int mediaCount, videoCount;
    private OnMediaImgIbl imgLoading;
    private int itemImgWidth = 0;
    //选中的数据
    private ArrayList<MediaEntity> optData = new ArrayList<>();
    //记录回显的 mediaPathSource
    private ArrayList<String> optPaths = new ArrayList<>();
    //true:设置数据时，清除已选中的数据
    private boolean isOnly;
    //true 图片（包括gif）与 视频互斥  即2选1
    private boolean isPart;

    public void setItemImgWidth(int itemImgWidth) {
        this.itemImgWidth = itemImgWidth;
        if (datas.size() > 0) {
            notifyDataSetChanged();
        }
    }

    public void setMediaOptMax(int mediaCount) {
        this.mediaCount = mediaCount;
    }

    public void setVideoOptMax(int videoCount) {
        this.videoCount = videoCount;
    }

    //设置互斥
    public void setPart(boolean isPart) {
        this.isPart = isPart;
    }

    public void setImgLoading(Context context, OnMediaImgIbl imgLoading) {
        this.imgLoading = imgLoading;
        this.context = context;
    }

    public void setDatas(ArrayList<MediaEntity> datas) {
        if (isOnly) {
            setOptDataReset();
        }
        this.datas = datas;
        notifyDataSetChanged();
    }

    //true  切换类型的时候 清除已选择的数据
    public void setOptDataOnly(boolean isOnly) {
        this.isOnly = isOnly;
    }

    //清除选中的数据
    public void setOptDataReset() {
        optData.clear();
        optPaths.clear();
    }

    public ArrayList<MediaEntity> getOptData() {
        return optData;
    }

    //设置回显数据 不能设置 isOnly=true
    public void setMediaOpt(ArrayList<MediaEntity> temp) {
        if (temp == null || temp.size() == 0) {
            return;
        }
        optData = temp;
        for (int i = 0; i < temp.size(); i++) {
            optPaths.add(temp.get(i).mediaPathSource);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.media_item_img, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        MediaEntity bean = datas.get(position);
        if ("-1".equals(bean.mediaType)) {
            holder.ivMedia.setScaleType(ImageView.ScaleType.CENTER);
            holder.ivMedia.setImageResource(R.mipmap.images_select_camera);
            holder.tvOpt.setVisibility(View.GONE);
            holder.tvType.setVisibility(View.GONE);
            holder.viewNoOpt.setVisibility(View.GONE);
        } else {
            setSelectState(bean);
            //设置选中
            boolean isOption = bean.isOption;
            if (isOption) {
                holder.tvOpt.setBackgroundResource(R.drawable.media_select_true);
                var index = optData.indexOf(bean);
                if (index >= 0) {
                    holder.tvOpt.setText("" + (index + 1));
                } else {
                    holder.tvOpt.setText("");
                }

            } else {
                holder.tvOpt.setBackgroundResource(R.drawable.media_select_false);
                holder.tvOpt.setText("");
            }
            holder.tvOpt.setVisibility(View.VISIBLE);
            holder.tvOpt.setOnClickListener(new Click(position));
            //设置图片显示样式
            if ((bean.width > 0 && bean.width <= itemImgWidth) && (bean.height > 0 && bean.height <= itemImgWidth)) {
                holder.ivMedia.setScaleType(ImageView.ScaleType.CENTER);
            } else {
                holder.ivMedia.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
            //加载图片
            imgLoading.onImageLoading(context, bean, holder.ivMedia);
            //设置标签
            String mediaType = getTag(bean);
            if (TextUtils.isEmpty(mediaType)) {
                holder.tvType.setVisibility(View.GONE);
            } else {
                holder.tvType.setText(mediaType.toUpperCase());
                holder.tvType.setVisibility(View.VISIBLE);
            }
            //设置互斥
            if (isPart && optData.size() > 0) {
                int tempType = optData.get(0).type;
                if (tempType == bean.type) {
                    holder.viewNoOpt.setVisibility(View.GONE);
                } else {
                    holder.viewNoOpt.setVisibility(View.VISIBLE);
                }
            } else {
                holder.viewNoOpt.setVisibility(View.GONE);
            }
        }
        //设置图的点击事件
        holder.ivMedia.setOnClickListener(new Click(position));
    }

    //回显时要 重新赋值选中状态
    private void setSelectState(MediaEntity bean) {
        if (optPaths == null || optPaths.size() == 0) {
            return;
        }
        int index = optPaths.indexOf(bean.mediaPathSource);
        if (index >= 0) {
            bean.isOption = true;
            optPaths.remove(index);
            for (int i = 0; i < optData.size(); i++) {
                MediaEntity temp = optData.get(i);
                if (bean.mediaPathSource.equals(temp.mediaPathSource)) {
                    bean.url = temp.url;
                    optData.set(i, bean);
                    return;
                }
            }

        }

    }

    //获取标签
    private String getTag(MediaEntity bean) {
        String mediaType = bean.mediaType;
        if (mediaType == null) {
            mediaType = "";
        }
        mediaType = mediaType.replace("image/", "");
        mediaType = mediaType.replace("video/", "");
        mediaType = mediaType.replace("png", "");
        mediaType = mediaType.replace("jpg", "");
        mediaType = mediaType.replace("webp", "");
        return mediaType;
    }

    @Override
    public int getItemCount() {
        return datas.size(); // 返回数据列表的大小
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivMedia;
        private TextView tvOpt, tvType;

        //不允许选择视图
        private View viewNoOpt;

        public ViewHolder(View itemView) {
            super(itemView);
            ivMedia = itemView.findViewById(R.id.iv_media);
            tvOpt = itemView.findViewById(R.id.tv_opt);
            tvType = itemView.findViewById(R.id.tv_type);
            viewNoOpt = itemView.findViewById(R.id.view_no_opt);

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
            MediaEntity image = datas.get(index);
            if (id == R.id.tv_opt) {
                if (isPart && optData.size() > 0) {
                    int tempType = optData.get(0).type;
                    if (tempType != image.type) {
                        ImageLog.d("互斥");
                        if (imgLoading != null) {
                            imgLoading.onImageSelect(image, -2);
                        }
                        return;
                    }
                }
                //选择了图片
                if (!image.isOption && image.type == 1 && optData.size() == mediaCount) {
                    ImageLog.d("选择图片 已达上限");
                    if (imgLoading != null) {
                        imgLoading.onImageSelect(image, -1);
                    }
                    return;
                }
                //选择了视频
                if (!image.isOption && image.type == 2 && optData.size() == videoCount && videoCount > 0) {
                    ImageLog.d("选择视频 已达上限");
                    if (imgLoading != null) {
                        imgLoading.onImageSelect(image, -1);
                    }
                    return;
                }
                //选择了视频 但是视频数量设置为0 与是 videoCount=mediaCount
                if (!image.isOption && image.type == 2 && optData.size() == mediaCount && videoCount == 0) {
                    ImageLog.d("已达上限");
                    if (imgLoading != null) {
                        imgLoading.onImageSelect(image, -1);
                    }
                    return;
                }
                if (!image.isOption) {
                    //添加一个
                    image.isOption = true;
                    optData.add(image);
                    notifyDataSetChanged();
                    //notifyItemChanged(index);
                } else {
                    //删除一个
                    image.isOption = false;
                    optData.remove(image);
                    notifyDataSetChanged();
                }
                if (imgLoading != null) {
                    imgLoading.onImageSelect(image, 0);
                }

            }
            if (id == R.id.iv_media) {
                if ("-1".equals(image.mediaType)) {
                    //拍照
                    return;
                }
                //查看大图
            }
        }

    }
}
