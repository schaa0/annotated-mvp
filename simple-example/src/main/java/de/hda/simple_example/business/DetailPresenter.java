package de.hda.simple_example.business;

import com.mvp.MvpPresenter;
import com.mvp.MvpPresenterFactory;
import com.mvp.annotation.Event;
import com.mvp.annotation.Presenter;

import javax.inject.Inject;

import de.hda.simple_example.container.DetailFragment;
import de.hda.simple_example.container.IDetailView;
import de.hda.simple_example.inject.ModuleRepository;
import de.hda.simple_example.model.Repository;

/**
 * Created by Andy on 18.12.2016.
 */

@Presenter(needsModules = { ModuleRepository.class })
public class DetailPresenter extends MvpPresenter<IDetailView> {

    private String currentId;

    @Inject
    public DetailPresenter(Repository repository){
        if (repository != null && repository != Repository.NULL)
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
