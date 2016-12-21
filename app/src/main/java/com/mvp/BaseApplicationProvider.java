package com.mvp;

import android.app.Application;

import com.mvp.annotation.ProvidesComponent;
import com.mvp.annotation.ProvidesModule;

/**
 * Created by Andy on 13.12.2016.
 */

public class BaseApplicationProvider extends Application {

    private ModuleEventBus moduleEventBus = new ModuleEventBus();

    @ProvidesModule
    public ModuleEventBus eventBus(){
        return moduleEventBus;
    }

}
