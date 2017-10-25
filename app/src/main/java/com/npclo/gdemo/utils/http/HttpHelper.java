package com.npclo.gdemo.utils.http;


import com.npclo.gdemo.data.HttpResponse;
import com.npclo.gdemo.utils.ApiException;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.TextCodec;
import io.jsonwebtoken.impl.crypto.MacProvider;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.functions.Func1;

/**
 *
 * @author Endless
 * @date 2017/7/19
 */

public class HttpHelper {
    private static final String BASE_URL = "https://www.npclo.com/api/";
    private static final int DEFAULT_TIMEOUT = 600;
    protected Retrofit retrofit;

    //构造方法私有
    protected HttpHelper() {
        //手动创建一个OkHttpClient并设置超时时间
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
        httpClientBuilder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
//        initHeader(httpClientBuilder);
        retrofit = new Retrofit.Builder()
                .client(httpClientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(BASE_URL)
                .build();
    }

    private void initHeader(OkHttpClient.Builder httpClientBuilder) {
        Date date = new Date();
        long time = date.getTime();
        SecretKey key = MacProvider.generateKey();
        byte[] keyBytes = key.getEncoded();

        String base64Encoded = TextCodec.BASE64.encode(keyBytes);
        String jwt = Jwts.builder()
                .setExpiration(new Date(time + 600 * 1000))
                .signWith(SignatureAlgorithm.HS256, base64Encoded)
                .compact();

        httpClientBuilder.addInterceptor(chain -> {
            Request original = chain.request();
            Request.Builder requestBuilder = original.newBuilder()
                    .header("X-Authorization", jwt); // <-- this is the important line
            Request request = requestBuilder.build();
            return chain.proceed(request);
        });
    }

    public class HttpResponseFunc<T> implements Func1<HttpResponse<T>, T> {
        @Override
        public T call(HttpResponse<T> httpResponse) {
            if (httpResponse.getStatus() >= 1000)
                throw new ApiException(httpResponse.getMsg());
            if (httpResponse.getData() == null) throw new ApiException("暂无数据");
            return httpResponse.getData();
        }
    }
}