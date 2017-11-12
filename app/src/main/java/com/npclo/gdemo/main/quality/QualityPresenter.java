package com.npclo.gdemo.main.quality;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.npclo.gdemo.base.BaseApplication;
import com.npclo.gdemo.data.quality.QualityItem;
import com.npclo.gdemo.utils.HexString;
import com.npclo.gdemo.utils.aes.AesException;
import com.npclo.gdemo.utils.aes.AesUtils;
import com.npclo.gdemo.utils.http.DemoHelper;
import com.npclo.gdemo.utils.schedulers.BaseSchedulerProvider;
import com.polidea.rxandroidble.RxBleConnection;
import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.utils.ConnectionSharingAdapter;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

/**
 * @author Endless
 */
public class QualityPresenter implements QualityContract.Presenter {
    public static final int STANDARD_LENGTH = 16;
    public static final int ADJUST_VALUE = 14;
    private static final int MEASURE_DURATION = 500;
    @NonNull
    private CompositeSubscription mSubscription;
    @NonNull
    private QualityContract.View fragment;
    @NonNull
    private BaseSchedulerProvider mSchedulerProvider;
    private AesUtils aesUtils;
    private RxBleDevice device;
    private PublishSubject<Void> disconnectTriggerSubject = PublishSubject.create();
    private UUID uuid;

    public QualityPresenter(@NonNull QualityFragment qualityFragment, @NonNull BaseSchedulerProvider schedulerProvider) {
        mSubscription = new CompositeSubscription();
        fragment = checkNotNull(qualityFragment);
        this.mSchedulerProvider = schedulerProvider;
        fragment.setPresenter(this);
    }

    @Override
    public void subscribe() {
        startMeasure();
    }

    @Override
    public void unsubscribe() {
        mSubscription.clear();
    }

    @Override
    public void getQualityItemInfoWithId(String id) {
        Subscription subscribe = new DemoHelper().getQualityItemWithId(id)
                .subscribeOn(mSchedulerProvider.io())
                .observeOn(mSchedulerProvider.ui())
                .subscribe(item -> fragment.handleQualityItemResult(item),
                        e -> fragment.handleError(e));
        mSubscription.add(subscribe);
    }

    @Override
    public void getQualityItemInfoWithCode(String code) {
        Subscription subscribe = new DemoHelper().getQualityItemWithCode(code)
                .subscribeOn(mSchedulerProvider.io())
                .subscribe(item -> fragment.handleQualityItemResult(item),
                        e -> fragment.handleError(e));
        mSubscription.add(subscribe);
    }

    @Override
    public void setUUID(UUID characteristicUUID) {
        uuid = checkNotNull(characteristicUUID);
    }

    @Override
    public void reConnect() {
        if (isConnected()) {
            triggerDisconnect();
        } else {
            startMeasure();
        }
    }

    private boolean isConnected() {
        Context context = ((QualityFragment) fragment).getActivity();
        String macAddress = BaseApplication.getMacAddress(context);
        if (macAddress != null) {
            device = BaseApplication.getRxBleClient(context).getBleDevice(macAddress);
            return device.getConnectionState() == RxBleConnection.RxBleConnectionState.CONNECTED;
        } else {
            return false;
        }
    }

    private Observable<RxBleConnection> prepareConnectionObservable() {
        Context context = ((QualityFragment) fragment).getActivity();
        String macAddress = BaseApplication.getMacAddress(context);
        if (macAddress != null) {
            device = BaseApplication.getRxBleClient(context).getBleDevice(macAddress);
            return device.establishConnection(false)
                    .takeUntil(disconnectTriggerSubject)
                    .compose(new ConnectionSharingAdapter());
        } else {
            return null;
        }
    }

    /**
     * 开启测量，需要检查device的状态
     */
    private void startMeasure() {
        Observable<RxBleConnection> connectionObservable = prepareConnectionObservable();
        if (connectionObservable != null) {
            Subscription subscribe = connectionObservable
                    .flatMap(rxBleConnection -> rxBleConnection.setupNotification(uuid))
                    .flatMap(notificationObservable -> notificationObservable)
                    .subscribeOn(mSchedulerProvider.computation())
                    .observeOn(mSchedulerProvider.ui())
                    .throttleFirst(MEASURE_DURATION, TimeUnit.MILLISECONDS)
                    .subscribe(this::handleBleResult, this::handleError);
            mSubscription.add(subscribe);
        } else {
            fragment.showDeviceError();
        }
    }

    private void triggerDisconnect() {
        disconnectTriggerSubject.onNext(null);
    }

    private void handleBleResult(byte[] v) {
        String s = HexString.bytesToHex(v);
        if (s.length() == STANDARD_LENGTH) {
            int code = Integer.parseInt("8D6A", 16);
            int length = Integer.parseInt(s.substring(0, 4), 16);
            int angle = Integer.parseInt(s.substring(4, 8), 16);
            int battery = Integer.parseInt(s.substring(8, 12), 16);
            int a1 = length ^ code;
            int a2 = angle ^ code;
            int a3 = battery ^ code;
            a1 += ADJUST_VALUE;
            fragment.handleMeasureData(a1, (float) a2 / 10, a3);
        }
    }

    private void handleError(Throwable e) {
        fragment.handleError(e);
    }

    @Override
    public void uploadResult(QualityItem item) {
        String s = (new Gson()).toJson(item);
        if (aesUtils == null) {
            aesUtils = new AesUtils();
        }
        String s1 = null;
        String nonce = aesUtils.getRandomStr();
        String timeStamp = Long.toString(System.currentTimeMillis());
        try {
            s1 = aesUtils.encryptMsg(s, timeStamp, nonce);
        } catch (AesException e) {
            e.printStackTrace();
        }
        new DemoHelper()
                .uploadQualityResult(s1)
                .subscribeOn(mSchedulerProvider.io())
                .observeOn(mSchedulerProvider.ui())
                .subscribe(v -> {
                        }, e -> fragment.handleError(e)
                );
    }
}