package de.hda.simple_example.container;

import android.content.Context;

import com.mvp.BaseApplicationProvider;
import com.mvp.ComponentEventBus;
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
public class ApplicationProvider extends BaseApplicationProvider {

    private ComponentApplication componentApplication;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @ProvidesModule
    public ModuleRepository moduleRepository(Repository repository) {
        return new ModuleRepository(repository);
    }

    @ProvidesModule
    public ModuleMainPresenterState moduleMainPresenterState(MainPresenter.State state){
        return new ModuleMainPresenterState(state);
    }

    @ProvidesModule
    public ModuleGithubService moduleGithubService(){
        return new ModuleGithubService();
    }

    @ProvidesModule
    public ModuleApplication moduleApplication(){
        return new ModuleApplication(this.getApplicationContext());
    }

    @ProvidesComponent
    public ComponentApplication componentApplication(ModuleGithubService moduleGithubService, ModuleApplication moduleApplication, ComponentEventBus componentEventBus){
        if (componentApplication == null)
        {
            componentApplication = DaggerComponentApplication.builder()
                                                             .moduleGithubService(moduleGithubService)
                                                             .moduleApplication(moduleApplication)
                                                             .componentEventBus(componentEventBus)
                                                             .build();
        }
        return componentApplication;
    }

}
