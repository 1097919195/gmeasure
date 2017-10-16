package com.npclo.gdemo.main.home;

import com.npclo.gdemo.base.BasePresenter;
import com.npclo.gdemo.base.BaseView;
import com.npclo.gdemo.data.quality.QualityItem;
import com.polidea.rxandroidble.RxBleConnection;
import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.exceptions.BleScanException;
import com.polidea.rxandroidble.scan.ScanResult;

import java.util.UUID;

import rx.Observable;

public interface HomeContract {
    interface Presenter extends BasePresenter {
        void connectDevice(String s);

        void startScan();

        void getQualityItemInfoWithId(String result);

        void getQualityItemInfoWithCode(String result);
    }

    interface View extends BaseView<Presenter> {

        void showLoading(boolean b);

        void handleBleScanException(BleScanException e);

        void showError(String s);

        void showError();

        void showConnected(RxBleDevice bleDevice);

        void isConnecting();

        void setLoadingIndicator(boolean bool);

        void showScanning();

        void handleScanResult(ScanResult scanResult);

        void closeScanResultDialog();

        void setNotificationInfo(UUID characteristicUUID, Observable<RxBleConnection> connectionObservable);

        void handleError(Throwable e);

        void handleQualityItemResult(QualityItem item);
    }
}