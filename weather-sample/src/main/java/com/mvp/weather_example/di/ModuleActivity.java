package com.mvp.weather_example.di;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import com.mvp.BaseModuleActivity;
import com.mvp.weather_example.service.ViewPagerFragmentFactory;

import dagger.Module;
import dagger.Provides;

@Module
public class ModuleActivity extends BaseModuleActivity {

    public ModuleActivity(AppCompatActivity activity) {
        super(activity);
    }

    @Provides
    public ViewPagerFragmentFactory fragmentFactory(Context context) {
        return new ViewPagerFragmentFactory(context);
    }
}
