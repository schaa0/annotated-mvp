package com.mvp;

public interface MvpViewInLayout<T extends IMvpPresenter<? extends MvpView>> extends MvpView {
     void setPresenter(T presenter);
}
