package com.mvp;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Andy on 26.12.2016.
 */

@Module
public class ModuleCustomEventBus extends ModuleEventBus {

    public ModuleCustomEventBus() { }

    public ModuleCustomEventBus(MvpEventBus eventBus) {
        super(eventBus);
    }

    @Provides
    @Singleton
    public EventBus customEventBus(){
        return eventBus;
    }
}
