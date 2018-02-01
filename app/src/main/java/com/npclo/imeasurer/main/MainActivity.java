package com.npclo.imeasurer.main;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.npclo.imeasurer.R;
import com.npclo.imeasurer.base.BaseActivity;
import com.npclo.imeasurer.base.BaseApplication;
import com.npclo.imeasurer.data.User;
import com.npclo.imeasurer.user.UserActivity;
import com.npclo.imeasurer.utils.Constant;
import com.npclo.imeasurer.utils.PreferencesUtils;
import com.npclo.imeasurer.utils.schedulers.SchedulerProvider;
import com.unisound.client.SpeechConstants;
import com.unisound.client.SpeechSynthesizer;

import kr.co.namee.permissiongen.PermissionGen;

/**
 * @author Endless
 * @date 2017/9/1
 */

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {
    private NavigationView navView;
    private DrawerLayout drawerLayout;
    private HomePresenter homePresenter;
    private String macAddress;
    private String deviceName;
    public SpeechSynthesizer speechSynthesizer;
    private TextView currTimesView;
    private TextView totalTimesView;
    private TextView userNameView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        init();
    }

    @Override
    protected void onStart() {
        super.onStart();
        initSpeech();
    }

    private void initSpeech() {
        if (speechSynthesizer == null) {
            speechSynthesizer = new SpeechSynthesizer(this, Constant.APP_KEY, Constant.APP_SECRET);
        }
        speechSynthesizer.setOption(SpeechConstants.TTS_SERVICE_MODE, SpeechConstants.TTS_SERVICE_MODE_NET);
        speechSynthesizer.setOption(SpeechConstants.TTS_KEY_VOICE_SPEED, 70);
        speechSynthesizer.init(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        speechSynthesizer = null;
    }

    private void init() {
        PermissionGen.with(MainActivity.this)
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
        //加载登录后的欢迎界面
        HomeFragment homeFragment = findFragment(HomeFragment.class);
        if (homeFragment == null) {
            homeFragment = HomeFragment.newInstance();
            loadRootFragment(R.id.content_frame, homeFragment);
            homePresenter = new HomePresenter(BaseApplication.getRxBleClient(this),
                    homeFragment, SchedulerProvider.getInstance());
        }
    }

    protected void initView() {
        setContentView(R.layout.act_main_new);
        Toolbar toolbar = (Toolbar) findViewById(R.id.basetoolbar);
        setSupportActionBar(toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navView = (NavigationView) findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(this);
        navView.getMenu().removeItem(R.id.nav_device);
        initDrawerMenuContent();
    }

    private void initDrawerMenuContent() {
        //判断是否已绑定设备
        PreferencesUtils instance = PreferencesUtils.getInstance(this);
        //判断是否已绑定设备
        macAddress = instance.getMacAddress();

        if (TextUtils.isEmpty(macAddress)) {
            navView.getMenu().add(R.id.device, R.id.nav_device, 0, "连接智能尺").setIcon(R.drawable.ic_blueteeth_unconnected);
        } else {
            deviceName = instance.getDeviceName();
            updateBlueToothState(deviceName);
        }
        View headerView = navView.getHeaderView(0);
        currTimesView = headerView.findViewById(R.id.curr_times);
        totalTimesView = headerView.findViewById(R.id.total_times);
        userNameView = headerView.findViewById(R.id.user_name);

        currTimesView.setText(String.valueOf(instance.getCurrTimes()));
        totalTimesView.setText(String.valueOf(instance.getTotalTimes()));
        String name = instance.getUserName();
        String nickname = instance.getUserNickname();

        userNameView.setText(!TextUtils.isEmpty(nickname) ? nickname : name);
    }

    public void updateBlueToothState(String name) {
        //先清除蓝牙设备信息菜单项
        navView.getMenu().removeItem(R.id.nav_device);
        navView.getMenu().add(R.id.device, R.id.nav_device, 0, "连接智能尺(已绑定)")
                .setIcon(R.drawable.ic_blueteeth_connected);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_logout:
                new MaterialDialog.Builder(this)
                        .title("确定要退出登录？")
                        .titleColor(getResources().getColor(R.color.ff5001))
                        .positiveText(R.string.sure)
                        .negativeText(R.string.cancel)
                        .backgroundColor(getResources().getColor(R.color.white))
                        .onPositive((dialog, which) -> {
                            dialog.dismiss();
                            homePresenter.logout();
                        })
                        .show();
                drawerLayout.closeDrawers();
                break;
            case R.id.nav_instruction:
                drawerLayout.closeDrawers();
                Toast.makeText(this, "暂无使用说明", Toast.LENGTH_SHORT).show();
//              Intent instructionIntent = new Intent(this, UserActivity.class);
//                instructionIntent.putExtra("support_type", Constant.USER_INSTRUCTION);
//                startActivity(instructionIntent);
                break;
            case R.id.nav_device:
                if (TextUtils.isEmpty(macAddress)) {
                    homePresenter.startScan();
                    drawerLayout.closeDrawers();
                } else {
                    new MaterialDialog.Builder(this)
                            .title("已绑定设备" + deviceName + "，需要更换设备？")
                            .titleColor(getResources().getColor(R.color.ff5001))
                            .positiveText(R.string.sure)
                            .negativeText(R.string.cancel)
                            .backgroundColor(getResources().getColor(R.color.white))
                            .onPositive((dialog, which) -> {
                                drawerLayout.closeDrawers();
                                homePresenter.startScan();
                            })
                            .show();
                }
                break;
            case R.id.nav_offset_setting:
                new MaterialDialog.Builder(this)
                        .content(R.string.input_offset)
                        .inputType(InputType.TYPE_CLASS_NUMBER)
                        .input(R.string.input_offset_hint, R.string.default_value, (dialog, offset) -> {
                            dialog.dismiss();
                            String s = offset.toString();
                            float v = 0.0f;
                            try {
                                v = !TextUtils.isEmpty(s) ? Float.parseFloat(s) : 0.0f;
                            } catch (NumberFormatException e) {
                                v = 0.0f;
                            }
                            PreferencesUtils.getInstance(this).setMeasureOffset(v);
                        }).show();
                drawerLayout.closeDrawers();
                break;
            case R.id.nav_account:
                drawerLayout.closeDrawers();
                Intent pwdIntent = new Intent(this, UserActivity.class);
                pwdIntent.putExtra("support_type", Constant.USER_PWD);
                startActivity(pwdIntent);
                break;
            case R.id.nav_feedback:
                drawerLayout.closeDrawers();
                Intent feedbackIntent = new Intent(this, UserActivity.class);
                feedbackIntent.putExtra("support_type", Constant.USER_FEEDBACK);
                startActivity(feedbackIntent);
                break;
            case R.id.nav_link:
                drawerLayout.closeDrawers();
                Intent contactIntent = new Intent(this, UserActivity.class);
                contactIntent.putExtra("support_type", Constant.USER_CONTACT);
                startActivity(contactIntent);
                break;
            case R.id.nav_version:
                drawerLayout.closeDrawers();
                homePresenter.manuallyGetLatestVersion();
                break;
            default:
                break;
        }
        return false;
    }

    public void updateUserinfoView(User user) {
        currTimesView.setText(String.valueOf(user.getCurrTimes()));
        totalTimesView.setText(String.valueOf(user.getTotalTimes()));
        userNameView.setText(!TextUtils.isEmpty(user.getNickname()) ? user.getNickname() : user.getName());
    }
}