package com.zakia.idn.ojekonlinefirebase.network

import com.zakia.idn.ojekonlinefirebase.utils.Constan
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

//konfiguration dari retrofit
object NetworkModule {
    fun getOkhttp() :OkHttpClient {
        val log2 = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        val client = OkHttpClient.Builder().addInterceptor(log2).build()

        return client
    }

    fun getRetrofit() : Retrofit {
        var retrofit = Retrofit.Builder().baseUrl(Constan.baseUrlRoute).addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create()).client(getOkhttp()).build()

        return retrofit
    }

    fun getRetrofitFCM() : Retrofit {
        var retrofit = Retrofit.Builder().baseUrl(Constan.baseUrlFcm).addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create()).client(getOkhttp()).build()

        return retrofit
    }

    fun getService() : ApiService {
        var service : ApiService = getRetrofit().create(ApiService::class.java)

        return service
    }

    fun getServiceFCM() : ApiService {
        var service : ApiService = getRetrofit().create(ApiService::class.java)

        return service
    }
}