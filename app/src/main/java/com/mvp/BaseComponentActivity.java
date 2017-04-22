package com.mvp;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;

public interface BaseComponentActivity {
    SharedPreferences sharedPreferences();
    ActivityRouter activityRouter();
    AppCompatActivity activity();
    FragmentRouter fragmentRouter();
}
