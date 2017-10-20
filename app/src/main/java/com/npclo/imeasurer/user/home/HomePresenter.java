package com.npclo.imeasurer.user.home;


import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.support.annotation.NonNull;

import com.npclo.imeasurer.data.app.AppRepository;
import com.npclo.imeasurer.utils.schedulers.SchedulerProvider;
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
 * Created by Endless on 2017/8/24.
 */

public class HomePresenter implements HomeContract.Presenter {
    private static final String TAG = HomePresenter.class.getSimpleName();
    @NonNull
    private final HomeContract.View fragment;
    @NonNull
    private SchedulerProvider mSchedulerProvider;
    @NonNull
    private RxBleClient rxBleClient;
    @NonNull
    private CompositeSubscription mSubscription;
    private RxBleDevice bleDevice;
    private UUID characteristicUUID;
    private PublishSubject<Void> disconnectTriggerSubject = PublishSubject.create();
    private Observable<RxBleConnection> connectionObservable;
    private Subscription scanSubscribe;


    public HomePresenter(@NonNull RxBleClient client, @NonNull HomeContract.View view,
                         @NonNull SchedulerProvider schedulerProvider) {
        rxBleClient = checkNotNull(client);
        fragment = checkNotNull(view);
        mSchedulerProvider = checkNotNull(schedulerProvider);
        mSubscription = new CompositeSubscription();
        fragment.setPresenter(this);
    }

    @Override
    public void subscribe() {

    }

    @Override
    public void unsubscribe() {
        mSubscription.clear();
    }

    @Override
    public void logout() {
        fragment.logout();
    }

    private Observable<RxBleConnection> prepareConnectionObservable() {
        checkNotNull(bleDevice);
        return bleDevice
                .establishConnection(false)
                .takeUntil(disconnectTriggerSubject)
                .compose(new ConnectionSharingAdapter());
    }

    private void handleError(Throwable e) {
        if (e instanceof BleScanException) {
            fragment.handleBleScanException((BleScanException) e);
        } else if (e instanceof BleAlreadyConnectedException) {
            fragment.showError("重复连接，请检查");
        } else {
            fragment.handleError(e);
        }
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

    public void connectDevice(String s) {
        try {
            if (scanSubscribe != null || scanSubscribe.isUnsubscribed()) {
                scanSubscribe.unsubscribe();
                fragment.closeScanResultDialog();
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
                                characteristicUUID = characteristic.getUuid();
                                connectionObservable = prepareConnectionObservable();
                                fragment.setNotificationUUID(characteristicUUID);
                                fragment.setBleAddress(s);
                                connectDevice();
                                break;
                            }
                        }
                    }
                }, this::handleError);
    }

    private void connectDevice() {
        if (isConnected()) {
            triggerDisconnect();
        } else {
            connectionObservable
                    .flatMap(RxBleConnection::discoverServices)
                    .flatMap(rxBleDeviceServices -> rxBleDeviceServices.getCharacteristic(characteristicUUID))
                    .observeOn(mSchedulerProvider.ui())
                    .doOnSubscribe(this::connecting)
                    .subscribe(c -> fragment.showConnected(bleDevice), this::handleError);
        }
    }

    private void connecting() {
        fragment.isConnecting();
    }

    public void startScan() {
        scanSubscribe = rxBleClient.scanBleDevices(new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                        .build(),
                new ScanFilter.Builder().build())
                .observeOn(mSchedulerProvider.ui())
                .doOnSubscribe(this::scanning)
                .doOnUnsubscribe(this::clearSubscription)
                .subscribe(this::handleResult, this::handleError);
        mSubscription.add(scanSubscribe);
    }

    private void handleResult(ScanResult scanResult) {
        fragment.handleScanResult(scanResult);
    }

    private void clearSubscription() {

    }

    private void scanning() {
        fragment.showScanning();
    }

    @Override
    public void checkVersion() {
        new AppRepository()
                .getLatestVersion()
                .subscribeOn(mSchedulerProvider.computation())
                .observeOn(mSchedulerProvider.ui())
                .subscribe(fragment::showGetVersionSuccess,
                        fragment::showGetVersionError);
    }
}