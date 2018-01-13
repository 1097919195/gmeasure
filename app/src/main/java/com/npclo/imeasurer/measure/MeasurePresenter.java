package com.npclo.imeasurer.measure;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.npclo.imeasurer.data.measure.Measurement;
import com.npclo.imeasurer.utils.HexString;
import com.npclo.imeasurer.utils.aes.AesUtils;
import com.npclo.imeasurer.utils.http.measurement.MeasurementHelper;
import com.npclo.imeasurer.utils.http.user.UserRepository;
import com.npclo.imeasurer.utils.schedulers.BaseSchedulerProvider;
import com.polidea.rxandroidble.RxBleConnection;
import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.utils.ConnectionSharingAdapter;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.MultipartBody;
import rx.Observable;
import rx.Subscription;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

/**
 * @author Endless
 */
public class MeasurePresenter implements MeasureContract.Presenter {
    private static final int MEASURE_DURATION = 500;
    public static final int STANDARD_BYTE = 16;
    @NonNull
    private MeasureContract.View fragment;
    @NonNull
    private BaseSchedulerProvider schedulerProvider;
    @NonNull
    private CompositeSubscription mSubscriptions;
    private AesUtils aesUtils;
    private RxBleDevice device;
    private PublishSubject<Void> disconnectTriggerSubject = PublishSubject.create();
    private UUID uuid;
    private String macAddress;
    private float offset;
    private Observable<RxBleConnection> connectionObservable;

    public MeasurePresenter(@NonNull MeasureContract.View view, @NonNull BaseSchedulerProvider schedulerProvider,
                            float offsetMeasure, String address, RxBleDevice bleDevice, @NonNull UUID u) {
        fragment = checkNotNull(view);
        this.schedulerProvider = checkNotNull(schedulerProvider);
        mSubscriptions = new CompositeSubscription();
        fragment.setPresenter(this);
        offset = offsetMeasure;
        macAddress = address;
        device = checkNotNull(bleDevice);
        uuid = u;
    }

    @Override
    public void subscribe() {
        reConnect();
    }

    @Override
    public void unsubscribe() {
        mSubscriptions.clear();
    }

    private void onHandleMeasureResult(byte[] v) {
        String s = HexString.bytesToHex(v);
        if (s.length() == STANDARD_BYTE) {
            int code = Integer.parseInt("8D6A", 16);
            int length = Integer.parseInt(s.substring(0, 4), 16);
            int angle = Integer.parseInt(s.substring(4, 8), 16);
            int battery = Integer.parseInt(s.substring(8, 12), 16);
            int a1 = length ^ code;
            int a2 = angle ^ code;
            int a3 = battery ^ code;
//            Log.e(TAG, "解析数据：长度: " + a1 + "; 角度:  " + a2 + "; 电量: " + a3);
            a1 += offset;
            fragment.handleMeasureData((float) a1 / 10, (float) a2 / 10, a3);
        } else {
            fragment.onHandleMeasureError();
        }
    }

    private void onHandleMeasureError(Throwable e) {
        fragment.onHandleMeasureError(e);
    }

    @Override
    public void saveMeasurement(Measurement measurement, MultipartBody.Part[] imgs) {
        String s = (new Gson()).toJson(measurement);

        Subscription subscribe = new MeasurementHelper()
                .saveMeasurement(s, imgs)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .doOnSubscribe(() -> fragment.showLoading(true))
                .subscribe(v -> fragment.showSuccessSave(),
                        e -> fragment.showSaveError(e),
                        () -> fragment.showSaveCompleted()
                );
        mSubscriptions.add(subscribe);
    }

    @Override
    public void getUserInfoWithOpenID(String id) {
        Subscription subscribe = new UserRepository()
                .getUserInfoWithOpenID(id)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .doOnSubscribe(() -> fragment.showLoading(true))
                .subscribe(
                        user -> fragment.onGetWechatUserInfoSuccess(user),
                        e -> fragment.showGetInfoError(e),
                        () -> fragment.showCompleteGetInfo());
        mSubscriptions.add(subscribe);
    }

    @Override
    public void getUserInfoWithCode(String code) {
        Subscription subscribe = new UserRepository()
                .getUserInfoWithCode(code)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .doOnSubscribe(() -> fragment.showLoading(true))
                .subscribe(
                        user -> fragment.onGetWechatUserInfoSuccess(user),
                        e -> fragment.showGetInfoError(e),
                        () -> fragment.showCompleteGetInfo());
        mSubscriptions.add(subscribe);
    }

    @Override
    public void reConnect() {
        //att 判断之前是否已连接测量设备
//        if (!isConnected()) {
//            startMeasure();
//        }
        if (isConnected()) {
            triggerDisconnect();
//            Subscription subscribe = prepareConnectionObservable()
//                    .flatMap(RxBleConnection::discoverServices)
//                    .flatMap(rxBleDeviceServices -> rxBleDeviceServices.getCharacteristic(uuid))
//                    .observeOn(schedulerProvider.ui())
//                    .subscribe(characteristic -> {
//                                Log.d("tag", "reConnect startMeasure");
//                                startMeasure();
//                            },
//                            e -> fragment.onHandleMeasureError(e),
//                            () -> fragment.showLoading(false)
//                    );
//            mSubscriptions.add(subscribe);
        } else {
            startMeasure();
        }
    }

    private Observable<RxBleConnection> prepareConnectionObservable() {
        return device.establishConnection(false)
                .takeUntil(disconnectTriggerSubject)
                .compose(new ConnectionSharingAdapter());
    }

    /**
     * 开启测量，需要检查device的状态
     */
    private void startMeasure() {
        connectionObservable = prepareConnectionObservable();
        Subscription subscribe = connectionObservable
                .flatMap(rxBleConnection -> rxBleConnection.setupNotification(uuid))
                .flatMap(notificationObservable -> notificationObservable)
                .observeOn(schedulerProvider.ui())
                .throttleFirst(MEASURE_DURATION, TimeUnit.MILLISECONDS)
                .subscribe(this::onHandleMeasureResult, this::onHandleMeasureError);
        mSubscriptions.add(subscribe);
    }

    @Override
    public void setUUID(UUID characteristicUUID) {
        uuid = characteristicUUID;
    }

    private void triggerDisconnect() {
//        disconnectTriggerSubject.onNext(null);
//        connectionObservable = null;
        device.establishConnection(false);
        startMeasure();
    }

    private boolean isConnected() {
        return !TextUtils.isEmpty(macAddress) && device.getConnectionState()
                == RxBleConnection.RxBleConnectionState.CONNECTED;
    }
}