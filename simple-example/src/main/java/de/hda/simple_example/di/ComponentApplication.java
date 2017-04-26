package de.hda.simple_example.di;

import android.content.SharedPreferences;

import com.mvp.ComponentEventBus;
import com.mvp.BaseModuleContext;
import com.mvp.ModuleEventBus;

import javax.inject.Singleton;

import dagger.Component;
import de.hda.simple_example.presenter.Settings;
import de.hda.simple_example.service.GithubService;

@Singleton
@Component(modules = { ModuleSingleton.class, ModuleEventBus.class})
public interface ComponentApplication extends ComponentEventBus {
    GithubService githubService();
    SharedPreferences sharedPreferences();
    Settings settings();
}
