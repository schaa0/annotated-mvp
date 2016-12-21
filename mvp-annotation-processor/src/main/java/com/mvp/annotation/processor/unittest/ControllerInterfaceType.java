package com.mvp.annotation.processor.unittest;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;

/**
 * Created by Andy on 19.12.2016.
 */

public class ControllerInterfaceType extends AbsGeneratingType {

    public ControllerInterfaceType(Filer filer, String packageName) {
        super(filer, packageName);
    }

    @Override
    protected TypeSpec.Builder build() {
        ClassName viewTypeParam = ClassName.get("com.mvp", "MvpView");
        TypeVariableName t = TypeVariableName.get("T");
        TypeVariableName b = TypeVariableName.get("B");
        TypeVariableName v = TypeVariableName.get("V", viewTypeParam);
        ParameterizedTypeName classTypeName = ParameterizedTypeName.get(ClassName.bestGuess("java.lang.Class"), v);
        return TypeSpec.interfaceBuilder("Controller")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addTypeVariable(t)
                .addTypeVariable(v)
                .addTypeVariable(b)
                .addMethod(MethodSpec.methodBuilder("withView")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .addParameter(v, "view")
                        .returns(t)
                        .build())
                .addMethod(MethodSpec.methodBuilder("withViewImplementation")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .returns(t)
                        .build())
                .addMethod(MethodSpec.methodBuilder("with")
                        .addParameter(ClassName.get(getPackageName(), "TestingContext"), "testingContext")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .returns(t)
                        .build())
                .addMethod(MethodSpec.methodBuilder("getViewClass")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .returns(classTypeName)
                        .build())
                .addMethod(MethodSpec.methodBuilder("withMockPresenter")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .returns(t)
                        .build())
                .addMethod(MethodSpec.methodBuilder("build")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .returns(b)
                        .build());
    }
}
