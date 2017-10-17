package com.npclo.gdemo.main.measure;

import com.npclo.gdemo.base.BasePresenter;
import com.npclo.gdemo.base.BaseView;
import com.polidea.rxandroidble.RxBleConnection;

import java.util.UUID;

import rx.Observable;

/**
 * Created by Endless on 2017/9/1.
 */

public interface MeasureContract {
    interface View extends BaseView<Presenter> {
        void handleError(Throwable e);

        void handleMeasureData(float v, float a2, int a3);
    }

    interface Presenter extends BasePresenter {
        void startMeasure(UUID characteristicUUID, Observable<RxBleConnection> connectionObservable);
    }
}
