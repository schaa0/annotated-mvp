package com.mvp.annotation.processor;

import com.mvp.annotation.BackgroundThread;
import com.mvp.annotation.Event;
import com.mvp.annotation.UiThread;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.Types;

public class ProxyInfo {

    private final TypeMirror presenterClass;
    private final TypeMirror viewClass;
    private final List<ExecutableElement> initialMethods;

    public ProxyInfo(TypeMirror presenterClass, TypeMirror viewClass, List<ExecutableElement> initialMethods){
        this.presenterClass = presenterClass;
        this.viewClass = viewClass;
        this.initialMethods = initialMethods;
    }

    public TypeSpec processMethods(Types typeUtils){
        List<MethodSpec> writtenMethods = new ArrayList<>();
        DeclaredType declaredType = (DeclaredType) presenterClass;
        TypeSpec.Builder builder = TypeSpec.classBuilder(convertDataClassToString(presenterClass) + "Proxy");
        builder.addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        builder.superclass(ClassName.get(presenterClass));
        builder.addField(ClassName.get(presenterClass), "presenterImpl", Modifier.PRIVATE, Modifier.FINAL);
        builder.addMethod(MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get(presenterClass), "presenterImpl")
                .addCode(CodeBlock.of("this.presenterImpl = presenterImpl;"))
                .build());
        for (ExecutableElement initialMethod : initialMethods) {
            boolean paramsShouldBeFinal = initialMethod.getAnnotation(Event.class) != null || initialMethod.getAnnotation(BackgroundThread.class) != null;
            MethodSpec.Builder methodBuilder = delegate(initialMethod, declaredType, typeUtils, paramsShouldBeFinal);
            MethodSpec method = methodBuilder.build();
            String statement;
            if (method.returnType == TypeName.VOID){
                statement = "presenterImpl." + method.name + "(" + parametersToString(method.parameters)  + ")";
                BackgroundThread backgroundAnnotation = initialMethod.getAnnotation(BackgroundThread.class);
                UiThread uiThreadAnnotation = initialMethod.getAnnotation(UiThread.class);
                ClassName looperType = ClassName.get("android.os", "Looper");
                if(backgroundAnnotation != null){
                    methodBuilder.beginControlFlow("if ($T.myLooper().equals($T.getMainLooper()))", looperType, looperType)
                            .addCode(CodeBlock.of("this.presenterImpl.submit(\"" + method.name + "\", new Runnable(){ @Override public void run() { " + statement +  ";   } });"))
                            .nextControlFlow("else")
                            .addStatement(statement)
                            .endControlFlow();
                }else if(uiThreadAnnotation != null){
                    methodBuilder.beginControlFlow("if ($T.myLooper().equals($T.getMainLooper()))", looperType, looperType)
                            .addStatement(statement)
                            .nextControlFlow("else")
                            .addCode(CodeBlock.of("this.presenterImpl.submitOnUiThread(new Runnable(){ @Override public void run() { " + statement +  "; } });"))
                            .endControlFlow();
                }else{
                    methodBuilder.addStatement(statement);
                }
            }else{
                statement = "return presenterImpl." + method.name + "(" + parametersToString(method.parameters)  + ")";
                methodBuilder.addStatement(statement);
            }
            method = methodBuilder.build();
            if (!writtenMethods.contains(method)) {
                builder.addMethod(method);
                writtenMethods.add(method);
            }
        }

        return builder.build();
    }

    private String parametersToString(List<ParameterSpec> parameters) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parameters.size(); i++) {
            ParameterSpec parameterSpec = parameters.get(i);
            sb.append(parameterSpec.name);
            if (i < parameters.size() - 1)
                sb.append(", ");
        }
        return sb.toString();
    }

    private String convertDataClassToString(TypeMirror dataClass) {
        String s = dataClass.toString();
        int index = s.lastIndexOf(".");
        return s.substring(index + 1);
    }

    public static MethodSpec.Builder delegate(ExecutableElement method, DeclaredType enclosing, Types types, boolean paramsShouldBeFinal) {
        ExecutableType executableType = (ExecutableType) types.asMemberOf(enclosing, method);
        List<? extends TypeMirror> resolvedParameterTypes = executableType.getParameterTypes();
        TypeMirror resolvedReturnType = executableType.getReturnType();

        MethodSpec.Builder builder = delegate(method);
        builder.returns(TypeName.get(resolvedReturnType));
        try {
            Field f = builder.getClass().getDeclaredField("parameters");
            f.setAccessible(true);
            List<ParameterSpec> parameters = (List<ParameterSpec>) f.get(builder);
            for (int i = 0, size = parameters.size(); i < size; i++) {
                ParameterSpec parameter = parameters.get(i);
                TypeName type = TypeName.get(resolvedParameterTypes.get(i));
                //parameters.set(i, parameter.toBuilder(type, parameter.name).build());
                ParameterSpec.Builder paramBuilder = ParameterSpec.builder(type, parameter.name);
                if (paramsShouldBeFinal) paramBuilder.addModifiers(Modifier.FINAL);
                parameters.set(i, paramBuilder.build());
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return builder;
    }

    public static MethodSpec.Builder delegate(ExecutableElement method) {

        Set<Modifier> modifiers = method.getModifiers();

        String methodName = method.getSimpleName().toString();
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName);

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

}

