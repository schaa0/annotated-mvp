package de.hda.simple_example.container;

import com.mvp.MvpView;

import java.util.List;

import de.hda.simple_example.model.Repository;

/**
 * Created by Andy on 18.12.2016.
 */

public interface MainActivityView extends MvpView {
    void setRepositories(List<Repository> repositories);
    void addRepositories(List<Repository> repositories);
    void showDetailViewInActivity(Repository repository);
    boolean isInPortrait();
    boolean isTabletAndInPortrait();
}
