package de.hda.simple_example.di;

import android.support.v7.app.AppCompatActivity;

import de.hda.simple_example.service.GithubService;

/**
 * Created by Andy on 22.04.2017.
 */

public class TestApp extends SimpleApplication
{

    private TestDaggerComponentApplication.ILocationManagerProvider locationManagerProvider;

    public TestApp withLocationManager(TestDaggerComponentApplication.ILocationManagerProvider locationManagerProvider)
    {
        this.locationManagerProvider = locationManagerProvider;
        return this;
    }

    @Override
    public ComponentApplication componentApplication()
    {
        return new TestDaggerComponentApplication()
                .moduleEventBus(this.mvpEventBus())
                .moduleSingleton(this.moduleSingleton())
                .withLocationManager(this.locationManagerProvider)
                .build();
    }

    @Override
    public ComponentActivity componentActivity(AppCompatActivity activity)
    {
        return new TestDaggerComponentActivity()
                .moduleActivity(this.moduleActivity(activity))
                .build();
    }

    @Override
    public ComponentFragment componentFragment(ComponentActivity componentActivity)
    {
        return new TestDaggerComponentFragment()
                .componentActivity(componentActivity)
                .build();
    }
}
