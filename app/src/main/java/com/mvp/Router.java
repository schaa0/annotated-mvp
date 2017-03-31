package com.mvp;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v4.util.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Andy on 31.03.2017.
 */

@Singleton
public class Router
{

    private Context context;

    @Inject
    public Router(Context context) {
        this.context = context;
    }

    public Navigation navigateTo(Class<?> target) {
        return new Navigation(this.context, target);
    }

    public static class Navigation {

        Intent intent;
        private Context context;

        Navigation(Context context, Class<?> target) {
            this.context = context;
            this.intent = new Intent(context, target);
        }
        
        public Navigation putExtra(String name, boolean value)
        {
            intent.putExtra(name, value);
            return this;
        }

        public Navigation putExtra(String name, byte value)
        {
            intent.putExtra(name, value);
            return this;
        }

        public Navigation putExtra(String name, char value)
        {
            intent.putExtra(name, value);
            return this;
        }

        public Navigation putExtra(String name, short value)
        {
            intent.putExtra(name, value);
            return this;
        }

        public Navigation putExtra(String name, int value)
        {
            intent.putExtra(name, value);
            return this;
        }

        public Navigation putExtra(String name, long value)
        {
            intent.putExtra(name, value);
            return this;
        }

        public Navigation putExtra(String name, float value)
        {
            intent.putExtra(name, value);
            return this;
        }

        public Navigation putExtra(String name, double value)
        {
            intent.putExtra(name, value);
            return this;
        }

        public Navigation putExtra(String name, String value)
        {
            intent.putExtra(name, value);
            return this;
        }

        public Navigation putExtra(String name, CharSequence value)
        {
            intent.putExtra(name, value);
            return this;
        }

        public Navigation putExtra(String name, Parcelable value)
        {
            intent.putExtra(name, value);
            return this;
        }

        public Navigation putExtra(String name, Parcelable[] value)
        {
            intent.putExtra(name, value);
            return this;
        }

        public Navigation putParcelableArrayListExtra(String name, ArrayList<? extends Parcelable> value)
        {
            intent.putParcelableArrayListExtra(name, value);
            return this;
        }

        public Navigation putIntegerArrayListExtra(String name, ArrayList<Integer> value)
        {
            intent.putIntegerArrayListExtra(name, value);
            return this;
        }

        public Navigation putStringArrayListExtra(String name, ArrayList<String> value)
        {
            intent.putStringArrayListExtra(name, value);
            return this;
        }

        public Navigation putCharSequenceArrayListExtra(String name, ArrayList<CharSequence> value)
        {
            intent.putCharSequenceArrayListExtra(name, value);
            return this;
        }

        public Navigation putExtra(String name, Serializable value)
        {
            intent.putExtra(name, value);
            return this;
        }

        public Navigation putExtra(String name, boolean[] value)
        {
            intent.putExtra(name, value);
            return this;
        }

        public Navigation putExtra(String name, byte[] value)
        {
            intent.putExtra(name, value);
            return this;
        }

        public Navigation putExtra(String name, short[] value)
        {
            intent.putExtra(name, value);
            return this;
        }

        public Navigation putExtra(String name, char[] value)
        {
            intent.putExtra(name, value);
            return this;
        }

        public Navigation putExtra(String name, int[] value)
        {
            intent.putExtra(name, value);
            return this;
        }

        public Navigation putExtra(String name, long[] value)
        {
            intent.putExtra(name, value);
            return this;
        }

        public Navigation putExtra(String name, float[] value)
        {
            intent.putExtra(name, value);
            return this;
        }

        public Navigation putExtra(String name, double[] value)
        {
            intent.putExtra(name, value);
            return this;
        }

        public Navigation putExtra(String name, String[] value)
        {
            intent.putExtra(name, value);
            return this;
        }

        public Navigation putExtra(String name, CharSequence[] value)
        {
            intent.putExtra(name, value);
            return this;
        }
        
        public void open() {
           this.context.startActivity(this.intent);
        }

    }

}
