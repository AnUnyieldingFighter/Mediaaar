package media.library.player.vido1.frg;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.media3.exoplayer.ExoPlayer;

import media.library.player.view.CustomExoPlayer;

public class VideoBaseFrg extends Fragment {
    protected FragmentActivity act;
    private long id = 0;

    public long getId(int index) {
        if (id == 0) {
            id = System.currentTimeMillis();
        }
        return id + index;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        int layoutId = getRootViewLayout();
        if (layoutId != 0) {
            View view = inflater.inflate(layoutId, container, false);
            return view;
        }
        return getRootView(inflater, container, savedInstanceState);
    }


    protected int getRootViewLayout() {
        return 0;
    }

    protected View getRootView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        act = getActivity();
        setViewInit(view, savedInstanceState);
    }

    protected void setViewInit(View view, Bundle savedInstanceState) {
    }

    public CustomExoPlayer getExoPlayer() {
        return null;
    }


}
