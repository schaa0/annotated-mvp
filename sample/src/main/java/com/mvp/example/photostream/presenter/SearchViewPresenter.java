package com.mvp.example.photostream.presenter;

import android.os.Looper;

import com.mvp.IMvpEventBus;
import com.mvp.MvpPresenter;
import com.mvp.annotation.BackgroundThread;
import com.mvp.annotation.Presenter;
import com.mvp.example.photostream.view.viewcontract.ISearchView;
import com.mvp.example.photostream.view.container.SearchViewContainer;

import java.util.concurrent.ExecutorService;

@Presenter
public class SearchViewPresenter extends MvpPresenter<ISearchView>{

    private SearchViewContainer.SavedState savedState = null;

    public SearchViewPresenter() { }

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
        view.restoreState(savedState);
    }

    @Override
    public void onViewDetached(ISearchView view) {
        savedState = view.saveCurrentState();
    }

    @BackgroundThread
    public void searchRepositories(String query){
        dispatchEvent(query, RecyclerViewPresenter.class);
    }

}
