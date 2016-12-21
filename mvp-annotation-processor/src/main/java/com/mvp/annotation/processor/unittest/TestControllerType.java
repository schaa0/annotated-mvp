package com.mvp.annotation.processor.unittest;

import com.mvp.annotation.processor.Gang;
import com.mvp.annotation.processor.Utils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Created by Andy on 14.12.2016.
 */

public class TestControllerType extends AbsGeneratingType {


    private Types typeUtils;
    private Elements elementUtils;
    private Gang gang;

    public TestControllerType(Filer filer, Types typeUtils, Elements elementUtils, String packageName, Gang gang) {
        super(filer, packageName);
        this.typeUtils = typeUtils;
        this.elementUtils = elementUtils;
        this.gang = gang;
    }

    @Override
    protected TypeSpec.Builder build() {
        String className = gang.getActivityClass().simpleName() + "Controller";
        ClassName testControllerClass = ClassName.bestGuess(concatSimpleNameWithPackage(className));
        ClassName moduleEventBusClass = ClassName.get("com.mvp", "ModuleEventBus");
        ParameterizedTypeName componentPresenterClass = ParameterizedTypeName.get(ClassName.get("com.mvp", "PresenterComponent"), gang.getViewClass(), gang.getPresenterClass());
        ClassName supportFragmentControllerClass = ClassName.bestGuess("org.robolectric.shadows.support.v4.SupportFragmentController");
        ParameterizedTypeName activityControllerClass = null;
        if (isActivity()) {
            activityControllerClass = ParameterizedTypeName.get(ClassName.bestGuess("org.robolectric.util.ActivityController"), gang.getActivityClass());
        }else if(isFragment()) {
            activityControllerClass = ParameterizedTypeName.get(supportFragmentControllerClass, gang.getActivityClass());
        }
        ClassName clazzClass = ClassName.get("java.lang", "Class");
        ParameterizedTypeName classOfActivity = ParameterizedTypeName.get(clazzClass, gang.getActivityClass());
        ClassName testingContextClass = ClassName.get("com.mvp", "TestingContext");
        ClassName robolectricClass = ClassName.get("org.robolectric", "Robolectric");
        ParameterizedTypeName delegateBinder = ParameterizedTypeName.get(ClassName.get("com.mvp", "DelegateBinder"), gang.getViewClass(), gang.getPresenterClass());
        ClassName constructorClass = ClassName.get("java.lang.reflect", "Constructor");

        ParameterizedTypeName onPresenterLoadedListenerInterface = ParameterizedTypeName.get(ClassName.get("com.mvp", "OnPresenterLoadedListener"), gang.getViewClass(), gang.getPresenterClass());

        String presenterFieldName = Utils.findPresenterFieldInViewImplementationClass(gang.getElementActivityClass());

        String constructorCode = null;
        ClassName[] constructorCodeParams = null;
        if (isActivity()){
            constructorCode = "this.controller = $T.buildActivity($T.class);\n";
            constructorCodeParams = new ClassName[]{robolectricClass, gang.getActivityClass()};
        }else{
            constructorCode = "this.controller = $T.of(fragment, activity);\n";
            constructorCodeParams = new ClassName[]{supportFragmentControllerClass};
        }

        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addCode(constructorCode, constructorCodeParams);

        if (isFragment()){
            constructorBuilder.addParameter(gang.getActivityClass(), "fragment");
            constructorBuilder.addParameter(ParameterizedTypeName.get(ClassName.get("java.lang", "Class"), WildcardTypeName.subtypeOf(ClassName.get("android.support.v7.app", "AppCompatActivity"))), "activity");
        }

        TypeSpec.Builder builder = TypeSpec.classBuilder(className);

        if (isFragment()){
            builder.addField(int.class, "container", Modifier.PRIVATE);
            builder.addMethod(MethodSpec.methodBuilder("in")
                    .addParameter(int.class, "container")
                    .addCode("this.container = container;\n")
                    .addCode("return this;\n")
                    .returns(testControllerClass)
                    .build());
        }

        if (isFragment()) {
            ClassName bundle = ClassName.get("android.os", "Bundle");
            builder.addField(bundle, "bundle", Modifier.PRIVATE);

            builder.addMethod(MethodSpec.methodBuilder("withSavedInstanceState")
                    .addParameter(bundle, "bundle")
                    .addModifiers(Modifier.PUBLIC)
                    .addCode("this.bundle = bundle;\n")
                    .addCode("return this;\n")
                    .returns(testControllerClass)
                    .build());
        }

        if (isFragment()) {
            ParameterizedTypeName activityController = ParameterizedTypeName.get(ClassName.get("org.robolectric.util", "ActivityController"), WildcardTypeName.subtypeOf(ClassName.bestGuess("android.support.v4.app.FragmentActivity")));
            MethodSpec.Builder activityControllerMethod = MethodSpec.methodBuilder("activityController")
                    .addModifiers(Modifier.PUBLIC)
                    .addCode("$T activityController = null;\n" +
                            "            try {\n" +
                            "                java.lang.reflect.Field f = controller.getClass().getDeclaredField(\"activityController\");\n" +
                            "                f.setAccessible(true);\n" +
                            "                return ($T) f.get(controller);\n" +
                            "            } catch (java.lang.NoSuchFieldException e) {\n" +
                            "                e.printStackTrace();\n" +
                            "            } catch (java.lang.IllegalAccessException e) {\n" +
                            "                e.printStackTrace();\n" +
                            "            }\n" +
                            "            return null;\n", activityController, activityController)
                    .returns(activityController);
            builder.addMethod(activityControllerMethod.build());
        }

        MethodSpec.Builder buildMethod = MethodSpec.methodBuilder("build")
                .addModifiers(Modifier.PUBLIC);

        if (isFragment()) {
            ClassName appCompatActivity = ClassName.bestGuess("android.support.v7.app.AppCompatActivity");
            buildMethod.addCode("$T activity = ($T) activityController().get();\n", appCompatActivity, appCompatActivity);
        }
        else
            buildMethod.addCode("$T activity = controller.get();\n", gang.getActivityClass());

        buildMethod.addCode("try {\n" +
                                "   Class<?> clazz = $T.forName(controller.get().getClass().getPackage().getName() + \".\" + controller.get().getClass().getSimpleName() + \"DelegateBinder\");\n" +
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
                        clazzClass, constructorClass, delegateBinder, delegateBinder, moduleEventBusClass, gang.getPresenterClass())
                .returns(gang.getPresenterClass());

        String createParams = isFragment() ? "container, bundle" : "";
        String onCreateParams = isFragment() ? "bundle" : "null";

        ClassName mvpActivityDelegate = ClassName.get("com.mvp", "MvpActivityDelegate");

        if (isFragment()) {
            MethodSpec.Builder methodInitialize = MethodSpec.methodBuilder("initialize")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(void.class);
            methodInitialize.beginControlFlow("if (this.bundle != null)");
            methodInitialize.addCode("this.bundle.putInt($T.KEY_INSTANCE_ID, this.hashCode());", mvpActivityDelegate);
            methodInitialize.endControlFlow();
            methodInitialize.addCode("controller.create(container, bundle);\n");
            builder.addMethod(methodInitialize.build());
        }

        MethodSpec.Builder methodSetupActivity = MethodSpec.methodBuilder("setupActivity");
        /*if (isFragment()){
            methodSetupActivity.beginControlFlow("if (this.bundle != null)");
            methodSetupActivity.addCode("this.bundle.putInt($T.KEY_INSTANCE_ID, this.hashCode());", mvpActivityDelegate);
            methodSetupActivity.endControlFlow();
        }*/


        return builder
                .addModifiers(Modifier.PUBLIC)
                .addField(activityControllerClass, "controller", Modifier.PRIVATE)
                .addField(componentPresenterClass, "presenterComponent", Modifier.PRIVATE)
                .addField(testingContextClass, "testingContext", Modifier.PRIVATE)
                .addMethod(constructorBuilder
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
                .addMethod(methodSetupActivity
                        .addModifiers(Modifier.PRIVATE)
                        .addParameter(delegateBinder, "binder")
                        .addCode(String.format((isFragment()?"":"controller.create(" + createParams + ");\n") +
                                "binder.onCreate("+ onCreateParams + ");\n" +
                                "binder.setOnPresenterLoadedListener(new $T(){\n" +
                                        "\n" +
                                        "    @Override\n" +
                                        "    public void onPresenterLoaded($T presenter) {\n" +
                                        "        controller.get()." + presenterFieldName + " = presenter;\n" +
                                        "    }\n" +
                                        "});\n" +
                                "controller.get()." + presenterFieldName + " = binder.getPresenter();\n" +
                                "controller.start();\n" +
                                "%s" +
                                "controller.resume();\n" +
                                "binder.onPostResume();\n" +
                                "controller.visible();", isActivity() ? "controller.postCreate(null);\n" : ""), onPresenterLoadedListenerInterface, gang.getPresenterClass())
                        .returns(void.class)
                        .build())
                .addMethod(buildMethod
                        .build())
                .addMethod(MethodSpec.methodBuilder("controller")
                        .addModifiers(Modifier.PUBLIC)
                        .addCode("return controller;\n")
                        .returns(activityControllerClass)
                        .build());
    }

    private boolean isFragment() {
        return Utils.isFragment(typeUtils, elementUtils, gang.getElementActivityClass().asType());
    }

    private boolean isActivity() {
        return Utils.isActivity(typeUtils, elementUtils, gang.getElementActivityClass().asType());
    }
}
