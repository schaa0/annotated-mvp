package de.hda.simple_example.business;

import com.mvp.MvpPresenter;
import com.mvp.annotation.BackgroundThread;
import com.mvp.annotation.Presenter;

import javax.inject.Inject;

import de.hda.simple_example.container.ExampleActivity;
import de.hda.simple_example.container.IView;
import de.hda.simple_example.inject.ModuleGithubService;

/**
 * Created by Andy on 13.12.2016.
 */

@Presenter(needsModules = ModuleGithubService.class, viewImplementation = ExampleActivity.class)
public class ExamplePresenter extends MvpPresenter<IView> {

    private GithubService githubService;

    @Inject
    public ExamplePresenter(GithubService githubService) {
        this.githubService = githubService;
    }

    @Override
    public void onViewAttached(IView view) {

    }

    @Override
    public void onViewReattached(IView view) {

    }

    @Override
    public void onViewDetached(IView view) {

    }

    @BackgroundThread
    public void loadThings() {
        dispatchEvent("event").toAny();
    }
}
