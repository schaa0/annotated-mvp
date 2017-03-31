package com.mvp.annotation.processor.unittest;

import com.mvp.annotation.ModuleParam;
import com.mvp.annotation.processor.Gang;
import com.mvp.annotation.processor.Utils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static com.mvp.annotation.processor.Utils.getAnnotationValue;
import static com.mvp.annotation.processor.Utils.hasProvidesAnnotation;
import static com.mvp.annotation.processor.Utils.toParameterName;

/**
 * Created by Andy on 15.12.2016.
 */
public class PresenterBuilderType extends AbsGeneratingType
{

    private final Gang gang;
    private Elements elementUtils;
    private Types typeUtil;
    private String shortestPackageName;
    private Element componentProvider;

    private HashMap<String, String> moduleClassNamesToFieldNames = new HashMap<>();
    private HashMap<String, String> componentClassNamesToFieldNames = new HashMap<>();

    public PresenterBuilderType(Filer filer, Elements elementUtils, Types typeUtil, String packageName, Gang gang, String shortestPackageName, Element componentProvider)
    {
        super(filer, packageName);
        this.elementUtils = elementUtils;
        this.typeUtil = typeUtil;
        this.gang = gang;
        this.shortestPackageName = shortestPackageName;
        this.componentProvider = componentProvider;
    }


    @Override
    protected TypeSpec.Builder build()
    {

        List<ClassName> modules = findModules(gang.getElementPresenterClass());
        List<ClassName> components = findComponents(gang.getElementPresenterClass());

        HashMap<String, Module> instancesFromModules = extractProvidedClassesFromModules(modules);
        HashMap<String, Component> instancesFromComponents = extractProvidedClassesFromComponents(components);

        HashMap<String, ExecutableElement> providingMethods = Utils.findProvidingMethods(typeUtil, this.componentProvider);

        ClassName activityControllerClassName = ClassName.get(getPackageName(), gang.getActivityClass().simpleName() + "Controller");
        ClassName presenterBuilderClass = ClassName.get(getPackageName(), gang.getPresenterClass().simpleName() + "Builder");
        ClassName bindingResultClassName = presenterBuilderClass.nestedClass("BindingResult");

        ParameterizedTypeName controllerInterface = ParameterizedTypeName.get(ClassName.get(shortestPackageName, "Controller"), presenterBuilderClass, gang.getViewClass(), bindingResultClassName);

        String className = gang.getPresenterClass().simpleName() + "Builder";

        TypeName providerType = ClassName.get(componentProvider.asType());
        TypeSpec.Builder builder = TypeSpec.classBuilder(className)
                                           .superclass(controllerInterface)
                                           .addModifiers(Modifier.PUBLIC)
                                           .addMethod(MethodSpec.constructorBuilder()
                                                                .addModifiers(Modifier.PUBLIC)
                                                                .addParameter(activityControllerClassName, "controller")
                                                                .addParameter(providerType, "provider")
                                                                .addStatement("this.controller = controller")
                                                                .addStatement("this.provider = provider")
                                                                .build())
                                           .addField(activityControllerClassName, "controller", Modifier.PRIVATE)
                                           .addField(providerType, "provider", Modifier.PRIVATE);


        builder.addMethod(MethodSpec.methodBuilder("getViewClass")
                                    .addAnnotation(Override.class)
                                    .addModifiers(Modifier.PROTECTED)
                                    .addCode("return $T.class;\n", gang.getViewClass())
                                    .returns(ParameterizedTypeName.get(ClassName.bestGuess("java.lang.Class"), gang.getViewClass()))
                                    .build());

        ClassName bundle = ClassName.get("android.os", "Bundle");
        builder.addField(bundle, "bundle", Modifier.PRIVATE);

        builder.addMethod(MethodSpec.methodBuilder("withSavedInstanceState")
                                    .addParameter(bundle, "bundle")
                                    .addModifiers(Modifier.PUBLIC)
                                    .addCode("this.bundle = bundle;\n")
                                    .addCode("return this;\n")
                                    .returns(presenterBuilderClass)
                                    .build());

        if (isActivity())
        {
            ClassName intent = ClassName.get("android.content", "Intent");
            builder.addField(intent, "intent", Modifier.PRIVATE);
            builder.addMethod(MethodSpec.methodBuilder("withIntent")
                                        .addParameter(intent, "intent")
                                        .addModifiers(Modifier.PUBLIC)
                                        .addCode("this.intent = intent;\n")
                                        .addCode("return this;\n")
                                        .returns(presenterBuilderClass)
                                        .build());
        }


        for (ClassName module : modules)
        {

            String parameterName = toParameterName(module);
            builder.addField(module, parameterName, Modifier.PRIVATE);
/*                   .addMethod(MethodSpec.methodBuilder("with")
                                        .addModifiers(Modifier.PUBLIC)
                                        .addParameter(module, parameterName)
                                        .returns(presenterBuilderClass)
                                        .addCode(String.format("this.%s = %s;\n", parameterName, parameterName))
                                        .addCode("return this;\n")
                                        .build());*/
        }

        for (ClassName component : components)
        {
            String parameterName = toParameterName(component);
            builder.addField(component, parameterName, Modifier.PRIVATE);
                   /*.addMethod(MethodSpec.methodBuilder("with")
                                        .addModifiers(Modifier.PUBLIC)
                                        .addParameter(component, parameterName)
                                        .returns(presenterBuilderClass)
                                        .addCode(String.format("this.%s = %s;\n", parameterName, parameterName))
                                        .addCode("return this;\n")
                                        .build());*/
        }

        for (Map.Entry<String, Module> instancesFromModule : instancesFromModules.entrySet())
        {
            Module module = instancesFromModule.getValue();
            ArrayList<ModuleMethod> moduleMethods = module.getModuleMethods();
            List<String> fieldNames = new ArrayList<>();
            for (ModuleMethod moduleMethod : moduleMethods)
            {
                ClassName providedInstanceClassName = moduleMethod.getClassType();
                ExecutableElement executableElement = moduleMethod.getExecutableElement();
                String value = getValueFromNamedAnnotation(executableElement);
                String parameterName = toParameterName(providedInstanceClassName) + value;
                builder.addField(providedInstanceClassName, parameterName, Modifier.PRIVATE);
                fieldNames.add(parameterName);
                if (!moduleMethod.isConstructorParameter() && providingMethods.get(providedInstanceClassName.toString()) != null)
                {
                    builder.addMethod(MethodSpec.methodBuilder("withInstance")
                                                .addModifiers(Modifier.PUBLIC)
                                                .addParameter(providedInstanceClassName, parameterName)
                                                .returns(presenterBuilderClass)
                                                .addCode(String.format("this.%s = %s;\n", parameterName, parameterName))
                                                .addCode("return this;\n")
                                                .build());
                    moduleClassNamesToFieldNames.put(providedInstanceClassName.toString(), parameterName);
                }
            }
            module.setFieldNames(fieldNames);
        }

        for (Map.Entry<String, Component> instancesFromComponent : instancesFromComponents.entrySet())
        {
            List<String> fieldNames = new ArrayList<>();
            Component component = instancesFromComponent.getValue();
            for (ExecutableElement method : component.methods)
            {
                if (method.getReturnType().toString().equals(void.class.getName())) {
                    fieldNames.add("");
                    continue;
                }
                ClassName providedInstanceClassName = ClassName.bestGuess(method.getReturnType().toString());
                String parameterName = toParameterName(providedInstanceClassName);
                String value = getValueFromNamedAnnotation(method);
                parameterName += value;
                builder.addField(providedInstanceClassName, parameterName, Modifier.PRIVATE);
                fieldNames.add(parameterName);
                String methodName;
                if (isConstructorParameter((TypeElement) gang.getElementPresenterClass(), method.getReturnType()))
                {
                    methodName = "parameter";
                } else
                {
                    methodName = "withInstance";
                }
                if (!value.equals("")) methodName += "Named" + value;
                builder.addMethod(MethodSpec.methodBuilder(methodName)
                                            .addModifiers(Modifier.PUBLIC)
                                            .addParameter(providedInstanceClassName, parameterName)
                                            .returns(presenterBuilderClass)
                                            .addCode(String.format("this.%s = %s;\n", parameterName, parameterName))
                                            .addCode("return this;\n")
                                            .build());
                componentClassNamesToFieldNames.put(providedInstanceClassName.toString() + value, parameterName);
            }
            component.setFieldNames(fieldNames);
        }

        builder.addField(gang.getPresenterClass(), "mockPresenter", Modifier.PRIVATE)
               .addMethod(MethodSpec.methodBuilder("withMockPresenter")
                                    .addModifiers(Modifier.PROTECTED)
                                    .addCode("this.mockPresenter = Mockito.mock($T.class);\n", gang.getPresenterClass())
                                    .addCode("return this;\n")
                                    .returns(presenterBuilderClass)
                                    .build());

        builder.addField(gang.getViewClass(), "view", Modifier.PRIVATE);
        builder.addMethod(MethodSpec.methodBuilder("withView")
                                    .addAnnotation(Override.class)
                                    .addModifiers(Modifier.PROTECTED)
                                    .addParameter(gang.getViewClass(), "view")
                                    .addCode("this.view = view;\n")
                                    .addCode("return this;\n")
                                    .returns(presenterBuilderClass)
                                    .build());

        builder.addMethod(MethodSpec.methodBuilder("withViewImplementation")
                                    .addAnnotation(Override.class)
                                    .addModifiers(Modifier.PROTECTED)
                                    .addCode("this.view = controller.activity();\n" +
                                            "return this;\n")
                                    .returns(presenterBuilderClass)
                                    .build());


        for (int i = 0; i < components.size(); i++)
        {
            ClassName component = components.get(i);
            String canonicalName = toCanonicalName(component);
            String name = component.simpleName() + "Delegate";
            TypeSpec.Builder createComponentClassBuilder = TypeSpec.classBuilder(name)
                                                                   .addModifiers(Modifier.PROTECTED, Modifier.STATIC)
                                                                   .addSuperinterface(component);
            MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder();
            Component theComponent = instancesFromComponents.get(canonicalName);
            String componentParameterName = toParameterName(component);
            constructorBuilder.addParameter(component, componentParameterName);
            createComponentClassBuilder.addField(component, componentParameterName, Modifier.PRIVATE);
            List<ExecutableElement> componentMethods = theComponent.methods;
            StringBuilder constructorInitializer = new StringBuilder(String.format("this.%s = %s;\n", componentParameterName, componentParameterName));
            for (ExecutableElement componentMethod : componentMethods)
            {
                if (componentMethod.getReturnType().toString().equals(void.class.getName())) {
                    continue;
                }
                ClassName typeName = ClassName.bestGuess(componentMethod.getReturnType().toString());
                String parameterName = toParameterName(typeName);
                String value = getValueFromNamedAnnotation(componentMethod);
                parameterName += value;
                constructorBuilder.addParameter(typeName, parameterName);
                createComponentClassBuilder.addField(typeName, parameterName, Modifier.PRIVATE);
                constructorInitializer.append(String.format("this.%s = %s;\n", parameterName, parameterName));
            }
            constructorBuilder.addCode(constructorInitializer.toString());
            createComponentClassBuilder.addMethod(constructorBuilder.build());
            for (int j = 0; j < componentMethods.size(); j++)
            {
                ExecutableElement componentMethod = componentMethods.get(j);
                if (componentMethod.getReturnType().toString().equals(void.class.getName())) {
                    VariableElement variableElement = componentMethod.getParameters().get(0);
                    createComponentClassBuilder.addMethod(MethodSpec.methodBuilder(componentMethod.getSimpleName().toString())
                            .addModifiers(Modifier.PUBLIC)
                            .addAnnotation(Override.class)
                            .addParameter(ClassName.get(variableElement.asType()), variableElement.getSimpleName().toString())
                            .returns(void.class)
                            .addStatement(String.format("%s.%s(%s)", componentParameterName,
                                    componentMethod.getSimpleName().toString(),
                                    variableElement.getSimpleName().toString())
                            ).build());
                    continue;
                }
                ClassName n = ClassName.bestGuess(componentMethod.getReturnType().toString());
                List<String> fieldNames = theComponent.fieldNames;
                String fieldName = fieldNames.get(j);
                createComponentClassBuilder.addMethod(MethodSpec.methodBuilder(componentMethod.getSimpleName().toString())
                                                                .addModifiers(Modifier.PUBLIC)
                                                                .addAnnotation(Override.class)
                                                                .returns(ClassName.get(componentMethod.getReturnType()))
                                                                .beginControlFlow(String.format("if (%s == null)", fieldName))
                                                                .addCode(String.format("return %s.%s();\n", componentParameterName, componentMethod.getSimpleName().toString()))
                                                                .nextControlFlow("else")
                                                                .addCode(String.format("return %s;\n}", fieldName))
                                                                .build());
            }
            builder.addType(createComponentClassBuilder.build());
        }

        for (int i = 0; i < modules.size(); i++)
        {
            ClassName module = modules.get(i);
            String canonicalName = toCanonicalName(module);
            TypeElement typeElement = elementUtils.getTypeElement(canonicalName);
            List<? extends Element> enclosedElements = typeElement.getEnclosedElements();
            List<? extends VariableElement> parameters = new ArrayList<>();
            for (Element enclosedElement : enclosedElements)
            {
                if (enclosedElement.getKind() == ElementKind.CONSTRUCTOR)
                {
                    ExecutableElement executableElement = (ExecutableElement) enclosedElement;
                    parameters = executableElement.getParameters();
                }
            }
            String name = module.simpleName() + "Delegate";
            TypeSpec.Builder createModuleClassBuilder = TypeSpec.classBuilder(name)
                                                                .addModifiers(Modifier.PROTECTED, Modifier.STATIC)
                                                                .superclass(module);
            MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder();
            Module theModule = instancesFromModules.get(canonicalName);
            ArrayList<ModuleMethod> moduleMethods = theModule.getModuleMethods();
            String superCall = "super(";
            for (int j = 0; j < parameters.size(); j++)
            {
                superCall += "null";
                if (j < parameters.size() - 1)
                {
                    superCall += ", ";
                }
            }
            superCall += ");\n";
            createModuleClassBuilder.addField(module, "module", Modifier.PRIVATE);
            constructorBuilder.addCode(superCall);
            constructorBuilder.addParameter(module, "module");
            constructorBuilder.addStatement("this.module = module");
            for (int j = 0; j < moduleMethods.size(); j++)
            {
                ModuleMethod moduleMethod = moduleMethods.get(j);
                if (moduleMethod.isConstructorParameter())
                    continue;

                ClassName n = moduleMethod.getClassType();
                List<String> fieldNames = theModule.getFieldNames();
                String fieldName = fieldNames.get(j);
                constructorBuilder.addParameter(n, fieldName);
                createModuleClassBuilder.addField(n, fieldName, Modifier.PRIVATE);
                constructorBuilder.addCode(String.format("this.%s = %s;\n", fieldName, fieldName));
                ExecutableElement executableElement = moduleMethod.getExecutableElement();
                createModuleClassBuilder.addMethod(MethodSpec.methodBuilder(executableElement.getSimpleName().toString())
                                                             .addModifiers(Modifier.PUBLIC)
                                                             .addAnnotation(Override.class)
                                                             .returns(ClassName.get(executableElement.getReturnType()))
                                                             .beginControlFlow(String.format("if (%s == null)", fieldName))
                                                             .addCode(String.format("return module.%s();\n", executableElement.getSimpleName().toString()))
                                                             .nextControlFlow("else")
                                                             .addCode(String.format("return %s;\n}", fieldName))
                                                             .build());
            }
            createModuleClassBuilder.addMethod(constructorBuilder.build());
            builder.addType(createModuleClassBuilder.build());
        }

        HashMap<String, String> m = findModuleParamMethods(gang.getElementActivityClass());

        for (int position = 0; position < modules.size(); position++)
        {
            ClassName module = modules.get(position);
            String canonicalName = toCanonicalName(module);
            Module theModule = instancesFromModules.get(canonicalName);
            List<ModuleMethod> containingInstanceClasses = theModule.getModuleMethods();
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("create" + module.simpleName())
                                                         .addModifiers(Modifier.PROTECTED);
            ExecutableElement method = providingMethods.get(module.toString());
            String parameterList = "provider." + method.getSimpleName().toString();
            List<? extends VariableElement> parameters = method.getParameters();
            parameterList += "(";
            if (!parameters.isEmpty()){
                for (int i = 0; i < parameters.size(); i++)
                {
                    VariableElement parameter = parameters.get(i);
                    String fieldName = moduleClassNamesToFieldNames.get(parameter.asType().toString());
                    parameterList += fieldName;
                    if (i < parameters.size() - 1)
                        parameterList += ", ";
                }
            }
            parameterList += ")";
            List<String> fieldNames = theModule.getFieldNames();
            if (!fieldNames.isEmpty())
                parameterList += ", ";
            for (int i = 0; i < fieldNames.size(); i++)
            {
                String fieldName = fieldNames.get(i);
                String methodName = m.get(containingInstanceClasses.get(i).getClassType().toString());
                if (methodName != null)
                    methodBuilder.addCode(String.format("this.%s = controller.activity().%s();\n", fieldName, methodName));
                parameterList += fieldName;
                if (i < fieldNames.size() - 1)
                {
                    parameterList += ", ";
                }
            }
            methodBuilder.returns(module);
            methodBuilder.addCode(String.format("this.%s = new %s(%s);\n", toParameterName(module), module.simpleName() + "Delegate", parameterList));
            methodBuilder.addStatement(String.format("return %s", toParameterName(module)));
            builder.addMethod(methodBuilder.build());
        }

        ParameterizedTypeName robolectricActivityControllerClass;
        if (isActivity())
        {
            robolectricActivityControllerClass = ParameterizedTypeName.get(ClassName.get("org.robolectric.util", "ActivityController"), gang.getActivityClass());
        } else if (isFragment())
        {
            robolectricActivityControllerClass = ParameterizedTypeName.get(ClassName.get("org.robolectric.shadows.support.v4", "SupportFragmentController"), gang.getActivityClass());
        } else
        {
            throw new IllegalStateException(String.format("class \"%s\" is not supported as view type!", gang.getElementActivityClass().asType().toString()));
        }

        if (isFragment())
        {
            builder.addField(int.class, "container", Modifier.PRIVATE);
            builder.addMethod(MethodSpec.methodBuilder("in")
                                        .addModifiers(Modifier.PUBLIC)
                                        .addParameter(int.class, "container")
                                        .addCode("this.container = container;\n")
                                        .addCode("return this;\n")
                                        .returns(presenterBuilderClass)
                                        .build());
        }

        TypeSpec.Builder bindingResultBuilder = TypeSpec.classBuilder("BindingResult")
                                                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                                                        .addField(robolectricActivityControllerClass, "controller", Modifier.PRIVATE)
                                                        .addField(gang.getPresenterClass(), "presenter", Modifier.PRIVATE)
                                                        .addField(gang.getViewClass(), "view", Modifier.PRIVATE)
                                                        .addMethod(MethodSpec.constructorBuilder()
                                                                             .addParameter(robolectricActivityControllerClass, "controller")
                                                                             .addParameter(gang.getPresenterClass(), "presenter")
                                                                             .addParameter(gang.getViewClass(), "view")
                                                                             .addCode("this.controller = controller;\n")
                                                                             .addCode("this.presenter = presenter;\n")
                                                                             .addCode("this.view = view;\n")
                                                                             .build())
                                                        .addMethod(MethodSpec.methodBuilder("controller")
                                                                             .addModifiers(Modifier.PUBLIC)
                                                                             .returns(robolectricActivityControllerClass)
                                                                             .addCode("return controller;\n")
                                                                             .build())
                                                        .addMethod(MethodSpec.methodBuilder("presenter")
                                                                             .addModifiers(Modifier.PUBLIC)
                                                                             .returns(gang.getPresenterClass())
                                                                             .addCode("return presenter;\n")
                                                                             .build())
                                                        .addMethod(MethodSpec.methodBuilder("view")
                                                                             .addModifiers(Modifier.PUBLIC)
                                                                             .returns(gang.getViewClass())
                                                                             .addCode("return view;\n")
                                                                             .build());


        builder.addType(bindingResultBuilder.build());

        String presenterFieldName = Utils.findPresenterFieldInViewImplementationClass(gang.getElementActivityClass());

        ClassName mockitoClassName = ClassName.get("org.mockito", "Mockito");

        ClassName daggerComponentClass = ClassName.get(getPackageName(gang.getElementPresenterClass()), "DaggerComponent" + gang.getPresenterClass().simpleName());
        ClassName modulePresenterDependenciesClass = ClassName.get(getPackageName(gang.getElementPresenterClass()), "Testable" + gang.getPresenterClass().simpleName() + "Dependencies");

        ParameterizedTypeName presenterComponent = ParameterizedTypeName.get(ClassName.get("com.mvp", "PresenterComponent"), gang.getViewClass(), gang.getPresenterClass());
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("build");

        methodBuilder.addStatement("controller.with(provider.mvpEventBus())");

        methodBuilder.addCode("this.controller.withSavedInstanceState(bundle);\n");

        if (isFragment())
        {
            methodBuilder.addCode("this.controller.in(container);\n");
        } else
        {
            methodBuilder.beginControlFlow("if (intent != null)");
            methodBuilder.addCode("this.controller.withIntent(intent);\n");
            methodBuilder.endControlFlow();
        }

        methodBuilder.addCode("this.controller.initialize();\n");

        for (ClassName module : modules)
        {
            buildInitializationCode(providingMethods, methodBuilder, module, moduleClassNamesToFieldNames, false);
        }

        for (ClassName component : components)
        {
            buildInitializationCode(providingMethods, methodBuilder, component, componentClassNamesToFieldNames, true);
        }

        methodBuilder
                .addModifiers(Modifier.PROTECTED)
                .addCode(String.format("$T %s;\n", presenterFieldName), gang.getPresenterClass())
                .beginControlFlow("if (mockPresenter != null)")
                .addCode("            " + presenterFieldName + " = controller.with(new $T() {\n" +
                        "                @Override\n" +
                        "                public $T newInstance() {\n" +
                        "                    $T.when(mockPresenter.getView()).thenReturn(view);\n" +
                        "                    return mockPresenter;\n" +
                        "                }\n" +
                        "\n" +
                        "                @Override\n" +
                        "                public $T view() {\n" +
                        "                    return view;\n" +
                        "                }\n" +
                        "            }).build();", presenterComponent, gang.getPresenterClass(), mockitoClassName, gang.getViewClass())
                .nextControlFlow("else");

        if (isActivity())
        {
            methodBuilder.addCode(presenterFieldName + " = controller.with(\n");
            methodBuilder.addCode("$T.builder()\n", daggerComponentClass);
            methodBuilder.addCode(".module" + gang.getPresenterClass().simpleName() + "Dependencies(new $T(controller.activity(), view))\n", modulePresenterDependenciesClass);
        } else if (isFragment())
        {
            methodBuilder.addCode(presenterFieldName + " = controller.with(\n");
            methodBuilder.addCode("$T.builder()\n", daggerComponentClass);
            methodBuilder.addCode(".module" + gang.getPresenterClass().simpleName() + "Dependencies(new $T(($T) controller.activityController().get(), view))\n", modulePresenterDependenciesClass, ClassName.bestGuess("android.support.v7.app.AppCompatActivity"));
        }

        for (ClassName module : modules)
        {
            String methodName = Character.toLowerCase(module.simpleName().charAt(0)) + module.simpleName().substring(1);
            methodBuilder.addCode("." + methodName + "(" + "create" + module.simpleName() + "())\n");
        }

        for (ClassName component : components)
        {
            StringBuilder paramsBuilder = new StringBuilder();
            List<ExecutableElement> methods = instancesFromComponents.get(component.toString()).methods;
            // real component is first parameter
            paramsBuilder.append(toParameterName(component));
            // fill up with remaining dependencies
            for (int i = 0; i < methods.size(); i++)
            {
                ExecutableElement executableElement = methods.get(i);
                TypeMirror returnType = executableElement.getReturnType();
                if (returnType.toString().equals(void.class.getName())) {
                    continue;
                }
                paramsBuilder.append(", ");
                ClassName paramClass = ClassName.bestGuess(returnType.toString());
                paramsBuilder.append(toParameterName(paramClass) + getValueFromNamedAnnotation(executableElement));
            }
            String delegateName = component.simpleName().toString() + "Delegate";
            String methodName = Character.toLowerCase(component.simpleName().charAt(0)) + component.simpleName().substring(1);
            methodBuilder.addCode(String.format("." + methodName + "(new %s(" + paramsBuilder.toString() + "))\n", delegateName));
        }

        ClassName moduleEventBusClass = ClassName.get("com.mvp", "ModuleEventBus");
        methodBuilder.addCode(".moduleEventBus(provider.mvpEventBus())\n");

        methodBuilder.addCode(".build()\n).build();\n")
                     .endControlFlow()
                     .addCode("return new BindingResult(this.controller.controller(), " + presenterFieldName + ", view);\n")
                     .returns(bindingResultClassName);

        builder.addMethod(methodBuilder.build());

        return builder;
    }

    private String getValueFromNamedAnnotation(ExecutableElement executableElement)
    {
        AnnotationValue annotationValue = Utils.getAnnotationValue(executableElement, "javax.inject.Named", "value");
        return annotationValue == null || annotationValue.getValue() == null ? "" : annotationValue.getValue().toString();
    }

    private void buildInitializationCode(HashMap<String, ExecutableElement> providingMethods, MethodSpec.Builder methodBuilder, ClassName className, HashMap<String, String> componentOrModuleClassToFieldName, boolean isComponent)
    {
        if (!isComponent) return;

        ExecutableElement executableElement = providingMethods.get(className.toString());
        if (executableElement == null)
        {
            throw new IllegalStateException(String.format("'%s' is not provided by application class!", className.toString()));
        }
        methodBuilder.beginControlFlow(String.format("if (this.%s == null)", toParameterName(className)));
        String methodName = executableElement.getSimpleName().toString();

        StringBuilder sb = new StringBuilder();
        List<? extends VariableElement> parameters = executableElement.getParameters();
        for (int i = 0; i < parameters.size(); i++)
        {
            VariableElement variableElement = parameters.get(i);
            TypeMirror typeMirror = variableElement.asType();
            String fieldName;
            if (isComponent && typeMirror.toString().equals("android.support.v7.app.AppCompatActivity")) {
                String controller = isActivity() ? "controller" : "activityController";
                fieldName = String.format("(android.support.v7.app.AppCompatActivity) controller.%s().get()", controller);
                sb.append(fieldName);
                if (i < parameters.size() - 1) sb.append(", ");
            }else if (isComponent){
                String classNameString = typeMirror.toString();
                ExecutableElement e = providingMethods.get(classNameString);
                fieldName = e != null ? e.getSimpleName().toString() : variableElement.getSimpleName().toString();
                sb.append(String.format("this.provider.%s()", fieldName));
                if (i < parameters.size() - 1) sb.append(", ");
            }else{
                fieldName = providingMethods.get(executableElement.getReturnType().toString()).getSimpleName().toString();
                sb.append(String.format("this.provider.%s()", fieldName));
                if (i < parameters.size() - 1) sb.append(", ");
            }
        }
        methodBuilder.addStatement(String.format("this.%s = this.provider.%s(%s)", toParameterName(className), methodName, sb.toString()));
        methodBuilder.endControlFlow();
    }

    private HashMap<String, String> findModuleParamMethods(Element elementActivityClass)
    {
        HashMap<String, String> m = new HashMap<>();
        TypeElement typeElement = (TypeElement) elementActivityClass;
        for (Element element : typeElement.getEnclosedElements())
        {
            if (element.getKind() == ElementKind.METHOD && element.getAnnotation(ModuleParam.class) != null)
            {
                ExecutableElement executableElement = (ExecutableElement) element;
                m.put(executableElement.getReturnType().toString(), executableElement.getSimpleName().toString());
            }
        }
        return m;
    }

    private boolean isActivity()
    {
        return Utils.isActivity(typeUtil, elementUtils, gang.getElementActivityClass().asType());
    }

    private boolean isFragment()
    {
        return Utils.isFragment(typeUtil, elementUtils, gang.getElementActivityClass().asType());
    }

    private String getPackageName(Element viewElement)
    {
        return elementUtils.getPackageOf(viewElement).getQualifiedName().toString();
    }

    private String toCanonicalName(ClassName module)
    {
        return module.packageName() + "." + module.simpleName();
    }

    private HashMap<String, Component> extractProvidedClassesFromComponents(List<ClassName> components)
    {
        HashMap<String, Component> componentHashMap = new HashMap<>();
        for (ClassName c : components)
        {
            TypeElement typeElement = elementUtils.getTypeElement(c.toString());

            if (typeElement.getKind() == ElementKind.INTERFACE)
            {
                Component component = new Component(typeElement, typeUtil);
                component.parse();
                componentHashMap.put(c.toString(), component);
            }
        }
        return componentHashMap;
    }

    private HashMap<String, Module> extractProvidedClassesFromModules(List<ClassName> modules)
    {
        HashMap<String, Module> classNames = new HashMap<>();
        for (ClassName module : modules)
        {
            String moduleCanonicalClassName = toCanonicalName(module);
            TypeElement typeElement = elementUtils.getTypeElement(moduleCanonicalClassName);
            List<? extends Element> enclosedElements = typeElement.getEnclosedElements();
            for (Element enclosedElement : enclosedElements)
            {
                if (enclosedElement.getKind() == ElementKind.METHOD)
                {
                    ExecutableElement executableElement = (ExecutableElement) enclosedElement;
                    if (hasProvidesAnnotation(executableElement))
                    {
                        TypeMirror returnType = executableElement.getReturnType();
                        if (!classNames.containsKey(moduleCanonicalClassName))
                        {
                            classNames.put(moduleCanonicalClassName, new Module(new ArrayList<ModuleMethod>()));
                        }
                        boolean isConstructorParam = isConstructorParameter(typeElement, returnType);
                        ClassName className = ClassName.bestGuess(returnType.toString());
                        classNames.get(moduleCanonicalClassName).getModuleMethods().add(new ModuleMethod(className, executableElement, isConstructorParam));
                    }
                }
            }
        }
        return classNames;
    }

    private boolean isConstructorParameter(TypeElement typeElement, TypeMirror returnType)
    {
        TypeElement activityElement = (TypeElement) gang.getElementActivityClass();
        for (Element element : activityElement.getEnclosedElements())
        {
            if (element.getKind() == ElementKind.METHOD){
                if (element.getAnnotation(ModuleParam.class) != null){
                    ExecutableElement executableElement = (ExecutableElement) element;
                    if (executableElement.getReturnType().toString().equals(returnType.toString()))
                        return false;
                }
            }
        }
        List<? extends Element> enclosedElements = typeElement.getEnclosedElements();
        for (Element enclosedElement : enclosedElements)
        {
            if (enclosedElement.getKind() == ElementKind.CONSTRUCTOR)
            {
                ExecutableElement executableElement = (ExecutableElement) enclosedElement;
                List<? extends VariableElement> parameters = executableElement.getParameters();
                for (VariableElement parameter : parameters)
                {
                    if (parameter.asType().toString().equals(returnType.toString()))
                        return true;
                }
            }
        }
        return false;
    }

    private List<ClassName> findModules(Element presenterElement)
    {
        List<ClassName> classNames = new ArrayList<>();
        AnnotationValue value = getAnnotationValue(presenterElement, "needsModules");
        List<Object> moduleClasses = value != null ? (List<Object>) value.getValue() : new ArrayList<>();
        for (Object moduleClass : moduleClasses)
        {
            String m = moduleClass.toString().replace(".class", "");
            classNames.add(ClassName.bestGuess(m));
        }
        return classNames;
    }

    private List<ClassName> findComponents(Element presenterElement)
    {
        List<ClassName> classNames = new ArrayList<>();
        AnnotationValue value = getAnnotationValue(presenterElement, "needsComponents");
        List<Object> componentClasses = value != null ? (List<Object>) value.getValue() : new ArrayList<>();
        for (Object componentClass : componentClasses)
        {
            String c = componentClass.toString().replace(".class", "");
            ClassName className = ClassName.bestGuess(c);
            classNames.add(className);
        }
        return classNames;
    }

    private static class ModuleMethod
    {
        private final ClassName classType;
        private final ExecutableElement executableElement;
        private boolean constructorParameter;

        ModuleMethod(ClassName classType, ExecutableElement executableElement, boolean constructorParameter)
        {
            this.classType = classType;
            this.executableElement = executableElement;
            this.constructorParameter = constructorParameter;
        }

        public boolean isConstructorParameter()
        {
            return constructorParameter;
        }

        public ClassName getClassType()
        {
            return classType;
        }

        public ExecutableElement getExecutableElement()
        {
            return executableElement;
        }
    }

    private static class Module
    {

        List<String> fieldName;
        private ArrayList<ModuleMethod> moduleMethods;

        Module(ArrayList<ModuleMethod> moduleMethods)
        {
            this.moduleMethods = moduleMethods;
        }

        public List<String> getFieldNames()
        {
            return fieldName;
        }

        public void setFieldNames(List<String> fieldName)
        {
            this.fieldName = fieldName;
        }

        public ArrayList<ModuleMethod> getModuleMethods()
        {
            return moduleMethods;
        }
    }

    private static class Component
    {

        public List<ExecutableElement> methods = new ArrayList<>();
        private HashMap<String, String> fieldTypeToMethodName = new HashMap<>();
        private TypeElement element;
        private final Types typeUtils;
        private List<String> fieldNames;

        public Component(TypeElement element, Types typeUtils)
        {
            this.element = element;
            this.typeUtils = typeUtils;
        }

        public void parse()
        {
            List<? extends TypeMirror> typeMirrors = typeUtils.directSupertypes(this.element.asType());
            for (TypeMirror typeMirror : typeMirrors) {
                Element element = typeUtils.asElement(typeMirror);
                this.internalParse(element);
            }

            this.internalParse(this.element);
        }

        private void internalParse(Element element) {
            if (element.getKind() != ElementKind.INTERFACE) {
                return;
            }
            if (element.asType().toString().equals(Object.class.getName())) {
                return;
            }
            List<? extends Element> enclosedElements = element.getEnclosedElements();
            for (Element enclosedElement : enclosedElements)
            {
                if (enclosedElement.getKind() == ElementKind.METHOD)
                {
                    ExecutableElement executableElement = (ExecutableElement) enclosedElement;
                    String methodName = executableElement.getSimpleName().toString();
                    fieldTypeToMethodName.put(executableElement.getReturnType().toString(), methodName);
                    methods.add(executableElement);
                }
            }
        }

        public void setFieldNames(List<String> fieldNames)
        {
            this.fieldNames = fieldNames;
        }
    }

}
