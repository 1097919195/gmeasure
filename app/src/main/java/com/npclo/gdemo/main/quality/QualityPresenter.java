package com.npclo.gdemo.main.quality;

import android.support.annotation.NonNull;

import com.npclo.gdemo.utils.HexString;
import com.npclo.gdemo.utils.http.DemoHelper;
import com.npclo.gdemo.utils.schedulers.BaseSchedulerProvider;
import com.polidea.rxandroidble.RxBleConnection;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
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
    private QualityFragment fragment;
    @NonNull
    private BaseSchedulerProvider mSchedulerProvider;

    public QualityPresenter(@NonNull QualityFragment qualityFragment, @NonNull BaseSchedulerProvider mSchedulerProvider) {
        mSubscription = new CompositeSubscription();
        fragment = checkNotNull(qualityFragment);
        this.mSchedulerProvider = mSchedulerProvider;
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
    public void startMeasure(UUID characteristicUUID, Observable<RxBleConnection> connectionObservable) {
        Subscription subscribe = connectionObservable
                .flatMap(rxBleConnection -> rxBleConnection.setupNotification(characteristicUUID))
                .flatMap(notificationObservable -> notificationObservable)
                .observeOn(mSchedulerProvider.ui())
                .throttleFirst(MEASURE_DURATION, TimeUnit.MILLISECONDS)
                .subscribe(this::handleBleResult, this::handleError);
        mSubscription.add(subscribe);
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


}