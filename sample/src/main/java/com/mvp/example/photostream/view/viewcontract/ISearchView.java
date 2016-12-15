package com.mvp.example.photostream.view.viewcontract;


import com.mvp.MvpViewInLayout;
import com.mvp.MvpViewWithStateInLayout;
import com.mvp.example.photostream.presenter.SearchViewPresenter;
import com.mvp.example.photostream.view.container.SearchViewContainer;

public interface ISearchView extends MvpViewWithStateInLayout<SearchViewPresenter> {
    void setUp();
}
