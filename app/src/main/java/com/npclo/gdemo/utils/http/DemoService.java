package com.npclo.gdemo.utils.http;

import com.npclo.gdemo.data.HttpResponse;
import com.npclo.gdemo.data.quality.QualityItem;

import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

/**
 *
 * @author Endless
 * @date 2017/8/1
 */

public interface DemoService {
    @GET("qc/id/{id}")
    Observable<HttpResponse<QualityItem>> getQualityItemWithId(@Path("id") String id);

    @GET("qc/code/{code}")
    Observable<HttpResponse<QualityItem>> getQualityItemWithCode(@Path("code") String code);
}
