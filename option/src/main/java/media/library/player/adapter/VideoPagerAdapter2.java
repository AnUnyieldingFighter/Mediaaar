package media.library.player.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;

import media.library.player.frg.VideoBaseFrg;

public class VideoPagerAdapter2 extends FragmentStateAdapter {
    private ArrayList<VideoBaseFrg> frgs = new ArrayList<>();
    //阈值，超过这个值 将显示无限
    private int maxPage;

    public VideoPagerAdapter2(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        this.maxPage = 5;
    }

    public VideoPagerAdapter2(@NonNull FragmentActivity fragmentActivity, int maxPage) {
        super(fragmentActivity);
        this.maxPage = maxPage;
    }

    public void setFrgs(ArrayList<VideoBaseFrg> frgs) {
        this.frgs = frgs;
    }

    public void setData(ArrayList<VideoBaseFrg> frgs) {
        this.frgs = frgs;
        notifyDataSetChanged();
    }

    public void updateItem(int position) {
        notifyItemChanged(position);
    }

    @Override
    public long getItemId(int position) {
        VideoBaseFrg frg = getFrg(position);
        var id = frg.getId(position);
        return id;
    }

    private VideoBaseFrg getFrg(int position) {
        int index = position % frgs.size();
        VideoBaseFrg frg = frgs.get(index);
        return frg;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        VideoBaseFrg frg = getFrg(position);
        return frg;
    }

    @Override
    public boolean containsItem(long itemId) {
        for (int i = 0; i < frgs.size(); i++) {
            var id = frgs.get(i).getId(i);
            if (id == itemId) {
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
        if (frgs.size() <= maxPage) {
            return frgs.size();
        }
        return Integer.MAX_VALUE;
    }

    //true 页面无限
    public boolean isInfinite() {
        if (frgs == null || frgs.size() == 0) {
            return false;
        }
        if (frgs.size() <= maxPage) {
            return false;
        }
        return true;
    }
}
