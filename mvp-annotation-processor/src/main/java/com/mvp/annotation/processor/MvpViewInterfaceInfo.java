package com.mvp.annotation.processor;

import java.util.HashMap;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

public class MvpViewInterfaceInfo {

    private final List<? extends Element> enclosedElements;
    private HashMap<String, String> methods = new HashMap<>();

    public MvpViewInterfaceInfo(List<? extends Element> enclosedElements){
        this.enclosedElements = enclosedElements;
        extractMethods();
    }

    private void extractMethods(){
        for (Element e : enclosedElements){
            if (e.getKind() == ElementKind.METHOD) {
                Name simpleName = e.getSimpleName();
                ExecutableElement executableElement = (ExecutableElement) e;
                String key = buildKey(executableElement);
                methods.put(key, simpleName.toString());
            }
        }
    }

    public boolean hasMethod(String viewMethodName, TypeMirror paramType){
        String key = buildKey(viewMethodName, paramType);
        return methods.get(key) != null;
    }

    public boolean hasMethod(String viewMethodName){
        if (methods.get(viewMethodName) != null) return true;
        return false;
    }

    private static String buildKey(ExecutableElement element){
        StringBuilder sb = new StringBuilder(element.getSimpleName());
        List<? extends VariableElement> parameters = element.getParameters();
        if (!parameters.isEmpty()) sb.append("#");
        for (VariableElement parameter : parameters) {
            sb.append(parameter.asType().toString());
        }
        return sb.toString();
    }

    private static String buildKey(String viewMethodName, TypeMirror paramType){
        return viewMethodName + "#" + paramType.toString();
    }

}
