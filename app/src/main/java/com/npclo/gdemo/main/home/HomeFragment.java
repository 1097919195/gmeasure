package com.npclo.gdemo.main.home;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.npclo.gdemo.R;
import com.npclo.gdemo.base.BaseApplication;
import com.npclo.gdemo.base.BaseFragment;
import com.npclo.gdemo.camera.CaptureActivity;
import com.npclo.gdemo.data.BleDevice;
import com.npclo.gdemo.data.quality.QualityItem;
import com.npclo.gdemo.main.MainActivity;
import com.npclo.gdemo.main.quality.QualityFragment;
import com.npclo.gdemo.main.quality.QualityPresenter;
import com.npclo.gdemo.utils.Gog;
import com.npclo.gdemo.utils.schedulers.SchedulerProvider;
import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.RxBleConnection;
import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.exceptions.BleAlreadyConnectedException;
import com.polidea.rxandroidble.exceptions.BleScanException;
import com.polidea.rxandroidble.scan.ScanResult;

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
    @BindView(R.id.base_toolbar)
    Toolbar toolbarBase;
    @BindView(R.id.btn_quality)
    AppCompatButton btnQuality;
    Unbinder unbinder;
    private HomeContract.Presenter mPresenter;
    private static final int SCAN_HINT = 1001;
    private static final int CODE_HINT = 1002;
    private ScanResultsAdapter scanResultsAdapter;
    private List<BleDevice> bleDeviceList = new ArrayList<>();
    private List<String> rxBleDeviceAddressList = new ArrayList<>();
    private MaterialDialog.Builder scanResultDialog;
    private MaterialDialog scanningProgressBar;
    private MaterialDialog resultDialog;
    private MenuItem itemBle;
    private RxBleClient rxBleClient;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.frag_home;
    }

    @Override
    protected void initView(View mRootView) {
        unbinder = ButterKnife.bind(this, mRootView);
        rxBleClient = BaseApplication.getRxBleClient(getActivity());
        initToolbar();
        configureResultList();
    }

    private void initBleState() {
        // FIXME: 2017/11/12 判断状态的调用地方
        String macAddress = BaseApplication.getMacAddress(getActivity());
        if (!TextUtils.isEmpty(macAddress)) {
            RxBleDevice rxBleDevice = rxBleClient.getBleDevice(macAddress);
            if (rxBleDevice != null && rxBleDevice.getConnectionState() == RxBleConnection.RxBleConnectionState.CONNECTED) {
                itemBle.setIcon(R.drawable.ble_connected);
            }
        }
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

    /**
     * 初始化toolbar的一些默认属性
     */
    protected void initToolbar() {
        toolbarBase.setTitleTextColor(getResources().getColor(R.color.toolbar_text));
        toolbarBase.inflateMenu(R.menu.base_toolbar_menu);
        itemBle = toolbarBase.getMenu().getItem(0);
        itemBle.setIcon(R.drawable.ble_disconnected);
    }

    @Override
    public void onResume() {
        super.onResume();
        initBleState();
        itemBle.setOnMenuItemClickListener(view -> {
            String macAddress = BaseApplication.getMacAddress(getActivity());
            if (TextUtils.isEmpty(macAddress)) {
                scanAndConnectBle();
            }
            return true;
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        mPresenter.unsubscribe();
    }

    private void configureResultList() {
        scanResultsAdapter = new ScanResultsAdapter(this, bleDeviceList);
        scanResultDialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.choose_device_prompt)
                .backgroundColor(getResources().getColor(R.color.white))
                .titleColor(getResources().getColor(R.color.scan_result_list_title))
                .dividerColor(getResources().getColor(R.color.divider))
                .adapter(scanResultsAdapter, null);

        //选择目的蓝牙设备
        scanResultsAdapter.setOnAdapterItemClickListener(v -> {
                    String s = ((TextView) v.findViewById(R.id.txt_mac)).getText().toString();
            mPresenter.chooseDeviceWithAddress(s);
                }
        );
    }

    @Override
    public void handleBleScanException(BleScanException bleScanException) {

        switch (bleScanException.getReason()) {
            case BleScanException.BLUETOOTH_NOT_AVAILABLE:
                Toast.makeText(getActivity(), getString(R.string.bluetooth_not_avavilable), Toast.LENGTH_SHORT).show();
                break;
            case BleScanException.BLUETOOTH_DISABLED:
                Toast.makeText(getActivity(), getString(R.string.bluetooth_disabled), Toast.LENGTH_SHORT).show();
                break;
            case BleScanException.LOCATION_PERMISSION_MISSING:
                Toast.makeText(getActivity(),
                        "On Android 6.0 location permission is required. Implement Runtime Permissions", Toast.LENGTH_SHORT).show();
                break;
            case BleScanException.LOCATION_SERVICES_DISABLED:
                Toast.makeText(getActivity(), "Location services needs to be enabled on Android 6.0", Toast.LENGTH_SHORT).show();
                break;
            case BleScanException.SCAN_FAILED_ALREADY_STARTED:
                Toast.makeText(getActivity(), "Scan with the same filters is already started", Toast.LENGTH_SHORT).show();
                break;
            case BleScanException.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED:
                Toast.makeText(getActivity(), "Failed to register application for bluetooth scan", Toast.LENGTH_SHORT).show();
                break;
            case BleScanException.SCAN_FAILED_FEATURE_UNSUPPORTED:
                Toast.makeText(getActivity(), "Scan with specified parameters is not supported", Toast.LENGTH_SHORT).show();
                break;
            case BleScanException.SCAN_FAILED_INTERNAL_ERROR:
                Toast.makeText(getActivity(), "Scan failed due to internal error", Toast.LENGTH_SHORT).show();
                break;
            case BleScanException.SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES:
                Toast.makeText(getActivity(), "Scan cannot start due to limited hardware resources", Toast.LENGTH_SHORT).show();
                break;
            case BleScanException.UNKNOWN_ERROR_CODE:
            case BleScanException.BLUETOOTH_CANNOT_START:
            default:
                Toast.makeText(getActivity(), "Unable to start scanning", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void handleScanResult(ScanResult result) {
        if (scanningProgressBar != null) {
            scanningProgressBar.dismiss();
            scanningProgressBar = null;
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
    public void handleError(Throwable e) {
        Gog.e(e.getMessage());
        if (e instanceof BleScanException) {
            handleBleScanException((BleScanException) e);
        } else if (e instanceof BleAlreadyConnectedException) {
            showToast("重复连接，请检查");
        } else {
            super.handleError(e);
        }
    }

    @Override
    public void showChoose(RxBleDevice bleDevice) {
        showToast(getString(R.string.device_connected));
        ((MainActivity) getActivity()).speechSynthesizer.playText("蓝牙连接成功");
        itemBle.setIcon(R.drawable.ble_connected);
        itemBle.setCheckable(false);
    }

    @Override
    public void setCharacteristicUUID(UUID characteristicUUID) {
        BaseApplication.setNotificationUUID(getActivity(), characteristicUUID);
    }

    @Override
    public void showScanning() {
        scanningProgressBar = new MaterialDialog.Builder(getActivity())
                .backgroundColor(getResources().getColor(R.color.white))
                .progress(true, 100)
                .show();
    }

    @Override
    public void closeScanResultDialog() {
        try {
            if (resultDialog != null || resultDialog.isShowing()) {
                resultDialog.dismiss();
                resultDialog = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.btn_quality)
    public void onViewClicked(View view) {
        Intent intent = new Intent(getActivity(), CaptureActivity.class);
        startActivityForResult(intent, 1001);
    }

    private void scanAndConnectBle() {
        BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        //判断蓝牙是否打开
        if (!defaultAdapter.isEnabled()) {
            new MaterialDialog.Builder(getActivity())
                    .content(getString(R.string.can_open_ble))
                    .positiveText(getString(R.string.open))
                    .negativeText(getString(R.string.cancel))
                    .backgroundColor(getResources().getColor(R.color.white))
                    .contentColor(getResources().getColor(R.color.primary))
                    .onPositive((dialog, which) -> defaultAdapter.enable())
                    .show();
        } else {
            ((MainActivity) getActivity()).getRxPermissions()
                    .request(Manifest.permission.ACCESS_COARSE_LOCATION)
                    .subscribe(grant -> {
                        if (grant) {
                            mPresenter.startScan();
                        } else {
                            showToast(getString(R.string.unauthorized_location));
                        }
                    });
        }
    }

    @Override
    public void handleQualityItemResult(QualityItem item) {
        QualityFragment qualityFragment = QualityFragment.newInstance();
        Bundle bundle = new Bundle();
        bundle.putParcelable("item", item);
        qualityFragment.setArguments(bundle);
        qualityFragment.setPresenter(new QualityPresenter(qualityFragment, SchedulerProvider.getInstance()));
        start(qualityFragment);
    }

    @Override
    public void setBleAddress(String address) {
        BaseApplication.setBleAddress(getActivity(), address);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String result = null;
        try {
            Bundle bundle = data.getExtras();
            result = bundle.getString("result");
        } catch (Exception e) {
            e.printStackTrace();
        }
        switch (resultCode) {
            case SCAN_HINT:
                if (result != null) {
                    mPresenter.getQualityItemInfoWithId(result);
                } else {
                    showToast(getString(R.string.scan_qrcode_failed));
                }
                break;
            case CODE_HINT:
                if (result != null) {
                    mPresenter.getQualityItemInfoWithId(result);
                } else {
                    showToast(getString(R.string.enter_qrcode_error));
                }
                break;
            default:
                break;
        }
    }
}