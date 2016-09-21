package com.mvp.example.photostream.view.viewcontract;

import com.mvp.MvpViewInLayout;
import com.mvp.example.photostream.presenter.ProgressBarPresenter;

public interface IProgressBar extends MvpViewInLayout<ProgressBarPresenter> {
    void showProgressBar();
    void hideProgressBar();
    boolean isLoading();
}
