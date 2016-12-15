package com.mvp;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Andy on 13.12.2016.
 */

@Module
public class ModuleEventBus {

    private IMvpEventBus eventBus = new MvpEventBus();

    public ModuleEventBus() {

    }

    public ModuleEventBus(IMvpEventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Provides
    public IMvpEventBus eventBus() {
        return eventBus;
    }

}
