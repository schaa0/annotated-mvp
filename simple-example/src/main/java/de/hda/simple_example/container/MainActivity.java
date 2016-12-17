package de.hda.simple_example.container;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.view.AbsSavedState;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.mvp.ModuleEventBus;
import com.mvp.annotation.Presenter;
import com.mvp.annotation.UIView;
import java.io.IOException;
import java.util.List;

import de.hda.simple_example.business.ComponentMainPresenter;
import de.hda.simple_example.business.DaggerComponentMainPresenter;
import de.hda.simple_example.business.MainPresenter;
import de.hda.simple_example.R;
import de.hda.simple_example.business.ModuleMainPresenter;
import de.hda.simple_example.business.ModuleMainPresenterDependencies;
import de.hda.simple_example.event.Contract;
import de.hda.simple_example.inject.ModuleGithubService;
import de.hda.simple_example.inject.ModuleLocationManager;
import de.hda.simple_example.model.Repository;

@UIView(presenter = MainPresenter.class)
public class MainActivity extends AppCompatActivity implements IView, RepositoryAdapter.OnItemClickListener {

    @Presenter
    MainPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.savedInstanceState = savedInstanceState;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        searchViewMenuItem = menu.findItem(R.id.action_search);
        this.searchView = (SearchView) MenuItemCompat.getActionView(searchViewMenuItem);
        if (savedInstanceState != null)
            internalRestoreInstanceState(savedInstanceState.<SavedState>getParcelable("mykey"));
        MenuItemCompat.setOnActionExpandListener(searchViewMenuItem, new MenuItemCompat.OnActionExpandListener() {

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                isExpanded = true;
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                isExpanded = false;
                return true;
            }
        });
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                isFocused = hasFocus;
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.trim().length() > 0) {
                    presenter.searchRepositories(query);
                    searchView.clearFocus();
                    return true;
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle("adapter", adapter.onSaveInstanceState());
        outState.putParcelable("mykey", saveInstanceState());
    }

    @Override
    protected void onDestroy() {
        recyclerView.removeOnScrollListener(scrollListener);
        savedInstanceState = null;
        super.onDestroy();
    }

    @Override
    public void showError(Contract.GithubServiceErrorEvent e) {
        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setUp() {
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        lm = new LinearLayoutManager(recyclerView.getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(lm);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        scrollListener = new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (presenter != null && !presenter.isLoading() && !presenter.reachedEndOfStream()) {
                    if (lm.findLastVisibleItemPosition() == adapter.getItemCount() - 1) {
                        presenter.loadMoreRepositories();
                    }
                }
            }
        };
        recyclerView.addOnScrollListener(scrollListener);
        adapter = new RepositoryAdapter();
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void showProgressBar() {
        progressBar.setVisibility(ProgressBar.VISIBLE);
    }

    @Override
    public void hideProgressBar() {
        progressBar.setVisibility(ProgressBar.GONE);
    }

    @Override
    public boolean isLoading() {
        return progressBar.getVisibility() == ProgressBar.VISIBLE;
    }

    @Override
    public void setRepositories(List<Repository> repositories) {
        adapter.set(repositories);
    }

    @Override
    public void addRepositories(List<Repository> repositories) {
        adapter.addAll(repositories);
    }

    @Override
    public void showDetailView(Repository repository) {
        Intent intent = new Intent(this, ExampleActivity.class);
        intent.putExtra("repository", repository);
        startActivity(intent);
    }

    @Override
    public void showDetailView(Location location) {
        Toast.makeText(this, String.format("Longitude: %d, Latitude: %d", location.getLongitude(), location.getLatitude()), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClick(int position) {
        presenter.showDetailView(adapter.getItemAtPosition(position));
    }

    private void internalRestoreInstanceState(SavedState savedState) {
        if (savedState != null && searchView != null) {
            isExpanded = savedState.isExpanded();
            isFocused = savedState.isFocused();
            if (isExpanded)
                MenuItemCompat.expandActionView(searchViewMenuItem);
            if (isFocused)
                searchView.requestFocus();
            else
                searchView.clearFocus();
            searchView.setQuery(savedState.getQuery(), false);
        }
    }

    public SavedState saveInstanceState() {
        SavedState savedState = new SavedState(AbsSavedState.EMPTY_STATE);
        savedState.setQuery(searchView.getQuery().toString());
        savedState.setFocused(isFocused);
        savedState.setExpanded(isExpanded);
        return savedState;
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        internalRestoreInstanceState(savedInstanceState.<SavedState>getParcelable("mykey"));
        adapter.onRestoreInstanceState(savedInstanceState.getBundle("adapter"));
    }

    Bundle savedInstanceState;
    MenuItem searchViewMenuItem;
    RecyclerView recyclerView;
    ProgressBar progressBar;
    RepositoryAdapter adapter;
    RecyclerView.OnScrollListener scrollListener;
    LinearLayoutManager lm;
    SearchView searchView;
    boolean isExpanded;
    boolean isFocused;

}