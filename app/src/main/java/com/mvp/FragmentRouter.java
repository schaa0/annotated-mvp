package com.mvp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.mvp.annotation.ActivityScope;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import javax.inject.Inject;

@ActivityScope
public class FragmentRouter {

    private FragmentManager fragmentManager;

    @Inject
    public FragmentRouter(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }

    public FragmentNavigation navigateTo(Class<?> target) {
        return new FragmentNavigation(this.fragmentManager, target);
    }

    public class FragmentNavigation {

        private final FragmentTransaction ft;
        private final Class<?> target;
        private FragmentManager fragmentManager;

        private Bundle bundle = new Bundle();
        private Fragment fragment;

        FragmentNavigation(FragmentManager fragmentManager, Class<?> target) {
            this.fragmentManager = fragmentManager;
            this.ft = fragmentManager.beginTransaction();
            this.target = target;
        }

        public FragmentNavigation putExtra(String name, boolean value)
        {
            bundle.putBoolean(name, value);
            return this;
        }

        public FragmentNavigation putExtra(String name, byte value)
        {
            bundle.putByte(name, value);
            return this;
        }

        public FragmentNavigation putExtra(String name, char value)
        {
            bundle.putChar(name, value);
            return this;
        }

        public FragmentNavigation putExtra(String name, short value)
        {
            bundle.putShort(name, value);
            return this;
        }

        public FragmentNavigation putExtra(String name, int value)
        {
            bundle.putInt(name, value);
            return this;
        }

        public FragmentNavigation putExtra(String name, long value)
        {
            bundle.putLong(name, value);
            return this;
        }

        public FragmentNavigation putExtra(String name, float value)
        {
            bundle.putFloat(name, value);
            return this;
        }

        public FragmentNavigation putExtra(String name, double value)
        {
            bundle.putDouble(name, value);
            return this;
        }

        public FragmentNavigation putExtra(String name, String value)
        {
            bundle.putString(name, value);
            return this;
        }

        public FragmentNavigation putExtra(String name, CharSequence value)
        {
            bundle.putCharSequence(name, value);
            return this;
        }

        public FragmentNavigation putExtra(String name, Parcelable value)
        {
            bundle.putParcelable(name, value);
            return this;
        }

        public FragmentNavigation putExtra(String name, Parcelable[] value)
        {
            bundle.putParcelableArray(name, value);
            return this;
        }

        public FragmentNavigation putParcelableArrayListExtra(String name, ArrayList<? extends Parcelable> value)
        {
            bundle.putParcelableArrayList(name, value);
            return this;
        }

        public FragmentNavigation putIntegerArrayListExtra(String name, ArrayList<Integer> value)
        {
            bundle.putIntegerArrayList(name, value);
            return this;
        }

        public FragmentNavigation putStringArrayListExtra(String name, ArrayList<String> value)
        {
            bundle.putStringArrayList(name, value);
            return this;
        }

        public FragmentNavigation putCharSequenceArrayListExtra(String name, ArrayList<CharSequence> value)
        {
            bundle.putCharSequenceArrayList(name, value);
            return this;
        }

        public FragmentNavigation putExtra(String name, Serializable value)
        {
            bundle.putSerializable(name, value);
            return this;
        }

        public FragmentNavigation putExtra(String name, boolean[] value)
        {
            bundle.putBooleanArray(name, value);
            return this;
        }

        public FragmentNavigation putExtra(String name, byte[] value)
        {
            bundle.putByteArray(name, value);
            return this;
        }

        public FragmentNavigation putExtra(String name, short[] value)
        {
            bundle.putShortArray(name, value);
            return this;
        }

        public FragmentNavigation putExtra(String name, char[] value)
        {
            bundle.putCharArray(name, value);
            return this;
        }

        public FragmentNavigation putExtra(String name, int[] value)
        {
            bundle.putIntArray(name, value);
            return this;
        }

        public FragmentNavigation putExtra(String name, long[] value)
        {
            bundle.putLongArray(name, value);
            return this;
        }

        public FragmentNavigation putExtra(String name, float[] value)
        {
            bundle.putFloatArray(name, value);
            return this;
        }

        public FragmentNavigation putExtra(String name, double[] value)
        {
            bundle.putDoubleArray(name, value);
            return this;
        }

        public FragmentNavigation putExtra(String name, String[] value)
        {
            bundle.putStringArray(name, value);
            return this;
        }

        public FragmentNavigation putExtra(String name, CharSequence[] value)
        {
            bundle.putCharSequenceArray(name, value);
            return this;
        }

        public FragmentNavigation add(String tag) {
            this.initFragment();
            this.ft.add(fragment, tag);
            return this;
        }

        public FragmentNavigation replace(int container, String tag) {
            this.initFragment();
            this.ft.replace(container, fragment, tag);
            return this;
        }

        public FragmentNavigation remove(String tag) {
            Fragment f = this.fragmentManager.findFragmentByTag(tag);
            if (f != null) {
                ft.remove(f);
            }
            return this;
        }

        public FragmentNavigation attach(String tag) {
            Fragment f = this.fragmentManager.findFragmentByTag(tag);
            if (f != null) {
                ft.attach(f);
            }
            return this;
        }

        public FragmentNavigation withBackStack(String name) {
            ft.addToBackStack(name);
            return this;
        }

        public FragmentNavigation detach(String tag) {
            Fragment f = this.fragmentManager.findFragmentByTag(tag);
            if (f != null) {
                ft.detach(f);
            }
            return this;
        }

        public void commit() {
            if (!ft.isEmpty()) {
                ft.commit();
            }
        }

        private void initFragment() {
            if (fragment == null) {
                try {
                    fragment = (Fragment) target.getConstructors()[0].newInstance();
                    fragment.setArguments(this.bundle);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
