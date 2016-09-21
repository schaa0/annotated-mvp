package com.mvp.example.photostream.presenter;

import android.os.Handler;
import android.os.Looper;
import com.mvp.EventListenerUtil;
import com.mvp.MvpEventBus;
import com.mvp.MvpPresenterFactory;
import com.mvp.annotation.OnEventListener;
import com.mvp.example.photostream.view.container.SearchViewContainer;
import com.mvp.example.photostream.view.viewcontract.ISearchView;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.util.concurrent.RoboExecutorService;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = com.mvp.example.BuildConfig.class, sdk = 21)
public class SearchViewPresenterTest {

    private SearchViewPresenter searchViewPresenter;
    MvpEventBus eventBus;
    private ISearchView searchView;
    @Before
    public void setUp() throws Exception {
        eventBus = new MvpEventBus();

        searchViewPresenter = new MvpPresenterFactory<ISearchView, SearchViewPresenter>(){
            @Override
            public SearchViewPresenter create() {
                return new SearchViewPresenter(eventBus, Looper.myLooper(), new RoboExecutorService());
            }
        }.create();

        searchViewPresenter.onInitialize();
        searchView = mock(ISearchView.class);
        searchViewPresenter.onInitialize();
        searchViewPresenter.setView(searchView);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void presenterInitialized() {
        assertEquals(searchViewPresenter.getView(), searchView);
    }

    @Test
    public void onViewAttached() throws Exception {
        searchViewPresenter.onViewAttached(searchView);
        verify(searchView).setUp();
    }

    @Test
    public void onViewDetached() throws Exception {
        searchViewPresenter.onViewDetached(searchView);
        verify(searchView).saveCurrentState();
    }

    @Test
    public void onViewReattached() throws Exception {
        searchViewPresenter.onViewReattached(searchView);
        verify(searchView, times(1)).setUp();
        verify(searchView).restoreState(ArgumentMatchers.any());
    }

    @Test
    public void searchRepositories() throws Exception {
        RecyclerViewPresenter receiver = mock(RecyclerViewPresenter.class);
        Handler handler = new Handler();
        RoboExecutorService service = new RoboExecutorService();
        OnEventListener<?> eventListener =
                EventListenerUtil.createEventListener(RecyclerViewPresenter.class, String.class, receiver, handler, service);
        eventBus.addEventListener(eventListener);
        searchViewPresenter.onViewAttached(searchView);
        searchViewPresenter.searchRepositories("query");
        verify(receiver).searchRepositories("query");
    }

}