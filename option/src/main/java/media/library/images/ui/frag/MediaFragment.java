package media.library.images.ui.frag;


import androidx.fragment.app.Fragment;

public class MediaFragment  extends Fragment   {
    private long id = 0;

    public long getId(int index) {
        if (id == 0) {
            id = System.currentTimeMillis();
        }
        return id + index;
    }


}
