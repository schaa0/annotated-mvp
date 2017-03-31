package de.hda.simple_example.di;

import android.support.v7.app.AppCompatActivity;

import com.mvp.BaseApplicationProvider;
import com.mvp.ModuleActivity;
import com.mvp.ModuleContext;
import com.mvp.annotation.Provider;
import com.mvp.annotation.ProvidesComponent;
import com.mvp.annotation.ProvidesModule;

import de.hda.simple_example.business.MainPresenter;
import de.hda.simple_example.di.ComponentActivity;
import de.hda.simple_example.di.ComponentApplication;
import de.hda.simple_example.di.ComponentFragment;
import de.hda.simple_example.di.DaggerComponentApplication;
import de.hda.simple_example.di.ModuleSingleton;
import de.hda.simple_example.di.ModuleMainPresenterState;
import de.hda.simple_example.di.ModuleRepository;
import de.hda.simple_example.model.Repository;

@Provider
public class SimpleApplication extends BaseApplicationProvider {

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
    public ModuleMainPresenterState moduleMainPresenterState(MainPresenter.State state){
        return new ModuleMainPresenterState(state);
    }

    @ProvidesComponent
    public ComponentApplication componentApplication(){
        return componentApplication;
    }

    @ProvidesComponent
    public ComponentActivity componentActivity(AppCompatActivity activity){
        return componentApplication.plus(new ModuleActivity(activity));
    }

    @ProvidesComponent
    public ComponentFragment componentFragment(ComponentActivity componentActivity) {
        return componentActivity.plus();
    }

}
