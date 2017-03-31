package de.hda.simple_example.container;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.mvp.MainActivityController;
import com.mvp.MainActivityPresenterBuilder;
import com.mvp.MainFragmentController;
import com.mvp.MainFragmentPresenterBuilder;
import com.mvp.PresenterType;
import com.mvp.TestCase;
import com.mvp.ViewType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.support.v4.SupportFragmentController;
import org.robolectric.util.ActivityController;

import java.util.Arrays;

import de.hda.simple_example.R;
import de.hda.simple_example.di.TestSimpleApplication;
import de.hda.simple_example.event.Contract;
import de.hda.simple_example.model.Repository;
import de.hda.simple_example.model.SearchResult;
import de.hda.simple_example.presenter.MainActivityPresenter;
import de.hda.simple_example.presenter.MainFragmentPresenter;
import de.hda.simple_example.service.GithubService;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Response;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.framework;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(constants = de.hda.simple_example.BuildConfig.class, sdk = 21, application = TestSimpleApplication.class)
public class MainPresenterIntegrationTest extends TestCase {

    private SupportFragmentController<MainFragment> mainFragmentController;
    private MainActivityView mainFragmentView;
    private ActivityController<MainActivity> mainActivityController;
    private IView mainActivityView;

    private MainFragmentPresenter mainPresenter;
    private MainActivityPresenter activityPresenter;

    private static final String ERROR_MESSAGE = "{ message: an error occured! }";
    private TestSimpleApplication provider;

    @Before
    public void setUp() throws Exception {
        provider = (TestSimpleApplication) RuntimeEnvironment.application;
    }

    @After
    public void tearDown() throws Exception {

    }

    private void buildMainPresenter(ViewType viewType, PresenterType presenterType, GithubService githubService) {
        buildMainPresenter(viewType, presenterType, githubService, new MainFragmentPresenter.State());
    }

    private void buildMainPresenter(ViewType viewType, PresenterType presenterType, final GithubService githubService, MainFragmentPresenter.State state) {

        MainFragment mainFragment = new MainFragment();
        Bundle b = new Bundle();
        b.putParcelable(MainFragmentPresenter.KEY_STATE, state);
        mainFragment.setArguments(b);

        MainFragmentController controller = new MainFragmentController(mainFragment, MainActivity.class);
        MainFragmentPresenterBuilder builder =
                new MainFragmentPresenterBuilder(controller, provider)
                        .parameter(githubService)
                        .in(R.id.container);

        MainFragmentPresenterBuilder.BindingResult binding = configurePresenter(builder, viewType, presenterType);

        mainPresenter = binding.presenter();
        mainFragmentController = binding.controller();
        mainFragmentView = binding.view();

    }

    private void buildMainActivityPresenter(ViewType viewType, PresenterType presenterType){

        MainActivityPresenterBuilder builder = new MainActivityPresenterBuilder(new MainActivityController(), provider);
        MainActivityPresenterBuilder.BindingResult binding = configurePresenter(builder, viewType, presenterType);

        activityPresenter = binding.presenter();
        mainActivityController = binding.controller();
        mainActivityView = binding.view();

    }

    @NonNull
    private GithubService buildFailingGithubService() {
        GithubService githubService = mock(GithubService.class);
        when(githubService.searchRepositories("query", 1))
                .thenReturn(new FailingCallAdapter<SearchResult>() {
                    @Override
                    public Response<SearchResult> buildResponse() {
                        return Response.error(500, ResponseBody.create(MediaType.parse("context/json"), ERROR_MESSAGE));
                    }
                });
        return githubService;
    }

    @NonNull
    private GithubService buildSucceedingGithubService() {
        GithubService githubService = mock(GithubService.class);
        when(githubService.searchRepositories(anyString(), anyInt())).thenReturn(new SucceedingCallAdapter());
        return githubService;
    }

    @Test
    public void shouldShowAnErrorMessageIfRequestingGithubApiFails() {
        buildMainPresenter(ViewType.MOCK, PresenterType.REAL, buildFailingGithubService());
        buildMainActivityPresenter(ViewType.MOCK, PresenterType.REAL);
        mainPresenter.onSearchRepositoriesEvent(new Contract.SearchRepositoriesEvent("query"));
        verify(mainActivityView).showProgressBar();
        verify(mainActivityView).hideProgressBar();
        verify(mainActivityView).showError(eq(new Contract.GithubServiceErrorEvent(ERROR_MESSAGE)));
    }

    @Test
    public void shouldDispatchSearchRepositoriesEventToNextPresenter() {
        buildMainActivityPresenter(ViewType.REAL, PresenterType.MOCK);
        mainActivityController.get().searchView.setQuery("query", true);
        verify(activityPresenter).sendEventSearchRepositories("query");
    }

    @Test
    public void receivedSearchResultsGetsAddedToAdapter(){
        buildMainPresenter(ViewType.MOCK, PresenterType.REAL, buildSucceedingGithubService());
        mainPresenter.onSearchRepositoriesEvent(new Contract.SearchRepositoriesEvent("google"));
        verify(mainFragmentView).setRepositories(ArgumentMatchers.<Repository>anyList());
    }

    @Test
    public void shouldAddReceivedSearchResultsToAdapter(){
        MainFragmentPresenter.State state = new MainFragmentPresenter.State();
        state.page = 2;
        state.query = "google";
        buildMainPresenter(ViewType.REAL, PresenterType.REAL, buildSucceedingGithubService(), state);
        mainFragmentController.get().repositoryAdapter.addAll(Arrays.asList(new Repository[]{Repository.NULL, Repository.NULL}));
        mainPresenter.loadMoreRepositories();
        int expected = 3;
        int actual = mainFragmentController.get().repositoryAdapter.getItemCount();
        assertEquals(expected, actual);
        Repository expectedRepository = mainFragmentController.get().repositoryAdapter.getItemAtPosition(2);
        assertEquals(expectedRepository.getId(), SucceedingCallAdapter.ID_FROM_MESSAGE);
    }

}