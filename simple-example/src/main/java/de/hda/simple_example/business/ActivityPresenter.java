package de.hda.simple_example.business;

import com.mvp.MvpPresenter;
import com.mvp.annotation.Event;
import com.mvp.annotation.Presenter;

import javax.inject.Inject;

import de.hda.simple_example.container.IView;
import de.hda.simple_example.di.ComponentApplication;
import de.hda.simple_example.event.Contract;

@Presenter(needsComponents = {ComponentApplication.class} )
public class ActivityPresenter extends MvpPresenter<IView> {

    private boolean isLoading;
    private Settings settings;
    boolean shouldSetLastQueryFromCache = false;

    protected ActivityPresenter() {}

    @Inject
    public ActivityPresenter(Settings settings){
        this.settings = settings;
    }

    @Override
    public void onViewAttached(IView view) {
        shouldSetLastQueryFromCache = true;
    }

    @Override
    public void onViewReattached(IView view) {
        showOrHideProgressBar();
        shouldSetLastQueryFromCache = false;
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

    public boolean isLoading() {
        return isLoading;
    }

    @Event(condition = "#.isLoading() == !this.isLoading()")
    public void onLoadingStateChanged(Contract.LoadingEvent loadingEvent){
        isLoading = loadingEvent instanceof Contract.LoadingStartedEvent;
        showOrHideProgressBar();
    }

    @Event
    public void onGithubServiceError(Contract.GithubServiceErrorEvent errorEvent){
        getView().showError(errorEvent);
    }

    public void sendEventSearchRepositories(String query) {
        settings.saveLastQuery(query);
        dispatchEvent(new Contract.SearchRepositoriesEvent(query)).toAny();
    }

    public void onSearchViewInitialized() {
        if (shouldSetLastQueryFromCache) {
            String lastQuery = settings.readLastQuery();
            if (!lastQuery.isEmpty())
                getView().setLastQuery(lastQuery);
        }
    }
}
