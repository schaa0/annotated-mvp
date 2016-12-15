package com.mvp.example.photostream.presenter;

import android.os.Looper;

import com.mvp.IMvpEventBus;
import com.mvp.MvpEventBus;
import com.mvp.MvpPresenterFactory;
import com.mvp.example.photostream.view.viewcontract.IMainActivityView;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.util.concurrent.RoboExecutorService;

import static org.mockito.Mockito.mock;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = com.mvp.example.BuildConfig.class, sdk = 21)
public class GithubRepositoryPresenterTest {

    private GithubRepositoryPresenter presenter;
    private IMvpEventBus eventBus;
    private IMainActivityView view;

    @Before
    public void setUp() throws Exception {
        eventBus =  new MvpEventBus();
        view = mock(IMainActivityView.class);
        presenter = new MvpPresenterFactory<IMainActivityView, GithubRepositoryPresenter>() {
            @Override
            public GithubRepositoryPresenter create() {
                return new GithubRepositoryPresenter(eventBus, Looper.myLooper(), new RoboExecutorService());
            }
        }.build();
        presenter.setView(view);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void onViewAttached() throws Exception {
        presenter.onViewAttached(view);
    }

    @Test
    public void onViewReattached() throws Exception {
        presenter.onViewReattached(view);
    }

    @Test
    public void onViewDetached() throws Exception {
        presenter.onViewDetached(view);
    }

}