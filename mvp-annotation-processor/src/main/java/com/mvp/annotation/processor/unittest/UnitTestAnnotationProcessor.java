package com.mvp.annotation.processor.unittest;

import com.mvp.annotation.internal.Generate;
import com.mvp.annotation.Provider;
import com.mvp.annotation.View;
import com.mvp.annotation.processor.Gang;
import com.mvp.annotation.processor.graph.ObjectGraph;
import com.squareup.javapoet.ClassName;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import static com.mvp.annotation.processor.Utils.getAnnotationValue;

public class UnitTestAnnotationProcessor extends AbstractProcessor
{

    private static final String MEMBER_PRESENTER_CLASS = "presenter";

    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private TypeElement provider;
    private Set<Element> viewElements = new LinkedHashSet<>();

    private boolean processed = false;
    private Messager messager;
    private boolean shouldSkipAllRounds = false;
    private ObjectGraph objectGraph;

    @Override
    public synchronized void init(ProcessingEnvironment env)
    {
        this.processingEnv = env;
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment env)
    {

        if (env.errorRaised()) {
            return false;
        }

        if (shouldSkipAllRounds)
            return false;

        Set<? extends Element> rootElements = env.getRootElements();
        if (viewElements.isEmpty())
            viewElements = findElementsAnnotatedWith(View.class, rootElements);

        String packageName = "com.mvp";

        log(String.format("is unit test: %s", isUnitTest()));
        log(String.format("is android test: %s", isAndroidTest()));
        log(String.format("object graph evaluated: %s", String.valueOf(objectGraph != null)));
        log(String.format("object graph generated: %s", String.valueOf(objectGraph != null && objectGraph.isGenerated())));

        if (objectGraph == null && !isUnitTest() && !isAndroidTest())
        {
            Iterator<Element> iterator = findElementsAnnotatedWith(Provider.class, rootElements).iterator();
            if (!iterator.hasNext()){
                messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, "There should be an application class annotated with @Provider!");
                shouldSkipAllRounds = true;
                return false;
            }
            TypeElement componentAnnotation = elementUtils.getTypeElement("dagger.Component");
            Set<? extends Element> components = env.getElementsAnnotatedWith(componentAnnotation);
            objectGraph = new ObjectGraph(processingEnv, components, elementUtils, typeUtils);
            objectGraph.evaluate();
            provider = elementUtils.getTypeElement(iterator.next().asType().toString());
            if (!viewElements.isEmpty()){
                new TriggerType(filer, packageName + ".trigger", viewElements, ClassName.get(provider), objectGraph.getTopNodes()).generate();
            }
            return false;
        }

        if (objectGraph != null && (isUnitTest() || isAndroidTest())) {
            List<TypeMirror> createdInterfaces = objectGraph.getCreatedInterfaces();
            String p = ClassName.bestGuess(provider.asType().toString()).packageName();
            String prefix = isAndroidTest() ? "AndroidTest" : "Test";
            ApplicationTestDelegate testDelegate = new ApplicationTestDelegate(processingEnv.getFiler(), p, typeUtils, elementUtils, provider, prefix, createdInterfaces);
            testDelegate.generate();
        }

        if (objectGraph != null && (isAndroidTest() || isUnitTest()))
        {
            objectGraph.generate();
        }

        if (processed)
            return false;

        processed = true;

        if (!isUnitTest() && !isAndroidTest()) {
            return false;
        }

        TypeElement triggerElement = elementUtils.getTypeElement(packageName + ".trigger.Trigger");
        if (triggerElement == null)
        {
            messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, "Trigger element has not been generated!");
            shouldSkipAllRounds = true;
            return false;
        }

        AnnotationValue graph = getAnnotationValue(triggerElement, Generate.class.getCanonicalName(), "graph");
        //objectGraph = ObjectGraph.createFromAnnotation(processingEnv, roundEnvironment, elementUtils, typeUtils, graph);
        AnnotationValue views = getAnnotationValue(triggerElement, Generate.class.getCanonicalName(), "views");
        AnnotationValue application = getAnnotationValue(triggerElement, Generate.class.getCanonicalName(), "application");
        if (application == null || application.getValue() == null){
            messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, "There should be an application class annotated with @Provider!");
            shouldSkipAllRounds = true;
            return false;
        }

        Object applicationClass =  application.getValue();
        provider = elementUtils.getTypeElement(ClassName.bestGuess(applicationClass.toString().replace(".class", "")).toString());

        Set<? extends Element> components = ObjectGraph.createFromAnnotation(elementUtils, graph, views);

        objectGraph = new ObjectGraph(processingEnv, components, elementUtils, typeUtils);
        objectGraph.evaluate();
        objectGraph.generate();

        if (isAndroidTest()){
            new TestRunnerType(filer, packageName, ClassName.get(provider.asType())).generate();
            return false;
        }

        if (viewElements.isEmpty())
        {
            List<Object> value = (List<Object>) views.getValue();

            for (Object o : value)
            {
                ClassName className = ClassName.bestGuess(o.toString().replace(".class", ""));
                viewElements.add(elementUtils.getTypeElement(className.toString()));
            }

        }

        new AbstractControllerClassType(filer, packageName).generate();
        new ViewEnumType(filer, packageName).generate();
        new PresenterEnumType(filer, packageName).generate();
        new TestCaseType(filer, packageName).generate();

        for (Element viewElement : viewElements)
        {
            TypeMirror activityType = viewElement.asType();
            Object v = getAnnotationValue(typeUtils.asElement(activityType), MEMBER_PRESENTER_CLASS).getValue();
            String presenterClassString = v.toString().replace(".class", "");
            TypeElement presenterElement = elementUtils.getTypeElement(presenterClassString);
            DeclaredType presenterType = typeUtils.getDeclaredType(elementUtils.getTypeElement("com.mvp.MvpPresenter"));
            TypeMirror uiViewType = findViewTypeOfPresenter(presenterType, presenterElement.asType());
            Gang gang = new Gang(typeUtils.asElement(activityType), elementUtils.getTypeElement(presenterClassString), typeUtils.asElement(uiViewType));
            //new TestControllerType(filer, typeUtils, elementUtils, packageName, gang).generate();
            //new PresenterBuilderType(filer, elementUtils, typeUtils, packageName, gang, packageName, provider).generate();
            new TestablePresenterModuleType(filer, elementUtils, getPackageName(presenterElement), gang).generate();
        }

        return false;
    }

    public void log(String message)
    {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, message);
    }

    public boolean isAndroidTest()
    {
        return elementUtils.getTypeElement("android.support.test.runner.AndroidJUnit4") != null;
    }

    public boolean isUnitTest()
    {
        return elementUtils.getTypeElement("org.robolectric.RobolectricTestRunner") != null;
    }

    private <T extends Annotation> Set<Element> findElementsAnnotatedWith(Class<T> clazz, Set<? extends Element> rootElements)
    {
        Set<Element> elements = new LinkedHashSet<>();
        for (Element element : rootElements)
        {
            if (element.getAnnotation(clazz) != null)
            {
                elements.add(element);
            }
        }
        return elements;
    }

    private String getPackageName(Element viewElement)
    {
        return elementUtils.getPackageOf(viewElement).getQualifiedName().toString();
    }

    private TypeMirror findViewTypeOfPresenter(DeclaredType presenterType, TypeMirror currentPresenterType)
    {
        TypeMirror baseViewType = elementUtils.getTypeElement("com.mvp.MvpView").asType();
        TypeMirror viewType = null;
        List<? extends TypeMirror> typeMirrors = typeUtils.directSupertypes(currentPresenterType);
        for (TypeMirror typeMirror : typeMirrors)
        {
            TypeMirror erasure = typeUtils.erasure(typeMirror);
            if (typeUtils.isAssignable(erasure, presenterType.asElement().asType()))
            {
                DeclaredType declaredType = (DeclaredType) typeMirror;
                List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
                if (!typeArguments.isEmpty())
                {
                    boolean found = false;
                    for (TypeMirror possibleViewType : typeArguments)
                    {
                        if (typeUtils.isAssignable(possibleViewType, baseViewType) && possibleViewType.toString().contains("."))
                        {
                            viewType = possibleViewType;
                            found = true;
                            break;
                        }
                    }
                    if (found) break;
                }
            } else
            {
                viewType = findViewTypeOfPresenter(presenterType, typeMirror);
                if (viewType != null)
                    break;
            }
        }
        return viewType;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes()
    {
        Set<String> supportedAnnotations = new HashSet<>();
        supportedAnnotations.add("*");
        return supportedAnnotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion()
    {
        return SourceVersion.latestSupported();
    }
}
