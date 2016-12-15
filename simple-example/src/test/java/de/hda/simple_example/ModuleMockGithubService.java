package de.hda.simple_example;

import java.io.IOException;

import dagger.Provides;
import de.hda.simple_example.business.GithubService;
import de.hda.simple_example.container.IView;
import de.hda.simple_example.container.MainActivity;
import de.hda.simple_example.inject.ModuleGithubService;
import de.hda.simple_example.model.SearchResult;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Andy on 02.12.2016.
 */

public class ModuleMockGithubService extends ModuleGithubService {

    @Override
    @Provides
    public GithubService getGithubService() {
        GithubService githubService = mock(GithubService.class);
        when(githubService.searchRepositories("query", 1)).thenReturn(new Call<SearchResult>() {
            @Override
            public Response<SearchResult> execute() throws IOException {
                return Response.error(500, ResponseBody.create(MediaType.parse("application/json"), "{}"));
            }

            @Override
            public void enqueue(Callback<SearchResult> callback) {

            }

            @Override
            public boolean isExecuted() {
                return false;
            }

            @Override
            public void cancel() {

            }

            @Override
            public boolean isCanceled() {
                return false;
            }

            @Override
            public Call<SearchResult> clone() {
                return null;
            }

            @Override
            public Request request() {
                return null;
            }
        });
        return githubService;
    }


}
