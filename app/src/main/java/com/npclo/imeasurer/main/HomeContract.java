package com.npclo.imeasurer.main;

import com.npclo.imeasurer.base.BasePresenter;
import com.npclo.imeasurer.base.BaseView;
import com.npclo.imeasurer.data.App;
import com.npclo.imeasurer.data.WechatUser;
import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.exceptions.BleScanException;
import com.polidea.rxandroidble.scan.ScanResult;

import java.util.UUID;

public interface HomeContract {
    interface Presenter extends BasePresenter {

        void getUserInfoWithCode(String result, String uid);

        void getUserInfoWithOpenID(String result, String uid);

        void autoGetLatestVersion();

        void logout();

        void startScan();

        void connectDevice(String s);

        void manuallyGetLatestVersion();
    }

    interface View extends BaseView<Presenter> {

        void showLoading(boolean b);

        void onGetWechatUserInfo(WechatUser info);

        void showGetInfoError(Throwable e);

        void showCompleteGetInfo();

        void onGetVersionInfo(App app);

        void onGetVersionError(Throwable e);

        void onLogout();

        void onHandleScanResult(ScanResult scanResult);

        void onShowError(String s);

        void onHandleBleScanException(BleScanException e);

        void onSetNotificationUUID(UUID characteristicUUID);

        void onDeviceChoose(RxBleDevice bleDevice);

        void onCloseScanResultDialog();
    }
}