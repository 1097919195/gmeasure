package com.npclo.gdemo.main.home;

import android.content.Intent;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.npclo.gdemo.R;
import com.npclo.gdemo.base.BaseApplication;
import com.npclo.gdemo.base.BaseFragment;
import com.npclo.gdemo.camera.CaptureActivity;
import com.npclo.gdemo.data.ble.BleDevice;
import com.polidea.rxandroidble.RxBleConnection;
import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.exceptions.BleScanException;
import com.polidea.rxandroidble.scan.ScanResult;
import com.unisound.client.SpeechConstants;
import com.unisound.client.SpeechSynthesizer;
import com.unisound.client.SpeechSynthesizerListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

public class HomeFragment extends BaseFragment implements HomeContract.View {
    public static final int REQUEST_CODE = 1001;
    @BindView(R.id.base_toolbar)
    Toolbar toolbarBase;
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
    private SpeechSynthesizer speechSynthesizer;
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

    private void startScan() {
        Intent intent = new Intent(getActivity(), CaptureActivity.class);
        startActivityForResult(intent, REQUEST_CODE);
    }

    /**
     * 初始化toolbar的一些默认属性
     */
    protected void initToolbar() {
        toolbarBase.setTitleTextColor(getResources().getColor(R.color.toolbar_text));//设置主标题颜色
    }

    @Override
    public void onResume() {
        super.onResume();

        if (BaseApplication.getFirstCheckHint(getActivity())) {
            BaseApplication.setIsFirstCheck(getActivity());
        }
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
        ;
        //选择目的蓝牙设备
        scanResultsAdapter.setOnAdapterItemClickListener(v -> {
                    String s = ((TextView) v.findViewById(R.id.txt_mac)).getText().toString();
                    mPresenter.connectDevice(s);
                }
        );
    }

    private void initSpeech() {
        String APPKEY = "hhzjkm3l5akcz5oiflyzmmmitzrhmsfd73lyl3y2";
        String APPSECRET = "29aa998c451d64d9334269546a4021b8";
        if (speechSynthesizer == null)
            speechSynthesizer = new SpeechSynthesizer(getActivity(), APPKEY, APPSECRET);
        speechSynthesizer.setOption(SpeechConstants.TTS_SERVICE_MODE, SpeechConstants.TTS_SERVICE_MODE_NET);
        speechSynthesizer.setTTSListener(new SpeechSynthesizerListener() {
            @Override
            public void onEvent(int i) {

            }

            @Override
            public void onError(int i, String s) {

            }
        });
        speechSynthesizer.init(null);// FIXME: 2017/8/24 语音播报需要联网
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
    public void showError() {
        showToast(getString(R.string.unKnownError));
    }

    @Override
    public void showConnected(RxBleDevice bleDevice) {
        connectingProgressBar.dismiss();
        showToast(getString(R.string.device_connected));
        speechSynthesizer.playText("蓝牙连接成功");
        BaseApplication.setRxBleDevice(getActivity(), bleDevice);
    }


    @Override
    public void setNotificationInfo(UUID characteristicUUID, Observable<RxBleConnection> connectionObservable) {
        BaseApplication.setNotificationInfo(getActivity(), characteristicUUID, connectionObservable);
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
}