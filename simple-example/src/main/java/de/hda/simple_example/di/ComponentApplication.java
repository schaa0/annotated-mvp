package de.hda.simple_example.di;

import android.content.Context;

import com.mvp.ComponentEventBus;
import com.mvp.EventBus;

import dagger.Component;
import de.hda.simple_example.business.GithubService;
import de.hda.simple_example.business.Settings;

/**
 * Created by Andy on 25.12.2016.
 */

@ApplicationScope
@Component(modules = { ModuleGithubService.class, ModuleApplication.class }, dependencies = { ComponentEventBus.class })
public interface ComponentApplication {
    GithubService githubService();
    EventBus eventBus();
    Context context();
    Settings settings();
}
