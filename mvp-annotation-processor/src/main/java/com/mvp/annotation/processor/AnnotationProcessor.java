package com.mvp.annotation.processor;

import com.mvp.annotation.Event;
import com.mvp.annotation.OnEventListener;
import com.mvp.annotation.ViewEvent;
import com.mvp.annotation.Presenter;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
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

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
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

public class AnnotationProcessor extends AbstractProcessor {

    static final ParameterizedTypeName IFACTORY_CLASS_NAME = ParameterizedTypeName.get(ClassName.get("com.mvp.annotation.processor", "IFactory"), WildcardTypeName.subtypeOf(TypeName.OBJECT));
    private HashMap<TypeMirror, List<Interceptor>> interceptors = new HashMap<>();
    private Types typeUtils;
    private Elements elementUtils;

    private HashMap<String, List<String>> allGeneratedEventListenerClasses = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        this.processingEnv = env;
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {

        DeclaredType presenterType = typeUtils.getDeclaredType(elementUtils.getTypeElement("com.mvp.MvpPresenter"));

        for (Element element : env.getElementsAnnotatedWith(Presenter.class)) {

            if (element.getKind() == ElementKind.CLASS) {

                TypeMirror classType = element.asType();

                DeclaredType declaredClassType = (DeclaredType) classType;
                List<? extends TypeMirror> typeArguments = declaredClassType.getTypeArguments();

                if (typeArguments.size() > 0) {
                    /* anonymous instances of generic classes are not supported, so no classes must be generated for this type */
                    continue;
                }
                if (declaredClassType.asElement().getModifiers().contains(Modifier.ABSTRACT)) {
                    /* anonymous abstract class instances are not supported, so no classes must be generated for this type */
                    continue;
                }

                TypeMirror viewType = findViewTypeOfPresenter(presenterType, classType);
                List<TypeMirror> basePresenters = findBasePresenters(classType);

                if (viewType == null){
                    throw new IllegalStateException(String.format("class: %s is annotated with @Presenter, but does not derive from: %s", classType, presenterType));
                }

                processPresenter(element, classType, basePresenters, viewType);

            } else {
                throw new IllegalStateException(element.getSimpleName() + " is not a class!");
            }
        }

        writeFactoryInterface();
        writeMethodsClass();

        return true;
    }

    private void writeFactoryInterface() {
        TypeSpec factory = TypeSpec.interfaceBuilder("IFactory")
                .addTypeVariable(TypeVariableName.get("T"))
                .addModifiers(Modifier.PUBLIC)
                .addMethod(
                        MethodSpec.methodBuilder("create")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .addParameter(TypeVariableName.get("T"), "presenter")
                        .addParameter(ClassName.get("android.os", "Handler"), "handler")
                        .addParameter(ClassName.get(ExecutorService.class), "service")
                        .returns(ParameterizedTypeName.get(ClassName.get(OnEventListener.class), WildcardTypeName.subtypeOf(TypeName.OBJECT)))
                        .build()
                ).build();
        writeClass(factory);
    }

    private void writeMethodsClass() {
        ClassName hashMapClass = ClassName.get(HashMap.class);
        ClassName listClass = ClassName.get(ArrayList.class);
        ClassName stringClass = ClassName.get("java.lang", "String");
        ParameterizedTypeName iFactoryListClass = ParameterizedTypeName.get(listClass, IFACTORY_CLASS_NAME);
        ParameterizedTypeName p = ParameterizedTypeName.get(hashMapClass, stringClass, iFactoryListClass);

        TypeSpec.Builder builder = TypeSpec.classBuilder("MvpEventListener")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
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
        writeClass(c);

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
        writeClass(t_);

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
            writeClass(c);
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
            writeClass(c);
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
            if (e.getKind() == ElementKind.METHOD && e.getModifiers().contains(Modifier.PUBLIC) && !e.getModifiers().contains(Modifier.FINAL) && !e.getModifiers().contains(Modifier.NATIVE)){
                ExecutableElement executableElement = (ExecutableElement) e;
                if (!allMethods.contains(executableElement)) {
                    allMethods.add(executableElement);
                }
            }
        }
        return allMethods;
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
                                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
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
                                        .addSuperinterface(ParameterizedTypeName.get(ClassName.get("com.mvp.annotation.processor", "IFactory"), ClassName.get(e.asType())))
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
        return supportedAnnotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

}
