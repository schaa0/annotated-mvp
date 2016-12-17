package de.hda.simple_example.container;

import com.mvp.MvpPresenter;
import com.mvp.OnPresenterLoadedListener;
import com.mvp.annotation.InjectUIView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.util.ActivityController;

import de.hda.simple_example.business.ExamplePresenter;
import de.hda.simple_example.model.Repository;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Created by Andy on 17.12.2016.
 */

@RunWith(RobolectricTestRunner.class)
@Config(constants = de.hda.simple_example.BuildConfig.class, sdk = 21)
public class ExamplePresenterUnitTest {

    public static final int REPOSITORY_ID = 29847;

    @InjectUIView private ActivityController<ExampleActivity> controller;

    private TestingContext testingContext;
    private Repository repository;

    private IExampleView view;
    private ExamplePresenter examplePresenter;

    @Before
    public void setUp() throws Exception{

        repository = new Repository();
        repository.setId(REPOSITORY_ID);

        testingContext = new TestingContext();

        ExamplePresenterBuilder.BindingResult result =
                new ExamplePresenterBuilder(new ExampleActivityController())
                    .with(testingContext)
                    .withView(mock(IExampleView.class))
                    .withInstance(repository)
                    .build();

        view = result.view();
        controller = result.controller();
        examplePresenter = result.presenter();
    }

    @Test
    public void shouldShowRepositoryIdAfterStart(){
        verify(view).showRepository(repository);
    }

}
