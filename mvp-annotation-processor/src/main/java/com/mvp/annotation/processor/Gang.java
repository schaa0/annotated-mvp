package com.mvp.annotation.processor;

import com.squareup.javapoet.ClassName;

import javax.lang.model.element.Element;

public class Gang {

    private final ClassName activityClass;
    private final ClassName viewClass;
    private final ClassName presenterClass;

    private final Element elementActivityClass;
    private final Element elementViewClass;
    private final Element elementPresenterClass;

    public Gang(Element activityClass, Element presenterClass, Element viewClass){
        this.activityClass = ClassName.bestGuess(activityClass.asType().toString());
        this.presenterClass = ClassName.bestGuess(presenterClass.asType().toString());
        this.viewClass = ClassName.bestGuess(viewClass.asType().toString());
        this.elementActivityClass = activityClass;
        this.elementViewClass = viewClass;
        this.elementPresenterClass = presenterClass;
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

    public Element getElementActivityClass() {
        return elementActivityClass;
    }

    public Element getElementViewClass() {
        return elementViewClass;
    }

    public Element getElementPresenterClass() {
        return elementPresenterClass;
    }
}
