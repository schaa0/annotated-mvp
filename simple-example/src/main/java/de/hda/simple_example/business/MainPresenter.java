package de.hda.simple_example.business;

import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;

import com.mvp.MvpPresenter;
import com.mvp.annotation.BackgroundThread;
import com.mvp.annotation.Event;
import com.mvp.annotation.Presenter;
import com.mvp.annotation.ViewEvent;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import de.hda.simple_example.container.IView;
import de.hda.simple_example.container.MainActivity;
import de.hda.simple_example.event.Contract;
import de.hda.simple_example.inject.ModuleGithubService;
import de.hda.simple_example.inject.ModuleLocationManager;
import de.hda.simple_example.model.Repository;
import de.hda.simple_example.model.SearchResult;
import retrofit2.Call;
import retrofit2.Response;

@Presenter(
        viewEvents = {
                @ViewEvent(eventType = Contract.GithubServiceErrorEvent.class, viewMethodName = "showError"),
                @ViewEvent(eventType = Contract.LoadingStartedEvent.class, viewMethodName = "showProgressBar"),
                @ViewEvent(eventType = Contract.LoadingFinishedEvent.class, viewMethodName = "hideProgressBar")
        },
        needsModules = {ModuleGithubService.class, ModuleLocationManager.class},
        viewImplementation = MainActivity.class
)
public class MainPresenter extends MvpPresenter<IView> implements LocationListener {

    protected GithubService service;
    private LocationManager locationManager;
    private SensorManager sensorManager;

    private boolean reachedEndOfStream;
    private boolean isLoading;
    private int page = 1;


    private Contract.LoadingEvent loading = new Contract.LoadingStartedEvent();
    private Contract.LoadingFinishedEvent notLoading = new Contract.LoadingFinishedEvent();
    private String query = "";

    @Inject
    public MainPresenter(GithubService service, @Named("location") LocationManager locationManager, SensorManager sensorManager) {
        this.service = service;
        this.locationManager = locationManager;
        this.sensorManager = sensorManager;
    }

    @Override
    public void onViewsInitialized() {
        super.onViewsInitialized();
    }

    @Override
    public void onViewAttached(IView view) {
        view.setUp();
    }

    @Override
    public void onViewReattached(IView view) {
        view.setUp();
        if (isLoading)
            view.showProgressBar();
        else
            view.hideProgressBar();
    }

    @Override
    public void onViewDetached(IView view) {
        isLoading = view.isLoading();
    }

    @BackgroundThread
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
                dispatchEvent(new Contract.GithubServiceErrorEvent(response.errorBody().string())).toAny();
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
        dispatchEvent(event).toAny();
    }

    public boolean isLoading() {
        return isLoading;
    }

    public boolean reachedEndOfStream() {
        return reachedEndOfStream;
    }

    public void showDetailView(Repository repository) {
        getView().showDetailView(repository);
    }

    @VisibleForTesting
    void setPage(int page){
        this.page = page;
    }

    @VisibleForTesting
    int getPage() {
        return page;
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Event
    public void onStringEvent(String lkhfe){

    }

}
