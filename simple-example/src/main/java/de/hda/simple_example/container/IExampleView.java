package de.hda.simple_example.container;

import com.mvp.MvpView;

import de.hda.simple_example.model.Repository;

/**
 * Created by Andy on 17.12.2016.
 */

public interface IExampleView extends MvpView {
    void showRepository(Repository repository);
}
