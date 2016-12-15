package com.mvp.example.photostream.view.container;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.mvp.example.R;
import com.mvp.example.photostream.DaggerComponents_ComponentActivity;
import com.mvp.example.photostream.ModuleDefault;
import com.mvp.example.photostream.presenter.GithubRepositoryPresenter;
import com.mvp.example.photostream.view.viewcontract.IMainActivityView;

import java.io.IOException;

import javax.inject.Inject;

public class MainActivity extends AppCompatActivity implements IMainActivityView {

    @Inject MainActivityDelegate delegate;

    Bundle savedInstanceState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DaggerComponents_ComponentActivity.builder().moduleDefault(new ModuleDefault(this)).build().inject(this);

        this.savedInstanceState = savedInstanceState;

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
        progressBarDelegate.onRestoreInstanceState(savedInstanceState);
        searchViewDelegate.onRestoreInstanceState(savedInstanceState);
        recyclerViewDelegate.onRestoreInstanceState(savedInstanceState);
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

}
