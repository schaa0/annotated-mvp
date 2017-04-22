package com.mvp.annotation.processor.graph;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Created by Andy on 17.04.2017.
 */

public class Dependency
{

    private final TypeElement typeElement;
    private final List<ExecutableElement> methods;

    private Dependency(TypeElement typeElement, List<ExecutableElement> methods)
    {
        this.typeElement = typeElement;
        this.methods = methods;
    }

    public static Dependency createFromComponent(Elements elementUtils, Types typeUtils, TypeElement component)
    {
        List<ExecutableElement> allMethods = new ArrayList<>();
        allMethods.addAll(getAllMethods(elementUtils, typeUtils, component));
        return new Dependency(component, allMethods);
    }

    private static List<ExecutableElement> getAllMethods(Elements elementUtils, Types typeUtils, TypeElement c)
    {
        List<ExecutableElement> allMethods = new ArrayList<>();
        if (!c.asType().toString().equals(Object.class.getName()))
        {
            for (Element element : c.getEnclosedElements())
            {
                if (element.getKind() == ElementKind.METHOD)
                {
                    allMethods.add((ExecutableElement) element);
                }
            }
            List<? extends TypeMirror> typeMirrors = typeUtils.directSupertypes(c.asType());
            for (TypeMirror typeMirror : typeMirrors)
            {
                if (!typeMirror.toString().equals(Object.class.getName()))
                {
                    allMethods.addAll(getAllMethods(elementUtils, typeUtils, elementUtils.getTypeElement(typeMirror.toString())));
                }
            }
        }
        return allMethods;
    }

    public static Dependency createFromModule(Elements elementUtils, Types typeUtils, TypeElement module)
    {
        if (module != null)
        {
            List<ExecutableElement> allMethods = new ArrayList<>();
            allMethods.addAll(getAllMethods(elementUtils, typeUtils, module));
            return new Dependency(module, allMethods);
        }
        return null;
    }

    public TypeElement getTypeElement()
    {
        return typeElement;
    }

    public List<ExecutableElement> getMethods()
    {
        return methods;
    }
}
