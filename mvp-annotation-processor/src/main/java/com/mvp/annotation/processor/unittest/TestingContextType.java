package com.mvp.annotation.processor.unittest;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;

/**
 * Created by Andy on 14.12.2016.
 */

public class TestingContextType extends AbsGeneratingType {

    public static final String TESTING_CONTEXT_CLASS_NAME = "TestingContext";

    public TestingContextType(Filer filer, String packageName){
        super(filer, packageName);
    }

    @Override
    protected TypeSpec.Builder build(){
        ClassName mvpEventBusClass = ClassName.get("com.mvp", "MvpEventBus");
        return TypeSpec.classBuilder(TESTING_CONTEXT_CLASS_NAME)
                .addModifiers(Modifier.PUBLIC)
                .addField(mvpEventBusClass, "mvpEventBus", Modifier.PRIVATE)
                .addMethod(MethodSpec.methodBuilder("eventBus")
                .addModifiers(Modifier.PUBLIC)
                .beginControlFlow("if (mvpEventBus == null)")
                .addCode("mvpEventBus = new MvpEventBus();")
                .endControlFlow()
                .addCode("return mvpEventBus;")
                .returns(mvpEventBusClass)
                .build());
    }

}
