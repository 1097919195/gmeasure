package com.npclo.gdemo.main.home;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.npclo.gdemo.R;
import com.npclo.gdemo.base.BaseFragment;
import com.npclo.gdemo.camera.CaptureActivity;
import com.npclo.gdemo.data.ble.BleDevice;
import com.npclo.gdemo.main.MainActivity;
import com.npclo.imeasurer.data.BleDevice;
import com.polidea.rxandroidble.RxBleConnection;
import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.exceptions.BleScanException;
import com.polidea.rxandroidble.scan.ScanResult;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import rx.Observable;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

public class HomeFragment extends BaseFragment implements HomeContract.View {
    public static final int REQUEST_CODE = 1001;
    @BindView(R.id.base_toolbar)
    Toolbar toolbarBase;
    @BindView(R.id.btn_ble)
    AppCompatButton btnBle;
    @BindView(R.id.btn_measure)
    AppCompatButton btnMeasure;
    @BindView(R.id.btn_quality)
    AppCompatButton btnQuality;
    Unbinder unbinder;
    private static final String TAG = HomeFragment.class.getSimpleName();
    private HomeContract.Presenter mPresenter;
    private MaterialDialog dialog;
    private static final int SCAN_HINT = 1001;
    private static final int CODE_HINT = 1002;
    private ScanResultsAdapter scanResultsAdapter;
    private List<BleDevice> bleDeviceList = new ArrayList<>();
    private List<String> rxBleDeviceAddressList = new ArrayList<>();
    private MaterialDialog.Builder scanResultDialog;
    private MaterialDialog connectingProgressBar;
    private MaterialDialog scanningProgressBar;
    private MaterialDialog resultDialog;

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
        initToolbar();
        configureResultList();
        initBleState();
    }

    private void initBleState() {
        RxBleDevice rxBleDevice = ((MainActivity) getActivity()).getRxBleDevice();
        if (rxBleDevice != null && rxBleDevice.getConnectionState() == RxBleConnection.RxBleConnectionState.CONNECTED) {
            toolbarBase.getMenu().getItem(0).setIcon(R.drawable.ble_connected);
            btnBle.setText(getString(R.string.connected));
            btnBle.setEnabled(false);
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
        toolbarBase.setTitleTextColor(getResources().getColor(R.color.toolbar_text));//设置主标题颜色
        toolbarBase.inflateMenu(R.menu.base_toolbar_menu);
        toolbarBase.getMenu().getItem(0).setIcon(R.drawable.ble_disconnected);
    }

    @Override
    public void onPause() {
        super.onPause();
        mPresenter.unsubscribe();
    }

    @Override
    public void showLoading(boolean b) {
        if (b) {
            dialog = new MaterialDialog.Builder(getActivity())
                    .progress(true, 100)
                    .backgroundColor(getResources().getColor(R.color.white))
                    .show();
        } else {
            dialog.dismiss();
        }
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
                    mPresenter.connectDevice(s);
                }
        );
    }

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
    public void showError(String s) {
        showToast(s);
    }

    @Override
    public void handleError(Throwable e) {
        handleError(e, TAG);
    }

    @Override
    public void showError() {
        showToast(getString(R.string.unKnownError));
    }

    @Override
    public void showConnected(RxBleDevice bleDevice) {
        connectingProgressBar.dismiss();
        showToast(getString(R.string.device_connected));
        ((MainActivity) getActivity()).speechSynthesizer.playText("蓝牙连接成功");
        ((MainActivity) getActivity()).setRxBleDevice(bleDevice);
        toolbarBase.getMenu().getItem(0).setIcon(R.drawable.ble_connected);
        btnBle.setText(getString(R.string.connected));
        btnBle.setEnabled(false);
    }

    @Override
    public void setNotificationInfo(UUID characteristicUUID, Observable<RxBleConnection> connectionObservable) {
        ((MainActivity) getActivity()).setCharacteristicUUID(characteristicUUID);
        ((MainActivity) getActivity()).setConnectionObservable(connectionObservable);
    }

    @Override
    public void isConnecting() {
        connectingProgressBar = new MaterialDialog.Builder(getActivity())
                .title(getString(R.string.connecting))
                .titleColor(getResources().getColor(R.color.ff5001))
                .backgroundColor(getResources().getColor(R.color.white))
                .progress(true, 100)
                .show();
    }

    @Override
    public void setLoadingIndicator(boolean bool) {

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

    @OnClick({R.id.btn_ble, R.id.btn_quality, R.id.btn_measure})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_ble:
                BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
                if (!defaultAdapter.isEnabled()) {
                    new MaterialDialog.Builder(getActivity())
                            .content(getString(R.string.can_open_ble))
                            .positiveText(getString(R.string.open))
                            .negativeText(getString(R.string.cancel))
                            .backgroundColor(getResources().getColor(R.color.white))
                            .contentColor(getResources().getColor(R.color.primary))
                            .onPositive((dialog, which) -> defaultAdapter.enable())
                            .show();
                }
                ((MainActivity) getActivity()).getRxPermissions()
                        .request(Manifest.permission.ACCESS_COARSE_LOCATION)
                        .subscribe(grant -> {
                            if (grant) {
                                mPresenter.startScan();
                            } else {
                                showToast(getString(R.string.unauthorized_location));
                            }
                        });
                break;
            case R.id.btn_measure:
                break;
            case R.id.btn_quality:
                Intent intent = new Intent(getActivity(), CaptureActivity.class);
                startActivityForResult(intent, 1001);
                break;
            default:
                break;
        }
    }
}