package com.mvp.annotation.processor.graph;

import com.mvp.annotation.processor.Utils;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * Created by Andy on 14.04.2017.
 */

public class NodeComponent
{

    private final ProcessingEnvironment processingEnvironment;
    private final Elements elementUtils;
    private final Types typeUtils;
    private final TypeElement component;
    private final List<ExecutableElement> allProvidingMethods;
    private final List<Dependency> dependentModules;
    private final List<Dependency> dependentComponents;

    private NodeComponent(ProcessingEnvironment processingEnvironment,
                          Elements elementUtils, Types typeUtils, TypeElement component,
                          List<ExecutableElement> allProvidingMethods, List<Dependency> dependentModules,
                          List<Dependency> dependentComponents)
    {
        this.processingEnvironment = processingEnvironment;
        this.elementUtils = elementUtils;
        this.typeUtils = typeUtils;
        this.component = component;
        this.allProvidingMethods = allProvidingMethods;
        this.dependentModules = dependentModules;
        this.dependentComponents = dependentComponents;
    }

    public static NodeComponent createFrom(ProcessingEnvironment processingEnvironment, Elements elementUtils, Types typeUtils, TypeElement component)
    {
        List<Dependency> dependentModules = getDependentModules(elementUtils, typeUtils, component);
        List<Dependency> dependentComponents = getDependentComponents(elementUtils, typeUtils, component);
        List<ExecutableElement> allProvidingMethods = getProvidingMethods(elementUtils, typeUtils, component);
        return new NodeComponent(processingEnvironment, elementUtils, typeUtils, component, allProvidingMethods, dependentModules, dependentComponents);
    }

    private static List<ExecutableElement> getProvidingMethods(Elements elementUtils, Types typeUtils, TypeElement component)
    {
        List<ExecutableElement> allMethods = new ArrayList<>();
        List<? extends TypeMirror> typeMirrors = typeUtils.directSupertypes(component.asType());
        for (TypeMirror typeMirror : typeMirrors)
        {
            TypeMirror erasure = typeUtils.erasure(typeMirror);
            if (!erasure.toString().equals(Object.class.getName())) {
                allMethods.addAll(getProvidingMethods(elementUtils, typeUtils, elementUtils.getTypeElement(erasure.toString())));
            }
        }
        for (Element element : component.getEnclosedElements())
        {
            if (element.getKind() == ElementKind.METHOD) {
                ExecutableElement executableElement = (ExecutableElement) element;
                allMethods.add(executableElement);
            }
        }
        return allMethods;
    }

    private static List<Dependency> getDependentModules(Elements elementUtils, Types typeUtils, TypeElement module)
    {
        List<Dependency> dependentModules = new ArrayList<>();
        AnnotationValue modules = Utils.getAnnotationValue(module, "dagger.Component", "modules");
        if (modules != null)
        {
            List<Object> values = (List<Object>) modules.getValue();
            if (values != null)
            {
                for (Object o : values)
                {
                    TypeElement m = elementUtils.getTypeElement(o.toString().replace(".class", ""));
                    Dependency fromModule = Dependency.createFromModule(elementUtils, typeUtils, m);
                    if (fromModule != null)
                    {
                        dependentModules.add(fromModule);
                    }
                }
            }
        }
        return dependentModules;
    }

    private static List<Dependency> getDependentComponents(Elements elementUtils, Types typeUtils, TypeElement component)
    {
        List<Dependency> dependentComponents = new ArrayList<>();
        AnnotationValue components = Utils.getAnnotationValue(component, "dagger.Component", "dependencies");
        if (components != null)
        {
            List<Object> values = (List<Object>) components.getValue();
            if (values != null)
            {
                for (Object o : values)
                {
                    TypeElement c = elementUtils.getTypeElement(o.toString().replace(".class", ""));
                    Dependency fromComponent = Dependency.createFromComponent(elementUtils, typeUtils, c);
                    if (fromComponent != null)
                    {
                        dependentComponents.add(fromComponent);
                    }
                }
            }
        }
        return dependentComponents;
    }

    public TopNode buildDependencyGraph()
    {
        TypeElement scope = findScopeOfElement(component);
        log("scope of " + component.toString() + ": " + scope.toString());
        Dependency dependentComponent = !dependentComponents.isEmpty() ? dependentComponents.get(0) : null;
        TypeElement dependentScope = dependentComponent != null ? findScopeOfElement(dependentComponent.getTypeElement()) : null;
        if (dependentScope != null)
            log("scope of dependent " + dependentComponent.toString() + ": " + dependentScope.toString());


        TopNode topNode = buildGraph(this.component);
        TypeElement superComponent = this.findSuperComponent();
        if (superComponent != null) {
            List<ResultNode> resultNodes = buildGraph(superComponent).getResultNodes();
            for (ResultNode resultNode : resultNodes)
            {
                topNode.addResultNode(resultNode);
            }
        }

        return topNode;
    }

    private TypeElement findSuperComponent()
    {
        List<? extends TypeMirror> typeMirrors = typeUtils.directSupertypes(component.asType());
        for (TypeMirror typeMirror : typeMirrors)
        {
            if (!typeMirror.toString().equals(Object.class.getName())) {
                return elementUtils.getTypeElement(typeMirror.toString());
            }
        }
        return null;
    }

    private TopNode buildGraph(TypeElement component)
    {
        TopNode topNode = new TopNode(elementUtils, component, allProvidingMethods, dependentComponents);

        List<? extends Element> enclosedElements = component.getEnclosedElements();

        for (Element enclosedElement : enclosedElements)
        {
            if (enclosedElement.getKind() == ElementKind.METHOD)
            {
                ExecutableElement executableElement = (ExecutableElement) enclosedElement;
                TypeMirror returnType = executableElement.getReturnType();
                if (!returnType.toString().equals(void.class.getName()))
                {
                    boolean isProvider = Utils.isProviderType(elementUtils, typeUtils, returnType);
                    returnType = Utils.getGenericTypeIfIsProvider(elementUtils, typeUtils, returnType);
                    topNode.addResultNode(this.findDependencies(topNode, returnType, isProvider, false));
                }else if (returnType.toString().equals(void.class.getName()) && executableElement.getParameters().size() == 1) {
                    TypeMirror parameter = executableElement.getParameters().get(0).asType();
                    MemberNode resultNode = new MemberNode(elementUtils, typeUtils, this.getClassName(), parameter);
                    List<TypeElement> typeElements = resultNode.searchForInjectMembers(elementUtils, parameter);
                    for (TypeElement typeElement : typeElements)
                    {
                        TypeMirror typeMirror = typeElement.asType();
                        boolean isProvider = Utils.isProviderType(elementUtils, typeUtils, typeMirror);
                        typeMirror = Utils.getGenericTypeIfIsProvider(elementUtils, typeUtils, typeMirror);
                        resultNode.addResultNode(this.findDependencies(topNode, typeMirror, isProvider, false));
                    }
                    topNode.addResultNode(resultNode);
                }
            }
        }
        return topNode;
    }

    private void log(String message) {
        processingEnvironment.getMessager().printMessage(Diagnostic.Kind.NOTE, message);
    }

    private ResultNode findDependencies(TopNode topNode, TypeMirror returnType, boolean isProvider, boolean memberInjector)
    {
        this.log("searching dependencies for: " + returnType.toString());
        ResultNode resultNode = this.tryFindInDependentComponent(topNode, returnType, isProvider, memberInjector);
        if (resultNode != null) {
            return resultNode;
        }
        resultNode = this.tryFindInDependentModules(topNode, returnType, isProvider, memberInjector);
        if (resultNode != null) {
            return resultNode;
        }
        resultNode = this.tryFindThroughInjectConstructor(topNode, returnType, isProvider, memberInjector);
        if (resultNode != null) {
            return resultNode;
        }
        return null;
    }

    private ResultNode tryFindThroughInjectConstructor(TopNode topNode, TypeMirror returnType, boolean isProvider, boolean memberInjector)
    {
        this.log("trying to find dependencies for: " + returnType.toString() + " through @Inject Constructor");
        TypeElement typeElement = elementUtils.getTypeElement(returnType.toString());
        List<? extends Element> enclosedElements = typeElement.getEnclosedElements();
        for (Element enclosedElement : enclosedElements)
        {
            if (enclosedElement.getKind() == ElementKind.CONSTRUCTOR && enclosedElement.getAnnotation(Inject.class) != null) {
                this.log("found @Inject constructor for: " + returnType.toString());
                ExecutableElement executableElement = (ExecutableElement) enclosedElement;
                List<? extends VariableElement> parameters = executableElement.getParameters();
                if (parameters.isEmpty()) {
                    this.log("no parameters found in @Inject constructor for: " + returnType.toString());
                }
                List<TypeElement> injectMembers = ResultNode.searchForInjectMembers(elementUtils, returnType);
                ResultNode resultNode = new ResultNode(elementUtils, typeUtils, this.getClassName(), returnType, typeElement, executableElement);
                resultNode.setIsProvider(isProvider);
                if (!injectMembers.isEmpty())
                {
                    MemberNode memberNode = new MemberNode(elementUtils, typeUtils, this.getClassName(), returnType);
                    for (TypeElement injectMember : injectMembers)
                    {
                        TypeMirror typeMirror = injectMember.asType();
                        boolean provider = Utils.isProviderType(elementUtils, typeUtils, typeMirror);
                        typeMirror = Utils.getGenericTypeIfIsProvider(elementUtils, typeUtils, typeMirror);
                        ResultNode dependencies = this.findDependencies(topNode, typeMirror, provider, false);
                        memberNode.addResultNode(dependencies);
                        resultNode.setMemberNode(memberNode);
                    }
                    topNode.addResultNode(memberNode);
                }
                for (VariableElement parameter : parameters)
                {
                    TypeMirror typeMirror = parameter.asType();
                    boolean provider = Utils.isProviderType(elementUtils, typeUtils, typeMirror);
                    typeMirror = Utils.getGenericTypeIfIsProvider(elementUtils, typeUtils, typeMirror);
                    resultNode.addResultNode(this.findDependencies(topNode, typeMirror, provider, false));
                }
                resultNode.setDependentOnInjectConstructor(true);
                return resultNode;
            }
        }
        return null;
    }

    private ResultNode tryFindInDependentModules(TopNode topNode, TypeMirror theClass, boolean isProvider, boolean memberInjector)
    {
        this.log("trying to find dependencies for: " + theClass.toString() + " in modules");
        for (Dependency dependentModule : dependentModules)
        {
            for (Element element : dependentModule.getMethods())
            {
                if (element.getKind() == ElementKind.METHOD)
                {
                    ExecutableElement executableElement = (ExecutableElement) element;
                    if (executableElement.getReturnType().toString().equals(theClass.toString()))
                    {
                        this.log("found providing method for : " + theClass.toString() + " in modules");
                        List<? extends VariableElement> parameters = executableElement.getParameters();
                        if (parameters.isEmpty()) {
                            this.log("no parameters found in providing method for: " + theClass.toString());
                        }
                        List<TypeElement> injectMembers = ResultNode.searchForInjectMembers(elementUtils, theClass);
                        ResultNode resultNode = new ResultNode(elementUtils, typeUtils, this.getClassName(), theClass, dependentModule.getTypeElement(), executableElement);
                        resultNode.setIsProvider(isProvider);
                        if (!injectMembers.isEmpty())
                        {
                            MemberNode memberNode = new MemberNode(elementUtils, typeUtils, this.getClassName(), theClass);
                            for (TypeElement injectMember : injectMembers)
                            {
                                TypeMirror typeMirror = injectMember.asType();
                                boolean provider = Utils.isProviderType(elementUtils, typeUtils, typeMirror);
                                typeMirror = Utils.getGenericTypeIfIsProvider(elementUtils, typeUtils, typeMirror);
                                ResultNode dependencies = this.findDependencies(topNode, typeMirror, provider, false);
                                memberNode.addResultNode(dependencies);
                                resultNode.setMemberNode(memberNode);
                            }
                            topNode.addResultNode(memberNode);
                        }
                        for (VariableElement parameter : parameters)
                        {
                            TypeMirror typeMirror = parameter.asType();
                            boolean provider = Utils.isProviderType(elementUtils, typeUtils, typeMirror);
                            typeMirror = Utils.getGenericTypeIfIsProvider(elementUtils, typeUtils, typeMirror);
                            resultNode.addResultNode(this.findDependencies(topNode, typeMirror, provider, false));
                        }
                        resultNode.setDependentOnModule(true);
                        return resultNode;
                    }
                }
            }
        }
        return null;
    }

    private ResultNode tryFindInDependentComponent(TopNode topNode, TypeMirror theClass, boolean isProvider, boolean memberInjector)
    {
        this.log("trying to find dependencies for: " + theClass.toString() + " in dependent components");
        for (Dependency dependency : dependentComponents)
        {
            for (Element element : dependency.getMethods())
            {
                if (element.getKind() == ElementKind.METHOD)
                {
                    ExecutableElement executableElement = (ExecutableElement) element;
                    if (executableElement.getReturnType().toString().equals(theClass.toString()))
                    {
                        this.log("found " + theClass.toString() + " in dependent component");
                        List<TypeElement> injectMembers = ResultNode.searchForInjectMembers(elementUtils, theClass);
                        ResultNode resultNode = new ResultNode(elementUtils, typeUtils, this.getClassName(), theClass, dependency.getTypeElement(), executableElement);
                        resultNode.setIsProvider(isProvider);
                        if (!injectMembers.isEmpty())
                        {
                            MemberNode memberNode = new MemberNode(elementUtils, typeUtils, this.getClassName(), theClass);
                            for (TypeElement injectMember : injectMembers)
                            {
                                TypeMirror typeMirror = injectMember.asType();
                                boolean provider = Utils.isProviderType(elementUtils, typeUtils, typeMirror);
                                typeMirror = Utils.getGenericTypeIfIsProvider(elementUtils, typeUtils, typeMirror);
                                ResultNode dependencies = this.findDependencies(topNode, typeMirror, provider, false);
                                memberNode.addResultNode(dependencies);
                                resultNode.setMemberNode(memberNode);
                            }
                            topNode.addResultNode(memberNode);
                        }
                        resultNode.setDependentOnParentComponent(true);
                        return resultNode;
                    }
                }
            }
        }
        return null;
    }

    private String getClassName() {
        return Utils.extractPackage(component.asType()) + ".TestDagger" +  this.component.getSimpleName().toString();
    }

    private TypeElement findScopeOfElement(Element element) {
        List<? extends AnnotationMirror> annotationMirrors = element.getAnnotationMirrors();
        for (AnnotationMirror annotationMirror : annotationMirrors)
        {
            TypeElement annotation = elementUtils.getTypeElement(annotationMirror.getAnnotationType().toString());
            List<? extends AnnotationMirror> mirrors = annotation.getAnnotationMirrors();
            for (AnnotationMirror mirror : mirrors)
            {
                if (mirror.getAnnotationType().toString().equals("javax.inject.Scope")) {
                    return annotation;
                }
            }
        }
        return null;
    }

}
