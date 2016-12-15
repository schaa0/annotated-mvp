package de.hda.simple_example.container;

import android.app.Application;

import com.mvp.MvpApplication;
import com.mvp.annotation.Provider;
import com.mvp.annotation.ProvidesModule;

import javax.inject.Named;
import javax.inject.Singleton;

import de.hda.simple_example.inject.ModuleGithubService;
import de.hda.simple_example.inject.ModuleLocationManager;

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

    @Override
    public void onCreate() {
        super.onCreate();
    }

}
