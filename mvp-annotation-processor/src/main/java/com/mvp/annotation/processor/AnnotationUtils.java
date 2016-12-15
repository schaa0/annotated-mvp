package com.mvp.annotation.processor;

import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;

/**
 * Created by Andy on 14.12.2016.
 */

public class AnnotationUtils {

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

}
