package com.images.ui.Manger;

import com.hjq.permissions.OnPermissionCallback;

public interface OnPMCallback extends OnPermissionCallback {
    //已获取
    void onAcquiredAll();
    void onError();
}
