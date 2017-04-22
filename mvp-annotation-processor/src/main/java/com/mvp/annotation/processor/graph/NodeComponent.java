package com.mvp.annotation.processor.graph;

import com.mvp.annotation.processor.Utils;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.inject.Inject;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
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

    private final PackageElement packageOf;
    private final ProcessingEnvironment processingEnvironment;
    private final Elements elementUtils;
    private final Types typeUtils;
    private final TypeElement component;
    private final List<ExecutableElement> allProvidingMethods;
    private final List<Dependency> dependentModules;
    private final List<Dependency> dependentComponents;

    private NodeComponent(PackageElement packageOf, ProcessingEnvironment processingEnvironment,
                          Elements elementUtils, Types typeUtils, TypeElement component,
                          List<ExecutableElement> allProvidingMethods, List<Dependency> dependentModules,
                          List<Dependency> dependentComponents)
    {
        this.packageOf = packageOf;
        this.processingEnvironment = processingEnvironment;
        this.elementUtils = elementUtils;
        this.typeUtils = typeUtils;
        this.component = component;
        this.allProvidingMethods = allProvidingMethods;
        this.dependentModules = dependentModules;
        this.dependentComponents = dependentComponents;
    }

    public static NodeComponent createFrom(PackageElement packageOf, ProcessingEnvironment processingEnvironment, Elements elementUtils, Types typeUtils, TypeElement component)
    {
        List<Dependency> dependentModules = getDependentModules(elementUtils, typeUtils, component);
        List<Dependency> dependentComponents = getDependentComponents(elementUtils, typeUtils, component);
        List<ExecutableElement> allProvidingMethods = getProvidingMethods(elementUtils, typeUtils, component);
        return new NodeComponent(packageOf, processingEnvironment, elementUtils, typeUtils, component, allProvidingMethods, dependentModules, dependentComponents);
    }

    private static List<ExecutableElement> getProvidingMethods(Elements elementUtils, Types typeUtils, TypeElement component)
    {
        List<ExecutableElement> allMethods = new ArrayList<>();
        List<? extends TypeMirror> typeMirrors = typeUtils.directSupertypes(component.asType());
        for (TypeMirror typeMirror : typeMirrors)
        {
            if (!typeMirror.toString().equals(Object.class.getName())) {
                allMethods.addAll(getProvidingMethods(elementUtils, typeUtils, elementUtils.getTypeElement(typeMirror.toString())));
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
                    topNode.addResultNode(this.findDependencies(returnType));
                }else if (returnType.toString().equals(void.class.getName()) && executableElement.getParameters().size() == 1) {
                    TypeMirror parameter = executableElement.getParameters().get(0).asType();
                    MemberNode resultNode = new MemberNode(elementUtils, typeUtils, this.getClassName(), parameter);
                    List<TypeElement> typeElements = resultNode.searchForInjectMembers(elementUtils);
                    for (TypeElement typeElement : typeElements)
                    {
                        resultNode.addResultNode(this.findDependencies(typeElement.asType()));
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

    private ResultNode findDependencies(TypeMirror returnType)
    {
        this.log("searching dependencies for: " + returnType.toString());
        ResultNode resultNode = this.tryFindInDependentComponent(returnType);
        if (resultNode != null) {
            return resultNode;
        }
        resultNode = this.tryFindInDependentModules(returnType);
        if (resultNode != null) {
            return resultNode;
        }
        resultNode = this.tryFindThroughInjectConstructor(returnType);
        if (resultNode != null) {
            return resultNode;
        }
        return null;
    }

    private ResultNode tryFindThroughInjectConstructor(TypeMirror returnType)
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
                ResultNode resultNode = new ResultNode(elementUtils, typeUtils, this.getClassName(), returnType, typeElement, executableElement);
                List<TypeElement> injectMembers = resultNode.searchForInjectMembers(elementUtils);
                for (TypeElement injectMember : injectMembers)
                {
                    resultNode.addResultNode(this.findDependencies(injectMember.asType()));
                }
                for (VariableElement parameter : parameters)
                {
                    resultNode.addResultNode(this.findDependencies(parameter.asType()));
                }
                resultNode.setDependentOnInjectConstructor(true);
                return resultNode;
            }
        }
        return null;
    }

    private ResultNode tryFindInDependentModules(TypeMirror theClass)
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
                        ResultNode resultNode = new ResultNode(elementUtils, typeUtils, this.getClassName(), theClass, dependentModule.getTypeElement(), executableElement);
                        List<TypeElement> injectMembers = resultNode.searchForInjectMembers(elementUtils);
                        for (TypeElement injectMember : injectMembers)
                        {
                            resultNode.addResultNode(this.findDependencies(injectMember.asType()));
                        }
                        for (VariableElement parameter : parameters)
                        {
                            resultNode.addResultNode(this.findDependencies(parameter.asType()));
                        }
                        resultNode.setDependentOnModule(true);
                        return resultNode;
                    }
                }
            }
        }
        return null;
    }

    private ResultNode tryFindInDependentComponent(TypeMirror theClass)
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
                        ResultNode resultNode = new ResultNode(elementUtils, typeUtils, this.getClassName(), theClass, dependency.getTypeElement(), executableElement);
                        List<TypeElement> injectMembers = resultNode.searchForInjectMembers(elementUtils);
                        for (TypeElement injectMember : injectMembers)
                        {
                            resultNode.addResultNode(this.findDependencies(injectMember.asType()));
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
