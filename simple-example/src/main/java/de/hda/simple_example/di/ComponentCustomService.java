package de.hda.simple_example.di;

import dagger.Component;
import de.hda.simple_example.business.CustomService;
import de.hda.simple_example.business.ServiceScope;


@ServiceScope
@Component(dependencies = {ComponentApplication.class})
public interface ComponentCustomService {
    CustomService customService();
}
