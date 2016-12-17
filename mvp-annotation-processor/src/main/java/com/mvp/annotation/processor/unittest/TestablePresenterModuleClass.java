package com.mvp.annotation.processor.unittest;

import com.mvp.annotation.processor.Gang;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.Elements;

/**
 * Created by Andy on 16.12.2016.
 */
public class TestablePresenterModuleClass extends AbsGeneratingClass{

    private final Filer filer;
    private Elements elementUtils;
    private final String packageName;
    private Gang gang;

    public TestablePresenterModuleClass(Filer filer, Elements elementUtils, String packageName, Gang gang) {
        super(filer, packageName);
        this.filer = filer;
        this.elementUtils = elementUtils;
        this.packageName = packageName;
        this.gang = gang;
    }

    private String getPackageName(Element viewElement) {
        return elementUtils.getPackageOf(viewElement).getQualifiedName().toString();
    }

    @Override
    protected TypeSpec.Builder build() {
        ClassName appCompatActivityClass = ClassName.get("android.support.v7.app", "AppCompatActivity");
        ClassName handler = ClassName.get("android.os", "Handler");
        ClassName looper = ClassName.get("android.os", "Looper");
        ClassName executorService = ClassName.get("org.robolectric.util.concurrent", "RoboExecutorService");
        ClassName context = ClassName.get("android.content", "Context");

        ClassName superClass = ClassName.get(getPackageName(gang.getElementPresenterClass()), "Module" + gang.getPresenterClass().simpleName() + "Dependencies");

        return TypeSpec.classBuilder("Testable" + gang.getPresenterClass().simpleName() + "Dependencies")
                .superclass(superClass)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(appCompatActivityClass, "activity")
                        .addParameter(gang.getViewClass(), "view")
                        .addCode("super(activity, view);\n")
                        .build())
                .addMethod(MethodSpec.methodBuilder("getMainHandler")
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(Override.class)
                        .addCode("return new $T($T.myLooper());\n", handler, looper)
                        .returns(handler)
                        .build())

                .addMethod(MethodSpec.methodBuilder("getBackgroundExecutorService")
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(Override.class)
                        .addCode("return new $T();\n", executorService)
                        .returns(executorService)
                        .build())
                .addMethod(MethodSpec.methodBuilder("getApplicationContext")
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(Override.class)
                        .addCode("return activity.getApplicationContext();\n")
                        .returns(context)
                        .build());
    }
}
