package com.npclo.imeasurer.main;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.npclo.imeasurer.R;
import com.npclo.imeasurer.account.AccountActivity;
import com.npclo.imeasurer.base.BaseFragment;
import com.npclo.imeasurer.camera.CaptureActivity;
import com.npclo.imeasurer.data.App;
import com.npclo.imeasurer.data.WechatUser;
import com.npclo.imeasurer.data.ble.BleDevice;
import com.npclo.imeasurer.measure.MeasureActivity;
import com.npclo.imeasurer.utils.Constant;
import com.npclo.imeasurer.utils.LogUtils;
import com.npclo.imeasurer.utils.PreferencesUtils;
import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.exceptions.BleScanException;
import com.polidea.rxandroidble.scan.ScanResult;
import com.unisound.client.SpeechSynthesizer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

/**
 * @author Endless
 */
public class HomeFragment extends BaseFragment implements HomeContract.View {
    public static final int REQUEST_CODE = 1001;
    @BindView(R.id.scan_img)
    ImageView scanImg;
    @BindView(R.id.scan_hint)
    TextView scanHint;
    Unbinder unbinder;
    private HomeContract.Presenter mPresenter;
    private static final int SCAN_HINT = 1001;
    private static final int CODE_HINT = 1002;
    private List<BleDevice> bleDeviceList = new ArrayList<>();
    private List<String> rxBleDeviceAddressList = new ArrayList<>();
    private ScanResultsAdapter scanResultsAdapter;
    private MaterialDialog.Builder scanResultDialog;
    private MaterialDialog cirProgressBar;
    private MaterialDialog resultDialog;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.frag_home;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void initView(View mRootView) {
        unbinder = ButterKnife.bind(this, mRootView);
        configureResultList();
    }

    @Override
    public void setPresenter(HomeContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.scan_img, R.id.scan_hint})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.scan_img:
            case R.id.scan_hint:
                startScan();
                break;
            default:
                break;
        }
    }

    private void startScan() {
        Intent intent = new Intent(getActivity(), CaptureActivity.class);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPresenter != null) {
            mPresenter.subscribe(); // FIXME: 2017/12/8 app闲置后  mPresenter对象为空
        }
        LogUtils.upload(getActivity());
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mPresenter != null) {
            mPresenter.unsubscribe();
        }
    }

    @Override
    protected void initComToolbar() {
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.basetoolbar);
        toolbar.setTitle(getString(R.string.app_name));
    }

    @Override
    public void showLoading(boolean b) {
        if (b) {
            cirProgressBar = new MaterialDialog.Builder(getActivity())
                    .progress(true, 100)
                    .backgroundColor(getResources().getColor(R.color.white))
                    .show();
        } else {
            cirProgressBar.dismiss();
        }
    }

    /**
     * 获取用户数据成功回调
     *
     * @param user 微信用户
     */
    @Override
    public void onGetWechatUserInfo(WechatUser user) {
        showLoading(false);
        Bundle bundle = new Bundle();
        bundle.putParcelable("user", user);
        Intent intent = new Intent(getActivity(), MeasureActivity.class);
        intent.putExtra("userBundle", bundle);
        startActivity(intent);
    }

    @Override
    public void showGetInfoError(Throwable e) {
        showLoading(false);
        onHandleError(e);
    }

    @Override
    public void showCompleteGetInfo() {
        showLoading(false);
    }

    @Override
    public void onGetVersionInfo(App app, String type) {
        int code = getVersionCode();
        if (app.getCode() > code && code != 0) {
            updateApp(app);
        } else {
            if (Constant.MANUAL.equals(type)) {
                showToast("已经是最新版");
            }
        }
    }

    @Override
    public void onGetVersionError(Throwable e) {
        onHandleError(e);
    }

    @Override
    public void onLogout() {
        // 退出登录，清除id和token
        MainActivity activity = (MainActivity) getActivity();
        PreferencesUtils instance = PreferencesUtils.getInstance(activity);
        instance.setToken("");
        Toast.makeText(activity, getString(R.string.logout_success), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(activity, AccountActivity.class);
        activity.startActivity(intent);
        activity.finish();
    }

    @Override
    public void onHandleScanResult(ScanResult result) {
        if (cirProgressBar != null) {
            cirProgressBar.dismiss();
            cirProgressBar = null;
        }
        RxBleDevice device = result.getBleDevice();
        if (resultDialog == null) {
            resultDialog = scanResultDialog.show();
        }
        if (!resultDialog.isShowing()) {
            resultDialog.show();
        }
        if (!rxBleDeviceAddressList.contains(device.getMacAddress())) {
            rxBleDeviceAddressList.add(device.getMacAddress());
            bleDeviceList.add(new BleDevice(device.getName(), device.getMacAddress(), result.getRssi()));
            scanResultsAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onShowError(String s) {
        showToast(s);
    }

    private void configureResultList() {
        scanResultsAdapter = new ScanResultsAdapter(this, bleDeviceList);
        scanResultDialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.choose_device_prompt)
                .backgroundColor(getResources().getColor(R.color.white))
                .titleColor(getResources().getColor(R.color.scan_result_list_title))
                .dividerColor(getResources().getColor(R.color.divider))
                .adapter(scanResultsAdapter, null);
        ;
        //选择目的蓝牙设备
        scanResultsAdapter.setOnAdapterItemClickListener(v -> {
                    String s = ((TextView) v.findViewById(R.id.txt_mac)).getText().toString();
                    mPresenter.connectDevice(s);
                }
        );
    }

    @Override
    public void onHandleBleScanException(BleScanException e) {
        switch (e.getReason()) {
            case BleScanException.BLUETOOTH_NOT_AVAILABLE:
                showToast(getString(R.string.bluetooth_not_avavilable));
                break;
            case BleScanException.BLUETOOTH_DISABLED:
                showToast(getString(R.string.bluetooth_disabled));
                break;
            case BleScanException.LOCATION_PERMISSION_MISSING:
                showToast("未授予获取位置权限");
                break;
            case BleScanException.LOCATION_SERVICES_DISABLED:
                showToast("6.0以上手机需要开启位置服务");
                break;
            case BleScanException.SCAN_FAILED_ALREADY_STARTED:
                showToast("Scan with the same filters is already started");
                break;
            case BleScanException.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED:
                showToast("Failed to register application for bluetooth scan");
                break;
            case BleScanException.SCAN_FAILED_FEATURE_UNSUPPORTED:
                showToast("Scan with specified parameters is not supported");
                break;
            case BleScanException.SCAN_FAILED_INTERNAL_ERROR:
                showToast("Scan failed due to internal error");
                break;
            case BleScanException.SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES:
                showToast("Scan cannot start due to limited hardware resources");
                break;
            case BleScanException.UNKNOWN_ERROR_CODE:
            case BleScanException.BLUETOOTH_CANNOT_START:
            default:
                showToast("不能够扫描外接蓝牙设备");
                break;
        }
    }

    @Override
    public void onSetNotificationUUID(UUID uuid) {
        PreferencesUtils.getInstance(getActivity()).setDeviceUuid(uuid.toString());
    }

    private void setBleAddress(String s) {
        PreferencesUtils.getInstance(getActivity()).setMacAddress(s);
    }

    private void setBleDeviceName(String name) {
        PreferencesUtils.getInstance(getActivity()).setDeviceName(name);
    }

    @Override
    public void onDeviceChoose(RxBleDevice bleDevice) {
        showToast(getString(R.string.device_connected));
        getSynthesizer().playText("蓝牙连接成功");
        setBleDeviceName(bleDevice.getName());
        setBleAddress(bleDevice.getMacAddress());
        //更新设备连接信息状态
        ((MainActivity) getActivity()).updateBlueToothState(bleDevice.getName());
    }

    @Override
    public void onCloseScanResultDialog() {
        try {
            if (resultDialog != null || resultDialog.isShowing()) {
                resultDialog.dismiss();
                resultDialog = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bundle bundle = data.getExtras();
        String result = bundle.getString("result");
        switch (resultCode) {
            case SCAN_HINT:
                if (result != null) {
                    mPresenter.getUserInfoWithOpenID(result);
                } else {
                    showToast(getString(R.string.scan_qrcode_failed));
                }
                break;
            case CODE_HINT:
                if (result != null) {
                    mPresenter.getUserInfoWithCode(result);
                } else {
                    showToast(getString(R.string.enter_qrcode_error));
                }
                break;
            default:
                break;
        }
    }

    private SpeechSynthesizer getSynthesizer() {
        return ((MainActivity) getActivity()).speechSynthesizer;
    }

    @Override
    public void onUpdateUserInfoError(Throwable e) {
        onHandleError(e);
    }
}