package de.hda.simple_example.container;

import android.os.Bundle;
import android.support.v4.view.AbsSavedState;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.mvp.annotation.Presenter;
import com.mvp.annotation.View;

import javax.inject.Inject;

import de.hda.simple_example.R;
import de.hda.simple_example.di.ComponentApplication;
import de.hda.simple_example.presenter.MainActivityPresenter;
import de.hda.simple_example.service.CustomService;
import de.hda.simple_example.di.ComponentActivity;
import de.hda.simple_example.di.SimpleApplication;
import de.hda.simple_example.event.Contract;


@View(presenter = MainActivityPresenter.class)
public class MainActivity extends AppCompatActivity implements IView {

    @Presenter
    MainActivityPresenter presenter;

    @Inject
    CustomService customService;

    public static final String KEY_SEARCHVIEW_STATE = "KEY_SEARCHVIEW_STATE";

    private ComponentActivity component;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        this.savedInstanceState = savedInstanceState;

        SimpleApplication provider = (SimpleApplication) getApplication();
        component = provider.componentActivity(this);
        component.inject(this);

        customService.register();

    }

    public ComponentApplication getParentComponent() {
        SimpleApplication provider = (SimpleApplication) getApplication();
        return provider.componentApplication();
    }

    public ComponentActivity getComponent() {
        return this.component;
    }

    @Override
    protected void onStart() {
        supportInvalidateOptionsMenu();
        super.onStart();
        if (presenter != null && searchViewMenuItem != null)
            presenter.onSearchViewInitialized();
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
        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> isFocused = hasFocus);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.trim().length() > 0) {
                    presenter.sendEventSearchRepositories(query);
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
        if (presenter != null)
            presenter.onSearchViewInitialized();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_SEARCHVIEW_STATE, saveInstanceState());
    }

    @Override
    protected void onDestroy() {
        savedInstanceState = null;
        customService.unregister();
        super.onDestroy();
    }

    @Override
    public void showError(Contract.GithubServiceErrorEvent e) {
        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
    public void setLastQuery(String lastQuery) {
        MenuItemCompat.expandActionView(searchViewMenuItem);
        this.searchView.setQuery(lastQuery, false);
    }

    @Override
    public boolean isDetailContainerPresent() {
        return findViewById(R.id.container_detail) != null;
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
        internalRestoreInstanceState(savedInstanceState.<SavedState>getParcelable(KEY_SEARCHVIEW_STATE));
    }


    ProgressBar progressBar;
    Bundle savedInstanceState;
    MenuItem searchViewMenuItem;
    SearchView searchView;
    boolean isExpanded;
    boolean isFocused;

}