package com.mvp.annotation.processor.unittest;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;

public class TestRunnerType extends AbsGeneratingType
{

    private TypeName applicationClassType;

    public TestRunnerType(Filer filer, String packageName, TypeName applicationClassType)
    {
        super(filer, packageName);
        this.applicationClassType = applicationClassType;
    }

    @Override
    protected TypeSpec.Builder build()
    {
        ClassName abstractRunnerType = ClassName.bestGuess("com.mvp.AbstractRunner");
        ParameterizedTypeName typeName = ParameterizedTypeName.get(abstractRunnerType, applicationClassType);
        return TypeSpec.classBuilder("MvpTestRunner")
                .addModifiers(Modifier.PUBLIC)
                .superclass(typeName);
    }
}
