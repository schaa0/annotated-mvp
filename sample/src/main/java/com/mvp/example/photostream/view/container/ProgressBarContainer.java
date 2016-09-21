package com.mvp.example.photostream.view.container;

import android.widget.ProgressBar;

import com.mvp.example.photostream.presenter.ProgressBarPresenter;
import com.mvp.example.photostream.view.viewcontract.IProgressBar;

public class ProgressBarContainer implements IProgressBar {

    private final ProgressBar progressBar;
    private ProgressBarPresenter presenter;

    public ProgressBarContainer(ProgressBar progressBar){
        this.progressBar = progressBar;
    }

    @Override
    public void setPresenter(ProgressBarPresenter presenter) {
        this.presenter = presenter;
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
}
