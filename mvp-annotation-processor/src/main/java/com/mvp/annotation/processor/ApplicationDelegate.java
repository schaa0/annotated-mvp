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
import javax.lang.model.element.AnnotationValue;
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
    private String prefix;
    private HashMap<String, ExecutableElement> moduleMethods;
    private HashMap<String, ExecutableElement> componentMethods;
    private Elements elementUtils;
    private HashMap<TypeMirror, ArrayList<ExecutableElement>> moduleToInstanceType = new HashMap<>();
    private HashMap<TypeMirror, ArrayList<TypeMirror>> componentsToInstanceType = new HashMap<>();
    private List<String> constructorInjectedDependencies = new ArrayList<>();
    public ApplicationDelegate(Filer filer, String packageName, Types typeUtils, Elements elementUtils, TypeElement provider, String prefix)
    {
        super(filer, packageName);
        this.typeUtils = typeUtils;
        this.elementUtils = elementUtils;
        this.provider = provider;
        this.prefix = prefix;
        moduleMethods = Utils.findProvidingMethodsOfModules(typeUtils, provider);
        componentMethods = Utils.findProvidingMethodsOfComponents(typeUtils, provider);
    }

    @Override
    protected TypeSpec.Builder build()
    {
        parseInstancesOfModules();
        parseInstancesOfComponents();
        HashMap<String, ExecutableElement> providingMethods = findProvidingMethods(typeUtils, provider);
        String delegateClass = prefix + provider.getSimpleName().toString();
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
                if (typeMirror.toString().equals("com.mvp.IMvpEventBus")){
                    continue;
                }
                String key = buildKey(method);
                if (!foundInstances.contains(key))
                {
                    List<TypeMirror> typeMirrors = searchForConstructorInjectedDependencies(method);
                    for (TypeMirror mirror : typeMirrors)
                    {
                        key = buildKey(method);
                        if (!foundInstances.contains(key))
                        {
                            constructorInjectedDependencies.add(mirror.toString());
                            foundInstances.add(addWithMethod(builder, delegateClass, method, mirror));
                        }
                    }
                    foundInstances.add(addWithMethod(builder, delegateClass, method, typeMirror));
                }
            }
        }

        for (Map.Entry<TypeMirror, ArrayList<ExecutableElement>> entry : moduleToInstanceType.entrySet())
        {
            TypeMirror module = entry.getKey();
            if (module.toString().equals("com.mvp.ModuleEventBus"))
                continue;
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
                name += getValueFromNamedAnnotation(method);
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
                        String parameterNameField = toParameterName(className) + getValueFromNamedAnnotation(method);
                        final String possible = String.format("%s.this.%s", delegateClass, parameterNameField);
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
                if (method.getReturnType().toString().equals(void.class.getName())){
                    methodBuilder.addStatement(String.format("component.%s(%s)", method.getSimpleName().toString(), params));
                    componentDelegateBuilder.addMethod(methodBuilder.build());
                    continue;
                }
                String key = buildKey(method);
                String name = toParameterName(ClassName.bestGuess(method.getReturnType().toString()));
                String parameterNameField = name + getValueFromNamedAnnotation(method);
                if (!foundInstances.contains(key))
                {
                    foundInstances.add(addWithMethod(builder, delegateClass, method, method.getReturnType()));
                }
                methodBuilder.beginControlFlow(String.format("if (%s.this.%s != null)", delegateClass, parameterNameField));
                methodBuilder.addStatement(String.format("return %s.this.%s", delegateClass, parameterNameField));
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
            List<? extends VariableElement> parameters1 = method.getParameters();
            methodBuilder.addModifiers(Modifier.PUBLIC);
            TypeMirror returnType = method.getReturnType();
            String s = typeUtils.asElement(returnType).getSimpleName().toString();
            ExecutableElement executableElement = providingMethods.get(returnType.toString());
            String params = "";
            List<? extends VariableElement> parameters = executableElement.getParameters();
            for (int i = 0; i < parameters.size(); i++)
            {
                boolean found = false;
                VariableElement parameter = parameters.get(i);
                TypeMirror typeMirror = parameter.asType();
                for (VariableElement variableElement : parameters1)
                {
                    TypeMirror variableType = variableElement.asType();
                    if (typeMirror.toString().equals(variableType.toString())) {
                        params += parameter.getSimpleName().toString();
                        if (i < parameters.size() - 1) params += ",";
                        found = true;
                        break;
                    }
                }
                if (found) {
                    continue;
                }
                ExecutableElement e = providingMethods.get(typeMirror.toString());
                if (e != null && !Utils.hasSubComponentAnnotation(typeUtils.asElement(e.getReturnType())))
                    params += "this." + e.getSimpleName().toString() + "()";
                else
                    params += parameter.getSimpleName().toString();
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
            if (method.getReturnType().toString().equals("com.mvp.ModuleEventBus"))
                continue;
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

    private String buildKey(ExecutableElement method)
    {
        String valueFromNamedAnnotation = getValueFromNamedAnnotation(method);
        ClassName className = ClassName.bestGuess(method.getReturnType().toString());
        String pre = (!valueFromNamedAnnotation.equals("")) ? "Named" : "";
        return "with" + pre + valueFromNamedAnnotation + toParameterName(className) + valueFromNamedAnnotation;
    }

    private String getValueFromNamedAnnotation(ExecutableElement executableElement)
    {
        AnnotationValue annotationValue = Utils.getAnnotationValue(executableElement, "javax.inject.Named", "value");
        return annotationValue == null || annotationValue.getValue() == null ? "" : annotationValue.getValue().toString();
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

    private String addWithMethod(TypeSpec.Builder builder, String delegateClass, ExecutableElement method, TypeMirror typeMirror)
    {
        ClassName theType = ClassName.bestGuess(typeMirror.toString());
        String valueFromNamedAnnotation = getValueFromNamedAnnotation(method);
        String name = toParameterName(theType) + valueFromNamedAnnotation;
        builder.addField(theType, name, Modifier.PRIVATE);
        String methodName = "with";
        methodName += (!valueFromNamedAnnotation.equals("")) ? "Named" + valueFromNamedAnnotation : "";
        builder.addMethod(MethodSpec.methodBuilder(methodName)
                                    .addModifiers(Modifier.PUBLIC)
                                    .addParameter(theType, name)
                                    .addStatement(String.format("this.%s = %s", name, name))
                                    .addStatement("return this")
                                    .returns(ClassName.bestGuess(getPackageName() + "." + delegateClass))
                                    .build()
        );

        return methodName + name;

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

    private List<ExecutableElement> findMethodsInComponent(TypeMirror component)
    {
        if (component.toString().equals(Object.class.getName()))
            return new ArrayList<>();
        List<ExecutableElement> methods = new ArrayList<>();
        TypeElement element = elementUtils.getTypeElement(component.toString());
        List<? extends TypeMirror> superTypes = typeUtils.directSupertypes(component);
        for (TypeMirror superType : superTypes)
        {
            List<ExecutableElement> methodsInComponent = findMethodsInComponent(superType);
            for (ExecutableElement executableElement : methodsInComponent)
            {
                if (!methods.contains(executableElement)) {
                    methods.add(executableElement);
                }
            }
        }
        List<? extends Element> enclosedElements = element.getEnclosedElements();
        for (Element enclosedElement : enclosedElements)
        {
            if (enclosedElement.getKind() == ElementKind.METHOD)
            {
                ExecutableElement executableElement = (ExecutableElement) enclosedElement;
                if (!methods.contains(executableElement))
                {
                    methods.add(executableElement);
                }

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

            List<? extends TypeMirror> superTypes = typeUtils.directSupertypes(element.asType());
            if (superTypes != null) {
                for (TypeMirror type : superTypes) {
                    if (type.toString().equals(Object.class.getName())) {
                        continue;
                    }
                    TypeElement typeElement = elementUtils.getTypeElement(type.toString());
                    if (typeElement != null && typeElement.getEnclosedElements() != null) {
                        for (Element t : typeElement.getEnclosedElements()) {
                            if (t.getKind() == ElementKind.METHOD) {
                                if (componentsToInstanceType.get(returnType) == null)
                                    componentsToInstanceType.put(returnType, new ArrayList<TypeMirror>());
                                componentsToInstanceType.get(returnType).add(((ExecutableElement) t).getReturnType());
                            }
                        }
                    }
                }
            }
            for (Element e : element.getEnclosedElements())
            {
                if (e.getKind() == ElementKind.METHOD)
                {
                    if (componentsToInstanceType.get(returnType) == null)
                        componentsToInstanceType.put(returnType, new ArrayList<TypeMirror>());
                    componentsToInstanceType.get(returnType).add(((ExecutableElement) e).getReturnType());
                }
            }
        }
    }

}
