package de.hda.simple_example.di;

import dagger.Component;
import de.hda.simple_example.container.MainActivity;
import de.hda.simple_example.container.MainFragment;


@ActivityScope
@Component(dependencies = {ComponentApplication.class})
public interface ComponentActivity
{
    void inject(MainActivity mainActivity);
}
