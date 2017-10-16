package com.npclo.gdemo.utils.http;


import com.npclo.gdemo.data.quality.QualityItem;

import rx.Observable;

import static android.R.attr.id;

public class DemoHelper extends HttpHelper {
    public Observable<QualityItem> getQualityItem(String id) {
        return retrofit.create(DemoService.class)
                .getQualityItem(id)
                .map(new HttpResponseFunc<>());
    }
}
