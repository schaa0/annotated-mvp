package com.mvp;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;

public interface BaseComponentActivity {
    SharedPreferences sharedPreferences();
    AppCompatActivity activity();
    FragmentRouter fragmentRouter();
    ActivityRouter activityRouter();
}
