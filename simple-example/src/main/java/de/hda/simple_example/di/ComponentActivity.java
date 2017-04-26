package de.hda.simple_example.di;

import com.mvp.BaseComponentActivity;
import com.mvp.BaseModuleActivity;
import com.mvp.annotation.ActivityScope;

import dagger.Component;
import de.hda.simple_example.container.MainActivity;
import de.hda.simple_example.presenter.Settings;
import de.hda.simple_example.service.GithubService;


@Component(modules = {ModuleActivity.class }, dependencies = { ComponentApplication.class })
@ActivityScope
public interface ComponentActivity extends BaseComponentActivity
{
    void inject(MainActivity mainActivity);
    GithubService githubService();
    Settings settings();
}
