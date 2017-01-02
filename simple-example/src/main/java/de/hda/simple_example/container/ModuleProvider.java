package de.hda.simple_example.container;

import com.mvp.BaseApplicationProvider;
import com.mvp.annotation.Provider;
import com.mvp.annotation.ProvidesComponent;
import com.mvp.annotation.ProvidesModule;

import de.hda.simple_example.business.MainPresenter;
import de.hda.simple_example.di.ComponentApplication;
import de.hda.simple_example.di.DaggerComponentApplication;
import de.hda.simple_example.di.ModuleApplication;
import de.hda.simple_example.di.ModuleGithubService;
import de.hda.simple_example.di.ModuleMainPresenterState;
import de.hda.simple_example.di.ModuleRepository;
import de.hda.simple_example.model.Repository;

@Provider
public class ModuleProvider extends BaseApplicationProvider {

    private ComponentApplication componentApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        componentApplication = DaggerComponentApplication.builder()
                .moduleGithubService(new ModuleGithubService())
                .moduleApplication(new ModuleApplication(getApplicationContext()))
                .componentEventBus(componentEventBus())
                .build();
    }

    @ProvidesModule
    public ModuleRepository moduleRepository(Repository repository) {
        return new ModuleRepository(repository);
    }

    @ProvidesModule
    public ModuleMainPresenterState moduleMainPresenterState(MainPresenter.State state){
        return new ModuleMainPresenterState(state);
    }

    @ProvidesComponent
    public ComponentApplication componentApplication(){
        return componentApplication;
    }

}
