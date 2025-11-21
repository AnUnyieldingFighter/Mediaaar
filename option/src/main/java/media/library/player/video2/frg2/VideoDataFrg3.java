package media.library.player.video2.frg2;

import android.text.TextUtils;

import com.app.net.common.AbstractBaseMapManager;
import com.app.net.manager.account.UserLoginManager;
import com.app.net.res.user.SysUser;
import com.app.ui.activity.base.MainApplication;
import com.app.ui.activity.user.account.UserLoginAct;
import com.app.ui.window.dialog.DialogCustomWaiting;
import com.app.utiles.HandlerUtil;
import com.app.utiles.other.ToastUtile;
import com.library.baseui.utile.app.ActivityCycle;
import com.library.baseui.utile.app.ActivityUtile;
import com.retrofits.net.common.RequestBack;

import androidx.fragment.app.FragmentActivity;
import sj.mblog.Logx;

public class VideoDataFrg3 extends VideoAnimationFrg2 implements RequestBack {
    //=======================等待dialog===============================
    private DialogCustomWaiting dialog;

    protected void dialogShow() {
        if (dialog != null && dialog.isShowing()) {
            return;
        }
        HandlerUtil.runInMainThread(dialogRun, 500);
    }

    protected void dialogDismiss() {
        HandlerUtil.removeCallbacks(dialogRun);
        dialogDie();
    }

    private void dialogDisplay() {
        if (act.isDestroyed() || act.isFinishing()) {
            return;
        }
        if (dialog == null) {
            dialog = new DialogCustomWaiting(act);
        }
        dialog.show();
    }

    private void dialogDie() {
        if (dialog == null) {
            return;
        }
        dialog.dismiss();
    }

    private Runnable dialogRun = new Runnable() {

        @Override
        public void run() {
            dialogDisplay();
        }
    };

    //=============================================
    public void onReqBack(int what, Object obj, String msg, String other) {
        showToast(msg, other);
    }

    protected void onUserPrivate() {

    }

    @Override
    public void onBack(int what, Object obj, String msg, String other) {
        FragmentActivity act = getActivity();
        if (act == null || act.isDestroyed() || act.isFinishing()) {
            return;
        }
        if (what == AbstractBaseMapManager.WHAT_LOGIN_NOT) {
            ActivityUtile.startActivityString(UserLoginAct.class, "2");
            return;
        }
        if (what == AbstractBaseMapManager.WHAT_TKN_ERROR) {
            MainApplication mainApplication = (MainApplication) act.getApplication();
            SysUser user = mainApplication.getUser();
            int loginType = 1;
            if (user != null) {
                user.isTokenFail = true;
                loginType = user.loginType;
            }
            if (loginType == 1) {
                //游客登录
                setGuestLogin();
            } else {
                ActivityUtile.startActivityString(UserLoginAct.class, "2");
                ActivityCycle.getInstance().setFinishAll(UserLoginAct.class);
            }
            return;
        }
        if (what == AbstractBaseMapManager.WHAT_USER_PRIVATE) {
            onUserPrivate();
            return;
        }
        onReqBack(what, obj, msg, other);
    }

    @Override
    public void onBackProgress(int i, String s, String s1, long l, long l1) {

    }

    private void showToast(String msg, String other) {
        Logx.d("消息-->", "msg:" + msg + " other:" + other);
        if ("成功".equals(msg)) {
            msg = "";
        }
        if (!TextUtils.isEmpty(other)) {
            ToastUtile.showToast(other);
            return;
        }
        if (!TextUtils.isEmpty(msg)) {
            ToastUtile.showToast(msg);
            return;
        }
    }

    //=======================游客重新登录===================================
    private UserLoginManager guestManager;

    private void setGuestLogin() {
        if (guestManager == null) {
            guestManager = new UserLoginManager(this);
        }
        MainApplication mainApplication = (MainApplication) act.getApplication();
        //游客登录
        String guestId = "";
        SysUser user = mainApplication.getUser();
        if (user != null) {
            guestId = user.guestId;
        }
        guestManager.doRequestVisitorAgain(guestId);

    }
}
