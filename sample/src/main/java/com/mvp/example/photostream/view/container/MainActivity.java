package com.mvp.example.photostream.view.container;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.mvp.IMvpEventBus;
import com.mvp.MvpActivityDelegate;
import com.mvp.MvpViewDelegate;
import com.mvp.example.R;
import com.mvp.example.photostream.presenter.GithubRepositoryPresenter;
import com.mvp.example.photostream.presenter.ProgressBarPresenter;
import com.mvp.example.photostream.presenter.RecyclerViewPresenter;
import com.mvp.example.photostream.presenter.SearchViewPresenter;
import com.mvp.example.photostream.service.GithubService;
import com.mvp.example.photostream.view.viewcontract.IProgressBar;
import com.mvp.example.photostream.view.viewcontract.IRecyclerView;
import com.mvp.example.photostream.view.viewcontract.ISearchView;
import com.mvp.example.photostream.view.viewcontract.IMainActivityView;

import java.io.IOException;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements IMainActivityView {

    private MvpActivityDelegate<IMainActivityView, GithubRepositoryPresenter> delegate;
    private MvpViewDelegate<IProgressBar, ProgressBarPresenter> progressBarDelegate;
    private MvpViewDelegate<IRecyclerView, RecyclerViewPresenter> recyclerViewDelegate;
    private SearchViewDelegate searchViewDelegate;
    private Bundle savedInstanceState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photostream_layout);
        this.savedInstanceState = savedInstanceState;
        delegate = new PhotoActivityDelegate();

        progressBarDelegate = new ProgressBarDelegate();
        recyclerViewDelegate = new RecyclerViewDelegate();
        searchViewDelegate = new SearchViewDelegate();

        delegate.onCreate(savedInstanceState);

        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBarDelegate.attachView(savedInstanceState, new ProgressBarContainer(progressBar));

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerViewDelegate.attachView(savedInstanceState, new RecyclerViewContainer(recyclerView, new RepositoryAdapter()));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        searchViewDelegate.attachView(savedInstanceState, new SearchViewContainer(searchMenuItem));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        delegate.onPostResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        delegate.onSaveInstanceState(outState);
        progressBarDelegate.onSaveInstanceState(outState);
        recyclerViewDelegate.onSaveInstanceState(outState);
        searchViewDelegate.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        searchViewDelegate.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        delegate.onDestroy();
        progressBarDelegate.onDestroy();
        recyclerViewDelegate.onDestroy();
        searchViewDelegate.onDestroy();
        savedInstanceState = null;
        super.onDestroy();
    }

    protected GithubRepositoryPresenter getPresenter() {
        return delegate.getPresenter();
    }

    @Override
    public void showError(IOException e) {
        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    private class PhotoActivityDelegate extends MvpActivityDelegate<IMainActivityView, GithubRepositoryPresenter> {

        PhotoActivityDelegate() {
            super(MainActivity.this, getApplicationContext(), getSupportLoaderManager());
        }

        @Override
        protected GithubRepositoryPresenter create(IMvpEventBus eventBus) {
            return new GithubRepositoryPresenter(eventBus);
        }
    }

    private class ProgressBarDelegate extends MvpViewDelegate<IProgressBar, ProgressBarPresenter> {

        ProgressBarDelegate() {
            super(getSupportLoaderManager(), getApplicationContext(), R.id.progressBar, MvpViewDelegate.CONTAINER_ACTIVITY);
        }

        @Override
        public ProgressBarPresenter create(IMvpEventBus eventBus) {
            return new ProgressBarPresenter(eventBus);
        }
    }

    private class RecyclerViewDelegate extends MvpViewDelegate<IRecyclerView, RecyclerViewPresenter> {

        RecyclerViewDelegate() {
            super(getSupportLoaderManager(), getApplicationContext(), R.id.recyclerView, MvpViewDelegate.CONTAINER_ACTIVITY);
        }

        @Override
        public RecyclerViewPresenter create(IMvpEventBus eventBus) {
            GithubService service = new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("https://api.github.com")
                    .build().create(GithubService.class);
            return new RecyclerViewPresenter(eventBus, service);
        }

    }

    private class SearchViewDelegate extends MvpViewDelegate<ISearchView, SearchViewPresenter>{

        public SearchViewDelegate() {
            super(getSupportLoaderManager(), getApplicationContext(), R.id.action_search, MvpViewDelegate.CONTAINER_ACTIVITY);
        }

        @Override
        public SearchViewPresenter create(IMvpEventBus eventBus) {
            return new SearchViewPresenter(eventBus);
        }
    }

}
