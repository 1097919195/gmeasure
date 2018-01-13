package com.npclo.imeasurer.account.signin;

import android.support.annotation.NonNull;

import com.npclo.imeasurer.utils.http.user.UserRepository;
import com.npclo.imeasurer.utils.schedulers.BaseSchedulerProvider;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

public class SignInPresenter implements SignInContract.Presenter {
    private SignInContract.View mView;
    @NonNull
    private final BaseSchedulerProvider mSchedulerProvider;
    @NonNull
    private CompositeSubscription mSubscriptions;

    public SignInPresenter(@NonNull SignInContract.View signinView,
                           @NonNull BaseSchedulerProvider schedulerProvider) {
        signinView = checkNotNull(signinView);
        mSchedulerProvider = checkNotNull(schedulerProvider);
        mSubscriptions = new CompositeSubscription();
        mView = signinView;
        mView.setPresenter(this);
    }

    @Override
    public void subscribe() {
    }

    @Override
    public void unsubscribe() {
        mSubscriptions.clear();
    }

    @Override
    public void signIn(String name, String pwd) {
        Subscription subscribe = new UserRepository()
                .signIn(name, pwd)
                .flatMap(res -> {
                    String msg = res.getMsg();
                    mView.saveToken(msg);
                    return new UserRepository().userInfo();
                })
                .subscribeOn(mSchedulerProvider.io())
                .observeOn(mSchedulerProvider.ui())
                .doOnSubscribe(() -> mView.showLoading(true))
                .subscribe(mView::showSignInSuccess,
                        mView::showSignInError,
                        mView::completeSignIn);
        mSubscriptions.add(subscribe);
    }
}