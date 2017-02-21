package com.mvp.annotation.processor.unittest;

import com.mvp.annotation.Generate;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeSpec;

import java.util.Iterator;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

/**
 * Created by Andy on 13.02.2017.
 */

public class TriggerType extends AbsGeneratingType
{
    private final Set<Element> viewElements;
    private ClassName applicationClassName;

    public TriggerType(Filer filer, String packageName, Set<Element> viewElements, ClassName applicationClassName)
    {
        super(filer, packageName);
        this.viewElements = viewElements;
        this.applicationClassName = applicationClassName;
    }

    @Override
    protected TypeSpec.Builder build()
    {
        String params = "{";
        ClassName[] vars = new ClassName[viewElements.size()];
        int i = 0;
        for (Iterator<Element> it = viewElements.iterator(); it.hasNext(); i++)
        {
            Element element = it.next();
            params += "$T.class";
            vars[i] = ClassName.bestGuess(element.asType().toString());
            if (i < viewElements.size() - 1)
            {
                params += ", ";
            }
        }
        params += "}";
        AnnotationSpec.Builder annotationBuilder = AnnotationSpec.builder(Generate.class);
        annotationBuilder.addMember("views", params, (Object[]) vars);
        annotationBuilder.addMember("application", "$T.class", applicationClassName);
        return TypeSpec.classBuilder("Trigger")
                .addModifiers(Modifier.ABSTRACT)
                .addAnnotation(annotationBuilder.build());
    }
}
