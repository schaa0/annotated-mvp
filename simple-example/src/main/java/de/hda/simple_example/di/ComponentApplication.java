package de.hda.simple_example.di;

import com.mvp.ComponentEventBus;
import com.mvp.ModuleActivity;
import com.mvp.ModuleContext;
import com.mvp.ModuleEventBus;

import javax.inject.Singleton;

import dagger.Component;
import de.hda.simple_example.business.GithubService;

/**
 * Created by Andy on 25.12.2016.
 */

@Singleton
@Component(modules = { ModuleSingleton.class, ModuleContext.class, ModuleEventBus.class})
public interface ComponentApplication extends ComponentEventBus {
    GithubService githubService();
    Settings settings();
    ComponentActivity plus(ModuleActivity module);
}
