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
    //数据数量，包含业务列表中的广告占位。
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
        setDataCount(dataSize, isInfinite);
    }

    //设置循环
    public void setDataLoop() {
        if (isInfinite) {
            return;
        }
        setDataCount(dataSize, true);
    }

    public void setDataCount(int dataSize, boolean isInfinite) {
        dataSize = Math.max(0, dataSize);
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
        //普通视频和广告统一按页面索引复用七个 Fragment。
        int index = position % frgs.size();
        VideoBaseFrg0 frg = frgs.get(index);
        return frg;
    }

    public VideoBaseFrg0 getIndexAtFrg(int index) {
        VideoBaseFrg0 frg = getFrg(index);
        return frg;
    }

    /**
     * 删除当前视频页，并按循环顺序前移后续视频页面，末位补入新页面。
     * 调用方对齐当前页码后，再通知刷新。
     */
    public void removeFrg(int position, VideoBaseFrg0 frg) {
        int index = position % frgs.size();
        for (int i = 0; i < frgs.size() - 1; i++) {
            int slot = (index + i) % frgs.size();
            frgs.set(slot, frgs.get((slot + 1) % frgs.size()));
        }
        frgs.set((index + frgs.size() - 1) % frgs.size(), frg);
        this.dataSize -= 1;
    }

    public void removeFrg(Fragment frgDel, VideoBaseFrg0 frg) {
        int index = frgs.indexOf(frgDel);
        if (index < 0 || frg == null) {
            return;
        }
        frgs.set(index, frg);
        this.dataSize -= 1;
        notifyDataSetChanged();
    }

    /**
     * 在当前页附近插入页面，按循环顺序后移已有页面，移出远端的一个页面。
     * 复用池数量不变；数据数量增加一条，包含业务层已插入的广告占位。
     * 返回移出的页面，由管理器释放其播放器；调用方对齐页码后再通知刷新。
     */
    public VideoBaseFrg0 addFrg(int position, VideoBaseFrg0 frg, int currentPosition) {
        int index = position % frgs.size();
        if (position < currentPosition - 1) {
            //插在已加载范围之前，当前页及相邻页整体后移一个槽位。
            Collections.rotate(frgs, 1);
            dataSize += 1;
            return frgs.set(index, frg);
        }
        //插在当前页附近时，从下一预加载页后面移出一个；更远的位置直接替换槽位。
        int lastIndex = (int) (Math.max((long) position, (long) currentPosition + 2) % frgs.size());
        VideoBaseFrg0 removed = frgs.get(lastIndex);
        while (lastIndex != index) {
            int previous = (lastIndex + frgs.size() - 1) % frgs.size();
            frgs.set(lastIndex, frgs.get(previous));
            lastIndex = previous;
        }
        frgs.set(index, frg);
        dataSize += 1;
        return removed;
    }

    /**
     * 判断对象是否已在复用池中，避免重复使用同一个 Fragment。
     */
    public boolean containsFrg(VideoBaseFrg0 frg) {
        return frgs.contains(frg);
    }

    /**
     * 获取包含广告占位的数据索引；无限翻页时循环取数据，无效位置返回 -1。
     */
    public int getVideoDataIndex(int position) {
        if (position < 0 || position >= getItemCount() || dataSize == 0) {
            return -1;
        }
        return isInfinite ? position % dataSize : position;
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

    /**
     * 无限翻页时删除列表中的一条数据（含广告占位），并对齐复用槽位。
     * replacement 非空表示当前视频也被删除，替换当前 Fragment；返回调整后的当前页码。
     */
    public int removeLoopVideo(int position, int currentPosition, VideoBaseFrg0 replacement) {
        int deletedIndex = getVideoDataIndex(position);
        int currentOrdinal = currentPosition;
        int removedBeforeCurrent = getRemovedVideoCount(currentOrdinal, deletedIndex);
        int targetOrdinal = currentOrdinal;
        if (replacement != null) {
            frgs.set(currentOrdinal % frgs.size(), replacement);
            //当前视频被删除，尽量保留下一条视频原有的 Fragment 和预加载结果。
            targetOrdinal += 1;
        }
        Collections.rotate(frgs, -getRemovedVideoCount(targetOrdinal, deletedIndex));
        dataSize -= 1;
        return currentPosition - removedBeforeCurrent;
    }

    /**
     * 计算指定视频序号之前，被删除的数据在各轮中共出现了多少次。
     */
    private int getRemovedVideoCount(int ordinal, int deletedIndex) {
        return ordinal / dataSize + (ordinal % dataSize > deletedIndex ? 1 : 0);
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
        return getFrg(pos);
    }

    public int getFragmentAt(Fragment frg) {
        return frgs.indexOf(frg);
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
        if (isInfinite && dataSize > 0) {
            return Integer.MAX_VALUE;
        }
        if (dataSize < 0) {
            dataSize = 0;
        }
        return getDataSize();
    }

    //true 页面无限
    public boolean isInfinite() {
        return isInfinite;
    }
}
