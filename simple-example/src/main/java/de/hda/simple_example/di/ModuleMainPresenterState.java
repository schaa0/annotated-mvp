package de.hda.simple_example.di;

import dagger.Module;
import dagger.Provides;
import de.hda.simple_example.business.MainPresenter;

/**
 * Created by Andy on 21.12.2016.
 */

@Module
public class ModuleMainPresenterState {

    private MainPresenter.State state;

    public ModuleMainPresenterState(MainPresenter.State state){
        this.state = state;
    }

    @Provides
    public MainPresenter.State state() {
        return state;
    }

}
