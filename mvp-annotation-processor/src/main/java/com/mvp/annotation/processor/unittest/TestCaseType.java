package com.mvp.annotation.processor.unittest;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.lang.reflect.Type;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;

/**
 * Created by Andy on 20.12.2016.
 */

public class TestCaseType extends AbsGeneratingType {

    public static final String TESTCASE_CLASSNAME = "TestCase";
    public static final String METHODNAME_CONFIGURE_PRESENTER = "configurePresenter";
    private static final String METHODNAME_GET_EVENT_BUS_COMPONENT = "componentEventBus";

    public TestCaseType(Filer filer, String packageName) {
        super(filer, packageName);
    }

    @Override
    protected TypeSpec.Builder build() {

        TypeVariableName t = TypeVariableName.get("T");
        TypeVariableName v = TypeVariableName.get("V", ClassName.get("com.mvp", "MvpView"));
        TypeVariableName a = TypeVariableName.get("A");

        ParameterizedTypeName controllerType = ParameterizedTypeName.get(ClassName.get(getPackageName(), "Controller"), t, v, a);
        ClassName viewTypeEnum = ClassName.get(getPackageName(), "ViewType");
        ClassName presenterTypeEnum = ClassName.get(getPackageName(), "PresenterType");

        TypeSpec.Builder builder = TypeSpec.classBuilder(TESTCASE_CLASSNAME);
        ClassName testingContextClass = ClassName.get(getPackageName(), "TestingContext");
        ClassName mockitoClass = ClassName.get("org.mockito", "Mockito");

        ClassName componentEventBus = ClassName.get("com.mvp", "ComponentEventBus");
        ClassName daggerComponentEventBus = ClassName.get("com.mvp", "DaggerComponentEventBus");
        ClassName customEventBus = ClassName.get("com.mvp", "ModuleCustomEventBus");
        builder.addField(testingContextClass, "testingContext")
               .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
               .addMethod(MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addCode("this.testingContext = new $T();\n", testingContextClass)
                    .build())
               .addMethod(MethodSpec.methodBuilder(METHODNAME_CONFIGURE_PRESENTER)
                    .addModifiers(Modifier.PROTECTED)
                    .addTypeVariable(t)
                    .addTypeVariable(v)
                    .addTypeVariable(a)
                    .addParameter(controllerType, "builder")
                    .addParameter(viewTypeEnum, "viewType")
                    .addParameter(presenterTypeEnum, "presenterType")
                    .addCode("builder.with(testingContext);\n")
                    .beginControlFlow("if (viewType == $T.REAL)", viewTypeEnum)
                    .addCode("builder.withViewImplementation();\n")
                    .nextControlFlow("else")
                    .addCode("builder.withView($T.mock(builder.getViewClass()));\n", mockitoClass)
                    .endControlFlow()
                    .beginControlFlow("if(presenterType == $T.MOCK)", presenterTypeEnum)
                    .addCode("builder.withMockPresenter();\n")
                    .endControlFlow()
                    .addCode("return builder.build();\n")
                    .returns(a)
                    .build())
               .addField(componentEventBus, "componentEventBus", Modifier.PRIVATE)
               .addMethod(MethodSpec.methodBuilder(METHODNAME_GET_EVENT_BUS_COMPONENT)
                             .addModifiers(Modifier.PROTECTED)
                             .returns(componentEventBus)
                             .beginControlFlow("if (this.componentEventBus == null)")
                             .addStatement("this.componentEventBus = $T.builder().moduleCustomEventBus(new $T(this.testingContext.eventBus())).build()", daggerComponentEventBus, customEventBus)
                             .endControlFlow()
                             .addStatement("return this.componentEventBus")
                             .build());

        return builder;
    }
}
