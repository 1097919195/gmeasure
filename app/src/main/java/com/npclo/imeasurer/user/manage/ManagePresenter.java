package com.npclo.imeasurer.user.manage;

import android.support.annotation.NonNull;

import com.npclo.imeasurer.utils.http.user.UserRepository;
import com.npclo.imeasurer.utils.schedulers.BaseSchedulerProvider;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

/**
 * Created by Endless on 2017/9/4.
 */

public class ManagePresenter implements ManageContract.Presenter {
    @NonNull
    private ManageFragment fragment;
    @NonNull
    private CompositeSubscription mSubscription;
    @NonNull
    private BaseSchedulerProvider provider;

    public ManagePresenter(@NonNull ManageContract.View view, @NonNull BaseSchedulerProvider schedulerProvider) {
        fragment = ((ManageFragment) checkNotNull(view));
        provider = checkNotNull(schedulerProvider);
        mSubscription = new CompositeSubscription();
        fragment.setPresenter(this);
    }

    @Override
    public void subscribe() {

    }

    @Override
    public void unsubscribe() {
        mSubscription.clear();
    }

    @Override
    public void resetPwd(String old, String newpwd) {
        Subscription subscribe = new UserRepository()
                .editPwd(old, newpwd)
                .subscribeOn(provider.io())
                .observeOn(provider.ui())
                .doOnSubscribe(() -> fragment.showLoading(true))
                .subscribe(
                        msg -> fragment.showEditSuccess(),
                        e -> fragment.showEditError(e),
                        () -> fragment.showEditCompleted());
        mSubscription.add(subscribe);
    }
}
