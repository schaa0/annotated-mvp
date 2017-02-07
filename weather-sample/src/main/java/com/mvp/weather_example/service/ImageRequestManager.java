package com.mvp.weather_example.service;

import android.graphics.Bitmap;

/**
 * Created by Andy on 23.12.2016.
 */

public interface ImageRequestManager {
    void load(String iconUrl, IconCallback callback);

    interface IconCallback {
        void onIconLoaded(Bitmap bitmap);
    }
}
