package com.npclo.gdemo.main.home;

import com.npclo.gdemo.base.BasePresenter;
import com.npclo.gdemo.base.BaseView;
import com.npclo.gdemo.data.quality.QualityItem;
import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.exceptions.BleScanException;
import com.polidea.rxandroidble.scan.ScanResult;

import java.util.UUID;

public interface HomeContract {
    interface Presenter extends BasePresenter {
        void chooseDeviceWithAddress(String s);

        void startScan();

        void getQualityItemInfoWithId(String result);

        void getQualityItemInfoWithCode(String result);
    }

    interface View extends BaseView<Presenter> {

        void handleBleScanException(BleScanException e);

        void showChoose(RxBleDevice bleDevice);

        void showScanning();

        void handleScanResult(ScanResult scanResult);

        void closeScanResultDialog();

        void setCharacteristicUUID(UUID characteristicUUID);

        void handleError(Throwable e);

        void handleQualityItemResult(QualityItem item);

        void setBleAddress(String address);
    }
}