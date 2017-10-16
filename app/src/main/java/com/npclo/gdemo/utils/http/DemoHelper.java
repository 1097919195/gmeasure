package com.npclo.gdemo.utils.http;


import com.npclo.gdemo.data.quality.QualityItem;

import rx.Observable;

public class DemoHelper extends HttpHelper {
    public Observable<QualityItem> getQualityItemWithId(String id) {
        return retrofit.create(DemoService.class)
                .getQualityItemWithId(id)
                .map(new HttpResponseFunc<>());
    }

    public Observable<QualityItem> getQualityItemWithCode(String code) {
        return retrofit.create(DemoService.class)
                .getQualityItemWithCode(code)
                .map(new HttpResponseFunc<>());
    }
}
