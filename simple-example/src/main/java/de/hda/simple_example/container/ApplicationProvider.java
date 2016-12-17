package de.hda.simple_example.container;

import android.app.Application;

import com.mvp.MvpApplication;
import com.mvp.annotation.Provider;
import com.mvp.annotation.ProvidesModule;

import javax.inject.Named;
import javax.inject.Singleton;

import de.hda.simple_example.inject.ModuleGithubService;
import de.hda.simple_example.inject.ModuleLocationManager;
import de.hda.simple_example.inject.ModuleRepository;
import de.hda.simple_example.model.Repository;

@Provider
public class ApplicationProvider extends MvpApplication {

    @ProvidesModule
    public ModuleGithubService getModuleGithubService(){
        return new ModuleGithubService();
    }

    @ProvidesModule
    public ModuleLocationManager getModuleLocationManager(){
        return new ModuleLocationManager(getApplicationContext());
    }

    @ProvidesModule
    public ModuleRepository getModuleRepository(Repository repository, RepositoryAdapter repositoryAdapter) {
        return new ModuleRepository(repository, repositoryAdapter);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

}
