package com.npclo.gdemo.main.quality;

import android.support.annotation.NonNull;

import rx.subscriptions.CompositeSubscription;

import static com.google.gson.internal.$Gson$Preconditions.checkNotNull;

/**
 * Created by Endless on 2017/10/13.
 */

public class QualityPresenter implements QualityContract.Presenter {
    @NonNull
    private CompositeSubscription compositeSubscription;

    public QualityPresenter(@NonNull CompositeSubscription compositeSubscription1) {
        compositeSubscription = checkNotNull(compositeSubscription1);
    }

    @Override
    public void subscribe() {

    }

    @Override
    public void unsubscribe() {
        compositeSubscription.clear();
    }
}

