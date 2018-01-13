package com.npclo.imeasurer.account.signup;

import com.npclo.imeasurer.base.BasePresenter;
import com.npclo.imeasurer.base.BaseView;
import com.npclo.imeasurer.data.User;
import com.npclo.imeasurer.data.ValidCode;

/**
 * Created by Endless on 2017/7/24.
 */

public interface SignUpContract {
    interface View extends BaseView<Presenter> {
        void showSignUpSuccess(User result);

        void showSignUpError(Throwable e);

        void completeSignUp();

        void showLoading(boolean bool);

        void showValidCodeSendSuccess(ValidCode code);

        void showValidCodeSendError(Throwable e);

        void showCompleteGetValidCode();
    }

    interface Presenter extends BasePresenter {
        void signUp(String name, String pwd, String code);

        void getValidCode(String mobile, String singup);
    }
}
