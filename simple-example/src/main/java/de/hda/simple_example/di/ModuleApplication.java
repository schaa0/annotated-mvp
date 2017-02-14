package de.hda.simple_example.di;

import android.content.Context;
import android.preference.PreferenceManager;

import dagger.Module;
import dagger.Provides;
import de.hda.simple_example.business.Settings;
import de.hda.simple_example.business.SettingsImpl;

@Module
public class ModuleApplication {

    private Context context;

    public ModuleApplication(Context context){
        this.context = context;
    }

    @Provides
    @ApplicationScope
    public Context context(){
        return context;
    }

    @Provides
    @ApplicationScope
    public Settings sharedPreferences(Context context){
        return new SettingsImpl(PreferenceManager.getDefaultSharedPreferences(context));
    }

}
