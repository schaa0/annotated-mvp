package de.hda.simple_example.container;

import com.mvp.BaseApplicationProvider;
import com.mvp.annotation.Provider;
import com.mvp.annotation.ProvidesModule;

import de.hda.simple_example.business.MainPresenter;
import de.hda.simple_example.inject.ModuleGithubService;
import de.hda.simple_example.inject.ModuleLocationManager;
import de.hda.simple_example.inject.ModuleMainPresenterState;
import de.hda.simple_example.inject.ModuleRepository;
import de.hda.simple_example.model.Repository;

@Provider
public class ModuleProvider extends BaseApplicationProvider {

    @ProvidesModule
    public ModuleGithubService getModuleGithubService(){
        return new ModuleGithubService();
    }

    @ProvidesModule
    public ModuleLocationManager getModuleLocationManager(){
        return new ModuleLocationManager(getApplicationContext());
    }

    @ProvidesModule
    public ModuleRepository getModuleRepository(Repository repository) {
        return new ModuleRepository(repository);
    }

    @ProvidesModule
    public ModuleMainPresenterState getModuleMainPresenterState(MainPresenter.State state){
        return new ModuleMainPresenterState(state);
    }

}
