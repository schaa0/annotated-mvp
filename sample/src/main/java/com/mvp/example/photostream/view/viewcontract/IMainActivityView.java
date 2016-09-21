package com.mvp.example.photostream.view.viewcontract;

import com.mvp.MvpView;

import java.io.IOException;

public interface IMainActivityView extends MvpView{
    void showError(IOException e);
}
