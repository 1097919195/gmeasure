package com.npclo.imeasurer.base;

import android.app.ActivityManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.Toolbar;
import android.view.Window;

import com.afollestad.materialdialogs.color.CircleView;
import com.npclo.imeasurer.R;
import com.npclo.imeasurer.utils.Constant;
import com.npclo.imeasurer.utils.SettingUtil;

import me.yokeyword.fragmentation.SupportActivity;

public abstract class BaseActivity extends SupportActivity {
    // 再点一次退出程序时间设置
    private static final long WAIT_TIME = 2000L;
    private long touchTime = 0;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        beforeInit();
    }

    protected void beforeInit() {
    }

//    @Override
//    public void onBackPressedSupport() {
//        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
//            pop();
//        } else {
//            if (System.currentTimeMillis() - touchTime < WAIT_TIME) {
//                finish();
//            } else {
//                touchTime = System.currentTimeMillis(); // FIXME: 2017/9/21 再按一次退出
//                Toast.makeText(this, R.string.press_again_exit, Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
    protected void initToolBar(Toolbar toolbar, boolean homeAsUpEnabled, String title) {
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(homeAsUpEnabled);
    }


    @Override
    protected void onResume() {
        super.onResume();
        int color = SettingUtil.getInstance().getColor();
        int drawable = Constant.ICONS_DRAWABLES[SettingUtil.getInstance().getCustomIconValue()];
        if (getSupportActionBar() != null) {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(color));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(CircleView.shiftColorDown(color));
            // 最近任务栏上色
            ActivityManager.TaskDescription tDesc = new ActivityManager.TaskDescription(
                    getString(R.string.app_name),
                    BitmapFactory.decodeResource(getResources(), drawable),
                    color);
            setTaskDescription(tDesc);
            if (SettingUtil.getInstance().getNavBar()) {
                getWindow().setNavigationBarColor(CircleView.shiftColorDown(color));
            } else {
                getWindow().setNavigationBarColor(Color.BLACK);
            }
        }
    }
}