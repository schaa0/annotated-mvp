package com.mvp;

import android.os.Bundle;

public interface MvpViewWithStateInLayout<T extends IMvpPresenter<? extends MvpView>> extends MvpViewInLayout<T> {
    void onSaveInstanceState(Bundle bundle);
    void onRestoreInstanceState(Bundle bundle);
}
