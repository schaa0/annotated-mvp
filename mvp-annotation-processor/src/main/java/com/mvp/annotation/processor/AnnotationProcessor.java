package com.mvp.annotation.processor;

import com.mvp.annotation.MvpScope;
import com.mvp.annotation.Provider;
import com.mvp.annotation.Event;
import com.mvp.annotation.OnEventListener;
import com.mvp.annotation.ProvidesComponent;
import com.mvp.annotation.ProvidesModule;
import com.mvp.annotation.UIView;
import com.mvp.annotation.ViewEvent;
import com.mvp.annotation.Presenter;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import com.squareup.javapoet.WildcardTypeName;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import javax.annotation.Generated;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.inject.Inject;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import static com.mvp.annotation.processor.AnnotationUtils.getAnnotationValue;

public class AnnotationProcessor extends AbstractProcessor {

    static final ParameterizedTypeName IFACTORY_CLASS_NAME = ParameterizedTypeName.get(ClassName.get("com.mvp", "IFactory"), WildcardTypeName.subtypeOf(TypeName.OBJECT));
    public static final String CLASSNAME_DEPENDENCY_PROVIDER = "DependencyProvider";
    public static final String MEMBER_NEEDS_MODULES = "needsModules";
    public static final String MEMBER_NEEDS_COMPONENTS = "needsComponents";
    private static final String MEMBER_VIEW_IMPLEMENTATION = "viewImplementation";
    private static ClassName APP_COMPAT_ACTIVITY;

    private HashMap<TypeMirror, List<Interceptor>> interceptors = new HashMap<>();
    private Types typeUtils;
    private Elements elementUtils;

    private HashMap<String, List<String>> allGeneratedEventListenerClasses = new HashMap<>();
    private HashMap<String, TypeMirror> allViewTypes = new HashMap<>();
    private List<TypeComponentPresenter> allComponentPresenters = new ArrayList<>();
    private TypeMirror applicationClassType;
    private Set<? extends Element> componentProviders;
    private Set<? extends Element> uiViewClasses;

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        this.processingEnv = env;
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {

        APP_COMPAT_ACTIVITY = ClassName.get("android.support.v7.app", "AppCompatActivity");

        if (componentProviders == null)
            componentProviders = env.getElementsAnnotatedWith(Provider.class);

        if (uiViewClasses == null)
            uiViewClasses = env.getElementsAnnotatedWith(UIView.class);

        Element componentProvider = componentProviders.iterator().next();

        applicationClassType = componentProvider.asType();

        DeclaredType presenterType = typeUtils.getDeclaredType(elementUtils.getTypeElement("com.mvp.MvpPresenter"));

        for (Element element : env.getElementsAnnotatedWith(Presenter.class)) {

            if (element.getKind() == ElementKind.CLASS) {

                TypeMirror classType = element.asType();

                DeclaredType declaredClassType = (DeclaredType) classType;
                List<? extends TypeMirror> typeArguments = declaredClassType.getTypeArguments();

                if (typeArguments.size() > 0) {
                    /* anonymous instances of generic moduleClasses are not supported, so no moduleClasses must be generated for this type */
                    continue;
                }
                if (declaredClassType.asElement().getModifiers().contains(Modifier.ABSTRACT)) {
                    /* anonymous abstract class instances are not supported, so no moduleClasses must be generated for this type */
                    continue;
                }

                TypeMirror viewType = findViewTypeOfPresenter(presenterType, classType);
                List<TypeMirror> basePresenters = findBasePresenters(classType);

                if (viewType == null){
                    throw new IllegalStateException(String.format("class: %s is annotated with @Presenter, but does not derive from: %s", classType, presenterType));
                }

                String presenterPackage = extractPackage(classType);

                String viewPackage = extractPackage(viewType);
                Presenter presenterAnnotation = element.getAnnotation(Presenter.class);

                AnnotationMemberModuleClasses annotationMemberModuleClasses = new AnnotationMemberModuleClasses(presenterPackage).parse(element);
                AnnotationMemberComponentClasses annotationMemberComponentClasses = new AnnotationMemberComponentClasses(presenterPackage).parse(element);

                AnnotationValue annotationValue = getAnnotationValue(element, MEMBER_VIEW_IMPLEMENTATION);
                Object object = annotationValue.getValue();

                ClassName viewImplementationClassName = ClassName.bestGuess(object.toString().replace(".class", ""));

                String simpleComponentPresenterClassName = "Component" + element.getSimpleName().toString();
                TypeSpec.Builder builder = TypeSpec.interfaceBuilder(simpleComponentPresenterClassName);

                String moduleFormat = annotationMemberModuleClasses.getModuleFormat();
                ClassName[] moduleClasses = annotationMemberModuleClasses.getClasses();

                String componentFormat = annotationMemberComponentClasses.getComponentFormat();
                ClassName[] componentClasses = annotationMemberComponentClasses.getClasses();

                ClassName annotationClass = ClassName.get("dagger", "Component");
                ClassName moduleAnnotation = ClassName.get("dagger", "Module");

                String componentDelegateBinderClassName = viewImplementationClassName.packageName() + ".Component" + viewImplementationClassName.simpleName() + "DelegateBinder";
                String delegateBinderClassName = viewImplementationClassName.packageName() + "." + viewImplementationClassName.simpleName() + "DelegateBinder";
                String delegateClassName = viewImplementationClassName.packageName() + "." + viewImplementationClassName.simpleName() + "Delegate";
                ClassName modulePresenterClass = ClassName.bestGuess("Module" + element.getSimpleName().toString());

                //builder.addAnnotation(MvpScope.class);
                builder.addAnnotation(AnnotationSpec.builder(annotationClass)
                        .addMember("modules", CodeBlock.of(moduleFormat, moduleClasses))
                        .build());

                builder.addMethod(MethodSpec.methodBuilder("view")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .returns(ClassName.get(viewType))
                        .build());

                builder.addMethod(MethodSpec.methodBuilder("context")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .returns(ClassName.bestGuess("android.content.Context"))
                        .build());

                builder.addMethod(MethodSpec.methodBuilder("loaderManager")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .returns(ClassName.bestGuess("android.support.v4.app.LoaderManager"))
                        .build());

                ParameterizedTypeName componentPresenterType = ParameterizedTypeName.get(ClassName.get("com.mvp", "ComponentPresenter"), ClassName.bestGuess(delegateBinderClassName), TypeName.get(viewType), TypeName.get(classType), ClassName.bestGuess(componentDelegateBinderClassName), modulePresenterClass);

                /*builder.addMethod(MethodSpec.methodBuilder("presenter")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .returns(componentPresenterType)
                        .build());*/

                builder.addModifiers(Modifier.PUBLIC);

                ParameterizedTypeName mvpPresenterModule = ParameterizedTypeName.get(ClassName.get("com.mvp", "MvpPresenterModule"), ClassName.get(viewType), ClassName.get(classType));
                ParameterizedTypeName presenterComponent = ParameterizedTypeName.get(ClassName.get("com.mvp", "PresenterComponent"), ClassName.get(viewType), ClassName.get(classType));

                builder.addSuperinterface(componentPresenterType);

                writeClass(builder.build(), presenterPackage);

                String componentPresenterClassName = presenterPackage + "." + simpleComponentPresenterClassName;

                allComponentPresenters.add(new TypeComponentPresenter(classType, componentPresenterClassName, moduleClasses, componentClasses));

                ParameterizedTypeName mvpModule = ParameterizedTypeName.get(ClassName.get("com.mvp", "MvpModule"), ClassName.get(viewType));

                ClassName activityType = APP_COMPAT_ACTIVITY;

                builder = TypeSpec.classBuilder("Module" + element.getSimpleName().toString() + "Dependencies");
                builder.superclass(mvpModule);
                builder.addModifiers(Modifier.PUBLIC);
                builder.addAnnotation(AnnotationSpec.builder(moduleAnnotation).build());
                builder.addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC)
                        .addParameter(activityType, "activity")
                        .addParameter(ClassName.get(viewType), "view")
                        .addCode("super(activity, view);")
                        .build());

                writeClass(builder.build(), presenterPackage);

                builder = TypeSpec.classBuilder("Module" + element.getSimpleName().toString());
                builder.superclass(mvpPresenterModule);
                builder.addModifiers(Modifier.PUBLIC);
                builder.addAnnotation(AnnotationSpec.builder(moduleAnnotation).build());
                builder.addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC)
                        .addParameter(activityType, "activity")
                        .addParameter(ClassName.get(viewType), "view")
                        .addParameter(presenterComponent, "componentPresenter")
                        .addCode("super(activity, view, componentPresenter);")
                        .build());

                /*builder.addMethod(MethodSpec.methodBuilder("provideBinder")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(ClassName.bestGuess(delegateBinderClassName))
                        .addAnnotation(ClassName.get("dagger", "Provides"))
                        .addParameter(ClassName.bestGuess(delegateClassName), "delegate")
                        .addCode("return new $T(delegate);",  ClassName.bestGuess(delegateBinderClassName))
                        .build());

                //IMvpEventBus eventBus, PresenterComponent<IView, ExamplePresenter> presenterComponent, IView view, Context context, LoaderManager loaderManager
                builder.addMethod(MethodSpec.methodBuilder("provideDelegate")
                        .addAnnotation(ClassName.get("dagger", "Provides"))
                        .addParameter(ClassName.get("com.mvp", "IMvpEventBus"), "eventBus")
                        .addParameter(presenterComponent, "presenterComponent")
                        .addParameter(ClassName.get(viewType), "view")
                        .addParameter(ClassName.bestGuess("android.content.Context"), "context")
                        .addParameter(ClassName.bestGuess("android.support.v4.app.LoaderManager"), "loaderManager")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(ClassName.bestGuess(delegateClassName))
                        .addCode("return new $T(eventBus, presenterComponent, view, context, loaderManager);",  ClassName.bestGuess(delegateClassName))
                        .build());
                */

                writeClass(builder.build(), presenterPackage);

                processPresenter(element, classType, basePresenters, viewType);

                allViewTypes.put(classType.toString(), viewType);

            }
        }

        writeFactoryInterface();
        writeMethodsClass();
        processUiViewClasses(env);

        if (env.processingOver()){
            generateDependencyProvider(env);
        }

        return true;
    }

    private HashMap<String, ExecutableElement> findProvidingMethods(Set<? extends Element> componentProviders){
        HashMap<String, ExecutableElement> providingMethods = new HashMap<>();
        for (Element element : componentProviders){
            if (element.getKind() == ElementKind.CLASS){
                TypeElement typeElement = (TypeElement) element;
                for (Map.Entry<String, ExecutableElement> e : findProvidingMethodsInternal(typeElement, providingMethods).entrySet()) {
                    if (!providingMethods.containsKey(e.getKey()))
                        providingMethods.put(e.getKey(), e.getValue());
                }
                typeElement = (TypeElement) typeUtils.asElement(typeElement.getSuperclass());
                while (!typeElement.toString().equals(Object.class.getName())) {
                    for (Map.Entry<String, ExecutableElement> e : findProvidingMethodsInternal(typeElement, providingMethods).entrySet()) {
                        if (!providingMethods.containsKey(e.getKey()))
                            providingMethods.put(e.getKey(), e.getValue());
                    }
                    typeElement = (TypeElement) typeUtils.asElement(typeElement.getSuperclass());
                }
            }
        }
        return providingMethods;
    }

    private HashMap<String, ExecutableElement> findProvidingMethodsInternal(TypeElement element, HashMap<String, ExecutableElement> providingMethods) {
        List<? extends Element> enclosedElements = element.getEnclosedElements();
        for (Element enclosedElement : enclosedElements) {
            if (enclosedElement.getKind() == ElementKind.METHOD){
                ProvidesModule providesModule = enclosedElement.getAnnotation(ProvidesModule.class);
                if (providesModule != null){
                    ExecutableElement executableElement = (ExecutableElement) enclosedElement;
                    providingMethods.put(executableElement.getReturnType().toString(), executableElement);
                }
            }
        }
        return providingMethods;
    }

    private void generateDependencyProvider(RoundEnvironment env) {

        HashMap<String, ExecutableElement> providingMethods = findProvidingMethods(this.componentProviders);

        TypeName applicationClassTypeName = ClassName.get(applicationClassType);
        TypeSpec.Builder builder = TypeSpec.classBuilder(CLASSNAME_DEPENDENCY_PROVIDER)
                .addModifiers(Modifier.PUBLIC)
                .addField(applicationClassTypeName, "application", Modifier.PRIVATE)
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(applicationClassTypeName, "application")
                        .addCode("this.application = application;")
                        .build());

        for (TypeComponentPresenter typeComponentPresenter : allComponentPresenters) {
            TypeMirror mirroredPresenterClass = typeComponentPresenter.getPresenterClass();
            ClassName presenterClass = ClassName.get(extractPackage(mirroredPresenterClass), typeUtils.asElement(mirroredPresenterClass).getSimpleName().toString());
            String presenterPackage = extractPackage(mirroredPresenterClass);
            String providerMethodName = Character.toLowerCase(presenterClass.simpleName().toString().charAt(0)) + presenterClass.simpleName().substring(1);

            TypeMirror viewTypeClass = allViewTypes.get(mirroredPresenterClass.toString());
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(providerMethodName)
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(ProvidesComponent.class)
                    .returns(ClassName.bestGuess(typeComponentPresenter.getComponentPresenterClassName()))
                    .addParameter(APP_COMPAT_ACTIVITY, "activity")
                    .addParameter(ClassName.get(viewTypeClass), "view");
            methodBuilder.addCode("return $T.builder()", ClassName.bestGuess(presenterPackage + ".DaggerComponent" + presenterClass.simpleName()));
            ClassName[] moduleClasses = typeComponentPresenter.getModuleClasses();
            for (int position = 0; position < moduleClasses.length - 1; position++) {
                ClassName moduleClass = moduleClasses[position];
                String simpleName = moduleClass.simpleName();
                String methodName = Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
                String o = moduleClass.packageName() + "." + moduleClass.simpleName();
                ExecutableElement executableElement = providingMethods.get(o);
                String moduleCode = String.format(".%s(application.%s())", methodName, executableElement.getSimpleName());
                methodBuilder.addCode(moduleCode);
            }

            ClassName[] componentClasses = typeComponentPresenter.getComponentClasses();
            for (ClassName componentClass : componentClasses) {
                String simpleName = componentClass.simpleName();
                String methodName = Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
                String componentCodeFormat = String.format(".%s(new $T)", methodName);
                methodBuilder.addCode(componentCodeFormat, componentClass);
            }

            ClassName modulePresenterDependencies = moduleClasses[moduleClasses.length-1];
            String methodName = Character.toLowerCase(modulePresenterDependencies.simpleName().charAt(0)) + modulePresenterDependencies.simpleName().substring(1);
            methodBuilder.addCode(String.format(".%s(new $T(activity, view))", methodName), modulePresenterDependencies);
            methodBuilder.addCode(".build();");
            builder.addMethod(methodBuilder.build());

        }

        writeClass(builder.build(), "com.mvp");
    }

    private String extractPackage(TypeMirror classType) {
        return classType.toString().replaceAll("." + convertDataClassToString(classType), "");
    }

    private void buildDelegateAndBinder(TypeMirror presenterClass, TypeMirror viewType, TypeMirror activityType, ParameterizedTypeName presenterComponent) {

        // Build Delegate

        String activityName = typeUtils.asElement(activityType).getSimpleName().toString();
        String delegateClassName = activityName + "Delegate";

        ParameterizedTypeName mvpActivityDelegate = ParameterizedTypeName.get(ClassName.bestGuess("com.mvp.MvpActivityDelegate"), ClassName.get(viewType), ClassName.get(presenterClass));

        TypeSpec delegateClass = TypeSpec.classBuilder(delegateClassName)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(Inject.class)
                        .addParameter(ClassName.get("com.mvp", "IMvpEventBus"), "eventBus")
                        .addParameter(presenterComponent, "presenterComponent")
                        .addParameter(ClassName.get(viewType), "view")
                        .addParameter(ClassName.bestGuess("android.content.Context"), "context")
                        .addParameter(ClassName.bestGuess("android.support.v4.app.LoaderManager"), "loaderManager")
                        .addCode("super(eventBus, presenterComponent, view, context, loaderManager);")
                        .build())
                .superclass(mvpActivityDelegate)
                .build();

        String activityPackage = extractPackage(activityType);
        writeClass(delegateClass, activityPackage);

        String binderClassName = delegateClassName + "Binder";

        ParameterizedTypeName delegateBinderInterface = ParameterizedTypeName.get(ClassName.get("com.mvp", "DelegateBinder"), ClassName.get(viewType), ClassName.get(presenterClass));

        String activityPackageName = activityPackage;
        String presenterPackageName = extractPackage(presenterClass);
        String initCode = "this.delegate = delegate;";
        String castCode = "$T binder = ($T) presenterComponent;\n";
        String binderConstructorCode = "binder.component(new $T(presenterComponent)).inject(this);";

        String constructCode = String.format("$T.builder()\n" +
                "            .%s(new $T(activity, presenterComponent.view(), presenterComponent))\n" +
                "            .moduleEventBus(moduleEventBus)\n" +
                "            .build()\n" +
                "            .inject(this);", "module" + typeUtils.asElement(presenterClass).getSimpleName().toString());

        ClassName modulePresenterClass = ClassName.get(presenterPackageName, "Module" + typeUtils.asElement(presenterClass).getSimpleName().toString());
        ParameterizedTypeName binderType = ParameterizedTypeName.get(ClassName.get("com.mvp", "Binder"), ClassName.get(activityPackageName, "Component" + activityName + "DelegateBinder"), modulePresenterClass);

        TypeSpec binderClass = TypeSpec.classBuilder(binderClassName)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(delegateBinderInterface)
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ClassName.get(activityPackage, activityName), "activity")
                        .addParameter(presenterComponent, "presenterComponent")
                        .addParameter(ClassName.get("com.mvp", "ModuleEventBus"), "moduleEventBus")
                        //.addCode(initCode)
                        .addCode(constructCode, ClassName.get(activityPackageName, "DaggerComponent" + activityName + "DelegateBinder"), modulePresenterClass)
                        //.addCode(castCode, binderType, binderType)
                        //.addCode(binderConstructorCode, modulePresenterClass)
                        .build())
                .addField(FieldSpec.builder(ClassName.bestGuess(activityPackage + "." + delegateClassName), "delegate")
                        .addAnnotation(Inject.class)
                        .build())
                .addMethod(MethodSpec.methodBuilder("onCreate")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ClassName.bestGuess("android.os.Bundle"), "savedInstanceState")
                        .addCode("delegate.onCreate(savedInstanceState);\n")
                        .returns(void.class)
                        .build())
                .addMethod(MethodSpec.methodBuilder("onPostResume")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addCode("delegate.onPostResume();\n")
                        .returns(void.class)
                        .build())
                .addMethod(MethodSpec.methodBuilder("onDestroy")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addCode("delegate.onDestroy();\n")
                        .returns(void.class)
                        .build())
                .addMethod(MethodSpec.methodBuilder("onSaveInstanceState")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ClassName.bestGuess("android.os.Bundle"), "outState")
                        .addCode("delegate.onSaveInstanceState(outState);\n")
                        .returns(void.class)
                        .build())
                .addMethod(MethodSpec.methodBuilder("getPresenter")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addCode("return delegate.getPresenter();\n")
                        .returns(ClassName.get(presenterClass))
                        .build())
                .build();

        writeClass(binderClass, activityPackage);

    }

    private void processUiViewClasses(RoundEnvironment env) {

        for (Element element : uiViewClasses) {
            TypeMirror activityType = element.asType();
            String packageName = extractPackage(activityType);

            UIView uiView = element.getAnnotation(UIView.class);
            TypeMirror presenter = null;
            try {
                uiView.presenter();
            } catch (MirroredTypeException ex) {
                presenter = ex.getTypeMirror();
            }

            TypeSpec.Builder builder = TypeSpec.interfaceBuilder("Component" + element.getSimpleName().toString() + "DelegateBinder");

            ClassName componentClass = ClassName.get("dagger", "Component");

            String packageName1 = extractPackage(presenter);
            //builder.addAnnotation(MvpScope.class);
            builder.addAnnotation(AnnotationSpec.builder(componentClass)
                    .addMember("modules", CodeBlock.of("{ $T.class, $T.class }", ClassName.get(packageName1, "Module" + typeUtils.asElement(presenter).getSimpleName().toString()),  ClassName.get("com.mvp", "ModuleEventBus")))
                    //.addMember("dependencies", "{ $T.class }", ClassName.get(extractPackage(presenter), "Component" + typeUtils.asElement(presenter).getSimpleName().toString()))
                    .build());

            ParameterizedTypeName componentPresenterType = ParameterizedTypeName.get(ClassName.get("com.mvp", "ComponentActivity"),  ClassName.bestGuess(extractPackage(activityType) + "." + element.getSimpleName().toString() + "DelegateBinder"));
            builder.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
            builder.addSuperinterface(componentPresenterType);

            //String activityPackageName = extractPackage(activityType);
            //String activityName = typeUtils.asElement(activityType).getSimpleName().toString();
            //ClassName className = ClassName.get(activityPackageName, "Component" + activityName + "DelegateBinder");

            String presenterPackage = extractPackage(presenter);
            ClassName componentPresenter = ClassName.get(presenterPackage, "Component" + typeUtils.asElement(presenter).getSimpleName().toString());
            /*builder.addMethod(MethodSpec.methodBuilder("component")
                    .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
                    .returns(componentPresenter)
                    .addParameter(ClassName.get(extractPackage(presenter), "Module" + typeUtils.asElement(presenter).getSimpleName().toString() + "Dependencies"), "module")
                    .build());*/

            writeClass(builder.build(), packageName);

            TypeMirror viewType = allViewTypes.get(presenter.toString());

            ParameterizedTypeName presenterComponent = ParameterizedTypeName.get(ClassName.get("com.mvp", "PresenterComponent"), ClassName.get(viewType), ClassName.get(presenter));

            buildDelegateAndBinder(presenter, viewType, activityType, presenterComponent);

        }
    }

    private void writeFactoryInterface() {
        TypeSpec factory = TypeSpec.interfaceBuilder("IFactory")
                .addTypeVariable(TypeVariableName.get("T"))
                .addMethod(
                        MethodSpec.methodBuilder("create")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .addParameter(TypeVariableName.get("T"), "presenter")
                        .addParameter(ClassName.get("android.os", "Handler"), "handler")
                        .addParameter(ClassName.get(ExecutorService.class), "service")
                        .returns(ParameterizedTypeName.get(ClassName.get(OnEventListener.class), WildcardTypeName.subtypeOf(TypeName.OBJECT)))
                        .build()
                ).build();
        writeClass(factory, "com.mvp");
    }

    private void writeMethodsClass() {
        ClassName hashMapClass = ClassName.get(HashMap.class);
        ClassName listClass = ClassName.get(ArrayList.class);
        ClassName stringClass = ClassName.get("java.lang", "String");
        ParameterizedTypeName iFactoryListClass = ParameterizedTypeName.get(listClass, IFACTORY_CLASS_NAME);
        ParameterizedTypeName p = ParameterizedTypeName.get(hashMapClass, stringClass, iFactoryListClass);

        TypeSpec.Builder builder = TypeSpec.classBuilder("MvpEventListener")
                .addModifiers(Modifier.FINAL)
                .addField(p, "METHODS", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .addStaticBlock(CodeBlock.of("METHODS = new HashMap<String, ArrayList<IFactory<?>>>();"));

        ParameterizedTypeName listOfOnEventListenersType = ParameterizedTypeName.get(ClassName.get(ArrayList.class), ParameterizedTypeName.get(ClassName.get(OnEventListener.class), WildcardTypeName.subtypeOf(TypeName.OBJECT)));

        MethodSpec method = MethodSpec.methodBuilder("get")
                .addParameter(stringClass, "clazz", Modifier.FINAL)
                .addParameter(TypeName.OBJECT, "e")
                .addParameter(ClassName.get("android.os", "Handler"), "handler")
                .addParameter(ClassName.get(ExecutorService.class), "service")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .addStatement("ArrayList<OnEventListener<?>> onEventListeners = new ArrayList<>();")
                .addStatement("ArrayList<IFactory<?>> factories = METHODS.get(clazz);")
                .beginControlFlow("if (factories != null)")
                .beginControlFlow("for (IFactory factory : factories)")
                .addCode(CodeBlock.of("onEventListeners.add((factory.create(e, handler, service)));"))
                .endControlFlow()
                .endControlFlow()
                .addCode(CodeBlock.of("return onEventListeners;"))
                .returns(listOfOnEventListenersType)
                .build();


        builder.addMethod(method);

        for (Map.Entry<String, List<String>> entry : allGeneratedEventListenerClasses.entrySet()) {

            CodeBlock.Builder initializerBuilder = CodeBlock.builder();

            initializerBuilder.add(CodeBlock.of("METHODS.put(\"" + entry.getKey() + "\", new ArrayList<$T>());\n", IFACTORY_CLASS_NAME));

            for (String clazz : entry.getValue()){
                initializerBuilder.add("METHODS.get(\"" + entry.getKey() + "\").add(new " + clazz + ".Factory());\n");
            }

            builder.addStaticBlock(initializerBuilder.build());
        }

        TypeSpec c = builder.build();
        writeClass(c, "com.mvp");

        allGeneratedEventListenerClasses.clear();
    }

    private void processPresenter(Element element, TypeMirror classType, List<TypeMirror> basePresenters, TypeMirror viewType) {

        List<? extends Element> enclosedElements = typeUtils.asElement(viewType).getEnclosedElements();
        MvpViewInterfaceInfo viewInterfaceInfo = new MvpViewInterfaceInfo(enclosedElements);
        TypeName className = ClassName.get(classType);
        Presenter presenter = element.getAnnotation(Presenter.class);
        List<Element> childElements = combineEnclosingElements(element, basePresenters);
        List<ExecutableElement> allMethods = combineAllDeclaredMethods(element);

        ProxyInfo info = new ProxyInfo(classType, viewType, allMethods);
        TypeSpec t_ = info.processMethods(typeUtils);
        //writeClass(t_, classType.toString().replaceAll("." + convertDataClassToString(classType), ""));
        writeClass(t_, "com.mvp");

        for (Element childElement : childElements){
            Event eventAnnotation = childElement.getAnnotation(Event.class);
            if (childElement.getKind() == ElementKind.METHOD && eventAnnotation != null){
                ExecutableElement method = (ExecutableElement) childElement;
                String methodName = method.getSimpleName().toString();
                TypeMirror parameterType = method.getParameters().get(0).asType();
                TypeMirror returnType = method.getReturnType();
                if (!interceptors.containsKey(parameterType)){
                    interceptors.put(parameterType, new ArrayList<Interceptor>());
                }
                List<Interceptor> interceptorList = interceptors.get(parameterType);
                Interceptor o = new Interceptor(methodName, parameterType, returnType, eventAnnotation.thread());
                if (!interceptorList.contains(o)){
                    interceptorList.add(o);
                }
            }
        }

        List<TypeMirror> declaredEventTypeInViewEvents = new ArrayList<>();
        List<ViewEvent> receivesEvent = combineEvents(presenter, basePresenters);

        allGeneratedEventListenerClasses.put(classType.toString(), new ArrayList<String>());

        for (ViewEvent event : receivesEvent) {

            boolean declaredParameterAvailable = true;

            String viewMethodName = event.viewMethodName();
            TypeMirror dataClass = parseDataClass(event);
            if (declaredEventTypeInViewEvents.contains(dataClass)){
                throw new IllegalStateException(String.format("found duplicate event type: \"%s\" in ViewEvents of presenter class: \"%s\"", dataClass.toString(), classType.toString() ));
            }
            declaredEventTypeInViewEvents.add(dataClass);
            if (!viewInterfaceInfo.hasMethod(viewMethodName, dataClass)){
                if (!viewInterfaceInfo.hasMethod(viewMethodName)) {
                    String paramType = typeUtils.asElement(dataClass).getSimpleName().toString();
                    throw new IllegalStateException(String.format("method \"void %s(%s)\" is not declared in: %s", viewMethodName, paramType, viewType.toString()));
                }else{
                    declaredParameterAvailable = false;
                }
            }
            List<Interceptor> ceptors = this.interceptors.get(dataClass);
            MethodSpec constructor = buildConstructor(className);
            MethodSpec.Builder processEventBuilder = createProcessEventBuilder(dataClass);
            MethodSpec onEventMethod = buildOnEventMethod(dataClass, viewMethodName, processEventBuilder, ceptors, declaredParameterAvailable);
            MethodSpec onDestroyMethod = buildOnDestroyMethod();
            MethodSpec processEventMethod = processEventBuilder.build();
            TypeSpec c = buildOnEventListenerClass(element, dataClass, className, constructor, onEventMethod, processEventMethod, onDestroyMethod, convertDataClassToString(dataClass));
            writeClass(c, "com.mvp");
        }

        for (Map.Entry<TypeMirror, List<Interceptor>> entry : interceptors.entrySet()){
            List<Interceptor> ceptors = entry.getValue();
            TypeMirror dataClass = entry.getKey();
            MethodSpec constructor = buildConstructor(className);
            MethodSpec.Builder processEventBuilder = createProcessEventBuilder(dataClass);
            MethodSpec onEventMethod = buildOnEventMethod(dataClass, null, processEventBuilder, ceptors, true);
            MethodSpec onDestroyMethod = buildOnDestroyMethod();
            MethodSpec processEventMethod = processEventBuilder.build();
            TypeSpec c = buildOnEventListenerClass(element, dataClass, className, constructor, onEventMethod, processEventMethod, onDestroyMethod, convertDataClassToString(dataClass));
            writeClass(c, "com.mvp");
        }

        interceptors.clear();

    }

    private List<ExecutableElement> combineAllDeclaredMethods(Element element) {
        List<ExecutableElement> allMethods = new ArrayList<>();
        List<? extends TypeMirror> superTypes = typeUtils.directSupertypes(element.asType());
        for (TypeMirror type : superTypes) {
            if (typeUtils.asElement(type).getKind() == ElementKind.INTERFACE)
                continue;
            List<ExecutableElement> recursiveResult = combineAllDeclaredMethods(typeUtils.asElement(type));
            for (ExecutableElement executableElement : recursiveResult) {
                if (!allMethods.contains(executableElement))
                    allMethods.add(executableElement);
            }
        }
        List<? extends Element> elements = element.getEnclosedElements();
        for (Element e : elements){
            if (e.getKind() == ElementKind.METHOD){
                ExecutableElement executableElement = (ExecutableElement) e;
                if (methodCanBeDecorated(e.getModifiers()) && isSupportedMethod(e.getSimpleName().toString())) {
                    if (!allMethods.contains(executableElement)) {
                        allMethods.add(executableElement);
                    }
                }
            }
        }
        return allMethods;
    }

    private boolean isSupportedMethod(String methodName) {
        String[] unsupportedMethods = new String[]{
                "clone",
                "finalize"
        };
        for (String unsupportedMethod : unsupportedMethods) {
            if (unsupportedMethod.equals(methodName))
                return false;
        }
        return true;
    }

    private boolean methodCanBeDecorated(Set<Modifier> modifiers) {
        Modifier[] unsupportedModifiers = new Modifier[]{
                Modifier.PRIVATE,
                Modifier.FINAL,
                Modifier.NATIVE,

        };
        for (Modifier unsupportedModifier : unsupportedModifiers) {
            if (modifiers.contains(unsupportedModifier))
                return false;
        }
        return true;
    }

    private List<ViewEvent> combineEvents(Presenter presenter, List<TypeMirror> basePresenters) {
        ViewEvent[] presenterViewEvents = presenter.viewEvents();
        List<ViewEvent> viewEvents = new ArrayList<>();
        viewEvents.addAll(Arrays.asList(presenterViewEvents));
        for (TypeMirror basePresenter : basePresenters) {
            Presenter annotation = typeUtils.asElement(basePresenter).getAnnotation(Presenter.class);
            viewEvents.addAll(Arrays.asList(annotation.viewEvents()));
        }
        return viewEvents;
    }

    private List<Element> combineEnclosingElements(Element element, List<TypeMirror> basePresenters) {
        List<Element> childElements = new ArrayList<>(element.getEnclosedElements());
        for (TypeMirror basePresenter : basePresenters) {
            List<? extends Element> t = typeUtils.asElement(basePresenter).getEnclosedElements();
            childElements.addAll(t);
        }
        return childElements;
    }

    private List<TypeMirror> findBasePresenters(TypeMirror presenterType){
        List<TypeMirror> basePresenterTypes = new ArrayList<>();
        List<? extends TypeMirror> typeMirrors = typeUtils.directSupertypes(presenterType);
        for (TypeMirror typeMirror : typeMirrors) {
            basePresenterTypes.addAll(findBasePresenters(typeMirror));
            Presenter annotation = typeUtils.asElement(typeMirror).getAnnotation(Presenter.class);
            if (annotation != null){
                basePresenterTypes.add(typeMirror);
            }
        }
        return basePresenterTypes;
    }

    private TypeMirror findViewTypeOfPresenter(DeclaredType presenterType, TypeMirror currentPresenterType) {
        TypeMirror baseViewType = elementUtils.getTypeElement("com.mvp.MvpView").asType();
        TypeMirror viewType = null;
        List<? extends TypeMirror> typeMirrors = typeUtils.directSupertypes(currentPresenterType);
        for (TypeMirror typeMirror : typeMirrors) {
            TypeMirror erasure = typeUtils.erasure(typeMirror);
            if (typeUtils.isAssignable(erasure, presenterType.asElement().asType())) {
                DeclaredType declaredType = (DeclaredType) typeMirror;
                List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
                if (!typeArguments.isEmpty()) {
                    boolean found = false;
                    for (TypeMirror possibleViewType : typeArguments) {
                        if (typeUtils.isAssignable(possibleViewType, baseViewType) && possibleViewType.toString().contains(".")) {
                            viewType = possibleViewType;
                            found = true;
                            break;
                        }
                    }
                    if (found) break;
                }
            }else{
                viewType = findViewTypeOfPresenter(presenterType, typeMirror);
                if (viewType != null)
                    break;
            }
        }

        return viewType;
    }

    private MethodSpec.Builder createProcessEventBuilder(TypeMirror dataClass) {
        ParameterizedTypeName targetType = ParameterizedTypeName.get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(TypeName.OBJECT));
        ArrayTypeName arrTargetType = ArrayTypeName.of(targetType);
        return MethodSpec.methodBuilder("processEvent")
                .addModifiers(Modifier.PRIVATE)
                .addParameter(TypeName.get(dataClass), "data", Modifier.FINAL)
                .addParameter(arrTargetType, "target", Modifier.FINAL);
    }

    private MethodSpec buildOnDestroyMethod() {
        return MethodSpec.methodBuilder("onDestroy")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addStatement("e.clear()")
                .addStatement("handler.clear()")
                .addStatement("service.clear()")
                .beginControlFlow("if (nextEventListener != null)")
                .addStatement("nextEventListener.onDestroy()")
                .endControlFlow()
                .addStatement("nextEventListener = null")
                .returns(void.class)
                .build();
    }

    private void note(String key, String message) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, String.format("%s: %s", key, message));
    }

    private void writeClass(TypeSpec clazz) {
        try {
            JavaFile.builder("com.mvp.annotation.processor", clazz)
                    .addFileComment("Generated code")
                    .build().writeTo(processingEnv.getFiler());
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private void writeClass(TypeSpec clazz, String packageName) {
        try {
            JavaFile.builder(packageName, clazz)
                    .addFileComment("Generated code")
                    .build().writeTo(processingEnv.getFiler());
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private TypeSpec buildOnEventListenerClass(Element e, TypeMirror dataClass, TypeName className,
                                               MethodSpec constructor, MethodSpec onEventMethod,
                                               MethodSpec processEventMethod,
                                               MethodSpec onDestroyMethod, String strDataClass) {

        ParameterizedTypeName targetType = ParameterizedTypeName.get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(TypeName.OBJECT));
        ArrayTypeName arrTargetType = ArrayTypeName.of(targetType);

        List<String> listOfClasses = allGeneratedEventListenerClasses.get(e.asType().toString());
        String clazz = e.getSimpleName().toString() + "__EventDelegate__" + strDataClass;

        if (!listOfClasses.contains(clazz)) {
            listOfClasses.add(clazz);
        }

        ParameterizedTypeName fieldTypeEventListener = ParameterizedTypeName.get(ClassName.get(WeakReference.class), className);
        ParameterizedTypeName fieldTypeHandler = ParameterizedTypeName.get(ClassName.get(WeakReference.class), ClassName.get("android.os", "Handler"));
        ParameterizedTypeName fieldTypeService = ParameterizedTypeName.get(ClassName.get(WeakReference.class), ClassName.get(ExecutorService.class));
        ParameterizedTypeName fieldTypenextEventListener = ParameterizedTypeName.get(ClassName.get(OnEventListener.class), TypeName.get(dataClass));
        return TypeSpec.classBuilder(clazz)
                                .addModifiers(Modifier.FINAL)
                                .addSuperinterface(ParameterizedTypeName.get(ClassName.get(OnEventListener.class), TypeName.get(dataClass)))
                                .addField(fieldTypeEventListener, "e", Modifier.PRIVATE, Modifier.FINAL)
                                .addField(fieldTypeHandler, "handler", Modifier.PRIVATE, Modifier.FINAL)
                                .addField(fieldTypeService, "service", Modifier.PRIVATE, Modifier.FINAL)
                                .addField(fieldTypenextEventListener, "nextEventListener", Modifier.PRIVATE)
                                .addMethod(constructor)
                                .addMethod(onEventMethod)
                                .addMethod(processEventMethod)
                                .addMethod(onDestroyMethod)
                                .addMethod(MethodSpec.methodBuilder("setNext")
                                        .addAnnotation(Override.class)
                                        .addModifiers(Modifier.PUBLIC)
                                        .addParameter(TypeName.OBJECT, "nextEventListener")
                                        //.beginControlFlow("if(this.nextEventListener == null)")
                                        .addStatement("this.nextEventListener = (" + fieldTypenextEventListener.toString() + ")nextEventListener")
                                        //.nextControlFlow("else")
                                        //.addStatement("this.nextEventListener.setNext(nextEventListener)")
                                        //.endControlFlow()
                                        .returns(TypeName.get(void.class))
                                        .build())
                                .addMethod(MethodSpec.methodBuilder("hasNext")
                                        .addAnnotation(Override.class)
                                        .addModifiers(Modifier.PUBLIC)
                                        .addStatement("return this.nextEventListener != null")
                                        .returns(TypeName.get(boolean.class))
                                        .build())
                                .addMethod(MethodSpec.methodBuilder("getNext")
                                        .addAnnotation(Override.class)
                                        .addModifiers(Modifier.PUBLIC)
                                        .addStatement("return this.nextEventListener")
                                        .returns(fieldTypenextEventListener)
                                        .build())
                                .addMethod(MethodSpec.methodBuilder("clearNext")
                                        .addAnnotation(Override.class)
                                        .addModifiers(Modifier.PUBLIC)
                                        .addStatement("this.nextEventListener = null")
                                        .returns(void.class)
                                        .build())
                                .addMethod(MethodSpec.methodBuilder("getDataClass")
                                        .addModifiers(Modifier.PUBLIC)
                                        .addAnnotation(Override.class)
                                        .addCode(CodeBlock.of("return $T.class;", TypeName.get(dataClass)))
                                        .returns(ParameterizedTypeName.get(ClassName.get(Class.class), TypeName.get(dataClass)))
                                        .build()
                                )
                                .addMethod(MethodSpec.methodBuilder("isTarget")
                                        .addModifiers(Modifier.PRIVATE)
                                        .addParameter(arrTargetType, "target")
                                        .addStatement("Class<?> presenterClass = this.e.get().getClass()")
                                        .beginControlFlow("for (Class<?> clazz : target)")
                                        .beginControlFlow("if (clazz.isAssignableFrom(presenterClass))")
                                        .addStatement("return true")
                                        .endControlFlow()
                                        .endControlFlow()
                                        .addStatement("return false")
                                        .returns(TypeName.BOOLEAN)
                                        .build())
                                .addType(TypeSpec.classBuilder("Factory")
                                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                                        .addSuperinterface(ParameterizedTypeName.get(IFACTORY_CLASS_NAME.rawType, className))
                                        .addMethod(MethodSpec.methodBuilder("create")
                                            .addAnnotation(Override.class)
                                            .addModifiers(Modifier.PUBLIC)
                                            .addParameter(ClassName.get(e.asType()), "e")
                                            .addParameter(ClassName.get("android.os", "Handler"), "handler")
                                            .addParameter(ClassName.get(ExecutorService.class), "service")
                                            .addCode(CodeBlock.of("return new " +  clazz + "(e, handler, service);"))
                                            .returns(ParameterizedTypeName.get(ClassName.get(OnEventListener.class), WildcardTypeName.subtypeOf(TypeName.OBJECT)))
                                            .build())
                                        .build())
                                .build();
    }

    private String convertDataClassToString(TypeMirror dataClass) {
        String s = dataClass.toString();
        int index = s.lastIndexOf(".");
        return s.substring(index + 1);
    }

    private MethodSpec buildOnEventMethod(TypeMirror dataClass, String viewMethodName, MethodSpec.Builder processEventBuilder, List<Interceptor> interceptors, boolean declaredParameterAvailable) {

        ParameterizedTypeName targetType = ParameterizedTypeName.get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(TypeName.OBJECT));
        ArrayTypeName arrTargetType = ArrayTypeName.of(targetType);

        MethodSpec.Builder builder = MethodSpec.methodBuilder("onEvent")
                                .addAnnotation(Override.class)
                                .addModifiers(Modifier.PUBLIC)
                                .addParameter(TypeName.get(dataClass), "data", Modifier.FINAL)
                                .addParameter(arrTargetType, "target", Modifier.FINAL);

        note("before", "applying interceptors");

        processEventBuilder.beginControlFlow("if (e.get() != null)");

        String finalStatement = "%s";

        List<Interceptor> usedInterceptors = new ArrayList<>();

        builder.beginControlFlow("if (target == null || isTarget(target))");

        if (interceptors != null && !interceptors.isEmpty()) {

            for (int i = 0; i < interceptors.size() ; i++) {
                Interceptor interceptor = interceptors.get(i);
                Interceptor previousInterceptor = null;
                if (i - 1 >= 0)
                    previousInterceptor = interceptors.get(i-1);

                String statement;

                if (interceptor.getParameterType().equals(dataClass)) {
                    if (interceptor.getReturnType().equals(dataClass)) {
                        statement = "data = e.get()." + interceptor.getMethodName() + "(data);";
                    } else {
                        statement = "e.get()." + interceptor.getMethodName()+ "(data);";
                    }
                    if (previousInterceptor == null){
                        if (interceptor.getThreadType().equals(Event.BACKGROUND_THREAD)){
                            ensureBackgroundThread(builder);
                        }else{
                            ensureUiThread(builder);
                        }
                        finalStatement = String.format(finalStatement, addStatement(statement));
                    }else if(previousInterceptor.getThreadType().equals(interceptor.getThreadType())){
                        finalStatement = String.format(finalStatement, addStatement(statement));
                    }else if(previousInterceptor.getThreadType().equals(Event.BACKGROUND_THREAD)){
                        finalStatement = String.format(finalStatement, addUiStatement(statement));
                    }else{
                        finalStatement = String.format(finalStatement, addBackgroundStatement(statement));
                    }
                    usedInterceptors.add(interceptor);
                }

            }

        }else{
            ensureUiThread(builder);
        }

        builder.nextControlFlow("else");
        builder.addStatement(addCallNextEventListenerStatement());
        builder.endControlFlow();

        note("after", "applying interceptors");

        if (viewMethodName != null){
            String statement = "e.get().getView()." + viewMethodName + (declaredParameterAvailable ? "(data);" : "();");
            if (interceptors != null && !interceptors.isEmpty()){
                String threadType = interceptors.get(interceptors.size()-1).getThreadType();
                note("last interceptor thread type", threadType);
                if (threadType.equals(Event.UI_THREAD))
                    finalStatement = String.format(finalStatement, addStatement(statement));
                else
                    finalStatement = String.format(finalStatement, addUiStatement(statement));
            }else {
                finalStatement = String.format(finalStatement, addStatement(statement));
            }
        }

        if (interceptors != null && !interceptors.isEmpty()) {
            interceptors.removeAll(usedInterceptors);
            usedInterceptors.clear();
        }

        finalStatement = String.format(finalStatement, addCallNextEventListenerStatement());
        finalStatement = finalStatement.replace("%s", "");
        processEventBuilder.addStatement(finalStatement);

        processEventBuilder.endControlFlow();

        return builder.returns(void.class).build();
    }

    private String addCallNextEventListenerStatement() {
        return "if (nextEventListener != null) nextEventListener.onEvent(data, target);";
    }

    private String addUiStatement(String statement) {
        return "handler.get().post(new Runnable(){ @Override public void run() { " + statement + ";%s\n } });";
    }

    private String addBackgroundStatement(String statement) {
        return "service.get().submit(new Runnable(){ @Override public void run() { " + statement + ";\n%s\n } });";
    }

    private String addStatement(String statement) {
        return statement + ";\n%s";
    }

    private void ensureBackgroundThread(MethodSpec.Builder builder) {
        ClassName looperType = ClassName.get("android.os", "Looper");
        builder.beginControlFlow("if ($T.myLooper() == $T.getMainLooper())", looperType, looperType)
                .addStatement("service.get().submit(new Runnable(){ @Override public void run() { processEvent(data, target); } })")
                .nextControlFlow("else")
                .addStatement("processEvent(data, target)")
                .endControlFlow();
    }

    private void ensureUiThread(MethodSpec.Builder builder) {
        ClassName looperType = ClassName.get("android.os", "Looper");
        builder.beginControlFlow("if ($T.myLooper() == $T.getMainLooper())", looperType, looperType)
                .addStatement("processEvent(data, target)")
                .nextControlFlow("else")
                .addStatement("handler.get().post(new Runnable(){ @Override public void run() { processEvent(data, target); } })")
                .endControlFlow();
    }

    private MethodSpec buildConstructor(TypeName className) {
        return MethodSpec.constructorBuilder()
                                .addModifiers(Modifier.PUBLIC)
                                .addParameter(className, "e")
                                .addParameter(ClassName.get("android.os", "Handler"), "handler")
                                .addParameter(ExecutorService.class, "service")
                                .addStatement("this.e = new WeakReference<>(e)")
                                .addStatement("this.handler = new WeakReference<>(handler)")
                                .addStatement("this.service = new WeakReference<>(service)")
                                .build();
    }

    private TypeMirror parseDataClass(ViewEvent event) {
        TypeMirror dataClass = null;
        try {
            event.eventType();
        } catch (MirroredTypeException ex) {
            dataClass = ex.getTypeMirror();
        }
        return dataClass;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> supportedAnnotations = new HashSet<>();
        supportedAnnotations.add(Presenter.class.getCanonicalName());
        supportedAnnotations.add(UIView.class.getCanonicalName());
        supportedAnnotations.add(Provider.class.getCanonicalName());
        supportedAnnotations.add(Generated.class.getCanonicalName());
        return supportedAnnotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    static class TypeComponentPresenter {
        private TypeMirror presenterClass;
        private String componentPresenterClassName;
        private final ClassName[] moduleClasses;
        private ClassName[] componentClasses;

        TypeComponentPresenter(TypeMirror presenterClass, String componentPresenterClassName, ClassName[] moduleClasses, ClassName[] componentClasses){
            this.presenterClass = presenterClass;
            this.componentPresenterClassName = componentPresenterClassName;
            this.moduleClasses = moduleClasses;
            this.componentClasses = componentClasses;
        }

        public TypeMirror getPresenterClass() {
            return presenterClass;
        }

        public String getComponentPresenterClassName() {
            return componentPresenterClassName;
        }

        public ClassName[] getModuleClasses() {
            return moduleClasses;
        }

        public ClassName[] getComponentClasses() {
            return componentClasses;
        }
    }

    private class AnnotationMemberModuleClasses {

        private String presenterPackage;
        private String moduleFormat;
        private ClassName[] classes;

        public AnnotationMemberModuleClasses(String presenterPackage) {
            this.presenterPackage = presenterPackage;
        }

        public String getModuleFormat() {
            return moduleFormat;
        }

        public ClassName[] getClasses() {
            return classes;
        }

        public AnnotationMemberModuleClasses parse(Element element) {
            AnnotationValue value = getAnnotationValue(element, MEMBER_NEEDS_MODULES);
            List<Object> moduleClasses = value != null ? (List<Object>) value.getValue() : new ArrayList<>();

            moduleFormat = "{ ";
            classes = new ClassName[moduleClasses.size() + 2];

            for (int i = 0; i < moduleClasses.size(); i++) {
                ClassName className = ClassName.bestGuess(moduleClasses.get(i).toString().replace(".class", ""));
                moduleFormat += "$T.class";
                classes[i] = className;
                moduleFormat += ", ";
            }
            moduleFormat += "$T.class, ";
            moduleFormat += "$T.class";
            classes[classes.length - 1] = ClassName.get(presenterPackage, "Module" + element.getSimpleName().toString() + "Dependencies");
            classes[classes.length - 2] = ClassName.get("com.mvp", "ModuleEventBus");
            moduleFormat += " }";
            return this;
        }
    }

    private class AnnotationMemberComponentClasses {

        private String presenterPackage;
        private String componentFormat;
        private ClassName[] classes;

        public AnnotationMemberComponentClasses(String presenterPackage) {
            this.presenterPackage = presenterPackage;
        }

        public String getComponentFormat() {
            return componentFormat;
        }

        public ClassName[] getClasses() {
            return classes;
        }

        public AnnotationMemberComponentClasses parse(Element element) {
            AnnotationValue value = getAnnotationValue(element, MEMBER_NEEDS_COMPONENTS);
            List<Object> moduleClasses = value != null ? (List<Object>) value.getValue() : new ArrayList<>();

            componentFormat = "{ ";
            classes = new ClassName[moduleClasses.size()];

            for (int i = 0; i < moduleClasses.size(); i++) {
                ClassName className = ClassName.bestGuess(moduleClasses.get(i).toString().replace(".class", ""));
                componentFormat += "$T.class";
                classes[i] = className;
                if (i < moduleClasses.size() - 1)
                    componentFormat += ", ";
            }
            //classes[classes.length - 1] = ClassName.get("com.mvp", "ComponentEventBus");
            //componentFormat += "$T.class";
            componentFormat += " }";
            return this;
        }
    }


}
