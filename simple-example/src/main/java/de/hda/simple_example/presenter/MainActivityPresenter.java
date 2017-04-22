package de.hda.simple_example.presenter;

import com.mvp.MvpPresenter;
import com.mvp.annotation.Event;
import com.mvp.annotation.Presenter;

import javax.inject.Inject;

import de.hda.simple_example.R;
import de.hda.simple_example.container.DetailFragment;
import de.hda.simple_example.container.IView;
import de.hda.simple_example.container.MainFragment;
import de.hda.simple_example.di.ComponentActivity;
import de.hda.simple_example.event.Contract;
import de.hda.simple_example.model.Repository;

@Presenter(components = {ComponentActivity.class} )
public class MainActivityPresenter extends MvpPresenter<IView> {

    private Settings settings;

    @Inject
    public MainActivityPresenter(Settings settings){
        this.settings = settings;
    }

    private boolean isLoading;

    private boolean shouldSetLastQueryFromCache = false;

    protected MainActivityPresenter() {}

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

    @Override
    public void onNavigationEnabled() {
        super.onNavigationEnabled();
        if (!this.isReattached()) {
            getFragmentRouter()
                    .navigateTo(MainFragment.class)
                    .replace(R.id.container, MainFragment.TAG)
                    .putExtra(MainFragmentPresenter.KEY_STATE, new MainFragmentPresenter.State())
                    .commit();
            if (getView().isDetailContainerPresent()) {
                getFragmentRouter()
                        .navigateTo(DetailFragment.class)
                        .replace(R.id.container_detail, DetailFragment.TAG)
                        .putExtra(DetailFragment.KEY_REPOSITORY, Repository.NULL)
                        .commit();
            }
        }
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
