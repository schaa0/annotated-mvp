package de.hda.simple_example.container;

import android.location.LocationManager;

import com.mvp.ModuleEventBus;
import com.mvp.PresenterComponent;

import org.robolectric.util.ActivityController;

import de.hda.simple_example.TestablePresenterModule;
import de.hda.simple_example.business.DaggerComponentMainPresenter;
import de.hda.simple_example.business.GithubService;
import de.hda.simple_example.business.MainPresenter;
import de.hda.simple_example.inject.ModuleGithubService;
import de.hda.simple_example.inject.ModuleLocationManager;

import static org.mockito.Mockito.when;

/**
 * Created by Andy on 13.12.2016.
 */

public class MainPresenterBuilder {

    public static class BindingResult{
        private final ActivityController<MainActivity> controller;
        private final MainPresenter presenter;
        private final IView view;

        public BindingResult(ActivityController<MainActivity> controller, MainPresenter presenter, IView view){
            this.controller = controller;
            this.presenter = presenter;
            this.view = view;
        }

        public ActivityController<MainActivity> controller() {
            return controller;
        }

        public MainPresenter presenter() {
            return presenter;
        }

        public IView view() {
            return view;
        }
    }

    private final MainActivityController controller;
    private ModuleGithubService moduleGithubService;
    private GithubService githubService;
    private ModuleLocationManager moduleLocationManager;
    private LocationManager locationManager;
    private MainPresenter mockMainPresenter;
    private IView view;
    private TestingContext testingContext;

    public MainPresenterBuilder(MainActivityController controller){
        this.controller = controller;
    }

    public MainPresenterBuilder with(ModuleGithubService moduleGithubService){
        this.moduleGithubService = moduleGithubService;
        return this;
    }

    public MainPresenterBuilder withInstance(GithubService githubService){
        this.githubService = githubService;
        return this;
    }

    public MainPresenterBuilder with(ModuleLocationManager moduleLocationManager){
        this.moduleLocationManager = moduleLocationManager;
        return this;
    }

    public MainPresenterBuilder withInstance(LocationManager locationManager){
        this.locationManager = locationManager;
        return this;
    }

    public MainPresenterBuilder withMock(MainPresenter mainPresenter){
        this.mockMainPresenter = mainPresenter;
        return this;
    }

    public BindingResult build(){

        MainPresenter presenter;

        if (mockMainPresenter != null){

            presenter = controller.with(new PresenterComponent<IView, MainPresenter>() {
                @Override
                public MainPresenter newInstance() {
                    when(mockMainPresenter.getView()).thenReturn(view);
                    return mockMainPresenter;
                }

                @Override
                public IView view() {
                    return view;
                }
            }).build();

        }else{

            presenter = controller.with(
                    DaggerComponentMainPresenter.builder()
                            .moduleMainPresenterDependencies(new TestablePresenterModule(controller.activity(), view))
                            .moduleLocationManager(createModuleLocationManager())
                            .moduleGithubService(createModuleGithubService())
                            .moduleEventBus(new ModuleEventBus(testingContext.eventBus()))
                            .build()
            ).build();

        }

        controller.activity().presenter = presenter;
        controller.activity().setUp();

        return new BindingResult(this.controller.controller(), presenter, view);

    }

    private ModuleLocationManager createModuleLocationManager() {
        if (moduleLocationManager == null && locationManager == null){
            return new ModuleLocationManager(controller.activity().getApplicationContext());
        }else if (locationManager != null){
            return new LocationManagerDelegate(locationManager);
        }else if(moduleLocationManager != null){
            return moduleLocationManager;
        }else{
            throw new IllegalStateException("LocationManager not set!");
        }
    }

    private ModuleGithubService createModuleGithubService() {
        if (moduleLocationManager == null && locationManager == null){
            return new ModuleGithubService();
        }else if (locationManager != null){
            return new GithubServiceDelegate(githubService);
        }else if(moduleLocationManager != null){
            return moduleGithubService;
        }else{
            throw new IllegalStateException("LocationManager not set!");
        }
    }

    public MainPresenterBuilder withActivityAsView() {
        this.view = controller.activity();
        return this;
    }

    public MainPresenterBuilder withView(IView view){
        this.view = view;
        return this;
    }

    public MainPresenterBuilder with(TestingContext testingContext) {
        this.testingContext = testingContext;
        controller.with(this.testingContext);
        return this;
    }

    private static final class GithubServiceDelegate extends ModuleGithubService{
        private final GithubService item;

        public GithubServiceDelegate(GithubService item){
            this.item = item;
        }

        @Override
        public GithubService getGithubService() {
            return item;
        }
    }

    private static final class LocationManagerDelegate extends ModuleLocationManager{
        private final LocationManager item;

        public LocationManagerDelegate(LocationManager item){
            super(null);
            this.item = item;
        }

        @Override
        public LocationManager locationManager() {
            return item;
        }

    }

}
