package com.mvp.weather_example.service;

import android.graphics.Bitmap;
import android.net.Uri;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

/**
 * Created by Andy on 23.12.2016.
 */

public class ImageRequestManagerImpl implements ImageRequestManager {

    private RequestManager requestManager;

    public ImageRequestManagerImpl(RequestManager requestManager){
        this.requestManager = requestManager;
    }

    @Override
    public void load(String iconUrl, final IconCallback iconCallback) {
        requestManager.load(Uri.parse(iconUrl)).asBitmap().into(new SimpleTarget<Bitmap>()
        {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation)
            {
                iconCallback.onIconLoaded(resource);
            }
        });
    }

}
