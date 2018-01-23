package com.npclo.imeasurer.measure;

import com.npclo.imeasurer.base.BasePresenter;
import com.npclo.imeasurer.base.BaseView;
import com.npclo.imeasurer.data.ThirdMember;
import com.npclo.imeasurer.data.measure.Measurement;
import com.npclo.imeasurer.data.measure.Result;
import com.npclo.imeasurer.data.WechatUser;

import java.util.UUID;

import okhttp3.MultipartBody;

/**
 * Created by Endless on 2017/9/1.
 */

public interface MeasureContract {
    interface View extends BaseView<Presenter> {

        void handleMeasureData(float v, float a2, int a3);

        void onSaveSuccess(Result result);

        void showSaveError(Throwable e);

        void showSaveCompleted();

        void showLoading(boolean b);

        void onGetWechatUserInfoSuccess(WechatUser user);

        void onShowGetInfoError(Throwable e);

        void showCompleteGetInfo();

        void onShowDevicePrepareConnectionError();

        void onHandleMeasureError();

        void onHandleMeasureError(Throwable e);

        void onGetThirdMemberInfo(ThirdMember user);

        void showGetThirdMemberInfoError(Throwable e);

        void showCompleteGetThirdMemberInfo();
    }

    interface Presenter extends BasePresenter {
        void saveMeasurement(Measurement measurement, MultipartBody.Part[] imgs);

        void getUserInfoWithOpenID(String id);

        void getUserInfoWithCode(String code);

        void reConnect();

        void setUUID(UUID characteristicUUID);

        void getThirdMemberInfo(String tid, String cid);
    }
}
