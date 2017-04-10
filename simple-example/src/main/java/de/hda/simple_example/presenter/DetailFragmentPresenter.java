package de.hda.simple_example.presenter;

import android.util.Log;

import com.mvp.MvpPresenter;
import com.mvp.annotation.Event;
import com.mvp.annotation.Presenter;

import javax.inject.Inject;

import de.hda.simple_example.container.DetailFragmentView;
import de.hda.simple_example.di.ComponentActivity;
import de.hda.simple_example.model.Repository;
import de.hda.simple_example.service.GithubService;

@Presenter(needsComponents = {ComponentActivity.class})
public class DetailFragmentPresenter extends MvpPresenter<DetailFragmentView> {

    private Repository repository;
    private String currentId;

    protected DetailFragmentPresenter() {}

    @Inject
    public DetailFragmentPresenter(Repository repository, GithubService githubService){
        this.repository = repository;
        Log.e(DetailFragmentPresenter.class.getName(), String.valueOf(githubService.hashCode()));
        if (repository != Repository.NULL)
            this.currentId = String.valueOf(repository.getId());
        else
            this.currentId = "";
    }

    @Override
    public void onViewAttached(DetailFragmentView view) {
        view.showId(currentId);
    }

    @Override
    public void onViewReattached(DetailFragmentView view) {
        view.showId(currentId);
    }

    @Override
    public void onViewDetached(DetailFragmentView view) {

    }

    @Event
    public void onShowRepositoryId(String id) {
        currentId = String.valueOf(id);
        if (getView() != null)
            getView().showId(id);
    }

}
