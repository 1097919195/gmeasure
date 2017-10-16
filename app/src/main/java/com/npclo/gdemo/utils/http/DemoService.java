package com.npclo.gdemo.utils.http;

import com.npclo.gdemo.data.HttpResponse;
import com.npclo.gdemo.data.quality.QualityItem;

import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Created by Endless on 2017/8/1.
 */

public interface DemoService {
    @GET("qc/itemsingle/{id}")
    Observable<HttpResponse<QualityItem>> getQualityItem(@Path("id") String id);
}
