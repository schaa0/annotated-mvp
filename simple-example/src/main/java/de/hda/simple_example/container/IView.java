package de.hda.simple_example.container;

import com.mvp.MvpView;

import de.hda.simple_example.event.Contract;

public interface IView extends MvpView{
    void showError(Contract.GithubServiceErrorEvent e);
    void showProgressBar();
    void hideProgressBar();
    boolean isLoading();
    void setLastQuery(String lastQuery);
    boolean isDetailContainerPresent();
}
