package com.mvp.example.photostream;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import com.mvp.IMvpEventBus;
import com.mvp.MvpModule;
import com.mvp.MvpPresenter;
import com.mvp.MvpViewDelegate;
import com.mvp.MvpViewInLayout;
import com.mvp.PresenterComponent;
import com.mvp.example.photostream.presenter.GithubRepositoryPresenter;
import com.mvp.example.photostream.presenter.ProgressBarPresenter;
import com.mvp.example.photostream.presenter.RecyclerViewPresenter;
import com.mvp.example.photostream.presenter.SearchViewPresenter;
import com.mvp.example.photostream.service.GithubService;
import com.mvp.example.photostream.view.viewcontract.IMainActivityView;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ModuleDefault extends MvpModule {

    public ModuleDefault(AppCompatActivity activity) {
        super(activity);
    }

    @Provides
    public Components.ComponentRecyclerViewPresenter componentRecyclerViewPresenter(){
        return DaggerComponents_ComponentRecyclerViewPresenter
                .builder()
                .moduleGithubService(new ModuleGithubService("https://api.github.com"))
                .build();
    }

    @Provides
    public Components.ComponentGithubRepositoryPresenter componentGithubRepositoryPresenter(){
        return DaggerComponents_ComponentGithubRepositoryPresenter.builder().build();
    }

    @Provides
    public Components.ComponentSearchViewPresenter componentSearchViewPresenter(){
        return DaggerComponents_ComponentSearchViewPresenter.builder().build();
    }

    @Provides
    public Components.ComponentProgressBarPresenter componentProgressBarPresenter(){
        return DaggerComponents_ComponentProgressBarPresenter.builder().build();
    }

    @Provides
    public PresenterComponent<GithubRepositoryPresenter> buildGRP(){
        return new PresenterComponent<GithubRepositoryPresenter>() {
            @Override
            public GithubRepositoryPresenter newInstance() {
                return DaggerComponents_ComponentGithubRepositoryPresenter.builder().moduleDefault(new ModuleDefault(getActivity())).build().newInstance();
            }
        };
    }

    @Provides
    public PresenterComponent<ProgressBarPresenter> buildPBP(){
        return new PresenterComponent<ProgressBarPresenter>() {
            @Override
            public ProgressBarPresenter newInstance() {
                return DaggerComponents_ComponentProgressBarPresenter.builder().moduleDefault(new ModuleDefault(getActivity())).build().newInstance();
            }
        };
    }

    @Provides
    public PresenterComponent<RecyclerViewPresenter> buildRVP(){
        return new PresenterComponent<RecyclerViewPresenter>() {
            @Override
            public RecyclerViewPresenter newInstance() {
                return DaggerComponents_ComponentRecyclerViewPresenter.builder().moduleGithubService(new ModuleGithubService("https://api.github.com")).moduleDefault(new ModuleDefault(getActivity())).build().newInstance();
            }
        };
    }

    @Provides
    public PresenterComponent<SearchViewPresenter> buildSVP(){
        return new PresenterComponent<SearchViewPresenter>() {
            @Override
            public SearchViewPresenter newInstance() {
                return DaggerComponents_ComponentSearchViewPresenter.builder().moduleDefault(new ModuleDefault(getActivity())).build().newInstance();
            }
        };
    }

    @Provides
    public IMainActivityView buildView(){
        return (IMainActivityView) getActivity();
    }

}
