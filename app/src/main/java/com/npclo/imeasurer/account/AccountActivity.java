package com.npclo.imeasurer.account;

import android.os.Bundle;
import android.view.Window;
import android.widget.Toast;

import com.npclo.imeasurer.R;
import com.npclo.imeasurer.account.signin.SignInFragment;
import com.npclo.imeasurer.account.signin.SignInPresenter;
import com.npclo.imeasurer.utils.schedulers.SchedulerProvider;

import me.yokeyword.fragmentation.SupportActivity;

/**
 * 管理用户登录和注册的界面，使用两个不同的fragment来分别承载
 */
public class AccountActivity extends SupportActivity {
    // 再点一次退出程序时间设置
    private static final long WAIT_TIME = 2000L;
    private long TOUCH_TIME = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.act_account);

        SignInFragment fragment = findFragment(SignInFragment.class);
        if (fragment == null) {
            fragment = SignInFragment.newInstance();
            loadRootFragment(R.id.content_frame, fragment);
            new SignInPresenter(fragment, SchedulerProvider.getInstance());
        }
    }

    /**
     * 再按一次退出处理逻辑
     */
    @Override
    public void onBackPressedSupport() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            pop();
        } else {
            if (System.currentTimeMillis() - TOUCH_TIME < WAIT_TIME) {
                finish();
            } else {
                TOUCH_TIME = System.currentTimeMillis();
                Toast.makeText(this, R.string.press_again_exit, Toast.LENGTH_SHORT).show();
            }
        }
    }
}