package de.hda.simple_example.di;

import dagger.Module;
import dagger.Provides;
import de.hda.simple_example.presenter.MainFragmentPresenter;

@Module
public class ModuleMainPresenterState {

    private MainFragmentPresenter.State state;

    public ModuleMainPresenterState(MainFragmentPresenter.State state){
        this.state = state;
    }

    @Provides
    public MainFragmentPresenter.State state() {
        return state;
    }

}
