package com.mvp.annotation.processor.graph;

import com.mvp.annotation.processor.Utils;
import com.mvp.annotation.processor.graph.Node;
import com.squareup.javapoet.JavaFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
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

public class ObjectGraph
{

    private final ProcessingEnvironment processingEnvironment;
    private final Elements elementUtils;
    private final Types typeUtils;
    private final RoundEnvironment env;

    private List<TopNode> topNodes = new ArrayList<>();

    public ObjectGraph(ProcessingEnvironment processingEnvironment, RoundEnvironment env, Elements elementUtils, Types typeUtils) {
        this.processingEnvironment = processingEnvironment;
        this.env = env;
        this.elementUtils = elementUtils;
        this.typeUtils = typeUtils;
    }

    private void log(String message) {
        processingEnvironment.getMessager().printMessage(Diagnostic.Kind.NOTE, message);
    }


    public void evaluate() {

        List<TypeElement> scopes = findAllScopes();

        HashMap<TypeElement, List<TypeElement>> scopeToObjects = new HashMap<>();

        for (TypeElement scope : scopes)
        {
            scopeToObjects.put(scope, new ArrayList<TypeElement>());
            Set<? extends Element> elements = env.getElementsAnnotatedWith(scope);
            for (Element element : elements)
            {
                if (!Utils.hasComponentAnnotation(element)) {
                    List<TypeElement> objects = scopeToObjects.get(scope);
                    objects.add(elementUtils.getTypeElement(element.asType().toString()));
                }
            }
        }

        TypeElement componentAnnotation = elementUtils.getTypeElement("dagger.Component");
        Set<? extends Element> components = env.getElementsAnnotatedWith(componentAnnotation);
        for (Element component : components)
        {
            PackageElement packageOf = elementUtils.getPackageOf(component);
            NodeComponent nodeComponent = NodeComponent.createFrom(packageOf, processingEnvironment, elementUtils, typeUtils, (TypeElement) component);
            TopNode topNode = nodeComponent.buildDependencyGraph();
            topNodes.add(topNode);
        }
    }

    private List<TypeElement> findAllScopes()
    {
        List<TypeElement> scopes = new ArrayList<>();

        TypeElement componentAnnotation = elementUtils.getTypeElement("dagger.Component");
        Set<? extends Element> elements = env.getElementsAnnotatedWith(componentAnnotation);
        for (Element element : elements)
        {
            List<? extends AnnotationMirror> annotationMirrors = element.getAnnotationMirrors();
            for (AnnotationMirror annotationMirror : annotationMirrors)
            {
                TypeElement annotation = elementUtils.getTypeElement(annotationMirror.getAnnotationType().toString());
                List<? extends AnnotationMirror> mirrors = annotation.getAnnotationMirrors();
                for (AnnotationMirror mirror : mirrors)
                {
                    if (mirror.getAnnotationType().toString().equals("javax.inject.Scope")) {
                        scopes.add(annotation);
                        break;
                    }
                }
            }
        }
        return scopes;
    }

    public List<String> generate()
    {
        List<String> delegates = new ArrayList<>();
        for (TopNode topNode : this.topNodes)
        {
            try {
                PackageElement packageOf = elementUtils.getPackageOf(topNode.getComponentType());
                JavaFile.builder(packageOf.getQualifiedName().toString(), topNode.toClass().build())
                        .addFileComment("Generated code")
                        .indent("   ")
                        .build().writeTo(processingEnvironment.getFiler());
            } catch (IOException e) {
                //e.printStackTrace();
            }
            log(topNode.toString());
            delegates.addAll(topNode.getCreatedDelegates());
        }
        return delegates;
    }
}
