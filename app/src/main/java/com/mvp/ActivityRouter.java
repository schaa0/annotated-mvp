package com.mvp;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;

import com.mvp.annotation.ActivityScope;

import java.io.Serializable;
import java.util.ArrayList;

import javax.inject.Inject;

@ActivityScope
public class ActivityRouter
{

    private AppCompatActivity activity;

    @Inject
    public ActivityRouter(AppCompatActivity activity) {
        this.activity = activity;
    }

    public ActivityNavigation navigateTo(Class<?> target) {
        return new ActivityNavigation(this.activity, target);
    }

    public void goBack() {
        this.activity.finish();
    }

    public static class ActivityNavigation {

        Intent intent;
        private Context context;

        ActivityNavigation(Context context, Class<?> target) {
            this.context = context;
            this.intent = new Intent(context, target);
        }
        
        public ActivityNavigation putExtra(String name, boolean value)
        {
            intent.putExtra(name, value);
            return this;
        }

        public ActivityNavigation putExtra(String name, byte value)
        {
            intent.putExtra(name, value);
            return this;
        }

        public ActivityNavigation putExtra(String name, char value)
        {
            intent.putExtra(name, value);
            return this;
        }

        public ActivityNavigation putExtra(String name, short value)
        {
            intent.putExtra(name, value);
            return this;
        }

        public ActivityNavigation putExtra(String name, int value)
        {
            intent.putExtra(name, value);
            return this;
        }

        public ActivityNavigation putExtra(String name, long value)
        {
            intent.putExtra(name, value);
            return this;
        }

        public ActivityNavigation putExtra(String name, float value)
        {
            intent.putExtra(name, value);
            return this;
        }

        public ActivityNavigation putExtra(String name, double value)
        {
            intent.putExtra(name, value);
            return this;
        }

        public ActivityNavigation putExtra(String name, String value)
        {
            intent.putExtra(name, value);
            return this;
        }

        public ActivityNavigation putExtra(String name, CharSequence value)
        {
            intent.putExtra(name, value);
            return this;
        }

        public ActivityNavigation putExtra(String name, Parcelable value)
        {
            intent.putExtra(name, value);
            return this;
        }

        public ActivityNavigation putExtra(String name, Parcelable[] value)
        {
            intent.putExtra(name, value);
            return this;
        }

        public ActivityNavigation putParcelableArrayListExtra(String name, ArrayList<? extends Parcelable> value)
        {
            intent.putParcelableArrayListExtra(name, value);
            return this;
        }

        public ActivityNavigation putIntegerArrayListExtra(String name, ArrayList<Integer> value)
        {
            intent.putIntegerArrayListExtra(name, value);
            return this;
        }

        public ActivityNavigation putStringArrayListExtra(String name, ArrayList<String> value)
        {
            intent.putStringArrayListExtra(name, value);
            return this;
        }

        public ActivityNavigation putCharSequenceArrayListExtra(String name, ArrayList<CharSequence> value)
        {
            intent.putCharSequenceArrayListExtra(name, value);
            return this;
        }

        public ActivityNavigation putExtra(String name, Serializable value)
        {
            intent.putExtra(name, value);
            return this;
        }

        public ActivityNavigation putExtra(String name, boolean[] value)
        {
            intent.putExtra(name, value);
            return this;
        }

        public ActivityNavigation putExtra(String name, byte[] value)
        {
            intent.putExtra(name, value);
            return this;
        }

        public ActivityNavigation putExtra(String name, short[] value)
        {
            intent.putExtra(name, value);
            return this;
        }

        public ActivityNavigation putExtra(String name, char[] value)
        {
            intent.putExtra(name, value);
            return this;
        }

        public ActivityNavigation putExtra(String name, int[] value)
        {
            intent.putExtra(name, value);
            return this;
        }

        public ActivityNavigation putExtra(String name, long[] value)
        {
            intent.putExtra(name, value);
            return this;
        }

        public ActivityNavigation putExtra(String name, float[] value)
        {
            intent.putExtra(name, value);
            return this;
        }

        public ActivityNavigation putExtra(String name, double[] value)
        {
            intent.putExtra(name, value);
            return this;
        }

        public ActivityNavigation putExtra(String name, String[] value)
        {
            intent.putExtra(name, value);
            return this;
        }

        public ActivityNavigation putExtra(String name, CharSequence[] value)
        {
            intent.putExtra(name, value);
            return this;
        }
        
        public void commit() {
            if (this.context != null) {
                this.context.startActivity(this.intent);
            }
        }

    }

}
