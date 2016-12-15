package com.mvp.example.photostream.presenter;

import android.os.Looper;

import com.mvp.IMvpEventBus;
import com.mvp.MvpPresenter;
import com.mvp.annotation.BackgroundThread;
import com.mvp.annotation.Presenter;
import com.mvp.example.photostream.view.viewcontract.ISearchView;
import com.mvp.example.photostream.view.container.SearchViewContainer;

import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Singleton;

@Presenter
public class SearchViewPresenter extends MvpPresenter<ISearchView>{

    public SearchViewPresenter() { }

    @Inject
    public SearchViewPresenter(IMvpEventBus eventBus) {
        super(eventBus);
    }

    public SearchViewPresenter(IMvpEventBus eventBus, Looper looper, ExecutorService executorService) {
        super(eventBus, looper, executorService);
    }

    @Override
    public void onViewAttached(ISearchView view) {
        view.setUp();
    }

    @Override
    public void onViewReattached(ISearchView view) {
        view.setUp();
    }

    @Override
    public void onViewDetached(ISearchView view) {

    }

    @BackgroundThread
    public void searchRepositories(String query){
        dispatchEvent(query).to(RecyclerViewPresenter.class);
    }

}
