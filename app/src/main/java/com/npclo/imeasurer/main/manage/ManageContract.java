package com.npclo.imeasurer.main.manage;

import com.npclo.imeasurer.base.BasePresenter;
import com.npclo.imeasurer.base.BaseView;

public interface ManageContract {
    interface Presenter extends BasePresenter {
        void resetPwd(String id, String old, String newpwd);
    }

    interface View extends BaseView<Presenter> {
        void showLoading(boolean b);

        void showEditSuccess();

        void showEditError(Throwable e);

        void showEditCompleted();
    }
}