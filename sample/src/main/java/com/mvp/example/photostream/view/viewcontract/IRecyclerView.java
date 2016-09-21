package com.mvp.example.photostream.view.viewcontract;

import com.mvp.MvpViewInLayout;
import com.mvp.example.photostream.model.Repository;
import com.mvp.example.photostream.presenter.RecyclerViewPresenter;
import java.util.List;

public interface IRecyclerView extends MvpViewInLayout<RecyclerViewPresenter> {
    void setUp();
    void onViewDetached();
    void setPresenter(RecyclerViewPresenter presenter);
    void setRepositories(List<Repository> repositories);
    void addRepositories(List<Repository> repositories);
}
