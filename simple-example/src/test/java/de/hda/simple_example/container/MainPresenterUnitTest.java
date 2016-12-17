package de.hda.simple_example.container;

import android.support.annotation.NonNull;
import android.view.View;

import com.mvp.annotation.InjectUIView;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowToast;
import org.robolectric.util.ActivityController;

import de.hda.simple_example.business.ExamplePresenter;
import de.hda.simple_example.business.GithubService;
import de.hda.simple_example.business.MainPresenter;
import de.hda.simple_example.event.Contract;
import de.hda.simple_example.inject.ModuleLocationManager;
import de.hda.simple_example.model.SearchResult;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Response;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(constants = de.hda.simple_example.BuildConfig.class, sdk = 21)
public class MainPresenterUnitTest {

    private enum ViewType {
        TYPE_ACTIVITY,
        TYPE_MOCK_VIEW
    }

    @InjectUIView private ActivityController<MainActivity> activity;

    private IView view;

    private MainPresenter presenter;
    private TestingContext testingContext = new TestingContext();

    private static final String ERROR_MESSAGE = "{ message: an error occured! }";

    @Before
    public void setUp() throws Exception {

    }

    private void buildMainPresenter(ViewType viewType) {

        GithubService githubService = buildFailingGithubService();

        MainPresenterBuilder builder =
                new MainPresenterBuilder(new MainActivityController())
                        .withInstance(githubService)
                        .with(new ModuleLocationManager(RuntimeEnvironment.application));

        MainPresenterBuilder.BindingResult binding = internalBuildMainPresenter(builder, viewType);

        presenter = binding.presenter();
        activity = binding.controller();
        view = binding.view();
    }

    private MainPresenterBuilder.BindingResult internalBuildMainPresenter(MainPresenterBuilder builder, ViewType viewType){
        builder.with(testingContext);
        if (viewType == ViewType.TYPE_ACTIVITY)
            builder.withActivityAsView();
        else
            builder.withView(mock(IView.class));
        return builder.build();
    }

    @NonNull
    private GithubService buildFailingGithubService() {
        GithubService githubService = mock(GithubService.class);
        when(githubService.searchRepositories("query", 1))
                .thenReturn(new CallAdapter<SearchResult>() {
                    @Override
                    public Response<SearchResult> buildResponse() {
                        return Response.error(500, ResponseBody.create(MediaType.parse("application/json"), ERROR_MESSAGE));
                    }
                });
        return githubService;
    }

    @Test
    public void theViewShouldShowAnErrorMessageIfTheQueryFails(){
        buildMainPresenter(ViewType.TYPE_MOCK_VIEW);
        presenter.searchRepositories("query");
        verify(view).showProgressBar();
        verify(view).showError(eq(new Contract.GithubServiceErrorEvent(ERROR_MESSAGE)));
        verify(view).hideProgressBar();
    }

    @Test
    public void theActivityShouldShowAToastWhenErrorOccures() {
        buildMainPresenter(ViewType.TYPE_ACTIVITY);
        presenter.searchRepositories("query");
        String textOfLatestToast = ShadowToast.getTextOfLatestToast();
        assertEquals(textOfLatestToast, ERROR_MESSAGE);
        assertEquals(View.GONE, activity.get().progressBar.getVisibility());
    }

    @After
    public void tearDown() throws Exception {

    }

}