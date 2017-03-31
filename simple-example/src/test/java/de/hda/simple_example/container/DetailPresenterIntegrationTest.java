package de.hda.simple_example.container;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.mvp.DetailFragmentController;
import com.mvp.DetailFragmentPresenterBuilder;
import com.mvp.MainFragmentController;
import com.mvp.MainFragmentPresenterBuilder;
import com.mvp.PresenterType;
import com.mvp.TestCase;
import com.mvp.ViewType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.support.v4.SupportFragmentController;

import java.util.Arrays;

import de.hda.simple_example.R;
import de.hda.simple_example.di.TestSimpleApplication;
import de.hda.simple_example.model.Repository;
import de.hda.simple_example.presenter.DetailFragmentPresenter;
import de.hda.simple_example.presenter.MainFragmentPresenter;
import de.hda.simple_example.service.GithubService;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


@RunWith(RobolectricTestRunner.class)
@Config(constants = de.hda.simple_example.BuildConfig.class, sdk = 21, application = TestSimpleApplication.class)
public class DetailPresenterIntegrationTest extends TestCase {

    private SupportFragmentController<DetailFragment> detailFragmentController;
    private DetailFragmentPresenter detailPresenter;
    private DetailFragmentView detailFragmentView;

    private SupportFragmentController<MainFragment> mainFragmentController;
    private MainFragmentPresenter mainPresenter;
    private MainActivityView mainFragmentView;

    private TestSimpleApplication provider;

    @Before
    public void setUp() throws Exception {
        this.provider = (TestSimpleApplication) RuntimeEnvironment.application;
    }

    private void buildMainPresenter(ViewType viewType, PresenterType presenterType) {
        buildMainPresenter(viewType, presenterType, new MainFragmentPresenter.State());
    }

    private void buildMainPresenter(ViewType viewType, PresenterType presenterType, MainFragmentPresenter.State state) {

        MainFragment fragment = new MainFragment();
        Bundle b = new Bundle();
        b.putParcelable(MainFragmentPresenter.KEY_STATE, state);
        fragment.setArguments(b);

        MainFragmentController controller = new MainFragmentController(fragment, MainActivity.class);
        MainFragmentPresenterBuilder builder =
                new MainFragmentPresenterBuilder(controller, provider)
                        .parameter(mock(GithubService.class))
                        .in(R.id.container);

        MainFragmentPresenterBuilder.BindingResult binding = configurePresenter(builder, viewType, presenterType);

        mainPresenter = binding.presenter();
        mainFragmentController = binding.controller();
        mainFragmentView = binding.view();
    }

    private void buildDetailPresenter(ViewType viewType, PresenterType presenterType, Repository repository, Bundle bundle) {

        DetailFragment fragment = new DetailFragment();
        Bundle b = new Bundle();
        b.putParcelable(DetailFragment.KEY_REPOSITORY, repository);
        fragment.setArguments(b);

        DetailFragmentController controller = new DetailFragmentController(fragment, DetailActivity.class);

        DetailFragmentPresenterBuilder builder =
                new DetailFragmentPresenterBuilder(controller, provider)
                        .withSavedInstanceState(bundle)
                        .parameter(mock(GithubService.class))
                        .in(R.id.container);

        DetailFragmentPresenterBuilder.BindingResult binding = configurePresenter(builder, viewType, presenterType);

        detailPresenter = binding.presenter();
        detailFragmentController = binding.controller();
        detailFragmentView = binding.view();
    }

    private void buildDetailPresenter(ViewType viewType, PresenterType presenterType, Repository repository) {
        buildDetailPresenter(viewType, presenterType, repository, null);
    }

    @NonNull
    private Repository createRepository(int repositoryId) {
        Repository repository = new Repository();
        repository.setId(repositoryId);
        return repository;
    }

    @Test
    public void shouldCallMethodOnViewAfterCreation(){
        Repository repository = createRepository(321);
        buildDetailPresenter(ViewType.MOCK, PresenterType.REAL, repository);
        verify(detailFragmentView).showId(String.valueOf(repository.getId()));
    }

    @Test
    public void idIsShownAfterRecreation(){
        Repository repository = createRepository(2428);
        buildDetailPresenter(ViewType.MOCK, PresenterType.MOCK, repository, new Bundle());
        verify(detailPresenter).onViewReattached(detailFragmentView);
        buildDetailPresenter(ViewType.MOCK, PresenterType.REAL, repository, new Bundle());
        verify(detailFragmentView).showId(String.valueOf(repository.getId()));
    }

    @Test
    public void shouldShowIdInTextViewAfterDetailFragmentIsCreated() {
        Repository repository = createRepository(123);
        buildDetailPresenter(ViewType.REAL, PresenterType.REAL, repository);
        String expected = String.valueOf(repository.getId());
        String actual = detailFragmentController.get().textView.getText().toString();
        assertEquals(expected, actual);
    }

    @Test
    public void shouldShowIdInTextViewWhenItemInListIsSelected() {
        buildMainPresenter(ViewType.REAL, PresenterType.REAL);
        buildDetailPresenter(ViewType.REAL, PresenterType.REAL, Repository.NULL);
        Repository repository = createRepository(456);
        mainFragmentController.get().setRepositories(Arrays.asList(new Repository[]{repository}));
        mainFragmentController.get().onItemClick(0);
        String actual = detailFragmentController.get().textView.getText().toString();
        String expected = String.valueOf(repository.getId());
        assertEquals(expected, actual);
    }

    @Test
    public void shouldNotShowAnIdInDetailFragmentIfNoRepositoryIsSet() {
        buildDetailPresenter(ViewType.MOCK, PresenterType.REAL, Repository.NULL);
        verify(detailFragmentView).showId(eq(""));
    }

    @Test
    public void shouldNotShowAnIdInDetailFragmentWhenItIsRecreatedAndNoRepositoryIsSet() {
        buildDetailPresenter(ViewType.MOCK, PresenterType.REAL, Repository.NULL, new Bundle());
        verify(detailFragmentView).showId(eq(""));
    }

}