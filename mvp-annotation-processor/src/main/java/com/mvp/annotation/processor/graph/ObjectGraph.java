package com.mvp.annotation.processor.graph;

import com.mvp.annotation.internal.*;
import com.mvp.annotation.processor.Utils;
import com.mvp.annotation.processor.graph.Node;
import com.mvp.annotation.processor.unittest.GraphCache;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

public class ObjectGraph
{

    private final ProcessingEnvironment processingEnvironment;
    private final Set<? extends Element> components;
    private final Elements elementUtils;
    private final Types typeUtils;

    private List<TopNode> topNodes = new ArrayList<>();
    private boolean isGenerated = false;

    public ObjectGraph(ProcessingEnvironment processingEnvironment, Set<? extends Element> components, Elements elementUtils, Types typeUtils) {
        this.processingEnvironment = processingEnvironment;
        this.components = components;
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
            if (!scopeToObjects.containsKey(scope))
            {
                scopeToObjects.put(scope, new ArrayList<TypeElement>());
            }
            for (Element element : this.components)
            {
                if (!Utils.hasComponentAnnotation(element)) {
                    List<TypeElement> objects = scopeToObjects.get(scope);
                    objects.add(elementUtils.getTypeElement(element.asType().toString()));
                }
            }
        }

        for (Element component : this.components)
        {
            NodeComponent nodeComponent = NodeComponent.createFrom(processingEnvironment, elementUtils, typeUtils, (TypeElement) component);
            TopNode topNode = nodeComponent.buildDependencyGraph();
            topNodes.add(topNode);
        }
    }

    private List<TypeElement> findAllScopes()
    {
        List<TypeElement> scopes = new ArrayList<>();

        for (Element element : this.components)
        {
            List<? extends AnnotationMirror> annotationMirrors = element.getAnnotationMirrors();
            for (AnnotationMirror annotationMirror : annotationMirrors)
            {
                TypeElement annotation = elementUtils.getTypeElement(annotationMirror.getAnnotationType().toString());
                List<? extends AnnotationMirror> mirrors = annotation.getAnnotationMirrors();
                for (AnnotationMirror mirror : mirrors)
                {
                    if (mirror.getAnnotationType().toString().equals("javax.inject.Scope")) {
                        if (!scopes.contains(annotation))
                        {
                            scopes.add(annotation);
                        }
                        break;
                    }
                }
            }
        }
        return scopes;
    }

    public void generate()
    {
        for (TopNode topNode : this.topNodes)
        {
            try {
                TypeElement componentType = topNode.getComponentType();
                PackageElement packageOf = elementUtils.getPackageOf(componentType);
                TypeSpec build = topNode.toClass().build();
                String packageName = packageOf.getQualifiedName().toString();
                JavaFile.builder(packageName, build)
                        .addFileComment("Generated code")
                        .indent("   ")
                        .build().writeTo(processingEnvironment.getFiler());
            } catch (IOException e) {

            }
            log(topNode.toString());
        }
        isGenerated = true;
    }

    public List<TypeMirror> getCreatedInterfaces()
    {
        List<TypeMirror> result = new ArrayList<>();
        for (TopNode topNode : this.topNodes)
        {
            result.addAll(topNode.getGeneratedInterfaceTypes());
        }
        return result;
    }

    public boolean isGenerated()
    {
        return this.isGenerated;
    }

    public List<TopNode> getTopNodes()
    {
        return this.topNodes;
    }

    public static Set<? extends Element> createFromAnnotation(Elements elementUtils, AnnotationValue graph, AnnotationValue views) {

        Set<Element> topNodes = new HashSet<>();

        AnnotationMirror mirror = (AnnotationMirror) graph.getValue();
        Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = mirror.getElementValues();
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : elementValues.entrySet())
        {
            String key = entry.getKey().getSimpleName().toString();
            if (key.equals("nodes")) {
                AnnotationValue nodes = entry.getValue();
                List<AnnotationValue> value = (List<AnnotationValue>) nodes.getValue();
                for (AnnotationValue annotationValue : value)
                {
                    AnnotationMirror e = (AnnotationMirror) annotationValue.getValue();
                    Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues1 = e.getElementValues();
                    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> nodeEntry : elementValues1.entrySet())
                    {
                        String nodeKey = nodeEntry.getKey().getSimpleName().toString();
                        if (nodeKey.equals("componentType")) {
                            String componentType = String.valueOf(nodeEntry.getValue().getValue()).replace(".class", "");
                            topNodes.add(elementUtils.getTypeElement(componentType));
                        }
                    }
                }
            }
        }

        /*List<Object> value = (List<Object>) views.getValue();
        for (Object o : value)
        {
            String viewClass = String.valueOf(o).replace(".class", "");
            TypeElement viewElement = elementUtils.getTypeElement(viewClass);
            TypeMirror presenter = Utils.findPresenterClassInViewImplementationClass(viewElement);
            String packageName = Utils.extractPackage(presenter);
            String className = Utils.extractClassName(presenter);
            topNodes.add(elementUtils.getTypeElement(packageName + ".Component" + className));
        }*/

        return topNodes;
    }

}
