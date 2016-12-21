package de.hda.simple_example.business;

import com.mvp.MvpPresenter;
import com.mvp.annotation.Event;
import com.mvp.annotation.Presenter;
import com.mvp.annotation.ViewEvent;

import javax.inject.Inject;

import de.hda.simple_example.container.IView;
import de.hda.simple_example.container.MainActivity;
import de.hda.simple_example.event.Contract;

/**
 * Created by Andy on 18.12.2016.
 */

@Presenter
public class ActivityPresenter extends MvpPresenter<IView> {

    private boolean isLoading;

    @Inject
    public ActivityPresenter(){

    }

    @Override
    public void onViewAttached(IView view) {

    }

    @Override
    public void onViewReattached(IView view) {
        showOrHideProgressBar();
    }

    private void showOrHideProgressBar() {
        if (isLoading)
            getView().showProgressBar();
        else
            getView().hideProgressBar();
    }

    @Override
    public void onViewDetached(IView view) {

    }

    @Event
    public void onLoadingStateChanged(Contract.LoadingEvent loadingEvent){
        isLoading = loadingEvent instanceof Contract.LoadingStartedEvent;
        showOrHideProgressBar();
    }

    @Event(thread = Event.UI_THREAD)
    public void onGithubServiceError(Contract.GithubServiceErrorEvent errorEvent){
        getView().showError(errorEvent);
    }

    public void sendEventSearchRepositories(String query) {
        dispatchEvent(new Contract.SearchRepositoriesEvent(query)).toAny();
    }
}
