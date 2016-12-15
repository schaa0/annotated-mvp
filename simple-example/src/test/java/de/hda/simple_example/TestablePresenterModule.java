package de.hda.simple_example;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;

import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.concurrent.RoboExecutorService;

import java.util.concurrent.ExecutorService;
import de.hda.simple_example.business.ModuleMainPresenterDependencies;
import de.hda.simple_example.container.IView;
import de.hda.simple_example.container.MainActivity;

/**
 * Created by Andy on 02.12.2016.
 */

public class TestablePresenterModule extends ModuleMainPresenterDependencies {

    public TestablePresenterModule(AppCompatActivity activity, IView view) {
        super(activity, view);
    }

    @Override
    public Handler getMainHandler() {
        return new Handler(Looper.myLooper());
    }

    @Override
    public ExecutorService getBackgroundExecutorService() {
        return new RoboExecutorService();
    }

    @Override
    public Context getApplicationContext() {
        return RuntimeEnvironment.application.getApplicationContext();
    }

}
