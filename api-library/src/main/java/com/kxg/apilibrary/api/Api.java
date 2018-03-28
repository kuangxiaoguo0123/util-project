package com.kxg.apilibrary.api;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by kuangxiaoguo on 2018/3/22.
 */

public class Api {

    private static final int TIME_OUT = 10000;
    private static final Map<String, Object> mServiceMap = new HashMap<>();

    private Api() {
    }

    public <S> S getService(Class<S> serviceClass) {
        String className = serviceClass.getName();
        if (!(mServiceMap.containsKey(className))) {
            throw new RuntimeException("You must init " + className + " first when application init !");
        }
        return (S) mServiceMap.get(className);
    }

    public <S> void init(Class<S> serviceClass, String baseUrl, OkHttpClient client) {
        if (client == null) {
            client = new OkHttpClient.Builder()
                    .connectTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
                    .writeTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
                    .readTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
                    .build();
        }
        if (baseUrl == null || TextUtils.isEmpty(baseUrl)) {
            throw new NullPointerException("baseUrl can not be null");
        }
        if (!baseUrl.endsWith("/")) {
            throw new IllegalArgumentException("baseUrl must be end with /");
        }
        createService(serviceClass, baseUrl, client);
    }

    public static Api getInstance() {
        return SingleHolder.API_INSTANCE;
    }

    private static class SingleHolder {
        private static final Api API_INSTANCE = new Api();
    }

    private <S> void createService(Class<S> serviceClass, String baseUrl, OkHttpClient client) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(client)
                .build();
        S service = retrofit.create(serviceClass);
        mServiceMap.put(serviceClass.getName(), service);
    }
}
