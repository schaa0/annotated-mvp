package de.hda.simple_example.di;

import dagger.Component;
import de.hda.simple_example.container.MainActivity;
import de.hda.simple_example.container.MainFragment;


@FragmentScope
@Component(dependencies = {ComponentApplication.class})
public interface ComponentFragment
{
    void inject(MainFragment mainFragment);
}
