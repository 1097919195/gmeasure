package com.npclo.imeasurer.account.signin;

import com.npclo.imeasurer.base.BasePresenter;
import com.npclo.imeasurer.base.BaseView;
import com.npclo.imeasurer.data.User;

/**
 * @author Endless
 * @date 2017/7/20
 */

public interface SignInContract {
    interface View extends BaseView<Presenter> {
        void showSignInSuccess(User v);

        void showSignInError(Throwable e);

        void completeSignIn();

        void showLoading(boolean bool);

        void saveToken(String msg);
    }

    interface Presenter extends BasePresenter {
        void signIn(String name, String pwd);
    }
}
