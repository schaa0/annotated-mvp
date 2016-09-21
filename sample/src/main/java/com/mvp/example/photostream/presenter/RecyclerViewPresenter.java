package com.mvp.example.photostream.presenter;

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

import retrofit2.Call;
import retrofit2.Response;

@Presenter
public class RecyclerViewPresenter extends MvpPresenter<IRecyclerView> {

    private boolean reachedEndOfStream;
    private boolean isLoading;

    private GithubService service;
    private int page = 1;

    private Contract.LoadingEvent loading = new Contract.LoadingStartedEvent();
    private Contract.LoadingFinishedEvent notLoading = new Contract.LoadingFinishedEvent();
    private String query = "";

    private List<Repository> repositories = new ArrayList<>();

    public RecyclerViewPresenter() { }

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
        view.setRepositories(repositories);
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
                dispatchEvent(new Contract.RepositoriesLoadedEvent(response.body(), page));
            }else
                dispatchEvent(new IOException(response.errorBody().toString()), GithubRepositoryPresenter.class);
        } catch (IOException e) {
            e.printStackTrace();
            dispatchEvent(e);
        }finally {
            dispatchLoadingStateChangedEvent(notLoading);
        }
        return result;
    }

    @Event
    public void onRepositoriesLoadedEvent(Contract.RepositoriesLoadedEvent e){
        SearchResult searchResult = e.getSearchResult();
        List<Repository> repositories = searchResult.getRepositories();
        this.repositories.addAll(repositories);
        reachedEndOfStream = repositories.isEmpty();
        if (e.isFirstPage())
            getView().setRepositories(repositories);
        else
            getView().addRepositories(repositories);
    }

    private void dispatchLoadingStateChangedEvent(Contract.LoadingEvent event) {
        isLoading = event instanceof Contract.LoadingStartedEvent;
        dispatchEvent(event, ProgressBarPresenter.class);
    }

    public boolean isLoading() {
        return isLoading;
    }

    public boolean reachedEndOfStream() {
        return reachedEndOfStream;
    }

    public void showDetailView(Repository repository) {

    }
}
