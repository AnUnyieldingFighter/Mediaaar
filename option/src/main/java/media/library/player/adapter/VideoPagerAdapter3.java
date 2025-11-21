package media.library.player.adapter;



import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import media.library.player.frg2.VideoBaseFrg0;

public class VideoPagerAdapter3 extends FragmentStateAdapter {
    private ArrayList<VideoBaseFrg0> frgs = new ArrayList<>();
    //数据数量
    private int dataSize;
    //true 无限翻页
    private boolean isInfinite;

    public VideoPagerAdapter3(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);

    }

    public VideoPagerAdapter3(@NonNull FragmentActivity fragmentActivity, int dataSize) {
        super(fragmentActivity);
        this.dataSize = dataSize;
    }

    public VideoPagerAdapter3(@NonNull Fragment fragment) {
        super(fragment);

    }

    public VideoPagerAdapter3(@NonNull Fragment fragment, int dataSize) {
        super(fragment);
        this.dataSize = dataSize;

    }

    public void setDataSize(int dataSize) {
        if (this.dataSize == dataSize) {
            return;
        }
        setDataCount(dataSize, false);
    }

    //设置循环
    public void setDataLoop() {
        if (isInfinite) {
            return;
        }
        setDataCount(dataSize, true);
    }

    public void setDataCount(int dataSize, boolean isInfinite) {
        if (this.dataSize == dataSize && this.isInfinite == isInfinite) {
            return;
        }
        this.dataSize = dataSize;
        this.isInfinite = isInfinite;
        notifyDataSetChanged();
    }

    public void setFrgs(ArrayList<VideoBaseFrg0> frgs) {
        this.frgs = frgs;
    }

    public void setData(ArrayList<VideoBaseFrg0> frgs) {
        this.frgs = frgs;
        notifyDataSetChanged();
    }

    public void updateItem(int position) {
        notifyItemChanged(position);
    }


    private VideoBaseFrg0 getFrg(int position) {
        int index = position % frgs.size();
        VideoBaseFrg0 frg = frgs.get(index);
        return frg;
    }

    public VideoBaseFrg0 getIndexAtFrg(int index) {
        VideoBaseFrg0 frg = getFrg(index);
        return frg;
    }

    public int getFrgSize() {
        return frgs.size();
    }

    public int getDataSize() {
        return dataSize;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        VideoBaseFrg0 frg = getFrg(position);
        return frg;
    }

    @Override
    public long getItemId(int position) {
        VideoBaseFrg0 frg = getFrg(position);
        return frg.hashCode();
    }

    @Override
    public boolean containsItem(long itemId) {
        for (int i = 0; i < frgs.size(); i++) {
            VideoBaseFrg0 frg = frgs.get(i);
            if (frg.hashCode() == itemId) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getItemCount() {
        if (frgs == null || frgs.size() == 0) {
            return 0;
        }
        if (isInfinite) {
            return Integer.MAX_VALUE;
        }
        if (dataSize < 0) {
            dataSize = 0;
        }
        return dataSize;
    }

    //true 页面无限
    public boolean isInfinite() {
        return isInfinite;
    }
}
