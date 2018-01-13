package com.npclo.imeasurer.utils.http.app;

import com.npclo.imeasurer.data.App;

import rx.Observable;

/**
 * Created by Endless on 2017/9/19.
 */

public interface AppDataSource {
    Observable<App> getLatestVersion();
}
