package de.hda.simple_example.di;

import com.mvp.annotation.FragmentScope;

import dagger.Component;
import dagger.Subcomponent;
import de.hda.simple_example.container.MainFragment;


@Subcomponent
@FragmentScope
public interface ComponentFragment
{
    void inject(MainFragment mainFragment);
}
