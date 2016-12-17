package de.hda.simple_example.business;

import com.mvp.MvpPresenter;
import com.mvp.annotation.BackgroundThread;
import com.mvp.annotation.Event;
import com.mvp.annotation.Presenter;

import javax.inject.Inject;

import de.hda.simple_example.container.ExampleActivity;
import de.hda.simple_example.container.IExampleView;
import de.hda.simple_example.container.IView;
import de.hda.simple_example.event.Contract;
import de.hda.simple_example.inject.ModuleGithubService;
import de.hda.simple_example.inject.ModuleRepository;
import de.hda.simple_example.model.Repository;

/**
 * Created by Andy on 13.12.2016.
 */

@Presenter(needsModules = {ModuleRepository.class}, viewImplementation = ExampleActivity.class)
public class ExamplePresenter extends MvpPresenter<IExampleView> {

    private Repository repository;

    @Override
    public void onViewAttached(IExampleView view) {

    }

    @Override
    public void onViewReattached(IExampleView view) {

    }

    @Override
    public void onViewDetached(IExampleView view) {

    }

    @Inject
    public ExamplePresenter(Repository repository) {
        this.repository = repository;
    }

    @BackgroundThread
    public void loadThings() {
        dispatchEvent("event").toAny();
    }

    @Event
    public void onRepositoriesLoaded(Contract.RepositoriesLoadedEvent event){

    }

    public void showRepositoryId() {
        getView().showRepository(repository);
    }

}
