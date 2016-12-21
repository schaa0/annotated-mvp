package de.hda.simple_example.container;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.mvp.PresenterType;
import com.mvp.TestCase;
import com.mvp.ViewType;
import com.mvp.annotation.InjectUIView;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.support.v4.SupportFragmentController;

import de.hda.simple_example.R;
import de.hda.simple_example.business.DetailPresenter;
import de.hda.simple_example.business.GithubService;
import de.hda.simple_example.business.MainPresenter;
import de.hda.simple_example.inject.ModuleLocationManager;
import de.hda.simple_example.model.Repository;
import edu.emory.mathcs.backport.java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@RunWith(RobolectricTestRunner.class)
@Config(constants = de.hda.simple_example.BuildConfig.class, sdk = 21)
public class DetailPresenterUnitTest extends TestCase {

    @InjectUIView
    private SupportFragmentController<DetailFragment> detailFragmentController;
    private DetailPresenter detailPresenter;
    private IDetailView detailFragmentView;

    @InjectUIView
    private SupportFragmentController<MainFragment> mainFragmentController;
    private MainPresenter mainPresenter;
    private IMainView mainFragmentView;

    private void buildMainPresenter(ViewType viewType, PresenterType presenterType) {
        buildMainPresenter(viewType, presenterType, new MainPresenter.State());
    }

    private void buildMainPresenter(ViewType viewType, PresenterType presenterType, MainPresenter.State state) {
        MainPresenterBuilder builder =
                new MainPresenterBuilder(new MainFragmentController(MainFragment.newInstance(state), MainActivity.class))
                        .with(new ModuleLocationManager(RuntimeEnvironment.application))
                        .withInstance(mock(GithubService.class))
                        .in(R.id.container);

        MainPresenterBuilder.BindingResult binding = configurePresenter(builder, viewType, presenterType);

        mainPresenter = binding.presenter();
        mainFragmentController = binding.controller();
        mainFragmentView = binding.view();
    }

    private void buildDetailPresenter(ViewType viewType, PresenterType presenterType, Repository repository, Bundle bundle) {
        DetailFragment fragment = DetailFragment.newInstance(repository);
        DetailPresenterBuilder builder =
                new DetailPresenterBuilder(new DetailFragmentController(fragment, DetailActivity.class))
                        .withSavedInstanceState(bundle)
                        .in(R.id.container);

        DetailPresenterBuilder.BindingResult binding = configurePresenter(builder, viewType, presenterType);

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
