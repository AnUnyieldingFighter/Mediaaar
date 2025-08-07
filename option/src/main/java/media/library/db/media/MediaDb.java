package media.library.db.media;


import media.library.images.config.entity.MediaEntity;
import media.library.db.BaseDatabase;

import java.util.List;


/**
 * @author 86134
 */

public class MediaDb {
    private BaseDatabase base;

    public MediaDb(BaseDatabase base) {
        this.base = base;
    }

    public MediaDao getDao() {
        return base.mediaDao();
    }

    public void put(MediaEntity data) {
        getDao().put(data);
    }

    public void putList(List<MediaEntity> datas) {
        getDao().putList(datas);
    }

    public void update(MediaEntity data) {
        getDao().update(data);
    }


    public void delete(MediaEntity data) {
        getDao().delete(data);
    }


   /* public void queryByProvinceCode(String provinceCode) {
        getDao().queryByProvinceCode(provinceCode);
    }*/

    public void queryCount() {
        getDao().queryCount();
    }

    public void queryAll() {
        getDao().queryAll();
    }

    public void deleteAll() {
        getDao().deleteAll();
    }
}
