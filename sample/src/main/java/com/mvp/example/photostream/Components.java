package com.mvp.example.photostream;

import com.mvp.MvpPresenter;
import com.mvp.MvpView;
import com.mvp.MvpViewDelegate;
import com.mvp.MvpViewInLayout;
import com.mvp.PresenterComponent;
import com.mvp.example.photostream.presenter.GithubRepositoryPresenter;
import com.mvp.example.photostream.presenter.ProgressBarPresenter;
import com.mvp.example.photostream.presenter.RecyclerViewPresenter;
import com.mvp.example.photostream.presenter.SearchViewPresenter;
import com.mvp.example.photostream.view.container.MainActivity;

import javax.inject.Singleton;
import dagger.Component;

public interface Components {

    @Component(modules = { ModuleDefault.class, ModuleGithubService.class })
    interface ComponentRecyclerViewPresenter extends PresenterComponent<RecyclerViewPresenter> {
        @Override
        RecyclerViewPresenter newInstance();
    }

    @Component(modules = { ModuleDefault.class })
    interface ComponentProgressBarPresenter extends PresenterComponent<ProgressBarPresenter> {
        @Override
        ProgressBarPresenter newInstance();
    }

    @Component(modules = { ModuleDefault.class })
    interface ComponentGithubRepositoryPresenter extends PresenterComponent<GithubRepositoryPresenter> {
        @Override
        GithubRepositoryPresenter newInstance();
    }

    @Component( modules = { ModuleDefault.class })
    interface ComponentSearchViewPresenter extends PresenterComponent<SearchViewPresenter> {
        @Override
        SearchViewPresenter newInstance();
    }

    @Component(modules = ModuleDefault.class)
    interface ComponentActivity {
        void inject(MainActivity mainActivity);
    }

}
