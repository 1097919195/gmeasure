package com.npclo.imeasurer.main;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.npclo.imeasurer.utils.http.app.AppRepository;
import com.npclo.imeasurer.utils.http.user.UserRepository;
import com.npclo.imeasurer.utils.schedulers.BaseSchedulerProvider;
import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.RxBleConnection;
import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.exceptions.BleAlreadyConnectedException;
import com.polidea.rxandroidble.exceptions.BleScanException;
import com.polidea.rxandroidble.scan.ScanFilter;
import com.polidea.rxandroidble.scan.ScanResult;
import com.polidea.rxandroidble.scan.ScanSettings;
import com.polidea.rxandroidble.utils.ConnectionSharingAdapter;

import java.util.UUID;

import rx.Observable;
import rx.Subscription;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

/**
 * @author Endless
 * @date 2017/9/1
 */

public class HomePresenter implements HomeContract.Presenter {
    @NonNull
    private HomeContract.View fragment;
    @NonNull
    private RxBleClient rxBleClient;
    @NonNull
    private final BaseSchedulerProvider mSchedulerProvider;
    @NonNull
    private CompositeSubscription mSubscriptions;
    private RxBleDevice bleDevice;
    private PublishSubject<Void> disconnectTriggerSubject = PublishSubject.create();
    private Subscription scanSubscribe;

    public HomePresenter(@NonNull RxBleClient client, @NonNull HomeContract.View view, @NonNull BaseSchedulerProvider schedulerProvider) {
        fragment = checkNotNull(view);
        rxBleClient = checkNotNull(client);
        mSchedulerProvider = checkNotNull(schedulerProvider);
        mSubscriptions = new CompositeSubscription();
        fragment.setPresenter(this);
    }

    @Override
    public void subscribe() {
        // TODO: 2017/12/5 获取用户最新量体统计数据   使用token
        autoGetLatestVersion();
    }

    @Override
    public void autoGetLatestVersion() {
        Subscription subscribe = new AppRepository()
                .getLatestVersion()
                .subscribeOn(mSchedulerProvider.io())
                .observeOn(mSchedulerProvider.ui())
                .subscribe(app -> fragment.onGetVersionInfo(app),
                        e -> fragment.onGetVersionError(e));
        mSubscriptions.add(subscribe);
    }

    @Override
    public void logout() {
        fragment.onLogout();
    }

    @Override
    public void startScan() {
        // TODO: 2017/12/5 筛选特定名字的蓝牙设备
        scanSubscribe = rxBleClient.scanBleDevices(new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .build(), new ScanFilter.Builder().build())
                //蓝牙名不为空
                .filter(s -> !TextUtils.isEmpty(s.getBleDevice().getName()))
                .observeOn(mSchedulerProvider.ui())
                .doOnSubscribe(this::onScanning)
                .doOnUnsubscribe(this::onClearSubscription)
                .subscribe(this::onHandleScanResult, this::onHandleScanError);
        mSubscriptions.add(scanSubscribe);
    }

    private void onHandleScanResult(ScanResult scanResult) {
        fragment.onHandleScanResult(scanResult);
    }

    private void onHandleScanError(Throwable e) {
        if (e instanceof BleScanException) {
            fragment.onHandleBleScanException((BleScanException) e);
        } else if (e instanceof BleAlreadyConnectedException) {
            fragment.onShowError("重复连接，请检查");
        } else {
            fragment.onHandleError(e);
        }
    }

    private void onClearSubscription() {
    }

    private void onScanning() {
        fragment.showLoading(true);
    }

    @Override
    public void unsubscribe() {
        mSubscriptions.clear();
    }

    @Override
    public void getUserInfoWithCode(String result, String uid) {
        Subscription subscribe = new UserRepository()
                .getUserInfoWithCode(result, uid)
                .subscribeOn(mSchedulerProvider.io())
                .observeOn(mSchedulerProvider.ui())
                .doOnSubscribe(() -> fragment.showLoading(true))
                .subscribe(
                        user -> fragment.onGetWechatUserInfo(user),
                        e -> fragment.showGetInfoError(e),
                        () -> fragment.showCompleteGetInfo());
        mSubscriptions.add(subscribe);
    }

    @Override
    public void getUserInfoWithOpenID(String oid, String uid) {
        Subscription subscribe = new UserRepository()
                .getUserInfoWithOpenID(oid, uid)
                .subscribeOn(mSchedulerProvider.io())
                .observeOn(mSchedulerProvider.ui())
                .doOnSubscribe(() -> fragment.showLoading(true))
                .subscribe(
                        user -> fragment.onGetWechatUserInfo(user),
                        e -> fragment.showGetInfoError(e),
                        () -> fragment.showCompleteGetInfo());
        mSubscriptions.add(subscribe);
    }

    private Observable<RxBleConnection> prepareConnectionObservable() {
        checkNotNull(bleDevice);
        return bleDevice
                .establishConnection(false)
                .takeUntil(disconnectTriggerSubject)
                .compose(new ConnectionSharingAdapter());
    }

    private void triggerDisconnect() {
        disconnectTriggerSubject.onNext(null);
    }

    private boolean isCharacteristicNotifiable(BluetoothGattCharacteristic characteristic) {
        return (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0;
    }

    private boolean isConnected() {
        checkNotNull(bleDevice);
        return bleDevice.getConnectionState() == RxBleConnection.RxBleConnectionState.CONNECTED;
    }

    @Override
    public void connectDevice(String s) {
        try {
            if (scanSubscribe != null || scanSubscribe.isUnsubscribed()) {
                scanSubscribe.unsubscribe();
                fragment.onCloseScanResultDialog();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        bleDevice = rxBleClient.getBleDevice(s);
        bleDevice.establishConnection(false)
                .flatMap(RxBleConnection::discoverServices)
                .first() // Disconnect automatically after discovery
                .observeOn(mSchedulerProvider.ui())
                .subscribe(deviceServices -> {
                    for (BluetoothGattService service : deviceServices.getBluetoothGattServices()) {
                        for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                            if (isCharacteristicNotifiable(characteristic)) {
                                UUID uuid = characteristic.getUuid();
                                fragment.onSetNotificationUUID(uuid);
//                                connectDevice(uuid);
                                fragment.onDeviceChoose(bleDevice);
                                break;
                            }
                        }
                    }
                }, this::onHandleConnectError);
    }

    @Override
    public void manuallyGetLatestVersion() {
        Subscription subscribe = new AppRepository()
                .getLatestVersion()
                .subscribeOn(mSchedulerProvider.io())
                .observeOn(mSchedulerProvider.ui())
                .doOnSubscribe(() -> fragment.showLoading(true))
                .subscribe(app -> fragment.onGetVersionInfo(app),
                        e -> fragment.onGetVersionError(e), () -> fragment.showLoading(false));
        mSubscriptions.add(subscribe);
    }

    private void onHandleConnectError(Throwable e) {
        fragment.onHandleError(e);
    }

    private void connectDevice(UUID uuid) {
        Observable<RxBleConnection> connectionObservable = prepareConnectionObservable();
        if (isConnected()) {
            triggerDisconnect();
        } else {
            connectionObservable
                    .flatMap(RxBleConnection::discoverServices)
                    .flatMap(rxBleDeviceServices -> rxBleDeviceServices.getCharacteristic(uuid))
                    .observeOn(mSchedulerProvider.ui())
                    .doOnSubscribe(this::connecting)
                    .subscribe(c -> fragment.onDeviceChoose(bleDevice), this::onHandleConnectError);
        }
    }

    private void connecting() {
        fragment.showLoading(true);
    }
}