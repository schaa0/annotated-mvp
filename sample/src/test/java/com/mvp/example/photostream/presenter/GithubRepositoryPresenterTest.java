package com.mvp.example.photostream.presenter;

import android.os.Looper;

import com.mvp.IMvpEventBus;
import com.mvp.MvpEventBus;
import com.mvp.annotation.processor.GithubRepositoryPresenterProxy;
import com.mvp.example.photostream.view.viewcontract.IMainActivityView;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.util.concurrent.RoboExecutorService;

import static org.mockito.Mockito.mock;


public class GithubRepositoryPresenterTest {

    private GithubRepositoryPresenter presenter;
    private IMvpEventBus eventBus;
    private IMainActivityView view;

    @Before
    public void setUp() throws Exception {
        eventBus =  new MvpEventBus();
        view = mock(IMainActivityView.class);
        GithubRepositoryPresenter presenterImpl = new GithubRepositoryPresenter(eventBus, Looper.myLooper(), new RoboExecutorService());
        presenter = new GithubRepositoryPresenterProxy(presenterImpl);
        presenter.onInitialize();
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