package de.hda.simple_example.container;

import android.location.Location;

import com.mvp.MvpView;

import java.io.IOException;
import java.util.List;

import de.hda.simple_example.model.Repository;

public interface IView extends MvpView{
    void showError(IOException e);
    void setUp();
    void showProgressBar();
    void hideProgressBar();
    boolean isLoading();
    void setRepositories(List<Repository> repositories);
    void addRepositories(List<Repository> repositories);
    void showToast(Repository repository);
    void showToast(Location location);
}
