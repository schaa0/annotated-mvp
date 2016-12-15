package com.mvp.annotation.processor;

import com.squareup.javapoet.ClassName;

public class Gang {

    private final ClassName activityClass;
    private final ClassName viewClass;
    private final ClassName presenterClass;

    public Gang(ClassName activityClass, ClassName presenterClass, ClassName viewClass){
        this.activityClass = activityClass;
        this.viewClass = viewClass;
        this.presenterClass = presenterClass;
    }

    public ClassName getActivityClass() {
        return activityClass;
    }

    public ClassName getViewClass() {
        return viewClass;
    }

    public ClassName getPresenterClass() {
        return presenterClass;
    }
}
