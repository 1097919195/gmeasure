package com.npclo.gdemo.main.measure;

import android.support.annotation.NonNull;

import com.npclo.gdemo.utils.HexString;
import com.npclo.gdemo.utils.schedulers.BaseSchedulerProvider;
import com.polidea.rxandroidble.RxBleConnection;

import java.util.UUID;

import rx.Observable;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

public class MeasurePresenter implements MeasureContract.Presenter {
    private static final String TAG = MeasurePresenter.class.getSimpleName();
    @NonNull
    private MeasureFragment fragment;
    @NonNull
    private BaseSchedulerProvider schedulerProvider;
    @NonNull
    private CompositeSubscription mSubscriptions;

    public MeasurePresenter(@NonNull MeasureContract.View view, @NonNull BaseSchedulerProvider schedulerProvider) {
        fragment = ((MeasureFragment) checkNotNull(view));
        this.schedulerProvider = checkNotNull(schedulerProvider);
        mSubscriptions = new CompositeSubscription();
        fragment.setPresenter(this);
    }

    @Override
    public void subscribe() {

    }

    @Override
    public void unsubscribe() {
        mSubscriptions.clear();
    }

    @Override
    public void startMeasure(UUID characteristicUUID, Observable<RxBleConnection> connectionObservable) {
        Subscription subscribe = connectionObservable
                .flatMap(rxBleConnection -> rxBleConnection.setupNotification(characteristicUUID))
                .flatMap(notificationObservable -> notificationObservable)
                .observeOn(schedulerProvider.ui())
                .subscribe(this::handleBleResult, this::handleError);
        mSubscriptions.add(subscribe);
    }

    private void handleBleResult(byte[] v) {
        String s = HexString.bytesToHex(v);
//        Log.e(TAG, "测量原始结果：" + s);
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
            fragment.handleMeasureData((float) a1 / 10, (float) a2 / 10, a3);
        }
    }

    private void handleError(Throwable e) {
        fragment.handleError(e);
    }
}