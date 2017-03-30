package de.hda.simple_example.business;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.mvp.MvpPresenter;
import com.mvp.annotation.BackgroundThread;
import com.mvp.annotation.Event;
import com.mvp.annotation.Presenter;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import de.hda.simple_example.container.IMainView;
import de.hda.simple_example.di.ComponentApplication;
import de.hda.simple_example.di.ModuleMainPresenterState;
import de.hda.simple_example.event.Contract;
import de.hda.simple_example.model.Repository;
import de.hda.simple_example.model.SearchResult;
import retrofit2.Call;
import retrofit2.Response;

@Presenter(
        needsModules = {ModuleMainPresenterState.class},
        needsComponents = {ComponentApplication.class}
)
public class MainPresenter extends MvpPresenter<IMainView> {

    public static final String KEY_STATE = "KEY_STATE";

    private State state;
    protected GithubService githubService;

    boolean isLoading;
    String orientationTag = "port";

    private final Contract.LoadingEvent loading = new Contract.LoadingStartedEvent();
    private final Contract.LoadingFinishedEvent notLoading = new Contract.LoadingFinishedEvent();

    protected MainPresenter() {}

    @Inject
    public MainPresenter(State state, GithubService githubService) {
        Log.e(MainPresenter.class.getName(), String.valueOf(githubService.hashCode()));
        this.state = state;
        this.githubService = githubService;
    }

    public Repository getLastSelectedRepository(){
        return state.lastSelectedRepository;
    }

    @Override
    public void onViewsInitialized() {
        super.onViewsInitialized();
    }

    @Override
    public void onViewAttached(IMainView view) {
        orientationTag = getView().provideOrientationTag();
    }

    @Override
    public void onViewReattached(IMainView view) {
        orientationTag = getView().provideOrientationTag();
        if (orientationTag.equals("sw600dp|port"))
            internalShowDetailView();
    }

    @Override
    public void onViewDetached(IMainView view) {

    }

    private void searchRepositories(String query){
        if (internalSearchRepositories(query, 1)){
            this.state.query = query;
            this.state.page = 2;
        }
    }

    @BackgroundThread
    public void loadMoreRepositories(){
        if (internalSearchRepositories(state.query, state.page)){
            state.page++;
        }
    }

    private boolean internalSearchRepositories(String query, final int page){
        boolean result = false;
        try {
            dispatchLoadingStateChangedEvent(loading);
            Call<SearchResult> repositories = githubService.searchRepositories(query, page);
            final Response<SearchResult> response = repositories.execute();
            if (response.code() == 200) {
                result = true;
                submitOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onRepositoriesLoaded(response.body(), page == 1);
                    }
                });
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

    private void onRepositoriesLoaded(SearchResult searchResult, boolean isFirstPage) {
        List<Repository> repositories = searchResult.getRepositories();
        state.reachedEndOfStream = repositories.isEmpty();
        if (isFirstPage)
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
        return state.reachedEndOfStream;
    }

    public void showDetailView(Repository repository){
        state.lastSelectedRepository = repository;
        internalShowDetailView();
    }

    private void internalShowDetailView() {

        final Repository repository = state.lastSelectedRepository;
        String strId = repository != Repository.NULL ? String.valueOf(repository.getId()) : "";
        dispatchEvent(strId).toAny();

        if (shouldShowDetailViewInAnotherActivity()){
            getView().showDetailViewInActivity(state.lastSelectedRepository);
        }

    }

    private boolean shouldShowDetailViewInAnotherActivity() {
        return orientationTag.contains("port") && state.lastSelectedRepository != null;
    }

    @Event(thread = Event.BACKGROUND_THREAD)
    public void onSearchRepositoriesEvent(Contract.SearchRepositoriesEvent event){
        searchRepositories(event.getQuery());
    }

    public static class State implements Parcelable {

        public int page;
        public String query;
        public boolean reachedEndOfStream;
        public Repository lastSelectedRepository;

        public State(){
            this(1, "", false, Repository.NULL);
        }

        public State(int page, String query, boolean reachedEndOfStream, Repository lastSelectedRepository){
            this.page = page;
            this.query = query;
            this.reachedEndOfStream = reachedEndOfStream;
            this.lastSelectedRepository = lastSelectedRepository;
        }

        protected State(Parcel in) {
            page = in.readInt();
            query = in.readString();
            reachedEndOfStream = in.readByte() != 0;
            lastSelectedRepository = in.readParcelable(Repository.class.getClassLoader());
        }

        public static final Creator<State> CREATOR = new Creator<State>() {
            @Override
            public State createFromParcel(Parcel in) {
                return new State(in);
            }

            @Override
            public State[] newArray(int size) {
                return new State[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(page);
            dest.writeString(query);
            dest.writeByte((byte) (reachedEndOfStream ? 1 : 0));
            dest.writeParcelable(lastSelectedRepository, flags);
        }
    }
}
