package com.images.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.images.ui.bean.ImageFile;
import com.images.config.ConfigBuild;
import com.images.imageselect.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/10/17.
 */
public class PopupAdapter extends BaseAdapter {
    //选中的文件夹
    private int fileSelectIndex;
    private List<ImageFile> files = new ArrayList<ImageFile>();

    public PopupAdapter(   ) {
     }

    public void setData(List<ImageFile> files) {
        this.files = files;
        notifyDataSetChanged();
    }

    public void setIndex(int fileSelectIndex) {
        this.fileSelectIndex = fileSelectIndex;
    }

    @Override
    public int getCount() {
        return files.size();
    }

    @Override
    public Object getItem(int i) {
        return files.get(i);
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
                    R.layout.item_file_image, null);
            holdeView.fileIv = (ImageView) view.findViewById(R.id.item_file_iv);
            holdeView.fileName = (TextView) view.findViewById(R.id.file_name_tv);
            holdeView.fileSize = (TextView) view.findViewById(R.id.file_size_tv);
            holdeView.fileSelectIv = (ImageView) view.findViewById(R.id.item_file_select_iv);
            view.setTag(holdeView);
        } else {
            holdeView = (HoldeView) view.getTag();
        }
        ImageFile file = files.get(i);

        holdeView.fileName.setText(file.fileName);
        holdeView.fileSize.setText((file.size) + "");
        int isShow = i == fileSelectIndex ? View.VISIBLE : View.GONE;
        holdeView.fileSelectIv.setVisibility(isShow);

        ConfigBuild.getBuild().setImageLoading(viewGroup.getContext(), file.firstImagePath, holdeView.fileIv);

        return view;
    }

    class HoldeView {
        public ImageView fileIv, fileSelectIv;
        public TextView fileName, fileSize;
    }
}
