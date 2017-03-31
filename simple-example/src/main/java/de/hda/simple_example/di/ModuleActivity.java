package de.hda.simple_example.di;

import android.support.v7.app.AppCompatActivity;
import com.mvp.BaseModuleActivity;
import dagger.Module;

@Module
public class ModuleActivity extends BaseModuleActivity {

    public ModuleActivity(AppCompatActivity activity) {
        super(activity);
    }
}
