package com.npclo.imeasurer.utils.http.app;

import com.npclo.imeasurer.data.HttpMsg;
import com.npclo.imeasurer.data.HttpResponse;
import com.npclo.imeasurer.data.app.App;

import okhttp3.RequestBody;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import rx.Observable;

public interface AppService {
    @GET("client/info")
    Observable<HttpResponse<App>> getLatestVersion();

    @Multipart
    @POST("client/log")
    Observable<HttpResponse<HttpMsg>> upload(@Part("log") RequestBody file);

    @POST("client/bug")
    Observable<HttpResponse<HttpMsg>> fixbug(@Part("bug") String s);
}