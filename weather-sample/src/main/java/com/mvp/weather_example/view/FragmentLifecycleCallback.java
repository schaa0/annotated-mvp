package com.mvp.weather_example.view;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;

/**
 * Created by Andy on 21.01.2017.
 */

public interface FragmentLifecycleCallback
{
    void onCreate(Bundle savedInstanceState);
    void onStart();
    void onActivityCreated(Bundle savedInstanceState);
    void onResume();
}
