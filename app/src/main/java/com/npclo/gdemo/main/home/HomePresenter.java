package com.npclo.gdemo.main.home;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.support.annotation.NonNull;

import com.npclo.gdemo.utils.http.DemoHelper;
import com.npclo.gdemo.utils.schedulers.BaseSchedulerProvider;
import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.RxBleConnection;
import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.scan.ScanFilter;
import com.polidea.rxandroidble.scan.ScanResult;
import com.polidea.rxandroidble.scan.ScanSettings;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

/**
 * @author Endless
 */
public class HomePresenter implements HomeContract.Presenter {
    @NonNull
    private HomeContract.View fragment;
    @NonNull
    private BaseSchedulerProvider mSchedulerProvider;
    @NonNull
    private RxBleClient rxBleClient;
    @NonNull
    private CompositeSubscription mSubscription;
    private Subscription scanSubscribe;

    public HomePresenter(@NonNull RxBleClient client, @NonNull HomeContract.View view,
                         @NonNull BaseSchedulerProvider schedulerProvider) {
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

    private void handleError(Throwable e) {
        fragment.handleError(e);
    }

    private boolean isCharacteristicNotifiable(BluetoothGattCharacteristic characteristic) {
        return (characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0;
    }


    /**
     * 根据macAddress连接设备
     *
     * @param s 设备地址
     */
    @Override
    public void chooseDeviceWithAddress(String s) {
        try {
            if (scanSubscribe != null || scanSubscribe.isUnsubscribed()) {
                scanSubscribe.unsubscribe();
                fragment.closeScanResultDialog();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        RxBleDevice bleDevice = rxBleClient.getBleDevice(s);
        bleDevice.establishConnection(false)
                .flatMap(RxBleConnection::discoverServices)
                .first() // Disconnect automatically after discovery
                .observeOn(mSchedulerProvider.ui())
                .subscribe(deviceServices -> {
                    for (BluetoothGattService service : deviceServices.getBluetoothGattServices()) {
                        for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                            if (isCharacteristicNotifiable(characteristic)) {
                                fragment.setCharacteristicUUID(characteristic.getUuid());
                                fragment.setBleAddress(s);
                                fragment.showChoose(bleDevice);
                                break;
                            }
                        }
                    }
                }, this::handleError);
    }

    @Override
    public void startScan() {
        scanSubscribe = rxBleClient.scanBleDevices(new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                        .build(),
                new ScanFilter.Builder().build())
                .observeOn(mSchedulerProvider.ui())
                .doOnSubscribe(this::scanning)
                .subscribe(this::handleScanResult, this::handleError);
        mSubscription.add(scanSubscribe);
    }

    private void handleScanResult(ScanResult scanResult) {
        fragment.handleScanResult(scanResult);
    }

    private void scanning() {
        fragment.showScanning();
    }

    @Override
    public void getQualityItemInfoWithId(String id) {
        Subscription subscribe = new DemoHelper().getQualityItemWithId(id)
                .subscribeOn(mSchedulerProvider.io())
                .observeOn(mSchedulerProvider.ui())
                .subscribe(
                        item -> fragment.handleQualityItemResult(item), e -> fragment.handleError(e));
        mSubscription.add(subscribe);
    }

    @Override
    public void getQualityItemInfoWithCode(String code) {
        Subscription subscribe = new DemoHelper().getQualityItemWithCode(code)
                .subscribeOn(mSchedulerProvider.io())
                .subscribe(item -> fragment.handleQualityItemResult(item), e -> fragment.handleError(e));
        mSubscription.add(subscribe);
    }
}