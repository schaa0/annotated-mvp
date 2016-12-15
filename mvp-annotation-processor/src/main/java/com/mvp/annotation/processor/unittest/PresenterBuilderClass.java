package com.mvp.annotation.processor.unittest;

import com.mvp.annotation.processor.Gang;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;

/**
 * Created by Andy on 15.12.2016.
 */
public class PresenterBuilderClass extends AbsGeneratingClass{

    private final Gang gang;

    public PresenterBuilderClass(Filer filer, String packageName, Gang gang) {
        super(filer, packageName);
        this.gang = gang;
    }


    @Override
    protected TypeSpec.Builder build() {
        ClassName activityControllerClassName = ClassName.get(getPackageName(), gang.getActivityClass() + "Controller");
        String className = gang.getPresenterClass() + "Builder";
        return TypeSpec.classBuilder(className)
                .addMethod(MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(activityControllerClassName, "controller")
                    .addCode("this.controller = controller;\n")
                    .build())
                .addField(activityControllerClassName, "controller", Modifier.PRIVATE);
    }
}
