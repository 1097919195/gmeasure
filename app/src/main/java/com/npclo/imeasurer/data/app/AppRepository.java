package com.npclo.imeasurer.data.app;

import com.npclo.imeasurer.utils.http.app.AppHttpHelper;

import rx.Observable;

/**
 *
 * @author Endless
 * @date 2017/9/19
 */

public class AppRepository implements AppDataSource {

    @Override
    public Observable<App> getLatestVersion() {
        return new AppHttpHelper()
                .getLatestVersion();
    }
}
