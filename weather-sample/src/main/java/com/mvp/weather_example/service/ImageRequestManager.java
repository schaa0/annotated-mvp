package com.mvp.weather_example.service;

import android.graphics.Bitmap;
import android.net.Uri;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import javax.inject.Inject;

public class ImageRequestManager {

    public interface IconCallback {
        void onIconLoaded(Bitmap bitmap);
    }

    private RequestManager requestManager;

    @Inject
    public ImageRequestManager(RequestManager requestManager){
        this.requestManager = requestManager;
    }

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
