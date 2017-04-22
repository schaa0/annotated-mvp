package de.hda.simple_example.di;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import com.mvp.MvpApplication;
import com.mvp.BaseModuleContext;
import com.mvp.annotation.Provider;
import com.mvp.annotation.ProvidesComponent;
import com.mvp.annotation.ProvidesModule;

@Provider
public class SimpleApplication extends MvpApplication {

    private ComponentApplication componentApplication;

    @ProvidesComponent
    public ComponentApplication componentApplication(){
        if (componentApplication == null)
        {
            componentApplication = DaggerComponentApplication.builder()
                                                             .moduleSingleton(this.moduleSingleton())
                                                             .moduleEventBus(this.mvpEventBus())
                                                             .build();
        }
        return componentApplication;
    }

    @ProvidesModule
    public ModuleSingleton moduleSingleton() {
        return new ModuleSingleton(this.getApplicationContext());
    }

    @ProvidesModule
    public ModuleActivity moduleActivity(AppCompatActivity activity) {
        return new ModuleActivity(activity);
    }

    @ProvidesComponent
    public ComponentActivity componentActivity(AppCompatActivity activity){
        return DaggerComponentActivity.builder()
                .componentApplication(this.componentApplication())
                .moduleActivity(new ModuleActivity(activity))
                .build();
    }

    @ProvidesComponent
    public ComponentFragment componentFragment(ComponentActivity componentActivity) {
        return DaggerComponentFragment.builder()
                .componentActivity(componentActivity)
                .build();
    }

}
