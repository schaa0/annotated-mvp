package com.mvp.annotation.processor;

import com.mvp.annotation.Presenter;
import com.mvp.annotation.ProvidesComponent;
import com.mvp.annotation.ProvidesModule;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
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

    public static AnnotationValue getAnnotationValue(Element element, String annotationName, String key) {
        List<? extends AnnotationMirror> mirrors = element.getAnnotationMirrors();
        AnnotationValue value = null;
        for (AnnotationMirror mirror : mirrors){
            if (!mirror.getAnnotationType().asElement().asType().toString().equals(annotationName))
                continue;
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

    public static String toParameterName(ClassName module)
    {
        return toParameterName(module.simpleName());
    }

    public static boolean isActivity(Types typeUtils, Elements elementUtils, TypeMirror activityType) {
        return typeUtils.isAssignable(activityType, elementUtils.getTypeElement("android.support.v7.app.AppCompatActivity").asType());
    }

    public static boolean isComponent(Element element) {
        return hasComponentAnnotation(element);
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

    public static boolean hasInjectAnnotation(Element element) {
        List<? extends AnnotationMirror> mirrors = element.getAnnotationMirrors();
        for (AnnotationMirror mirror : mirrors){
            Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = mirror.getElementValues();
            if (mirror.getAnnotationType().asElement().asType().toString().equals("javax.inject.Inject")){
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

    public static String extractPackage(TypeMirror classType) {
        return classType.toString().replaceAll("." + convertDataClassToString(classType), "");
    }

    public static String extractClassName(TypeMirror classType) {
        return convertDataClassToString(classType);
    }

    public static String convertDataClassToString(TypeMirror dataClass) {
        String s = dataClass.toString();
        int index = s.lastIndexOf(".");
        return s.substring(index + 1);
    }

    public static HashMap<String, ExecutableElement> findProvidingMethodsOfModules(Types typeUtils, Element componentProvider){
        HashMap<String, ExecutableElement> providingMethods = new HashMap<>();
        if (componentProvider.getKind() == ElementKind.CLASS){
            TypeElement typeElement = (TypeElement) componentProvider;
            for (Map.Entry<String, ExecutableElement> e : findProvidingModuleMethodsInternal(typeElement, providingMethods).entrySet()) {
                if (!providingMethods.containsKey(e.getKey())) providingMethods.put(e.getKey(), e.getValue());
            }
            typeElement = (TypeElement) typeUtils.asElement(typeElement.getSuperclass());
            while (!typeElement.toString().equals(Object.class.getName())) {
                for (Map.Entry<String, ExecutableElement> e : findProvidingModuleMethodsInternal(typeElement, providingMethods).entrySet()) {
                    if (!providingMethods.containsKey(e.getKey())) providingMethods.put(e.getKey(), e.getValue());
                }
                typeElement = (TypeElement) typeUtils.asElement(typeElement.getSuperclass());
            }
        }
        return providingMethods;
    }

    public static HashMap<String, ExecutableElement> findProvidingMethodsOfComponents(Types typeUtils, Element componentProvider){
        HashMap<String, ExecutableElement> providingMethods = new HashMap<>();
        if (componentProvider.getKind() == ElementKind.CLASS){
            TypeElement typeElement = (TypeElement) componentProvider;
            for (Map.Entry<String, ExecutableElement> e : findProvidingComponentMethodsInternal(typeElement, providingMethods).entrySet()) {
                if (!providingMethods.containsKey(e.getKey()))
                    providingMethods.put(e.getKey(), e.getValue());
            }
            typeElement = (TypeElement) typeUtils.asElement(typeElement.getSuperclass());
            while (!typeElement.toString().equals(Object.class.getName())) {
                for (Map.Entry<String, ExecutableElement> e : findProvidingComponentMethodsInternal(typeElement, providingMethods).entrySet()) {
                    if (!providingMethods.containsKey(e.getKey()))
                        providingMethods.put(e.getKey(), e.getValue());
                }
                typeElement = (TypeElement) typeUtils.asElement(typeElement.getSuperclass());
            }
        }
        return providingMethods;
    }

    public static HashMap<String, ExecutableElement> findProvidingMethods(Types typeUtils, Element componentProvider){
        HashMap<String, ExecutableElement> providingMethods = new HashMap<>();
        if (componentProvider.getKind() == ElementKind.CLASS){
            TypeElement typeElement = (TypeElement) componentProvider;
            for (Map.Entry<String, ExecutableElement> e : findProvidingModuleMethodsInternal(typeElement, providingMethods).entrySet()) {
                if (!providingMethods.containsKey(e.getKey())) providingMethods.put(e.getKey(), e.getValue());
            }
            for (Map.Entry<String, ExecutableElement> e : findProvidingComponentMethodsInternal(typeElement, providingMethods).entrySet()) {
                if (!providingMethods.containsKey(e.getKey())) providingMethods.put(e.getKey(), e.getValue());
            }
            typeElement = (TypeElement) typeUtils.asElement(typeElement.getSuperclass());
            while (!typeElement.toString().equals(Object.class.getName())) {
                for (Map.Entry<String, ExecutableElement> e : findProvidingModuleMethodsInternal(typeElement, providingMethods).entrySet()) {
                    if (!providingMethods.containsKey(e.getKey())) providingMethods.put(e.getKey(), e.getValue());
                }
                for (Map.Entry<String, ExecutableElement> e : findProvidingComponentMethodsInternal(typeElement, providingMethods).entrySet()) {
                    if (!providingMethods.containsKey(e.getKey())) providingMethods.put(e.getKey(), e.getValue());
                }
                typeElement = (TypeElement) typeUtils.asElement(typeElement.getSuperclass());
            }
        }
        return providingMethods;
    }

    private static HashMap<String, ExecutableElement> findProvidingModuleMethodsInternal(TypeElement element, HashMap<String, ExecutableElement> providingMethods) {
        List<? extends Element> enclosedElements = element.getEnclosedElements();
        for (Element enclosedElement : enclosedElements) {
            if (enclosedElement.getKind() == ElementKind.METHOD){
                ProvidesModule providesModule = enclosedElement.getAnnotation(ProvidesModule.class);
                if (providesModule != null){
                    ExecutableElement executableElement = (ExecutableElement) enclosedElement;
                    providingMethods.put(executableElement.getReturnType().toString(), executableElement);
                }
            }
        }
        return providingMethods;
    }

    private static HashMap<String, ExecutableElement> findProvidingComponentMethodsInternal(TypeElement element, HashMap<String, ExecutableElement> providingMethods) {
        List<? extends Element> enclosedElements = element.getEnclosedElements();
        for (Element enclosedElement : enclosedElements) {
            if (enclosedElement.getKind() == ElementKind.METHOD){
                ProvidesComponent providesComponent = enclosedElement.getAnnotation(ProvidesComponent.class);
                if (providesComponent != null){
                    ExecutableElement executableElement = (ExecutableElement) enclosedElement;
                    providingMethods.put(executableElement.getReturnType().toString(), executableElement);
                }
            }
        }
        return providingMethods;
    }

    public static MethodSpec.Builder overrideConstructor(ExecutableElement method){
        Set<Modifier> modifiers = method.getModifiers();

        MethodSpec.Builder methodBuilder = MethodSpec.constructorBuilder();

        for (AnnotationMirror mirror : method.getAnnotationMirrors()) {
            AnnotationSpec annotationSpec = AnnotationSpec.get(mirror);
            if (!annotationSpec.type.equals(ClassName.get(Override.class)))
                methodBuilder.addAnnotation(annotationSpec);
        }

        modifiers = new LinkedHashSet<>(modifiers);
        modifiers.remove(Modifier.ABSTRACT);
        methodBuilder.addModifiers(modifiers);

        for (TypeParameterElement typeParameterElement : method.getTypeParameters()) {
            TypeVariable var = (TypeVariable) typeParameterElement.asType();
            methodBuilder.addTypeVariable(TypeVariableName.get(var));
        }

        List<? extends VariableElement> parameters = method.getParameters();
        for (VariableElement parameter : parameters) {
            TypeName type = TypeName.get(parameter.asType());
            String name = parameter.getSimpleName().toString();
            Set<Modifier> parameterModifiers = parameter.getModifiers();
            ParameterSpec.Builder parameterBuilder = ParameterSpec.builder(type, name)
                                                                  .addModifiers(parameterModifiers.toArray(new Modifier[parameterModifiers.size()]));
            for (AnnotationMirror mirror : parameter.getAnnotationMirrors()) {
                parameterBuilder.addAnnotation(AnnotationSpec.get(mirror));
            }
            methodBuilder.addParameter(parameterBuilder.build());
        }
        methodBuilder.varargs(method.isVarArgs());

        for (TypeMirror thrownType : method.getThrownTypes()) {
            methodBuilder.addException(TypeName.get(thrownType));
        }

        return methodBuilder;

    }

    public static MethodSpec.Builder override(ExecutableElement method) {

        Set<Modifier> modifiers = method.getModifiers();

        String methodName = method.getSimpleName().toString();
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName);

        for (AnnotationMirror mirror : method.getAnnotationMirrors()) {
            AnnotationSpec annotationSpec = AnnotationSpec.get(mirror);
            //if (!annotationSpec.type.equals(ClassName.get(Override.class)))
            //    methodBuilder.addAnnotation(annotationSpec);
        }

        modifiers = new LinkedHashSet<>(modifiers);
        modifiers.remove(Modifier.ABSTRACT);
        methodBuilder.addModifiers(modifiers);

        for (TypeParameterElement typeParameterElement : method.getTypeParameters()) {
            TypeVariable var = (TypeVariable) typeParameterElement.asType();
            methodBuilder.addTypeVariable(TypeVariableName.get(var));
        }

        methodBuilder.returns(TypeName.get(method.getReturnType()));

        List<? extends VariableElement> parameters = method.getParameters();
        for (VariableElement parameter : parameters) {
            TypeName type = TypeName.get(parameter.asType());
            String name = parameter.getSimpleName().toString();
            Set<Modifier> parameterModifiers = parameter.getModifiers();
            ParameterSpec.Builder parameterBuilder = ParameterSpec.builder(type, name)
                                                                  .addModifiers(parameterModifiers.toArray(new Modifier[parameterModifiers.size()]));
            for (AnnotationMirror mirror : parameter.getAnnotationMirrors()) {
                parameterBuilder.addAnnotation(AnnotationSpec.get(mirror));
            }
            methodBuilder.addParameter(parameterBuilder.build());
        }
        methodBuilder.varargs(method.isVarArgs());

        for (TypeMirror thrownType : method.getThrownTypes()) {
            methodBuilder.addException(TypeName.get(thrownType));
        }

        return methodBuilder;
    }

    public static boolean hasSubComponentAnnotation(Element element)
    {
        List<? extends AnnotationMirror> mirrors = element.getAnnotationMirrors();
        for (AnnotationMirror mirror : mirrors){
            if (mirror.getAnnotationType().asElement().asType().toString().equals("dagger.Subcomponent")){
                return true;
            }
        }
        return false;
    }

    public static boolean hasComponentAnnotation(Element element)
    {
        List<? extends AnnotationMirror> mirrors = element.getAnnotationMirrors();
        for (AnnotationMirror mirror : mirrors){
            if (mirror.getAnnotationType().asElement().asType().toString().equals("dagger.Component")){
                return true;
            }
        }
        return false;
    }

    public static String toParameterName(String simpleName)
    {
        return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
    }

    public static boolean isInnerClass(Types typeUtils, TypeMirror clazz)
    {
        Element factoryElement = typeUtils.asElement(clazz);
        Element enclosingElement = factoryElement.getEnclosingElement();
        if (enclosingElement != null && enclosingElement.getKind() == ElementKind.CLASS) {
            return true;
        }
        return false;
    }
}
