package com.npclo.imeasurer.account.signup;

import android.support.annotation.NonNull;

import com.npclo.imeasurer.utils.http.user.UserRepository;
import com.npclo.imeasurer.utils.schedulers.BaseSchedulerProvider;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

public class SignUpPresenter implements SignUpContract.Presenter {
    private SignUpFragment mView;
    @NonNull
    private final BaseSchedulerProvider mSchedulerProvider;
    @NonNull
    private CompositeSubscription mSubscriptions;

    public SignUpPresenter(@NonNull SignUpContract.View signUpView,
                           @NonNull BaseSchedulerProvider schedulerProvider) {
        signUpView = checkNotNull(signUpView);
        mSchedulerProvider = checkNotNull(schedulerProvider);
        mSubscriptions = new CompositeSubscription();
        mView = (SignUpFragment) signUpView;
        mView.setPresenter(this);
    }

    @Override
    public void signUp(String name, String pwd, String code) {
        Subscription subscribe = new UserRepository()
                .signUp(name, pwd, code)
                .subscribeOn(mSchedulerProvider.io())
                .observeOn(mSchedulerProvider.ui())
                .doOnSubscribe(() -> mView.showLoading(true))
                .subscribe(
                        result -> mView.showSignUpSuccess(result),
                        e -> mView.showSignUpError(e),
                        () -> mView.completeSignUp());
        mSubscriptions.add(subscribe);
    }

    @Override
    public void subscribe() {
    }

    @Override
    public void unsubscribe() {
        mSubscriptions.clear();
    }

    @Override
    public void getValidCode(String mobile, String type) {
        Subscription subscribe = new UserRepository()
                .getValidCode(mobile, type)
                .subscribeOn(mSchedulerProvider.io())
                .observeOn(mSchedulerProvider.ui())
                .doOnSubscribe(() -> mView.showLoading(true))
                .subscribe(
                        code -> mView.showValidCodeSendSuccess(code),
                        e -> mView.showValidCodeSendError(e),
                        () -> mView.showCompleteGetValidCode());
        mSubscriptions.add(subscribe);
    }
}