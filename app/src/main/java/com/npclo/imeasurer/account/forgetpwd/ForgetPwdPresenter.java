package com.npclo.imeasurer.account.forgetPwd;

import android.support.annotation.NonNull;

import com.npclo.imeasurer.utils.http.user.UserRepository;
import com.npclo.imeasurer.utils.schedulers.BaseSchedulerProvider;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

public class ForgetPwdPresenter implements ForgetPwdContract.Presenter {
    private ForgetPwdFragment fragment;
    @NonNull
    private final BaseSchedulerProvider mSchedulerProvider;
    @NonNull
    private CompositeSubscription mSubscriptions;

    public ForgetPwdPresenter(ForgetPwdContract.View mView, @NonNull BaseSchedulerProvider provider) {
        fragment = (ForgetPwdFragment) checkNotNull(mView);
        fragment.setPresenter(this);
        mSchedulerProvider = checkNotNull(provider);
        mSubscriptions = new CompositeSubscription();
    }

    @Override
    public void subscribe() {

    }

    @Override
    public void unsubscribe() {
        mSubscriptions.clear();
    }

    @Override
    public void getValidCode(String s) {
        Subscription subscribe = new UserRepository()
                .getValidCode(s, "reset")
                .subscribeOn(mSchedulerProvider.io())
                .observeOn(mSchedulerProvider.ui())
                .doOnSubscribe(() -> fragment.showLoading(true))
                .subscribe(
                        code -> fragment.showValidCodeSendSuccess(code),
                        e -> fragment.showValidCodeSendError(e),
                        () -> fragment.showCompleteGetValidCode());
        mSubscriptions.add(subscribe);
    }

    @Override
    public void resetPwd(String mobile, String pwd, String code) {
        Subscription subscribe = new UserRepository()
                .resetPwd(mobile, pwd, code)
                .subscribeOn(mSchedulerProvider.io())
                .observeOn(mSchedulerProvider.ui())
                .doOnSubscribe(() -> fragment.showLoading(true))
                .subscribe(
                        v -> fragment.showResetPwdSuccess(),
                        e -> fragment.showResetPwdError(e),
                        () -> fragment.showResetPwdComplete());
        mSubscriptions.add(subscribe);
    }
}
