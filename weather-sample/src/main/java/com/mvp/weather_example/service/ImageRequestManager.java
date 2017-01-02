package com.mvp.weather_example.service;

import android.graphics.Bitmap;
import android.net.Uri;

import com.bumptech.glide.request.target.SimpleTarget;

/**
 * Created by Andy on 23.12.2016.
 */

public interface ImageRequestManager {
    void load(String iconUrl, SimpleTarget<Bitmap> target);
}
