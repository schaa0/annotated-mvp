package com.mvp.example.photostream.presenter;

import android.os.Looper;
import android.support.annotation.VisibleForTesting;

import com.mvp.IMvpEventBus;
import com.mvp.MvpPresenter;
import com.mvp.annotation.Event;
import com.mvp.annotation.Presenter;
import com.mvp.annotation.ViewEvent;
import com.mvp.example.photostream.event.Contract;
import com.mvp.example.photostream.view.viewcontract.IProgressBar;

import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

@Presenter(
        viewEvents = {
                @ViewEvent(eventType = Contract.LoadingStartedEvent.class, viewMethodName = "showProgressBar"),
                @ViewEvent(eventType = Contract.LoadingFinishedEvent.class, viewMethodName = "hideProgressBar"),
        }
)
public class ProgressBarPresenter extends MvpPresenter<IProgressBar> {

    private boolean isLoading;

    public ProgressBarPresenter() { }

    @Inject
    public ProgressBarPresenter(IMvpEventBus eventBus){
        super(eventBus);
    }

    @VisibleForTesting
    public ProgressBarPresenter(IMvpEventBus eventBus, Looper looper, ExecutorService executorService) {
        super(eventBus, looper, executorService);
    }

    @Override
    public void onViewAttached(IProgressBar view) {

    }

    @Override
    public void onViewReattached(IProgressBar view) {
        if (isLoading)
            view.showProgressBar();
        else
            view.hideProgressBar();
    }

    @Override
    public void onViewDetached(IProgressBar view) {
        isLoading = view.isLoading();
    }

}
