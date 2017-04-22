package de.hda.simple_example.di;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;

import com.mvp.ActivityRouter;
import com.mvp.FragmentRouter;
import com.mvp.annotation.FragmentScope;

import dagger.Component;
import dagger.Subcomponent;
import de.hda.simple_example.container.MainFragment;
import de.hda.simple_example.service.GithubService;


@Component(dependencies = { ComponentActivity.class })
@FragmentScope
public interface ComponentFragment
{
    void inject(MainFragment mainFragment);
    GithubService githubService();
    SharedPreferences sharedPreferences();
    AppCompatActivity activity();
    FragmentRouter fragmentRouter();
    ActivityRouter activityRouter();
}
