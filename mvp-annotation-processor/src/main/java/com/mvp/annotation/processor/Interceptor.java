package com.mvp.annotation.processor;


import javax.lang.model.type.TypeMirror;

public class Interceptor {

    private final String methodName;
    private final TypeMirror parameterType;
    private final TypeMirror returnType;
    private final String threadType;
    private String eventCondition;

    public Interceptor(String methodName, TypeMirror parameterType, TypeMirror returnType, String threadType, String eventCondition){
        this.methodName = methodName;
        this.parameterType = parameterType;
        this.returnType = returnType;
        this.threadType = threadType;
        this.eventCondition = eventCondition;
    }

    public String getMethodName() {
        return methodName;
    }

    public TypeMirror getReturnType() {
        return returnType;
    }

    public TypeMirror getParameterType() {
        return parameterType;
    }

    public String getThreadType() {
        return threadType;
    }

    public String getEventCondition() {
        return eventCondition;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Interceptor))
            return false;
        Interceptor other = (Interceptor) obj;
        if (!other.methodName.equals(methodName)) return false;
        if (!other.parameterType.equals(parameterType)) return false;
        if (!other.returnType.equals(returnType)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return ((methodName.hashCode() + parameterType.hashCode()) >> 2) + returnType.hashCode();
    }
}
