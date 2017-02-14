package com.mvp.weather_example.service;

import android.graphics.Bitmap;

public interface ImageRequestManager {
    void load(String iconUrl, IconCallback callback);

    interface IconCallback {
        void onIconLoaded(Bitmap bitmap);
    }
}
