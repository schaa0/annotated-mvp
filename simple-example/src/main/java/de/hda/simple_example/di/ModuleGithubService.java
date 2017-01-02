package de.hda.simple_example.di;

import dagger.Module;
import dagger.Provides;
import de.hda.simple_example.business.GithubService;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class ModuleGithubService {

    private String githubApiEndpoint = "https://api.github.com";

    @Provides
    @ApplicationScope
    public GithubService getGithubService() {
        return new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(githubApiEndpoint)
                .build().create(GithubService.class);
    }
}
