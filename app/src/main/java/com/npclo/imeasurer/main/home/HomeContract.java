package com.npclo.imeasurer.main.home;

import com.npclo.imeasurer.base.BasePresenter;
import com.npclo.imeasurer.base.BaseView;
import com.npclo.imeasurer.data.wuser.WechatUser;

public interface HomeContract {
    interface Presenter extends BasePresenter {

        void getUserInfoWithCode(String result);

        void getUserInfoWithOpenID(String result);
    }

    interface View extends BaseView<Presenter> {

        void showLoading(boolean b);

        void showGetInfoSuccess(WechatUser info);

        void showGetInfoError(Throwable e);

        void showCompleteGetInfo();
    }
}