package com.mvp;

import android.util.Log;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Andy on 13.12.2016.
 */

@Module
public class ModuleEventBus {

    protected MvpEventBus eventBus;

    public ModuleEventBus() {
        this(new MvpEventBus());
    }

    public ModuleEventBus(MvpEventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Provides
    public IMvpEventBus eventBus() {
        return eventBus;
    }

    public void destroy() {
        eventBus.destroy();
    }
}
