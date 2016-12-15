package com.mvp;

import android.support.v7.app.AppCompatActivity;

/**
 * Created by Andy on 02.12.2016.
 */

public interface ComponentActivity<A> {
    void inject(A injectee);
}
