package de.hda.simple_example.di;

import android.content.Context;
import android.preference.PreferenceManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.hda.simple_example.business.GithubService;
import de.hda.simple_example.business.Settings;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class ModuleSingleton
{

    @Provides
    @Singleton
    public Settings sharedPreferences(Context context){
        return new Settings(PreferenceManager.getDefaultSharedPreferences(context));
    }


    @Provides
    @Singleton
    public GithubService getGithubService() {
        return new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://api.github.com")
                .build().create(GithubService.class);
    }
}
