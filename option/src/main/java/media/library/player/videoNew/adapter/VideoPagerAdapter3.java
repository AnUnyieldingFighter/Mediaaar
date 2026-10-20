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

    /**
     * 按当前页及前后页的索引调整复用槽位，不改变 Fragment 数量。
     * 这里只交换位置，数据数量和刷新由调用方处理。
     *
     * @param indexDel frgNow 在删除数据之前的页面索引，不是被删除视频的索引。
     * @param indexUpdate frgNow 在删除数据之后的页面索引。
     * @param frgNow 删除后保留的当前页面。
     * @param frgUp 删除后的上一页，为空时不调整该页。
     * @param frgDown 删除后的下一页，为空时不调整该页。
     */
    public void setFrgLast(int indexDel, int indexUpdate, VideoBaseFrg0 frgNow, VideoBaseFrg0 frgUp, VideoBaseFrg0 frgDown) {
        int size = frgs.size();
        if (indexDel < 0 || indexUpdate < 0 || size == 0 || frgNow == null) {
            return;
        }
        int nowSlot = indexUpdate % size;
        //先将当前页面从删除前的槽位交换到删除后的槽位。
        Collections.swap(frgs, indexDel % size, nowSlot);
        VideoBaseFrg0[] pages = {frgUp, frgDown};
        int[] slots = {(nowSlot + size - 1) % size, (nowSlot + 1) % size};
        for (int i = 0; i < pages.length; i++) {
            if (pages[i] == null) {
                continue;
            }
            int oldSlot = frgs.indexOf(pages[i]);
            if (oldSlot >= 0) {
                //交换时保留被占用槽位的 Fragment，避免直接覆盖导致重复或丢失。
                Collections.swap(frgs, oldSlot, slots[i]);
            }
        }
    }

    public void updateItem(int position) {
        notifyItemChanged(position);
    }


    private VideoBaseFrg0 getFrg(int position) {
        int size = frgs.size();
        if (position < 0 || size == 0) {
            return null;
        }
        //普通视频和广告统一按页面索引复用七个 Fragment。
        int index = position % size;
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
     * 仅用于相邻位置插入广告页面，保留原当前页及前后页。
     *
     * <p>先取得插入前的当前页和前后页。索引小于插入位置的页面保持原索引，
     * 其余页面的索引加一，再通过交换槽位将这三个 Fragment 放到对应的新位置。
     * 将新广告 Fragment 放入插入位置对应的槽位，并移出该槽位原来的 Fragment，
     * 最多保留原三页和广告共四页的对应关系，frgs 的总数量仍为七个。
     * 例如当前播放 C，在原 D 的位置插入 Y，保留 B、C、Y、D，原 D 移到 Y 后面。
     *
     * 本方法将 dataSize 加一；业务列表、播放器释放、当前页码对齐和刷新由调用方处理。
     *
     * @param position 插入前当前页的前一位或后一位索引，从 0 开始。
     * @param frg 新建的广告 Fragment。
     * @param currentPosition 插入前正在播放的页面索引，从 0 开始。
     * @return 被移出复用池的 Fragment，供调用方清理。
     */
    public VideoBaseFrg0 addFrg(int position, VideoBaseFrg0 frg, int currentPosition) {
        int size = frgs.size();
        VideoBaseFrg0 frgNow = getFrg(currentPosition);
        VideoBaseFrg0 frgUp = currentPosition > 0 ? getFrg(currentPosition - 1) : null;
        VideoBaseFrg0 frgDown = currentPosition + 1 < getItemCount() ? getFrg(currentPosition + 1) : null;
        VideoBaseFrg0[] pages = {frgNow, frgUp, frgDown};
        int[] oldIndexes = {currentPosition, currentPosition - 1, currentPosition + 1};
        //插入位置之前的页面索引不变，其余页面索引后移一位。
        for (int i = 0; i < pages.length; i++) {
            if (pages[i] == null) {
                continue;
            }
            int newIndex = oldIndexes[i];
            if (newIndex >= position) {
                newIndex += 1;
            }
            Collections.swap(frgs, frgs.indexOf(pages[i]), newIndex % size);
        }
        //原当前页及前后页已放好，广告槽位不会占用这三个页面。
        VideoBaseFrg0 removed = frgs.set(position % size, frg);
        dataSize += 1;
        return removed;
    }

    /**
     * 非相邻位置插入一条数据，不创建或移出 Fragment。
     * 插在当前页之前时，原当前页和前后页的索引都加一，同步调整它们的复用槽位；
     * 插在当前页之后时，原三页的索引和槽位不变。
     * 本方法仅将 dataSize 加一并调整槽位，业务列表、页码对齐和刷新由调用方处理。
     *
     * @param position 插入前列表中的非相邻插入位置，从 0 开始。
     * @param currentPosition 插入前正在播放的页面索引，从 0 开始。
     */
    public void setFrgUpdate(int position, int currentPosition) {
        if (position < currentPosition) {
            VideoBaseFrg0 frgNow = getFrg(currentPosition);
            VideoBaseFrg0 frgUp = currentPosition > 0 ? getFrg(currentPosition - 1) : null;
            VideoBaseFrg0 frgDown = currentPosition + 1 < getItemCount() ? getFrg(currentPosition + 1) : null;
            setFrgLast(currentPosition, currentPosition + 1, frgNow, frgUp, frgDown);
        }
        dataSize += 1;
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
     * 删除一个数据
     */
    public void removeData() {
        if (this.dataSize > 0) {
            this.dataSize -= 1;
        }
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
