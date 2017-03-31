package com.mvp;

import android.app.Application;
import android.support.v7.app.AppCompatActivity;

import com.mvp.annotation.ProvidesComponent;
import com.mvp.annotation.ProvidesModule;

public class MvpApplication extends Application {

    private ModuleEventBus moduleEventBus;

    @ProvidesModule
    public ModuleEventBus mvpEventBus() {
        return moduleEventBus;
    }

    @Override
    public void onCreate() {
        moduleEventBus = new ModuleEventBus();
        super.onCreate();
    }

    @Override
    public void onTerminate() {
        moduleEventBus.destroy();
        super.onTerminate();
    }
}
