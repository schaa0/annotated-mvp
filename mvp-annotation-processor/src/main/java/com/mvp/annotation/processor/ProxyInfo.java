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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.Types;

public class ProxyInfo {

    public static final String FORMAT_MISSING_PARAMETERLESS_CONSTRUCTOR = "%s must declare a protected or public constructor!";
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
        List<? extends Element> elements = declaredType.asElement().getEnclosedElements();
        boolean hasParameterLessConstructor = false;
        for (Element element : elements){
            if (element.getKind() == ElementKind.CONSTRUCTOR){
                ExecutableElement e = (ExecutableElement) element;
                List<? extends VariableElement> params = e.getParameters();
                if (params.isEmpty()) {
                    hasParameterLessConstructor = true;
                    break;
                }
            }
        }

        if (!hasParameterLessConstructor)
            throw new IllegalStateException(String.format(FORMAT_MISSING_PARAMETERLESS_CONSTRUCTOR, presenterClass.toString()));

        TypeSpec.Builder builder = TypeSpec.classBuilder(convertDataClassToString(presenterClass) + "Proxy");
        builder.addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        builder.superclass(ClassName.get(presenterClass));
        builder.addField(ClassName.get(presenterClass), "presenterImpl", Modifier.PRIVATE, Modifier.FINAL);
        builder.addMethod(MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get(presenterClass), "presenterImpl")
                .addCode("super();\n")
                .addCode("this.presenterImpl = presenterImpl;\n")
                .build());
        for (ExecutableElement initialMethod : initialMethods) {
            boolean paramsShouldBeFinal = initialMethod.getAnnotation(Event.class) != null || initialMethod.getAnnotation(BackgroundThread.class) != null || initialMethod.getAnnotation(UiThread.class) != null;
            MethodSpec.Builder methodBuilder = delegate(initialMethod, declaredType, typeUtils, paramsShouldBeFinal);
            MethodSpec method = methodBuilder.build();

            String statement;

            if (methodIsProtectedAndDeclaredInLibraryPackage(initialMethod)){
                continue;
            }

            if (methodIsPackageProtected(initialMethod.getModifiers())){

                int size = initialMethod.getParameters().size();
                String[] typeNames = new String[size];
                String[] parameters = new String[size];
                String typeMirrorFormat = "";
                String parameterFormat = "";
                for (int i = 0; i < size; i++){
                    VariableElement e = initialMethod.getParameters().get(i);
                    if (!e.asType().toString().equals("T"))
                        typeNames[i] = e.asType().toString();
                    parameters[i] = e.getSimpleName().toString();
                    if (!e.asType().toString().equals("T"))
                        typeMirrorFormat += "%s.class";
                    parameterFormat += "%s";
                    if (i < size - 1){
                        if (!e.asType().toString().equals("T"))
                            typeMirrorFormat += ", ";
                        parameterFormat += ", ";
                    }
                }

                if (!typeMirrorFormat.equals("")) {
                    typeMirrorFormat = String.format(typeMirrorFormat, (Object[]) typeNames);
                    typeMirrorFormat = ", " + typeMirrorFormat;
                }

                if (!parameterFormat.equals("")){
                    parameterFormat = String.format(parameterFormat, (Object[]) parameters);
                    parameterFormat = ", " + parameterFormat;
                }

                String ret = (method.returnType != TypeName.VOID ? "return " : "");
                String cast = (method.returnType != TypeName.VOID) ? ("(" + method.returnType  + ")") : "";
                methodBuilder.addCode(CodeBlock.of("try {"));
                methodBuilder.addCode(CodeBlock.of("$T m = this.presenterImpl.getClass().getDeclaredMethod(\"" + initialMethod.getSimpleName() + "\"" + typeMirrorFormat + ");\n", Method.class));
                methodBuilder.addCode(CodeBlock.of( ret + cast + "m.invoke(this.presenterImpl" + parameterFormat + ");"));
                methodBuilder.addCode(CodeBlock.of("} catch (NoSuchMethodException e) {\n" +
                        "            e.printStackTrace();\n" +
                        "        } catch ($T e) {\n" +
                        "            e.printStackTrace();\n" +
                        "        } catch (IllegalAccessException e) {\n" +
                        "            e.printStackTrace();\n" +
                        "        }", InvocationTargetException.class));

               // methodBuilder.addCode(CodeBlock.of("throw new $T();", IllegalStateException.class));

            }else if (method.returnType == TypeName.VOID){
                statement = String.format("presenterImpl.%s(%s)", method.name, parametersToString(method.parameters));
                BackgroundThread backgroundAnnotation = initialMethod.getAnnotation(BackgroundThread.class);
                UiThread uiThreadAnnotation = initialMethod.getAnnotation(UiThread.class);
                ClassName looperType = ClassName.get("android.os", "Looper");
                if(backgroundAnnotation != null){
                    methodBuilder.beginControlFlow("if ($T.myLooper().equals($T.getMainLooper()))", looperType, looperType)
                            .addStatement(String.format("this.presenterImpl.submit(\"%s\", new Runnable(){ @Override public void run() { %s; } })", method.name, statement))
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

    private boolean methodIsProtectedAndDeclaredInLibraryPackage(ExecutableElement initialMethod) {
        TypeElement typeElement = (TypeElement) initialMethod.getEnclosingElement();
        for (Modifier modifier : initialMethod.getModifiers()) {
            if (modifier.equals(Modifier.PROTECTED)){
                String packageName = Utils.extractPackage(typeElement.asType());
                ClassName presenterClassName = ClassName.bestGuess(presenterClass.toString());
                if (!packageName.equals(presenterClassName.packageName())){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean methodIsPackageProtected(Set<Modifier> modifiers) {
        if (modifiers.contains(Modifier.PUBLIC)) return false;
        if (modifiers.contains(Modifier.PROTECTED)) return false;
        return true;
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

