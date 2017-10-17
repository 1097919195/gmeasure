package com.npclo.gdemo.main.quality;

import com.npclo.gdemo.base.BasePresenter;
import com.npclo.gdemo.base.BaseView;
import com.npclo.gdemo.data.quality.QualityItem;
import com.polidea.rxandroidble.RxBleConnection;

import java.util.UUID;

import rx.Observable;

/**
 * Created by Endless on 2017/10/13.
 */

public interface QualityContract {
    interface Presenter extends BasePresenter {
        void startMeasure(UUID characteristicUUID, Observable<RxBleConnection> connectionObservable);

        void getQualityItemInfoWithId(String result);

        void getQualityItemInfoWithCode(String result);
    }

    interface View extends BaseView<Presenter> {
        void handleError(Throwable e);

        void handleMeasureData(int a1, float a2, int a3);

        void handleQualityItemResult(QualityItem item);
    }
}
