package com.npclo.imeasurer.account.forgetPwd;

import com.npclo.imeasurer.base.BasePresenter;
import com.npclo.imeasurer.base.BaseView;
import com.npclo.imeasurer.data.ValidCode;

public interface ForgetPwdContract {
    interface View extends BaseView<Presenter> {

        void showLoading(boolean b);

        void showValidCodeSendSuccess(ValidCode code);

        void showValidCodeSendError(Throwable e);

        void showCompleteGetValidCode();

        void showResetPwdSuccess();

        void showResetPwdError(Throwable e);

        void showResetPwdComplete();
    }

    interface Presenter extends BasePresenter {

        void getValidCode(String s);

        void resetPwd(String mobile, String pwd, String code);
    }
}
