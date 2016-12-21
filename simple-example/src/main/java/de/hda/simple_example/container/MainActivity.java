package de.hda.simple_example.container;

import android.os.Bundle;
import android.support.v4.view.AbsSavedState;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.mvp.annotation.Presenter;
import com.mvp.annotation.UIView;

import de.hda.simple_example.business.ActivityPresenter;
import de.hda.simple_example.business.MainPresenter;
import de.hda.simple_example.R;
import de.hda.simple_example.event.Contract;

@UIView(presenter = ActivityPresenter.class)
public class MainActivity extends AppCompatActivity implements IView {

    @Presenter
    ActivityPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);
        this.savedInstanceState = savedInstanceState;
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        if (savedInstanceState == null){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance(), MainFragment.TAG)
                    .replace(R.id.container_detail, DetailFragment.newInstance(), DetailFragment.TAG)
                    .commit();
        }

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
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("mykey", saveInstanceState());
    }

    @Override
    protected void onDestroy() {
        savedInstanceState = null;
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
    }

    Bundle savedInstanceState;
    MenuItem searchViewMenuItem;
    ProgressBar progressBar;
    SearchView searchView;
    boolean isExpanded;
    boolean isFocused;

}