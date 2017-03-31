package com.mvp.weather_example.di;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.mvp.annotation.ApplicationScope;
import com.mvp.weather_example.R;
import com.mvp.weather_example.service.WeatherApi;

import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class ModuleSingleton
{

    @Provides
    @Named("apiKey")
    @Singleton
    public String apiKey(Context context) {
        return context.getString(R.string.api_key);
    }

    @Provides
    @Singleton
    public WeatherApi weatherAPI() {
        return new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("http://api.openweathermap.org")
                .client(new OkHttpClient.Builder()
                        .connectTimeout(5, TimeUnit.SECONDS)
                        .readTimeout(5, TimeUnit.SECONDS)
                        .build())
                .build()
                .create(WeatherApi.class);
    }

    @Provides
    @Singleton
    public RequestManager glide(Context context){
        return Glide.with(context);
    }

}
