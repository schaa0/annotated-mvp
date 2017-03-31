package de.hda.simple_example.di;

import android.support.v7.app.AppCompatActivity;

import com.mvp.BaseModuleActivity;
import com.mvp.MvpApplication;
import com.mvp.ModuleContext;
import com.mvp.annotation.Provider;
import com.mvp.annotation.ProvidesComponent;
import com.mvp.annotation.ProvidesModule;
import de.hda.simple_example.presenter.MainFragmentPresenter;
import de.hda.simple_example.model.Repository;

@Provider
public class SimpleApplication extends MvpApplication {

    private ComponentApplication componentApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        componentApplication = DaggerComponentApplication.builder()
                                                         .moduleSingleton(new ModuleSingleton())
                                                         .moduleContext(new ModuleContext(this.getApplicationContext()))
                                                         .moduleEventBus(this.mvpEventBus())
                                                         .build();
    }

    @ProvidesModule
    public ModuleRepository moduleRepository(Repository repository) {
        return new ModuleRepository(repository);
    }

    @ProvidesModule
    public ModuleMainPresenterState moduleMainPresenterState(MainFragmentPresenter.State state){
        return new ModuleMainPresenterState(state);
    }

    @ProvidesComponent
    public ComponentApplication componentApplication(){
        return componentApplication;
    }

    @ProvidesComponent
    public ComponentActivity componentActivity(AppCompatActivity activity){
        return DaggerComponentActivity.builder()
                .componentApplication(this.componentApplication)
                .moduleActivity(new ModuleActivity(activity))
                .build();
    }

    public ComponentFragment componentFragment(ComponentActivity componentActivity) {
        return DaggerComponentFragment.builder()
                .componentActivity(componentActivity)
                .build();
    }

}
