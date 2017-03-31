package com.mvp.annotation.processor.unittest;

import com.mvp.annotation.Generate;
import com.mvp.annotation.Provider;
import com.mvp.annotation.View;
import com.mvp.annotation.processor.ApplicationDelegate;
import com.mvp.annotation.processor.Gang;
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
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment)
    {
        if (shouldSkipAllRounds)
            return true;
        if (processed)
            return true;

        processed = true;

        Set<? extends Element> rootElements = roundEnvironment.getRootElements();
        if (viewElements.isEmpty())
            viewElements = findElementsAnnotatedWith(View.class, rootElements);

        String packageName = "com.mvp";
        TypeElement typeElement = elementUtils.getTypeElement("org.robolectric.RobolectricTestRunner");
        TypeElement typeElement1 = elementUtils.getTypeElement("android.support.test.runner.AndroidJUnit4");

        if (typeElement == null && typeElement1 == null)
        {
            Iterator<Element> iterator = findElementsAnnotatedWith(Provider.class, rootElements).iterator();
            if (!iterator.hasNext()){
                messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, "There should be an application class annotated with @Provider!");
                shouldSkipAllRounds = true;
                return true;
            }
            provider = elementUtils.getTypeElement(iterator.next().asType().toString());
            if (!viewElements.isEmpty()){
                new TriggerType(filer, packageName + ".trigger", viewElements, ClassName.get(provider)).generate();
            }
            return true;
        }

        TypeElement triggerElement = elementUtils.getTypeElement(packageName + ".trigger.Trigger");
        if (triggerElement == null)
        {
            messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, "Trigger element has not been generated!");
            shouldSkipAllRounds = true;
            return true;
        }

        AnnotationValue views = getAnnotationValue(triggerElement, Generate.class.getCanonicalName(), "views");
        AnnotationValue application = getAnnotationValue(triggerElement, Generate.class.getCanonicalName(), "application");
        if (application == null || application.getValue() == null){
            messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, "There should be an application class annotated with @Provider!");
            shouldSkipAllRounds = true;
            return true;
        }

        Object applicationClass =  application.getValue();
        provider = elementUtils.getTypeElement(ClassName.bestGuess(applicationClass.toString().replace(".class", "")).toString());

        if (typeElement1 != null || typeElement != null){
            String p = ClassName.bestGuess(provider.asType().toString()).packageName();
            String prefix = typeElement1 != null ? "AndroidTest" : "Test";
            ApplicationDelegate applicationDelegate = new ApplicationDelegate(processingEnv.getFiler(), p, typeUtils, elementUtils, (TypeElement) provider, prefix);
            applicationDelegate.generate();
            if (typeElement1 != null)
            {
                new TestRunnerType(filer, packageName, ClassName.get(provider.asType())).generate();
                return true;
            }
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
            new TestControllerType(filer, typeUtils, elementUtils, packageName, gang).generate();
            new PresenterBuilderType(filer, elementUtils, typeUtils, packageName, gang, packageName, provider).generate();
            new TestablePresenterModuleType(filer, elementUtils, getPackageName(presenterElement), gang).generate();
        }

        return true;
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

    private <T extends Annotation> Element findElementAnnotatedWith(Class<T> clazz, Set<? extends Element> elements)
    {
        for (Element element : elements)
        {
            if (element.getAnnotation(clazz) != null)
            {
                return element;
            }
        }
        return null;
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
