package com.mvp.annotation.processor;

import com.mvp.annotation.Presenter;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Created by Andy on 14.12.2016.
 */

public class Utils {

    public static AnnotationValue getAnnotationValue(Element element, String key) {
        List<? extends AnnotationMirror> mirrors = element.getAnnotationMirrors();
        AnnotationValue value = null;
        for (AnnotationMirror mirror : mirrors){
            Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = mirror.getElementValues();
            for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : elementValues.entrySet()){
                if(key.equals(entry.getKey().getSimpleName().toString())) {
                    value = entry.getValue();
                    break;
                }
            }
        }
        return value;
    }

    public static boolean isActivity(Types typeUtils, Elements elementUtils, TypeMirror activityType) {
        return typeUtils.isAssignable(activityType, elementUtils.getTypeElement("android.support.v7.app.AppCompatActivity").asType());
    }

    public static boolean isFragment(Types typeUtils, Elements elementUtils, TypeMirror activityType) {
        return typeUtils.isAssignable(activityType, elementUtils.getTypeElement("android.support.v4.app.Fragment").asType());
    }

    public static boolean hasProvidesAnnotation(Element element) {
        List<? extends AnnotationMirror> mirrors = element.getAnnotationMirrors();
        AnnotationValue value = null;
        for (AnnotationMirror mirror : mirrors){
            Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = mirror.getElementValues();
            if (mirror.getAnnotationType().asElement().asType().toString().equals("dagger.Provides")){
                return true;
            }
        }
        return false;
    }

    public static String getShortestPackageName(Elements elementUtils, Set<? extends Element> elements) {
        String shortest = null;
        for (Element viewElement : elements) {
            String s = elementUtils.getPackageOf(viewElement).getQualifiedName().toString();
            if (shortest == null)
                shortest = s;
            else if (s.length() < shortest.length())
                shortest = s;
        }
        return shortest;
    }

    public static String findPresenterFieldInViewImplementationClass(Element elementActivityClass) {
        TypeElement typeElement = (TypeElement) elementActivityClass;
        List<? extends Element> enclosedElements = typeElement.getEnclosedElements();
        for (Element enclosedElement : enclosedElements) {
            if (enclosedElement.getKind() == ElementKind.FIELD){
                Presenter presenterAnnotation = enclosedElement.getAnnotation(Presenter.class);
                if (presenterAnnotation != null){
                    VariableElement variableElement = (VariableElement) enclosedElement;
                    return variableElement.getSimpleName().toString();
                }
            }
        }
        return null;
    }
}
