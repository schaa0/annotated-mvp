package com.mvp.example.photostream.presenter;

import android.os.Looper;
import android.support.annotation.VisibleForTesting;
import android.widget.Toast;

import com.mvp.IMvpEventBus;
import com.mvp.MvpPresenter;
import com.mvp.annotation.BackgroundThread;
import com.mvp.annotation.Event;
import com.mvp.annotation.Presenter;
import com.mvp.example.photostream.event.Contract;
import com.mvp.example.photostream.model.Repository;
import com.mvp.example.photostream.model.SearchResult;
import com.mvp.example.photostream.service.GithubService;
import com.mvp.example.photostream.view.viewcontract.IRecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Response;

@Presenter
public class RecyclerViewPresenter extends MvpPresenter<IRecyclerView> {

    protected GithubService service;

    private boolean reachedEndOfStream;
    private boolean isLoading;
    private int page = 1;

    private Contract.LoadingEvent loading = new Contract.LoadingStartedEvent();
    private Contract.LoadingFinishedEvent notLoading = new Contract.LoadingFinishedEvent();
    private String query = "";

    public RecyclerViewPresenter() {

    }

    @Inject
    public RecyclerViewPresenter(IMvpEventBus eventBus, GithubService service) {
        super(eventBus);
        this.service = service;
    }

    @Override
    public void onViewAttached(IRecyclerView view) {
        view.setUp();
    }

    @Override
    public void onViewReattached(IRecyclerView view) {
        view.setUp();
    }

    @Override
    public void onViewDetached(IRecyclerView view) {

    }

    @Event(thread = Event.BACKGROUND_THREAD)
    public void searchRepositories(String query){
        if (internalSearchRepositories(query, 1)){
            this.query = query;
            this.page = 2;
        }
    }

    @BackgroundThread
    public void loadMoreRepositories(){
        if (internalSearchRepositories(query, page)){
            page++;
        }
    }

    private boolean internalSearchRepositories(String query, int page){
        boolean result = false;
        try {
            dispatchLoadingStateChangedEvent(loading);
            Call<SearchResult> repositories = service.searchRepositories(query, page);
            Response<SearchResult> response = repositories.execute();
            if (response.code() == 200) {
                result = true;
                dispatchEvent(new Contract.RepositoriesLoadedEvent(response.body(), page)).toAny();
            }else
                dispatchEvent(new IOException(response.errorBody().toString())).to(GithubRepositoryPresenter.class);
        } catch (IOException e) {
            e.printStackTrace();
            dispatchEvent(e).toAny();
        }finally {
            dispatchLoadingStateChangedEvent(notLoading);
        }
        return result;
    }

    @Event
    public void onRepositoriesLoadedEvent(Contract.RepositoriesLoadedEvent e){
        SearchResult searchResult = e.getSearchResult();
        List<Repository> repositories = searchResult.getRepositories();
        reachedEndOfStream = repositories.isEmpty();
        if (e.isFirstPage())
            getView().setRepositories(repositories);
        else
            getView().addRepositories(repositories);
    }

    private void dispatchLoadingStateChangedEvent(Contract.LoadingEvent event) {
        isLoading = event instanceof Contract.LoadingStartedEvent;
        dispatchEvent(event).to(ProgressBarPresenter.class);
    }

    public boolean isLoading() {
        return isLoading;
    }

    public boolean reachedEndOfStream() {
        return reachedEndOfStream;
    }

    public void showDetailView(Repository repository) {
        getView().showToast(repository);
    }

    @VisibleForTesting
    void setPage(int page){
        this.page = page;
    }

    @VisibleForTesting
    int getPage() {
        return page;
    }
}
