/*
 * The MIT License
 *
 * Copyright (c) 2016 Andreas Schattney
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.mvp.example.photostream.view.container;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.AbsSavedState;
import android.view.MenuItem;
import android.view.View;

import com.mvp.example.photostream.presenter.SearchViewPresenter;
import com.mvp.example.photostream.view.viewcontract.ISearchView;

public class SearchViewContainer implements ISearchView {

    private MenuItem searchViewMenuItem;
    SearchView searchView;
    SearchViewPresenter presenter;
    boolean isExpanded;
    boolean isFocused;

    public SearchViewContainer(final MenuItem searchViewMenuItem){
        this.searchViewMenuItem = searchViewMenuItem;
    }

    @Override
    public void setUp() {
        this.searchView = (SearchView) MenuItemCompat.getActionView(searchViewMenuItem);
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
                if (query.trim().length() > 0){
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
    }

    private void internalRestoreInstanceState(SavedState savedState) {
        if (savedState != null) {
            if (savedState.isExpanded())
                MenuItemCompat.expandActionView(searchViewMenuItem);
            if (savedState.isFocused())
                searchView.requestFocus();
            else
                searchView.clearFocus();
            searchView.setQuery(savedState.getQuery(), false);
        }
    }

    public SavedState saveInstanceState(){
        SavedState savedState = new SavedState(AbsSavedState.EMPTY_STATE);
        savedState.setQuery(searchView.getQuery().toString());
        savedState.setFocused(isFocused);
        savedState.setExpanded(isExpanded);
        return savedState;
    }

    @Override
    public void setPresenter(SearchViewPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        bundle.putParcelable("mykey", saveInstanceState());
    }

    @Override
    public void onRestoreInstanceState(Bundle bundle) {
        internalRestoreInstanceState(bundle.<SavedState>getParcelable("mykey"));
    }

    public static class SavedState extends View.BaseSavedState {

        private boolean focused;
        private boolean expanded;
        private String query;

        public SavedState(Parcel source) {
            super(source);
            focused = source.readInt() == 1;
            expanded = source.readInt() == 1;
            query = source.readString();
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(focused ? 1 : 0);
            dest.writeInt(expanded ? 1 : 0);
            dest.writeString(query);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

        public void setFocused(boolean focused) {
            this.focused = focused;
        }

        public boolean isExpanded() {
            return expanded;
        }

        public void setExpanded(boolean expanded) {
            this.expanded = expanded;
        }

        public boolean isFocused() {
            return focused;
        }

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }
    }

}
