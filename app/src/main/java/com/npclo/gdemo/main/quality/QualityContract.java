package com.npclo.gdemo.main.quality;

import com.npclo.gdemo.base.BasePresenter;
import com.npclo.gdemo.base.BaseView;
import com.npclo.gdemo.data.quality.QualityItem;

import java.util.UUID;

/**
 * Created by Endless on 2017/10/13.
 */

public interface QualityContract {
    interface Presenter extends BasePresenter {
        void getQualityItemInfoWithId(String result);

        void getQualityItemInfoWithCode(String result);

        void setUUID(UUID characteristicUUID);

        void reConnect();

        void uploadResult(QualityItem item);
    }

    interface View extends BaseView<Presenter> {
        void handleError(Throwable e);

        void handleMeasureData(int a1, float a2, int a3);

        void handleQualityItemResult(QualityItem item);

        void showDeviceError();
    }
}
