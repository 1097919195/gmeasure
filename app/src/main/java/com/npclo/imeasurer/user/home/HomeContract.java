package com.npclo.imeasurer.user.home;

import com.npclo.imeasurer.base.BasePresenter;
import com.npclo.imeasurer.base.BaseView;
import com.npclo.imeasurer.data.app.App;
import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.exceptions.BleScanException;
import com.polidea.rxandroidble.scan.ScanResult;

import java.util.UUID;

/**
 * Created by Endless on 2017/8/24.
 */

public interface HomeContract {
    interface View extends BaseView<Presenter> {

        void logout();

        void handleBleScanException(BleScanException e);

        void showError(String s);

        void showError();

        void showConnected(RxBleDevice bleDevice);

        void isConnecting();

        void setLoadingIndicator(boolean bool);

        void showScanning();

        void handleScanResult(ScanResult scanResult);

        void closeScanResultDialog();

        void setNotificationUUID(UUID characteristicUUID);

        void showGetVersionSuccess(App app);

        void showGetVersionError(Throwable e);

        void setBleAddress(String macAddress);
    }

    interface Presenter extends BasePresenter {
        void logout();

        void connectDevice(String s);

        void startScan();

        void checkVersion();
    }
}
