package de.hda.simple_example.container;

import android.location.Location;

import com.mvp.MvpView;

import java.io.IOException;
import java.util.List;

import de.hda.simple_example.event.Contract;
import de.hda.simple_example.model.Repository;

public interface IView extends MvpView{
    void showError(Contract.GithubServiceErrorEvent e);
    void showProgressBar();
    void hideProgressBar();
    boolean isLoading();
}
