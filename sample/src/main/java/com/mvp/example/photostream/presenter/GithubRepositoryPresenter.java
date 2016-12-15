package com.mvp.example.photostream.presenter;

import android.os.Looper;
import android.support.annotation.VisibleForTesting;

import com.mvp.IMvpEventBus;
import com.mvp.MvpPresenter;
import com.mvp.annotation.Presenter;
import com.mvp.annotation.ViewEvent;
import com.mvp.example.photostream.view.viewcontract.IMainActivityView;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

@Presenter(
        viewEvents = {
                @ViewEvent(eventType = IOException.class, viewMethodName = "showError")
        }
)
public class GithubRepositoryPresenter extends MvpPresenter<IMainActivityView> {

    public GithubRepositoryPresenter() { }

    @Inject
    public GithubRepositoryPresenter(IMvpEventBus eventBus) {
        super(eventBus);
    }

    @VisibleForTesting
    GithubRepositoryPresenter(IMvpEventBus eventBus, Looper looper, ExecutorService executorService) {
        super(eventBus, looper, executorService);
    }

    @Override
    public void onViewAttached(IMainActivityView view) { }

    @Override
    public void onViewReattached(IMainActivityView view) { }

    @Override
    public void onViewDetached(IMainActivityView view) { }

}
