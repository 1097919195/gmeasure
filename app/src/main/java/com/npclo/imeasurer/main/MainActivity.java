package com.npclo.imeasurer.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.npclo.imeasurer.R;
import com.npclo.imeasurer.base.BaseActivity;
import com.npclo.imeasurer.base.BaseApplication;
import com.npclo.imeasurer.data.App;
import com.npclo.imeasurer.data.User;
import com.npclo.imeasurer.user.UserActivity;
import com.npclo.imeasurer.utils.Constant;
import com.npclo.imeasurer.utils.Gog;
import com.npclo.imeasurer.utils.LogUtils;
import com.npclo.imeasurer.utils.PreferencesUtils;
import com.npclo.imeasurer.utils.ToastUtil;
import com.npclo.imeasurer.utils.schedulers.SchedulerProvider;
import com.npclo.imeasurer.utils.views.CircleImageView;
import com.unisound.client.SpeechConstants;
import com.unisound.client.SpeechSynthesizer;

import kr.co.namee.permissiongen.PermissionGen;

/**
 * @author Endless
 * @date 2017/9/1
 */

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {
    public NavigationView navView;
    private DrawerLayout drawerLayout;
    private HomePresenter homePresenter;
    private String macAddress;
    private String deviceName;
    public SpeechSynthesizer speechSynthesizer;//提供对已安装的语音合成引擎的功能的访问
    private TextView currTimesView;
    private TextView totalTimesView;
    private TextView userNameView;
    private CircleImageView logoView;

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
        try {
            if (speechSynthesizer == null) {
                speechSynthesizer = new SpeechSynthesizer(this, Constant.APP_KEY, Constant.APP_SECRET);
            }
            speechSynthesizer.setOption(SpeechConstants.TTS_SERVICE_MODE, SpeechConstants.TTS_SERVICE_MODE_NET);
            speechSynthesizer.setOption(SpeechConstants.TTS_KEY_VOICE_SPEED, 70);
            speechSynthesizer.init(null);
        } catch (Exception e) {
            LogUtils.fixBug("语音播报出现异常，异常原因: " + LogUtils.getStackMsg(e));
        }
    }

    private void init() {
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
        PreferencesUtils instance = PreferencesUtils.getInstance(this);
        //判断是否已绑定设备
        macAddress = instance.getMacAddress();
        //判断是否连接过蓝牙
        if (TextUtils.isEmpty(macAddress)) {
            navView.getMenu().add(R.id.device, R.id.nav_device, 0, "连接智能尺").setIcon(R.drawable.ic_blueteeth_unconnected);
        } else {
            deviceName = instance.getDeviceName();
            updateBlueToothState(deviceName);
        }
        //判断是否加载特定量体合同
        String contractName = instance.getContractName();
        if (TextUtils.isEmpty(contractName)) {
            updateContractName("默认");
        } else {
            updateContractName(contractName);
        }
        View headerView = navView.getHeaderView(0);
        currTimesView = headerView.findViewById(R.id.curr_times);
        totalTimesView = headerView.findViewById(R.id.total_times);
        userNameView = headerView.findViewById(R.id.user_name);
        logoView = headerView.findViewById(R.id.logo);

        currTimesView.setText(String.valueOf(instance.getCurrTimes()));
        totalTimesView.setText(String.valueOf(instance.getTotalTimes()));
        String name = instance.getUserName();
        String nickname = instance.getUserNickname();
        userNameView.setText(!TextUtils.isEmpty(nickname) ? nickname : name);
        String logoSrc = instance.getUserLogo();
        if (!TextUtils.isEmpty(logoSrc)) {
            Glide.with(this).load(Constant.getImgUrl() + logoSrc)
                    .apply(new RequestOptions().error(R.drawable.load_fail_pic))
                    .into(logoView);
        } else {
            logoView.setImageDrawable(getResources().getDrawable(R.mipmap.ic_launcher));
        }
    }

    public void updateBlueToothState(String name) {
        //先清除蓝牙设备信息菜单项
        navView.getMenu().removeItem(R.id.nav_device);
        navView.getMenu()
                .add(R.id.device, R.id.nav_device, 0, "连接智能尺(已绑定)")
                .setIcon(R.drawable.ic_blueteeth_connected);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        PreferencesUtils instance = PreferencesUtils.getInstance(this);
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
                break;
            case R.id.nav_instruction:
                drawerLayout.closeDrawers();
                Toast.makeText(this, "使用说明开发中", Toast.LENGTH_SHORT).show();
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
                            .title("已绑定智能尺: " + deviceName + "，需要连接新智能尺？")
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
                            instance.setMeasureOffset(v);
                        }).show();
                break;
            case R.id.nav_contract:
                new MaterialDialog.Builder(this)
                        .backgroundColor(getResources().getColor(R.color.white))
                        .title(R.string.contract_setting)
                        .content(R.string.contract_instruction)
                        .positiveText(R.string.new_contract)
                        .negativeText(R.string.default_contract)
                        .onPositive((dialog, action) -> homePresenter.getThirdOrgMeasurePartByContractNum()
                        )
                        .onNegative((dialog, action) -> homePresenter.getThirdOrgDefaultParts(instance.getUserOrgid())
                        )
                        .show();
                break;
            case R.id.nav_account:
                Intent pwdIntent = new Intent(this, UserActivity.class);
                pwdIntent.putExtra("support_type", Constant.USER_PWD);
                startActivity(pwdIntent);
                break;
            case R.id.nav_feedback:
                Intent feedbackIntent = new Intent(this, UserActivity.class);
                feedbackIntent.putExtra("support_type", Constant.USER_FEEDBACK);
                startActivity(feedbackIntent);
                break;
            case R.id.nav_link:
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

    public void updateContractName(String name) {
        navView.getMenu().removeItem(R.id.nav_contract);
        navView.getMenu()
                .add(R.id.device, R.id.nav_contract, 1, "合同号(" + name + ")")
                .setIcon(R.drawable.ic_contract);
    }

    public void updateUserinfoView(User user) {
        currTimesView.setText(String.valueOf(user.getCurrTimes()));
        totalTimesView.setText(String.valueOf(user.getTotalTimes()));
        userNameView.setText(!TextUtils.isEmpty(user.getNickname()) ? user.getNickname() : user.getName());
        String logoSrc = user.getLogo();
        if (!TextUtils.isEmpty(logoSrc)) {
            Glide.with(this).load(Constant.getImgUrl() + logoSrc)
                    .apply(new RequestOptions().error(R.drawable.load_fail_pic))
                    .into(logoView);
        } else {
            logoView.setImageDrawable(getResources().getDrawable(R.mipmap.ic_launcher));
        }
    }

    @Override
    public void onBackPressedSupport() {
        if (drawerLayout.isDrawerOpen(navView)){
            drawerLayout.closeDrawers();
            return;
        }
        super.onBackPressedSupport();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechSynthesizer != null) {
            speechSynthesizer = null;
        }
    }
}