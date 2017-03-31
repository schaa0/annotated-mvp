package de.hda.simple_example.di;

import com.mvp.ModuleActivity;
import com.mvp.annotation.ActivityScope;

import dagger.Component;
import dagger.Subcomponent;
import de.hda.simple_example.container.MainActivity;


@Subcomponent(modules = {ModuleActivity.class})
@ActivityScope
public interface ComponentActivity
{
    void inject(MainActivity mainActivity);
    ComponentFragment plus();
}
