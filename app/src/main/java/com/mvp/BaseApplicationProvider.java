package com.mvp;

import android.app.Application;

import com.mvp.annotation.ProvidesComponent;
import com.mvp.annotation.ProvidesModule;

public class BaseApplicationProvider extends Application {

    private ModuleEventBus moduleEventBus;
    private ComponentEventBus componentEventBus;

    @ProvidesModule
    public ModuleEventBus mvpEventBus() {
        return moduleEventBus;
    }

    @ProvidesComponent
    public ComponentEventBus componentEventBus() {
        return componentEventBus;
    }

    @Override
    public void onCreate() {
        moduleEventBus = new ModuleEventBus();
        componentEventBus = DaggerComponentEventBus.builder()
                                   .moduleCustomEventBus(new ModuleCustomEventBus(mvpEventBus().eventBus))
                                   .build();
        super.onCreate();
    }

    @Override
    public void onTerminate() {
        moduleEventBus.destroy();
        super.onTerminate();
    }
}
