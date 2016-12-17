package de.hda.simple_example.inject;

import com.mvp.MvpModule;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.hda.simple_example.business.GithubService;
import de.hda.simple_example.container.IView;
import de.hda.simple_example.container.MainActivity;
import de.hda.simple_example.model.Repository;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class ModuleGithubService {

    private String githubApiEndpoint = "https://api.github.com";

    @Provides
    public GithubService getGithubService() {
        return new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(githubApiEndpoint)
                .build().create(GithubService.class);
    }
}
