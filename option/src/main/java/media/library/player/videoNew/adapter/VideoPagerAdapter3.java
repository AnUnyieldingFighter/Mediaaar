package media.library.player.videoNew.adapter;


import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import media.library.player.videoNew.frg2.VideoBaseFrg0;

public class VideoPagerAdapter3 extends FragmentStateAdapter {
    private ArrayList<VideoBaseFrg0> frgs = new ArrayList<>();
    //独立插入的广告页，键为页面索引，不参与视频页面复用。
    private final TreeMap<Integer, VideoBaseFrg0> insertedFrgs = new TreeMap<>();
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
        if ((dataSize < this.dataSize || this.isInfinite && !isInfinite)
                && (!isInfinite || dataSize == 0)) {
            //视频减少时移除超出新列表末尾的广告；清空视频时同时清空广告。
            //从后往前删除，计算广告前的视频数量时不会受已删除广告影响。
            for (Integer position : new ArrayList<>(insertedFrgs.descendingKeySet())) {
                if (dataSize == 0 || position - insertedFrgs.headMap(position).size() > dataSize) {
                    insertedFrgs.remove(position);
                } else {
                    break;
                }
            }
        }
        this.dataSize = dataSize;
        this.isInfinite = isInfinite;
        notifyDataSetChanged();
    }

    public void setFrgs(ArrayList<VideoBaseFrg0> frgs) {
        insertedFrgs.clear();
        this.frgs = frgs;
        this.dataSize = frgs.size();
    }

    public void setData(ArrayList<VideoBaseFrg0> frgs) {
        insertedFrgs.clear();
        this.frgs = frgs;
        this.dataSize = frgs.size();
        notifyDataSetChanged();
    }

    public void updateItem(int position) {
        notifyItemChanged(position);
    }


    private VideoBaseFrg0 getFrg(int position) {
        VideoBaseFrg0 inserted = insertedFrgs.get(position);
        if (inserted != null) {
            return inserted;
        }
        //复用槽位按经过的视频页数量计算，无限翻页时也继续顺序复用七个页面。
        int index = (position - insertedFrgs.headMap(position).size()) % frgs.size();
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
        int index = (position - insertedFrgs.headMap(position).size()) % frgs.size();
        for (int i = 0; i < frgs.size() - 1; i++) {
            int slot = (index + i) % frgs.size();
            frgs.set(slot, frgs.get((slot + 1) % frgs.size()));
        }
        frgs.set((index + frgs.size() - 1) % frgs.size(), frg);
        this.dataSize -= 1;
        shiftInsertedFrgs(position + 1, -1);
    }

    public void removeFrg(Fragment frgDel, VideoBaseFrg0 frg) {
        for (Integer position : new ArrayList<>(insertedFrgs.keySet())) {
            if (insertedFrgs.get(position) == frgDel) {
                removeData(position, position);
                notifyDataSetChanged();
                return;
            }
        }
        frgs.remove(frgDel);
        if (frg != null) {
            frgs.add(frg);
        }
        this.dataSize -= 1;
        notifyDataSetChanged();
    }

    /**
     * 插入独立页面，不改变视频数据数量和原有七个视频页面的复用顺序。
     * 调用方对齐当前页码后，再通知刷新。
     */
    public void addFrg(int position, VideoBaseFrg0 frg) {
        shiftInsertedFrgs(position, 1);
        insertedFrgs.put(position, frg);
    }

    /**
     * 判断页面是否为独立插入的广告页。
     */
    public boolean isInsertedFrg(int position) {
        return insertedFrgs.containsKey(position);
    }

    /**
     * 判断对象是否已在视频复用池或独立页面中，避免重复使用同一个 Fragment。
     */
    public boolean containsFrg(VideoBaseFrg0 frg) {
        return frgs.contains(frg) || insertedFrgs.containsValue(frg);
    }

    /**
     * 将页面索引转换为原视频列表索引；无限翻页时循环取数据，广告页或无效位置返回 -1。
     */
    public int getVideoDataIndex(int position) {
        if (position < 0 || position >= getItemCount() || isInsertedFrg(position) || dataSize == 0) {
            return -1;
        }
        int index = position - insertedFrgs.headMap(position).size();
        return isInfinite ? index % dataSize : index;
    }

    /**
     * 插入或删除页面时，统一调整后面广告页的索引。
     */
    private void shiftInsertedFrgs(int fromPosition, int offset) {
        ArrayList<Integer> positions = new ArrayList<>(insertedFrgs.tailMap(fromPosition).keySet());
        if (offset > 0) {
            Collections.reverse(positions);
        }
        for (Integer position : positions) {
            VideoBaseFrg0 frg = insertedFrgs.remove(position);
            insertedFrgs.put(position + offset, frg);
        }
    }

    /**
     * 删除非当前页的数据；删除前面的数据时，将当前 Fragment 对应的槽位前移一位。
     * 调用方对齐当前页码后，再调用 notifyDataSetChanged 刷新。
     */
    public void removeData(int position, int currentPosition) {
        if (insertedFrgs.remove(position) == null) {
            if (position < currentPosition) {
                Collections.rotate(frgs, -1);
            }
            this.dataSize -= 1;
        }
        shiftInsertedFrgs(position + 1, -1);
    }

    /**
     * 无限翻页时删除原视频列表中的一条数据，移除它在各轮中的位置，并对齐广告和视频复用槽位。
     * replacement 非空表示当前视频也被删除，替换当前 Fragment；返回调整后的当前页码。
     */
    public int removeLoopVideo(int position, int currentPosition, VideoBaseFrg0 replacement) {
        int deletedIndex = getVideoDataIndex(position);
        int currentOrdinal = currentPosition - insertedFrgs.headMap(currentPosition).size();
        int removedBeforeCurrent = getRemovedVideoCount(currentOrdinal, deletedIndex);
        int targetOrdinal = currentOrdinal;
        if (replacement != null) {
            frgs.set(currentOrdinal % frgs.size(), replacement);
            //当前视频被删除，尽量保留下一条视频原有的 Fragment 和预加载结果。
            targetOrdinal += 1;
        }
        Collections.rotate(frgs, -getRemovedVideoCount(targetOrdinal, deletedIndex));
        TreeMap<Integer, VideoBaseFrg0> remainingAds = new TreeMap<>();
        for (Integer page : insertedFrgs.keySet()) {
            int ordinal = page - insertedFrgs.headMap(page).size();
            remainingAds.put(page - getRemovedVideoCount(ordinal, deletedIndex), insertedFrgs.get(page));
        }
        insertedFrgs.clear();
        insertedFrgs.putAll(remainingAds);
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
        return dataSize + insertedFrgs.size();
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
        for (VideoBaseFrg0 frg : insertedFrgs.values()) {
            if (frg.hashCode() == itemId) {
                return true;
            }
        }
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
