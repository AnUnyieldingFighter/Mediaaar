package media.library.images.ui.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import media.library.images.ui.frag.MediaFragment;

import java.util.ArrayList;

public class MediaPagerAdapter2 extends FragmentStateAdapter {
    public MediaPagerAdapter2(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    private ArrayList<MediaFragment> list = new ArrayList<>();

    public void setData(ArrayList<MediaFragment> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return list.get(position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public long getItemId(int position) {
        long id = list.get(position).getId(position);
        return id;
    }

    @Override
    public boolean containsItem(long itemId) {
        for (int i = 0; i < list.size(); i++) {
            var id = list.get(i).getId(i);
            if (id == itemId) {
                return true;
            }
        }
        return false;
    }

}
