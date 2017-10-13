package com.npclo.gdemo.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Window;
import android.widget.Toast;

import com.npclo.gdemo.R;

import me.yokeyword.fragmentation.SupportActivity;

public abstract class BaseActivity extends SupportActivity {
    // 再点一次退出程序时间设置
    private static final long WAIT_TIME = 2000L;
    private long TOUCH_TIME = 0;

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        beforeInit();
    }

    protected void beforeInit() {
    }

    @Override
    public void onBackPressedSupport() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            pop();
        } else {
            if (System.currentTimeMillis() - TOUCH_TIME < WAIT_TIME) {
                finish();
            } else {
                TOUCH_TIME = System.currentTimeMillis(); // FIXME: 2017/9/21 再按一次退出
                Toast.makeText(this, R.string.press_again_exit, Toast.LENGTH_SHORT).show();
            }
        }
    }
}