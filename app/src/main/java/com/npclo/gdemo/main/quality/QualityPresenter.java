package com.npclo.gdemo.main.quality;

import android.support.annotation.NonNull;

import com.npclo.gdemo.utils.HexString;
import com.npclo.gdemo.utils.http.DemoHelper;
import com.npclo.gdemo.utils.schedulers.BaseSchedulerProvider;
import com.polidea.rxandroidble.RxBleConnection;

import java.util.UUID;

import rx.Observable;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

public class QualityPresenter implements QualityContract.Presenter {
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
                .subscribe(this::handleBleResult, this::handleError);
        mSubscription.add(subscribe);
    }

    @Override
    public void getQualityItemInfoWithId(String id) {
        Subscription subscribe = new DemoHelper().getQualityItemWithId(id)
                .subscribeOn(mSchedulerProvider.io())
                .observeOn(mSchedulerProvider.ui())
                .subscribe(item -> fragment.handleQualityItemResult(item), e -> fragment.handleError(e));
        mSubscription.add(subscribe);
    }

    @Override
    public void getQualityItemInfoWithCode(String code) {
        Subscription subscribe = new DemoHelper().getQualityItemWithCode(code)
                .subscribeOn(mSchedulerProvider.io())
                .subscribe(item -> fragment.handleQualityItemResult(item), e -> fragment.handleError(e));
        mSubscription.add(subscribe);
    }

    private void handleBleResult(byte[] v) {
        String s = HexString.bytesToHex(v);
        if (s.length() == 16) { //判断接收到的数据是否准确
            int code = Integer.parseInt("8D6A", 16);
            int length = Integer.parseInt(s.substring(0, 4), 16);
            int angle = Integer.parseInt(s.substring(4, 8), 16);
            int battery = Integer.parseInt(s.substring(8, 12), 16);
            int a1 = length ^ code;
            int a2 = angle ^ code;
            int a3 = battery ^ code;
//            Log.e(TAG, "获得数据：长度: " + a1 + "; 角度:  " + a2 + "; 电量: " + a3);
            a1 += 14; //校正数据
            fragment.handleMeasureData(a1, (float) a2 / 10, a3);
        }
    }

    private void handleError(Throwable e) {
        fragment.handleError(e);
    }


}