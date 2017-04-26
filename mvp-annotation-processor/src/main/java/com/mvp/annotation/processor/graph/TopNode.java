package com.mvp.annotation.processor.graph;

import com.mvp.annotation.processor.Utils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Generated;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

public class TopNode
{

    private final Elements elements;
    private final TypeElement component;
    private final List<ExecutableElement> allProvidingMethods;
    private final List<Dependency> dependentComponents;
    private List<ResultNode> resultNodes = new ArrayList<>();

    private List<String> createdProviders = new ArrayList<>();
    private List<String> constructedProviders = new ArrayList<>();
    private List<String> createdDelegates = new ArrayList<>();
    private TypeSpec.Builder classBuilder;

    public TopNode(Elements elements, TypeElement component, List<ExecutableElement> allProvidingMethods, List<Dependency> dependentComponents) {
        this.elements = elements;
        this.component = component;
        this.allProvidingMethods = allProvidingMethods;
        this.dependentComponents = dependentComponents;
    }

    public void addResultNode(ResultNode resultNode)
    {
        if (resultNode != null)
        {
            this.resultNodes.add(resultNode);
        }
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Objectgraph for component: ").append(component.toString()).append("\n");
        for (ResultNode resultNode : resultNodes)
        {
            sb.append(resultNode.toString()).append("\n");
        }
        return sb.toString();
    }

    public List<ResultNode> getResultNodes()
    {
        return this.resultNodes;
    }

    public TypeSpec.Builder toClass() {

        List<String> createdClasses = new ArrayList<>();
        List<String> createdInterfaces = new ArrayList<>();

        String componentName = this.getDaggerClassName();
        classBuilder = TypeSpec.classBuilder(componentName);
        classBuilder.addModifiers(Modifier.PUBLIC);
        classBuilder.addSuperinterface(ClassName.get(component));

        ClassName componentClassName = ClassName.bestGuess(this.getDaggerClassName());

        for (Dependency dependency : dependentComponents)
        {
            String name = Utils.toParameterName(Utils.extractClassName(dependency.getTypeElement().asType()));
            classBuilder.addField(ClassName.get(dependency.getTypeElement()), name);
            classBuilder.addMethod(MethodSpec.methodBuilder(name)
                                             .addModifiers(Modifier.PUBLIC)
                                             .addParameter(ClassName.get(dependency.getTypeElement().asType()), name)
                                             .addStatement(String.format("this.%s = %s", name, name))
                                             .addStatement("return this")
                                             .returns(componentClassName)
                                             .build());
        }

        Set<TypeElement> modules = new HashSet<>();

        for (ResultNode resultNode : resultNodes)
        {
            resultNode.buildDelegateInterface(classBuilder, createdInterfaces);

            resultNode.findAndAddModules(modules);

            resultNode.buildProviderField(this, createdProviders);
            if (!resultNode.isDependentOnParentComponent()) {
                resultNode.buildDelegateField(this, createdDelegates);
                resultNode.buildWithDelegateMethod(this, createdDelegates);
            }
            resultNode.toClass(classBuilder, createdClasses);
        }

        for (TypeElement module : modules)
        {
            String name = Utils.toParameterName(Utils.extractClassName(module.asType()));
            classBuilder.addField(ClassName.get(module), name);
            classBuilder.addMethod(MethodSpec.methodBuilder(name)
                                             .addModifiers(Modifier.PUBLIC)
                                             .addParameter(ClassName.get(module.asType()), name)
                                             .addStatement(String.format("this.%s = %s", name, name))
                                             .addStatement("return this")
                                             .returns(componentClassName)
                                             .build());
        }

        MethodSpec.Builder initializeMethod = MethodSpec.methodBuilder("initialize")
                                                  .addModifiers(Modifier.PRIVATE)
                                                  .returns(void.class);

        classBuilder.addMethod(MethodSpec.methodBuilder("build")
                                         .addModifiers(Modifier.PUBLIC)
                                         .returns(componentClassName)
                                         .addStatement("this.initialize()")
                                         .addStatement("return this")
                                         .build());

        for (ExecutableElement method : allProvidingMethods)
        {
            for (ResultNode resultNode : resultNodes)
            {
                if (resultNode.tryOverrideComponentMethod(elements, classBuilder, method))
                {
                    break;
                }
            }
        }

        for (ResultNode resultNode : resultNodes)
        {
            resultNode.buildProviders(initializeMethod, constructedProviders);
        }

        classBuilder.addMethod(initializeMethod.build());

        return classBuilder;
    }

    public TypeElement getComponentType()
    {
        return this.component;
    }

    public TypeSpec.Builder getClassBuilder()
    {
        return this.classBuilder;
    }

    public String getDaggerClassName()
    {
        String componentName = "TestDagger" + component.getSimpleName().toString();
        return componentName;
    }

    public List<TypeMirror> getGeneratedInterfaceTypes()
    {
        List<TypeMirror> generatedInterfaceTypes = new ArrayList<>();
        for (ResultNode resultNode : resultNodes)
        {
            generatedInterfaceTypes.addAll(resultNode.getGeneratedInterfaceType());
        }
        return generatedInterfaceTypes;
    }

    public String toAnnotation()
    {
        String myId = UUID.randomUUID().toString();
        StringBuilder sb = new StringBuilder();
        int size = this.resultNodes.size();
        for (int position = 0; position < size; position++)
        {
            ResultNode resultNode = this.resultNodes.get(position);
            String s = resultNode.toAnnotation(UUID.randomUUID().toString(), "");
            sb.append(s);
            if (position < size - 1) {
                sb.append(", ");
            }
        }
        String dc = this.dc();
        String annotation = "@com.mvp.annotation.internal.TopNode(id = \"%s\", componentType = %s, dependentComponents = %s, nodes = {%s})";
        String component = this.getComponentType().toString() + ".class";
        return String.format(annotation, myId, component, dc, sb.toString());
    }

    private String dc()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("{ ");
        for (int position = 0; position < this.dependentComponents.size(); position++)
        {
            Dependency dc = this.dependentComponents.get(position);
            sb.append(dc.getTypeElement().toString()).append(".class");
            if (position < this.dependentComponents.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(" }");
        return sb.toString();
    }

    public static TopNode createFromAnnotation(Elements elements, String topNodeId, AnnotationMirror nodes, String componentType, List<Object> dependentComponents)
    {
        TypeElement typeElement = elements.getTypeElement(componentType);
       // TopNode topNode = new TopNode(elements, typeElement, )
        return null;
    }
}
