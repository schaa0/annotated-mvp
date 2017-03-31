package de.hda.simple_example.di;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.hda.simple_example.service.GithubService;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class ModuleSingleton
{

    @Provides
    @Singleton
    public GithubService getGithubService() {
        return new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://api.github.com")
                .build().create(GithubService.class);
    }
}
