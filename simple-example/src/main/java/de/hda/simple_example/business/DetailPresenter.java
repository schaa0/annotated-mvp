package de.hda.simple_example.business;

import android.util.Log;

import com.mvp.MvpPresenter;
import com.mvp.annotation.Event;
import com.mvp.annotation.Presenter;

import javax.inject.Inject;

import de.hda.simple_example.container.IDetailView;
import de.hda.simple_example.di.ComponentApplication;
import de.hda.simple_example.di.ModuleRepository;
import de.hda.simple_example.model.Repository;

@Presenter(needsModules = { ModuleRepository.class }, needsComponents = {ComponentApplication.class})
public class DetailPresenter extends MvpPresenter<IDetailView> {

    private String currentId;

    protected DetailPresenter() {}

    @Inject
    public DetailPresenter(Repository repository, GithubService githubService){
        Log.e(DetailPresenter.class.getName(), String.valueOf(githubService.hashCode()));
        if (repository != Repository.NULL)
            this.currentId = String.valueOf(repository.getId());
        else
            this.currentId = "";
    }

    @Override
    public void onViewAttached(IDetailView view) {
        view.showId(currentId);
    }

    @Override
    public void onViewReattached(IDetailView view) {
        view.showId(currentId);
    }

    @Override
    public void onViewDetached(IDetailView view) {

    }

    @Event
    public void onShowRepositoryId(String id) {
        currentId = String.valueOf(id);
        if (getView() != null)
            getView().showId(id);
    }

}
