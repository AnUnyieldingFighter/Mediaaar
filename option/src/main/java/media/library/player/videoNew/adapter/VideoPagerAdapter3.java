package media.library.player.videoNew.adapter;


import java.util.ArrayList;
import java.util.Collections;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import media.library.player.videoNew.frg2.VideoBaseFrg0;

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
        this.dataSize = frgs.size();
    }

    public void setData(ArrayList<VideoBaseFrg0> frgs) {
        this.frgs = frgs;
        this.dataSize = frgs.size();
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

    /**
     * 删除当前页，并按循环顺序前移后续页面，末位补入新页面。
     */
    public void removeFrg(int position, VideoBaseFrg0 frg) {
        int index = position % frgs.size();
        for (int i = 0; i < frgs.size() - 1; i++) {
            int slot = (index + i) % frgs.size();
            frgs.set(slot, frgs.get((slot + 1) % frgs.size()));
        }
        frgs.set((index + frgs.size() - 1) % frgs.size(), frg);
        this.dataSize -= 1;
        notifyDataSetChanged();
    }

    public void removeFrg(Fragment frgDel, VideoBaseFrg0 frg) {
        frgs.remove(frgDel);
        if (frg != null) {
            frgs.add(frg);
        }
        this.dataSize -= 1;
        notifyDataSetChanged();
    }

    /**
     * 删除非当前页的数据；删除前面的数据时，将当前 Fragment 对应的槽位前移一位。
     * 调用方对齐当前页码后，再调用 notifyDataSetChanged 刷新。
     */
    public void removeData(int position, int currentPosition) {
        if (position < currentPosition) {
            Collections.rotate(frgs, -1);
        }
        this.dataSize -= 1;
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

    // 可选：让 Adapter 知道这个 Fragment 是哪个
    public VideoBaseFrg0 getFragmentAt(int pos) {
        return frgs.get(pos % frgs.size());
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
