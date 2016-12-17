package com.mvp.annotation.processor.unittest;

import com.mvp.annotation.processor.Gang;
import com.mvp.annotation.processor.Utils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;

/**
 * Created by Andy on 14.12.2016.
 */

public class TestControllerClass extends AbsGeneratingClass {


    private Gang gang;

    public TestControllerClass(Filer filer, String packageName, Gang gang) {
        super(filer, packageName);
        this.gang = gang;
    }

    @Override
    protected TypeSpec.Builder build() {
        String className = gang.getActivityClass().simpleName() + "Controller";
        ClassName testControllerClass = ClassName.bestGuess(concatSimpleNameWithPackage(className));
        ClassName moduleEventBusClass = ClassName.get("com.mvp", "ModuleEventBus");
        ParameterizedTypeName componentPresenterClass = ParameterizedTypeName.get(ClassName.get("com.mvp", "PresenterComponent"), gang.getViewClass(), gang.getPresenterClass());
        ParameterizedTypeName activityControllerClass = ParameterizedTypeName.get(ClassName.bestGuess("org.robolectric.util.ActivityController"), gang.getActivityClass());
        ClassName clazzClass = ClassName.get("java.lang", "Class");
        ParameterizedTypeName classOfActivity = ParameterizedTypeName.get(clazzClass, gang.getActivityClass());
        ClassName testingContextClass = ClassName.get(getPackageName(), "TestingContext");
        ClassName robolectricClass = ClassName.get("org.robolectric", "Robolectric");
        ParameterizedTypeName delegateBinder = ParameterizedTypeName.get(ClassName.get("com.mvp", "DelegateBinder"), gang.getViewClass(), gang.getPresenterClass());
        ClassName constructorClass = ClassName.get("java.lang.reflect", "Constructor");

        ParameterizedTypeName onPresenterLoadedListenerInterface = ParameterizedTypeName.get(ClassName.get("com.mvp", "OnPresenterLoadedListener"), gang.getViewClass(), gang.getPresenterClass());

        String presenterFieldName = Utils.findPresenterFieldInViewImplementationClass(gang.getElementActivityClass());

        return TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addField(activityControllerClass, "controller", Modifier.PRIVATE)
                .addField(componentPresenterClass, "presenterComponent", Modifier.PRIVATE)
                .addField(testingContextClass, "testingContext", Modifier.PRIVATE)
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addCode("this.controller = $T.buildActivity($T.class);\n", robolectricClass, gang.getActivityClass())
                        .build())
                .addMethod(MethodSpec.methodBuilder("with")
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(componentPresenterClass, "presenterComponent")
                        .addCode("this.presenterComponent = presenterComponent;\n")
                        .addCode("return this;\n")
                        .returns(testControllerClass)
                        .build())
                .addMethod(MethodSpec.methodBuilder("with")
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(testingContextClass, "testingContext")
                        .addCode("this.testingContext = testingContext;\n")
                        .addCode("return this;\n")
                        .returns(testControllerClass)
                        .build())
                .addMethod(MethodSpec.methodBuilder("activity")
                        .addModifiers(Modifier.PUBLIC)
                        .addCode("return controller.get();\n")
                        .returns(gang.getActivityClass())
                        .build())
                .addMethod(MethodSpec.methodBuilder("setupActivity")
                        .addModifiers(Modifier.PRIVATE)
                        .addParameter(delegateBinder, "binder")
                        .addCode("controller.create();\n" +
                                "binder.onCreate(null);\n" +
                                "binder.setOnPresenterLoadedListener(new $T(){\n" +
                                        "\n" +
                                        "    @Override\n" +
                                        "    public void onPresenterLoaded($T presenter) {\n" +
                                        "        controller.get()." + presenterFieldName + " = presenter;\n" +
                                        "    }\n" +
                                        "});\n" +
                                "controller.get()." + presenterFieldName + " = binder.getPresenter();\n" +
                                "controller.start();\n" +
                                "controller.postCreate(null);\n" +
                                "controller.resume();\n" +
                                "binder.onPostResume();\n" +
                                "controller.visible();", onPresenterLoadedListenerInterface, gang.getPresenterClass())
                        .returns(void.class)
                        .build())
                .addMethod(MethodSpec.methodBuilder("build")
                        .addModifiers(Modifier.PUBLIC)
                        .addCode("try {\n" +
                                        "   $T activity = controller.get();\n" +
                                        "   Class<?> clazz = $T.forName(activity.getClass().getPackage().getName() + \".\" + activity.getClass().getSimpleName() + \"DelegateBinder\");\n" +
                                        "   $T<?> constructor = clazz.getDeclaredConstructors()[0];\n" +
                                        "   $T binder = ($T) constructor.newInstance(activity, presenterComponent, new $T(testingContext.eventBus()));\n" +
                                        "   setupActivity(binder);\n" +
                                        "   $T presenter = binder.getPresenter();\n" +
                                        "   return presenter;\n" +
                                        "}  catch (java.lang.ClassNotFoundException e) {\n" +
                                        "       e.printStackTrace();\n" +
                                        "}  catch (java.lang.IllegalAccessException e) {\n" +
                                        "       e.printStackTrace();\n" +
                                        "}  catch (java.lang.InstantiationException e) {\n" +
                                        "       e.printStackTrace();\n" +
                                        "} catch (java.lang.reflect.InvocationTargetException e) {\n" +
                                        "       e.printStackTrace();\n" +
                                        "}\n" +
                                        "throw new java.lang.IllegalStateException(\"presenter could not be instantiated!\");",
                                gang.getActivityClass(), clazzClass, constructorClass, delegateBinder, delegateBinder, moduleEventBusClass, gang.getPresenterClass())
                        .returns(gang.getPresenterClass())
                        .build())
                .addMethod(MethodSpec.methodBuilder("controller")
                        .addModifiers(Modifier.PUBLIC)
                        .addCode("return controller;\n")
                        .returns(activityControllerClass)
                        .build());
    }
}
