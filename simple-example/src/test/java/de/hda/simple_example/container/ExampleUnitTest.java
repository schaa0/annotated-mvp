package de.hda.simple_example.container;

import android.location.LocationManager;
import android.support.v4.app.FragmentContainer;
import android.support.v4.app.FragmentController;
import android.widget.ProgressBar;

import com.mvp.annotation.InjectPresenter;
import com.mvp.annotation.InjectUIView;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.util.ActivityController;

import java.io.IOException;
import java.util.ArrayList;

import javax.inject.Named;

import de.hda.simple_example.ModuleMockGithubService;
import de.hda.simple_example.ModuleMockLocationManager;
import de.hda.simple_example.business.MainPresenter;
import de.hda.simple_example.event.Contract;
import de.hda.simple_example.model.SearchResult;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(constants = de.hda.simple_example.BuildConfig.class, sdk = 21)
public class ExampleUnitTest {

    @InjectUIView private ActivityController<MainActivity> activity;

    private IView view;
    private MainPresenter presenter;
    private LocationManager manager;

    @Before
    public void setUp() throws Exception {

        TestingContext testingContext = new TestingContext();
        view = mock(IView.class);

        manager = mock(LocationManager.class);
        when(manager.getAllProviders()).thenReturn(new ArrayList<String>());

        MainPresenterBuilder.BindingResult binding =
                 new MainPresenterBuilder(new MainActivityController())
                .with(testingContext)
                .with(new ModuleMockGithubService())
                .withInstance(manager)
                .with(new ModuleMockLocationManager())
                .withView(view)
                .build();

        presenter = binding.presenter();
        activity = binding.controller();

    }

    @Test
    public void theViewShouldShowAnErrorMessageIfTheQueryFails(){
        presenter.searchRepositories("query");
        verify(view).showProgressBar();
        verify(view).showError(any(IOException.class));
        verify(view).hideProgressBar();
    }

    @After
    public void tearDown() throws Exception {

    }

}