package com.mvp.annotation.processor;


import com.mvp.annotation.ProvidesModule;
import com.mvp.annotation.processor.unittest.AbsGeneratingType;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

public class ModuleBuilder extends AbsGeneratingType {

    private final String simplePresenterClassName;
    private final List<TypeMirror> params;
    private final Types types;
    private TypeSpec.Builder builder;

    public ModuleBuilder(Filer filer, String packageName, Types types, String simplePresenterClassName, List<TypeMirror> params) {
        super(filer, packageName);
        this.types = types;
        Collections.sort(params, new Comparator<TypeMirror>() {
            @Override
            public int compare(TypeMirror t1, TypeMirror t2) {
                return t1.toString().compareTo(t2.toString());
            }
        });
        this.simplePresenterClassName = simplePresenterClassName;
        this.params = params;
    }

    @Override
    protected TypeSpec.Builder build() {
        builder = TypeSpec.classBuilder("ModuleParams" + this.simplePresenterClassName);
        builder.addModifiers(Modifier.PUBLIC);
        builder.addAnnotation(ClassName.get("dagger", "Module"));
        this.createFields();
        this.builder.addMethod(this.createConstructor());
        this.createProvidingMethods();
        return builder;
    }

    private void createProvidingMethods() {
        for (int position = 0; position < this.params.size(); position++) {
            TypeMirror param = this.params.get(position);
            String paramTypeName = types.asElement(param).getSimpleName().toString();
            MethodSpec.Builder builder = MethodSpec.methodBuilder(paramTypeName.toLowerCase());
            builder.addModifiers(Modifier.PUBLIC);
            TypeName returnType = ClassName.get(param);
            builder.returns(returnType);
            String variableName = String.format("param%s", position);
            builder.addStatement("return this." + variableName);
            builder.addAnnotation(ClassName.get("dagger", "Provides"));
            this.builder.addMethod(builder.build());
        }
    }

    private void createFields() {
        for (int i = 0; i < this.params.size(); i++) {
            TypeMirror param = this.params.get(i);
            this.builder.addField(ClassName.get(param), "param" + i, Modifier.PRIVATE);
        }
    }

    private MethodSpec createConstructor() {
        MethodSpec.Builder builder = MethodSpec.constructorBuilder();
        builder.addModifiers(Modifier.PUBLIC);
        for (int i = 0; i < this.params.size(); i++) {
            TypeMirror param = this.params.get(i);
            builder.addParameter(ClassName.get(param), "param" + i);
            builder.addStatement(String.format("this.param%s = param%s", i, i));
        }
        return builder.build();
    }


}
