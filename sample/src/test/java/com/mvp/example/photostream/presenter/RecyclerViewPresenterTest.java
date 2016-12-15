package com.mvp.example.photostream.presenter;

import android.os.Looper;

import com.mvp.MvpEventBus;
import com.mvp.MvpPresenterFactory;
import com.mvp.example.photostream.event.Contract;
import com.mvp.example.photostream.model.Repository;
import com.mvp.example.photostream.model.SearchResult;
import com.mvp.example.photostream.service.GithubService;
import com.mvp.example.photostream.view.viewcontract.IRecyclerView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.util.concurrent.RoboExecutorService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executors;

import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = com.mvp.example.BuildConfig.class, sdk = 21)
public class RecyclerViewPresenterTest {

    private RecyclerViewPresenter recyclerViewPresenter;
    private IRecyclerView recyclerView;

    @Before
    public void setUp() throws Exception {
/*        recyclerViewPresenter = new MvpPresenterFactory<IRecyclerView, RecyclerViewPresenter>() {
            @Override
            public RecyclerViewPresenter newInstance() {
                return new RecyclerViewPresenter(new MvpEventBus(), new GithubService() {
                    @Override
                    public Call<SearchResult> searchRepositories(@Query("q") String query, @Query("page") int page) {
                        return new StubbedSearchResult();
                    }
                }, Looper.myLooper(), new RoboExecutorService());
            }
        }.build();
        recyclerView = mock(IRecyclerView.class);
        recyclerViewPresenter.setView(recyclerView);*/
    }

    @Test
    public void onViewAttached() throws Exception {
        recyclerViewPresenter.onViewAttached(recyclerView);
        verify(recyclerView).setUp();
    }

    @Test
    public void onViewReattached() throws Exception {
        recyclerViewPresenter.onViewReattached(recyclerView);
        verify(recyclerView).setUp();
        verify(recyclerView).setRepositories(ArgumentMatchers.<Repository>anyList());
    }

    @Test
    public void onViewDetached() throws Exception {
        recyclerViewPresenter.onViewDetached(recyclerView);
        recyclerViewPresenter.setView(null);
        assertNull(recyclerViewPresenter.getView());
    }

    @Test
    public void searchRepositories() throws Exception {
        recyclerViewPresenter.searchRepositories("query");
        verify(recyclerView).setRepositories(ArgumentMatchers.<Repository>anyList());
        assertEquals(2, recyclerViewPresenter.getPage());
    }

    @Test
    public void loadMoreRepositories() throws Exception {
        recyclerViewPresenter.setPage(2);
        recyclerViewPresenter.loadMoreRepositories();
        verify(recyclerView).addRepositories(ArgumentMatchers.<Repository>anyList());
        assertEquals(3, recyclerViewPresenter.getPage());
    }

    @Test
    public void onRepositoriesLoadedEvent() throws Exception {
        SearchResult searchResult = new SearchResult();
        ArrayList<Repository> repositories = new ArrayList<>();
        searchResult.setRepositories(repositories);
        recyclerViewPresenter.onRepositoriesLoadedEvent(new Contract.RepositoriesLoadedEvent(searchResult, 1));
        verify(recyclerView).setRepositories(repositories);
    }

    private static class StubbedSearchResult implements Call<SearchResult> {

        StubbedSearchResult() {
        }

        @Override
        public Response<SearchResult> execute() throws IOException {
            SearchResult body = new SearchResult();
            body.setRepositories(new ArrayList<Repository>());
            return Response.success(body);
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
    }

}