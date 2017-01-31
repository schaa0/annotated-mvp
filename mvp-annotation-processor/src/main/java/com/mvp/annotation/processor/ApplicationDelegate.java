package com.mvp.annotation.processor;

import com.mvp.annotation.processor.unittest.AbsGeneratingType;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static com.mvp.annotation.processor.Utils.findProvidingMethods;
import static com.mvp.annotation.processor.Utils.hasInjectAnnotation;
import static com.mvp.annotation.processor.Utils.hasProvidesAnnotation;
import static com.mvp.annotation.processor.Utils.override;
import static com.mvp.annotation.processor.Utils.overrideConstructor;
import static com.mvp.annotation.processor.Utils.toParameterName;

/**
 * Created by Andy on 26.01.2017.
 */

public class ApplicationDelegate extends AbsGeneratingType
{

    private Types typeUtils;
    private TypeElement provider;
    private HashMap<String, ExecutableElement> moduleMethods;
    private HashMap<String, ExecutableElement> componentMethods;
    private Elements elementUtils;
    private HashMap<TypeMirror, ArrayList<ExecutableElement>> moduleToInstanceType = new HashMap<>();
    private HashMap<TypeMirror, ArrayList<TypeMirror>> componentsToInstanceType = new HashMap<>();
    private List<String> constructorInjectedDependencies = new ArrayList<>();
    public ApplicationDelegate(Filer filer, String packageName, Types typeUtils, Elements elementUtils, TypeElement provider)
    {
        super(filer, packageName);
        this.typeUtils = typeUtils;
        this.elementUtils = elementUtils;
        this.provider = provider;
        moduleMethods = Utils.findProvidingMethodsOfModules(typeUtils, provider);
        componentMethods = Utils.findProvidingMethodsOfComponents(typeUtils, provider);
    }

    @Override
    protected TypeSpec.Builder build()
    {
        parseInstancesOfModules();
        parseInstancesOfComponents();
        HashMap<String, ExecutableElement> providingMethods = findProvidingMethods(typeUtils, provider);
        String delegateClass = provider.getSimpleName().toString() + "Delegate";
        TypeSpec.Builder builder = TypeSpec.classBuilder(delegateClass);
        builder.superclass(ClassName.get(provider));
        builder.addModifiers(Modifier.PUBLIC);

        List<String> foundInstances = new ArrayList<>();

        HashMap<TypeMirror, ExecutableElement> moduleConstructorParams = new HashMap<>();
        for (Map.Entry<TypeMirror, ArrayList<ExecutableElement>> entry : moduleToInstanceType.entrySet())
        {
            for (ExecutableElement method : entry.getValue())
            {

                TypeMirror typeMirror = method.getReturnType();
                if (!foundInstances.contains(typeMirror.toString()))
                {
                    List<TypeMirror> typeMirrors = searchForConstructorInjectedDependencies(method);
                    addWithMethod(builder, delegateClass, typeMirror);
                    for (TypeMirror mirror : typeMirrors)
                    {
                        if (!foundInstances.contains(mirror.toString()))
                        {
                            constructorInjectedDependencies.add(mirror.toString());
                            addWithMethod(builder, delegateClass, mirror);
                            foundInstances.add(mirror.toString());
                        }
                    }
                    foundInstances.add(typeMirror.toString());
                }
            }
        }

        for (Map.Entry<TypeMirror, ArrayList<ExecutableElement>> entry : moduleToInstanceType.entrySet())
        {
            TypeMirror module = entry.getKey();
            List<ExecutableElement> methods = findMethodsInModule(module);
            List<ExecutableElement> constructors = findConstructors(module);
            String moduleSimpleClassName = typeUtils.asElement(module).getSimpleName().toString();
            TypeSpec.Builder moduleDelegateBuilder = TypeSpec.classBuilder(moduleSimpleClassName + "Delegate");
            moduleDelegateBuilder.addModifiers(Modifier.PUBLIC);
            moduleDelegateBuilder.superclass(ClassName.get(module));
            ExecutableElement c = null;
            for (ExecutableElement constructor : constructors)
            {
                MethodSpec.Builder constructorBuilder = overrideConstructor(constructor);
                List<? extends VariableElement> parameters = constructor.getParameters();
                if (!moduleSimpleClassName.equals("ModuleEventBus") && !parameters.isEmpty())
                    c = constructor;
                String superCall = "";
                for (int position = 0; position < parameters.size(); position++)
                {
                    VariableElement parameter = parameters.get(position);
                    superCall += parameter.getSimpleName().toString();
                    if (position < parameters.size() - 1) superCall += ", ";
                }
                constructorBuilder.addStatement(String.format("super(%s)", superCall));
                moduleDelegateBuilder.addMethod(constructorBuilder.build());
            }
            if (c != null) moduleConstructorParams.put(module, c);
            for (ExecutableElement method : methods)
            {
                MethodSpec.Builder methodBuilder = override(method);
                String name = toParameterName(ClassName.bestGuess(method.getReturnType().toString()));
                methodBuilder.addModifiers(Modifier.PUBLIC);
                String params = "";
                List<? extends VariableElement> parameters = method.getParameters();
                int size = parameters.size();
                for (int position = 0; position < size; position++)
                {
                    VariableElement variableElement = parameters.get(position);
                    TypeMirror typeMirror = variableElement.asType();
                    if (constructorInjectedDependencies.contains(typeMirror.toString())){
                        ClassName className = ClassName.bestGuess(typeMirror.toString());
                        String parameterName = "ref" + className.simpleName();
                        final String possible = String.format("%s.this.%s", delegateClass, toParameterName(className));
                        String s = className.simpleName();
                        methodBuilder.addStatement(String.format("final %s %s;", s, parameterName));
                        methodBuilder.beginControlFlow(String.format("if (%s != null)", possible));
                        methodBuilder.addStatement(String.format("%s = %s", parameterName, possible));
                        methodBuilder.nextControlFlow("else");
                        methodBuilder.addStatement(String.format("%s = %s", parameterName, variableElement.getSimpleName().toString()));
                        methodBuilder.endControlFlow();
                        params += parameterName;
                    }else{
                        params += variableElement.getSimpleName().toString();
                    }
                    if (position < size - 1)
                        params += ", ";
                }
                methodBuilder.beginControlFlow(String.format("if (%s.this.%s != null)", delegateClass, name));
                methodBuilder.addStatement(String.format("return %s.this.%s", delegateClass, name));
                methodBuilder.nextControlFlow("else");
                methodBuilder.addStatement(String.format("return super.%s(%s)", method.getSimpleName().toString(), params));
                methodBuilder.endControlFlow();
                moduleDelegateBuilder.addMethod(methodBuilder.build());
            }

            builder.addType(moduleDelegateBuilder.build());
        }

        for (Map.Entry<TypeMirror, ArrayList<TypeMirror>> entry : componentsToInstanceType.entrySet())
        {
            for (TypeMirror typeMirror : entry.getValue())
            {
                if (!foundInstances.contains(typeMirror.toString()))
                {
                    addWithMethod(builder, delegateClass, typeMirror);
                    foundInstances.add(typeMirror.toString());
                }
            }
            TypeMirror component = entry.getKey();
            List<ExecutableElement> methods = findMethodsInComponent(component);
            String moduleComponentClassName = typeUtils.asElement(component).getSimpleName().toString();
            TypeSpec.Builder componentDelegateBuilder = TypeSpec.classBuilder(moduleComponentClassName + "Delegate");
            componentDelegateBuilder.addModifiers(Modifier.PUBLIC);
            componentDelegateBuilder.addSuperinterface(ClassName.get(component));
            componentDelegateBuilder.addField(ClassName.get(component), "component", Modifier.PRIVATE);
            componentDelegateBuilder.addMethod(MethodSpec.constructorBuilder()
                        .addParameter(ClassName.get(component), "component")
                        .addStatement("this.component = component")
                        .build());
            for (ExecutableElement method : methods)
            {
                MethodSpec.Builder methodBuilder = override(method);
                String name = toParameterName(ClassName.bestGuess(method.getReturnType().toString()));
                methodBuilder.addModifiers(Modifier.PUBLIC);
                String params = "";
                List<? extends VariableElement> parameters = method.getParameters();
                int size = parameters.size();
                for (int position = 0; position < size; position++)
                {
                    VariableElement variableElement = parameters.get(position);
                    params += variableElement.getSimpleName().toString();
                    if (position < size - 1)
                        params += ", ";
                }
                methodBuilder.beginControlFlow(String.format("if (%s.this.%s != null)", delegateClass, name));
                methodBuilder.addStatement(String.format("return %s.this.%s", delegateClass, name));
                methodBuilder.nextControlFlow("else");
                methodBuilder.addStatement(String.format("return this.component.%s(%s)", method.getSimpleName().toString(), params));
                methodBuilder.endControlFlow();
                componentDelegateBuilder.addMethod(methodBuilder.build());
            }
            builder.addType(componentDelegateBuilder.build());
        }

        List<ExecutableElement> m = new ArrayList<>(new HashSet<>(componentMethods.values()));

        for (ExecutableElement method : m)
        {
            MethodSpec.Builder methodBuilder = override(method);
            methodBuilder.addModifiers(Modifier.PUBLIC);
            TypeMirror returnType = method.getReturnType();
            String s = typeUtils.asElement(returnType).getSimpleName().toString();
            ExecutableElement executableElement = providingMethods.get(returnType.toString());
            String params = "";
            List<? extends VariableElement> parameters = executableElement.getParameters();
            for (int i = 0; i < parameters.size(); i++)
            {
                VariableElement parameter = parameters.get(i);
                TypeMirror typeMirror = parameter.asType();
                ExecutableElement e = providingMethods.get(typeMirror.toString());
                params += "this." + e.getSimpleName().toString() + "()";
                if (i < parameters.size() - 1) params += ",";
            }
            methodBuilder.addStatement(String.format("return new %sDelegate(super.%s(%s))", s, executableElement.getSimpleName().toString(), params));
            builder.addMethod(methodBuilder.build());
        }

        m = new ArrayList<>(new HashSet<>(moduleMethods.values()));
        for (ExecutableElement method : m)
        {
            MethodSpec.Builder methodBuilder = override(method);
            methodBuilder.addModifiers(Modifier.PUBLIC);
            TypeMirror returnType = method.getReturnType();
            String s = typeUtils.asElement(returnType).getSimpleName().toString();
            ExecutableElement constructor = moduleConstructorParams.get(returnType);
            HashMap<TypeMirror, String> variableNames = new HashMap<>();
            if (constructor != null)
            {
                for (VariableElement variableElement : constructor.getParameters())
                {
                    variableNames.put(variableElement.asType(), variableElement.getSimpleName().toString());
                }
            }
            for (VariableElement variableElement : method.getParameters())
            {
                variableNames.put(variableElement.asType(), variableElement.getSimpleName().toString());
            }
            String p = "";
            if (constructor != null)
            {
                List<? extends VariableElement> parameters = constructor.getParameters();
                for (int position = 0; position < parameters.size(); position++)
                {
                    TypeMirror type = parameters.get(position).asType();
                    String name;
                    if (type.toString().equals(elementUtils.getTypeElement("android.content.Context").asType().toString()))
                    {
                        name = "getApplicationContext()";
                    } else
                    {
                        name = variableNames.get(type);
                    }
                    p += name;
                    if (position < parameters.size() - 1) p += ", ";
                }
            }
            methodBuilder.addStatement(String.format("return new %sDelegate(%s)", s, p));
            builder.addMethod(methodBuilder.build());
        }

        try
        {
            Class.forName("android.support.test.InstrumentationRegistry");
            builder.addMethod(MethodSpec.methodBuilder("apply")
                                        .addModifiers(Modifier.PUBLIC)
                                        .addStatement("$T.getInstrumentation().callApplicationOnCreate(this)", ClassName.bestGuess("android.support.test.InstrumentationRegistry"))
                                        .returns(void.class)
                                        .build());
        }catch(Exception e){

        }

        return builder;
    }

    private void addWithMethod(TypeSpec.Builder builder, String delegateClass, TypeMirror typeMirror)
    {
        ClassName module = ClassName.bestGuess(typeMirror.toString());
        String name = toParameterName(module);
        builder.addField(module, name, Modifier.PRIVATE);
        builder.addMethod(MethodSpec.methodBuilder("with")
                                    .addModifiers(Modifier.PUBLIC)
                                    .addParameter(module, name)
                                    .addStatement(String.format("this.%s = %s", name, name))
                                    .addStatement("return this")
                                    .returns(ClassName.bestGuess(getPackageName() + "." + delegateClass))
                                    .build()
        );
    }

    private List<TypeMirror> searchForConstructorInjectedDependencies(ExecutableElement providingMethod)
    {
        List<TypeMirror> typeMirrors = new ArrayList<>();
        if (providingMethod != null)
        {
            List<? extends VariableElement> parameters = providingMethod.getParameters();
            for (VariableElement parameter : parameters)
            {
                TypeMirror param = parameter.asType();
                TypeElement typeElement = elementUtils.getTypeElement(param.toString());
                ExecutableElement constructor = getAtInjectConstructor(typeElement);
                if (constructor != null){
                    /*for (VariableElement variableElement : constructor.getParameters())
                    {
                        typeMirrors.addAll(searchForConstructorInjectedDependencies(variableElement.asType()));
                    }*/
                    typeMirrors.add(parameter.asType());
                }
            }
        }
        return typeMirrors;
    }

    private ExecutableElement getAtInjectConstructor(TypeElement typeElement)
    {
        for (Element element : typeElement.getEnclosedElements())
        {
            if (element.getKind() == ElementKind.CONSTRUCTOR){
                ExecutableElement e = (ExecutableElement) element;
                if (hasInjectAnnotation(e))
                    return e;
            }
        }
        return null;
    }

    private List<ExecutableElement> findMethodsInComponent(TypeMirror module)
    {
        if (module.toString().equals(Object.class.getName()))
            return new ArrayList<>();
        List<ExecutableElement> methods = new ArrayList<>();
        TypeElement element = elementUtils.getTypeElement(module.toString());
        if (!Utils.hasComponentAnnotation(element)){
            return new ArrayList<>();
        }
        List<? extends TypeMirror> superTypes = typeUtils.directSupertypes(module);
        for (TypeMirror superType : superTypes)
        {
            methods.addAll(findMethodsInComponent(superType));
        }
        List<? extends Element> enclosedElements = element.getEnclosedElements();
        for (Element enclosedElement : enclosedElements)
        {
            if (enclosedElement.getKind() == ElementKind.METHOD)
            {
                ExecutableElement executableElement = (ExecutableElement) enclosedElement;
                if (!executableElement.getReturnType().toString().equals(void.class.getName()))
                    methods.add((ExecutableElement) enclosedElement);
            }
        }
        return methods;
    }

    private List<ExecutableElement> findConstructors(TypeMirror module)
    {
        List<ExecutableElement> methods = new ArrayList<>();
        Element element = typeUtils.asElement(module);
        List<? extends Element> enclosedElements = element.getEnclosedElements();
        for (Element enclosedElement : enclosedElements)
        {
            if (enclosedElement.getKind() == ElementKind.CONSTRUCTOR)
            {
                methods.add((ExecutableElement) enclosedElement);
            }
        }
        return methods;
    }

    private List<ExecutableElement> findMethodsInModule(TypeMirror module)
    {
        if (module.toString().equals(Object.class.toString()))
            return new ArrayList<>();
        List<ExecutableElement> methods = new ArrayList<>();
        Element element = typeUtils.asElement(module);
        List<? extends TypeMirror> superTypes = typeUtils.directSupertypes(module);
        for (TypeMirror superType : superTypes)
        {
            methods.addAll(findMethodsInModule(superType));
        }
        List<? extends Element> enclosedElements = element.getEnclosedElements();
        for (Element enclosedElement : enclosedElements)
        {
            if (enclosedElement.getKind() == ElementKind.METHOD && Utils.hasProvidesAnnotation(enclosedElement))
            {
                methods.add((ExecutableElement) enclosedElement);
            }
        }
        return methods;
    }

    private void parseInstancesOfModules()
    {
        moduleToInstanceType = new HashMap<>();
        for (Map.Entry<String, ExecutableElement> entry : moduleMethods.entrySet())
        {
            TypeMirror returnType = entry.getValue().getReturnType();
            TypeElement element = elementUtils.getTypeElement(returnType.toString());
            for (Element e : element.getEnclosedElements())
            {
                if (e.getKind() == ElementKind.METHOD && hasProvidesAnnotation(e))
                {
                    if (moduleToInstanceType.get(returnType) == null)
                        moduleToInstanceType.put(returnType, new ArrayList<ExecutableElement>());
                    moduleToInstanceType.get(returnType).add(((ExecutableElement) e));
                }
            }
        }
    }

    private void parseInstancesOfComponents()
    {
        componentsToInstanceType = new HashMap<>();
        for (Map.Entry<String, ExecutableElement> entry : componentMethods.entrySet())
        {
            ExecutableElement method = entry.getValue();
            TypeMirror returnType = method.getReturnType();
            TypeElement element = elementUtils.getTypeElement(returnType.toString());
            for (Element e : element.getEnclosedElements())
            {
                if (e.getKind() == ElementKind.METHOD && !returnType.toString().equals(void.class.toString()))
                {
                    if (componentsToInstanceType.get(returnType) == null)
                        componentsToInstanceType.put(returnType, new ArrayList<TypeMirror>());
                    componentsToInstanceType.get(returnType).add(((ExecutableElement) e).getReturnType());
                }
            }
        }
    }

}
