package com.mvp.example.photostream;

import com.mvp.example.photostream.service.GithubService;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Andy on 02.12.2016.
 */

@Module
public class ModuleGithubService {

    private String githubApiEndpoint;

    public ModuleGithubService(String githubApiEndpoint){
        this.githubApiEndpoint = githubApiEndpoint;
    }

    @Provides
    public GithubService getGithubService() {
        return new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(githubApiEndpoint)
                .build().create(GithubService.class);
    }


}
