package com.npclo.imeasurer.account;

import android.Manifest;
import android.os.Bundle;
import android.view.Window;

import com.npclo.imeasurer.R;
import com.npclo.imeasurer.account.signin.SignInFragment;
import com.npclo.imeasurer.account.signin.SignInPresenter;
import com.npclo.imeasurer.utils.Gog;
import com.npclo.imeasurer.utils.PreferencesUtils;
import com.npclo.imeasurer.utils.schedulers.SchedulerProvider;

import kr.co.namee.permissiongen.PermissionGen;
import me.yokeyword.fragmentation.SupportActivity;

/**
 * 管理用户登录和注册的界面，使用两个不同的fragment来分别承载
 *
 * @author Endless
 */
public class AccountActivity extends SupportActivity {
    PreferencesUtils instance;
    private boolean firstStart;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.act_account);

        instance = PreferencesUtils.getInstance(this);
        if (firstStart == instance.getFirstStart()) {
            instance.setFirstStart(true);
            requestPermission();
        }

        SignInFragment fragment = findFragment(SignInFragment.class);
        if (fragment == null) {
            fragment = SignInFragment.newInstance();
            loadRootFragment(R.id.content_frame, fragment);
            new SignInPresenter(fragment, SchedulerProvider.getInstance());
        }
    }

    private void requestPermission() {
        Gog.e("first");
        PermissionGen.with(this)
                .addRequestCode(100)
                .permissions(
                        Manifest.permission.LOCATION_HARDWARE,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.CAMERA)
                .request();
    }
}