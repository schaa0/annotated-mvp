package com.mvp.example;

import android.os.Handler;
import android.os.Looper;

import com.mvp.*;
import com.mvp.example.photostream.event.Contract;
import com.mvp.example.photostream.presenter.ProgressBarPresenter;
import com.mvp.example.photostream.presenter.RecyclerViewPresenter;
import com.mvp.example.photostream.view.viewcontract.IProgressBar;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.util.concurrent.RoboExecutorService;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = com.mvp.example.BuildConfig.class, sdk = 21)
public class ExampleUnitTest {

    private ProgressBarPresenter progressBarPresenter;
    private IProgressBar progressBarView;
    private IMvpEventBus eventBus;

    @Before
    public void setUp() throws Exception {
        eventBus = new MvpEventBus();
        progressBarPresenter = new MvpPresenterFactory<IProgressBar, ProgressBarPresenter>(){
            @Override
            public ProgressBarPresenter create() {
                return new ProgressBarPresenter(eventBus, Looper.myLooper(), new RoboExecutorService());
            }
        }.build();
        progressBarView = mock(IProgressBar.class);
        progressBarPresenter.setView(progressBarView);
    }

    @After
    public void tearDown() throws Exception {
        progressBarPresenter.onViewDetached(progressBarView);
        progressBarPresenter.onDestroyed();
    }


    @Test
    public void viewShouldBeAttachedToPresenter() {
        progressBarPresenter.onViewAttached(progressBarView);
        assertNotNull(progressBarPresenter.getView());
    }

    @Test
    public void viewShouldBeDetachedFromPresenter() {
        progressBarPresenter.onViewAttached(progressBarView);
        assertNotNull(progressBarPresenter.getView());
        progressBarPresenter.onViewDetached(progressBarView);
    }

    @Test
    public void shouldShowProgressBar() {
        progressBarPresenter.onViewAttached(progressBarView);
        eventBus.dispatchEvent(new Contract.LoadingStartedEvent(), ProgressBarPresenter.class);
        verify(progressBarView).showProgressBar();
    }

    @Test
    public void progressBarPresenterShouldReceiveLoadingEvent(){
        ProgressBarPresenter progressBarPresenter = new ProgressBarPresenter(eventBus, Looper.myLooper(), new RoboExecutorService());
        progressBarPresenter.onInitialize();
        IProgressBar progressBar = mock(IProgressBar.class);
        progressBarPresenter.setView(progressBar);
        eventBus.dispatchEvent(new Contract.LoadingStartedEvent());
        verify(progressBar).showProgressBar();
    }

    @Test
    public void shouldHideProgressBar() {
        progressBarPresenter.onViewAttached(progressBarView);
        eventBus.dispatchEvent(new Contract.LoadingFinishedEvent(), ProgressBarPresenter.class);
        verify(progressBarView).hideProgressBar();
    }

    @Test
    public void testRecyclerViewPresenterReceivesStringEvent() {
        RecyclerViewPresenter mock = mock(RecyclerViewPresenter.class);
        Handler handler = new Handler();
        RoboExecutorService service = new RoboExecutorService();
        eventBus.addEventListener(EventListenerUtil.createEventListener(RecyclerViewPresenter.class, String.class, mock, handler, service));
        eventBus.dispatchEvent("query");
        verify(mock).searchRepositories("query");
    }

}