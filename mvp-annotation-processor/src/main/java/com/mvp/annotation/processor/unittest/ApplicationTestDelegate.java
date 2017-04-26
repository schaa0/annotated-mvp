package com.mvp.annotation.processor.unittest;

import com.mvp.annotation.processor.Utils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.HashMap;
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

import dagger.Component;
import dagger.Module;

import static com.mvp.annotation.processor.Utils.findProvidingMethods;

/**
 * Created by Andy on 22.04.2017.
 */

class ApplicationTestDelegate extends AbsGeneratingType
{
    private final List<TypeMirror> createdInterfaces;
    private Types typeUtils;
    private TypeElement provider;
    private String prefix;
    private HashMap<String, ExecutableElement> componentMethods;
    private Elements elementUtils;

    public ApplicationTestDelegate(Filer filer, String packageName, Types typeUtils, Elements elementUtils, TypeElement provider, String prefix, List<TypeMirror> createdInterfaces)
    {
        super(filer, packageName);
        this.typeUtils = typeUtils;
        this.elementUtils = elementUtils;
        this.provider = provider;
        this.prefix = prefix;
        this.createdInterfaces = createdInterfaces;
        this.componentMethods = Utils.findProvidingMethodsOfComponents(typeUtils, provider);
    }

    @Override
    protected TypeSpec.Builder build()
    {
        HashMap<String, ExecutableElement> providingMethods = findProvidingMethods(typeUtils, provider);
        String delegateClass = this.getClassName();
        TypeSpec.Builder builder = TypeSpec.classBuilder(delegateClass);
        builder.superclass(ClassName.get(provider));
        builder.addModifiers(Modifier.PUBLIC);

        for (Map.Entry<String, ExecutableElement> entry : componentMethods.entrySet())
        {
            ExecutableElement method = entry.getValue();
            MethodSpec.Builder overridenMethod = Utils.override(method);
            List<? extends VariableElement> parameters = method.getParameters();
            HashMap<String, VariableElement> typeToParam = new HashMap<>();
            for (VariableElement parameter : parameters)
            {
                typeToParam.put(parameter.asType().toString(), parameter);
            }
            String className = Utils.extractPackage(method.getReturnType()) + ".TestDagger" + Utils.extractClassName(method.getReturnType());
            TypeElement testComponent = elementUtils.getTypeElement(className);
            List<ExecutableElement> methodsOfTestComponent = new ArrayList<>();
            for (Element element : testComponent.getEnclosedElements())
            {
                if (element.getKind() == ElementKind.METHOD)
                {
                    ExecutableElement executableElement = (ExecutableElement) element;
                    if (executableElement.getParameters().isEmpty())
                    {
                        continue;
                    }
                    TypeMirror param = executableElement.getParameters().get(0).asType();
                    Element paramElement = typeUtils.asElement(param);
                    if (paramElement.getAnnotation(Module.class) != null || paramElement.getAnnotation(Component.class) != null)
                    {
                        methodsOfTestComponent.add(executableElement);
                    }
                }
            }
            overridenMethod.addCode("return new $T()\n", testComponent);
            for (ExecutableElement executableElement : methodsOfTestComponent)
            {
                TypeMirror returnType = executableElement.getParameters().get(0).asType();
                VariableElement variableElement = typeToParam.get(returnType.toString());
                ExecutableElement providingMethod = providingMethods.get(returnType.toString());
                String paramName;
                boolean isMethod = false;
                if (variableElement != null)
                {
                    paramName = variableElement.getSimpleName().toString();
                } else
                {
                    paramName = "this." + providingMethod.getSimpleName().toString() + "(";
                    isMethod = true;
                }
                String methodName = Utils.toParameterName(Utils.extractClassName(returnType));
                StringBuilder sb = new StringBuilder();
                int size = providingMethod.getParameters().size();
                for (int i = 0; i < size; i++)
                {
                    String o = providingMethod.getParameters().get(i).asType().toString();
                    ExecutableElement executableElement1 = providingMethods.get(o);
                    if (typeToParam.get(o) != null)
                    {
                        VariableElement param = typeToParam.get(o);
                        sb.append(param.getSimpleName().toString());
                    } else if (executableElement1 != null)
                    {
                        sb.append(String.format("this.%s()", executableElement1.getSimpleName().toString()));
                    }
                    if (executableElement1 != null || typeToParam.get(o) != null)
                    {
                        if (i < size - 1)
                        {
                            sb.append(", ");
                        }
                    }
                }
                if (isMethod)
                {
                    sb.append(")");
                }
                overridenMethod.addCode(String.format(".%s(%s%s)\n", methodName, paramName, sb.toString()));
            }
            List<String> created = new ArrayList<>();
            for (TypeMirror createdInterface : createdInterfaces)
            {
                String classNameString = createdInterface.toString();
                if (!created.contains(classNameString))
                {
                    TypeElement element = (TypeElement) typeUtils.asElement(createdInterface);
                    for (Element e : element.getEnclosedElements())
                    {
                        if (e.getKind() == ElementKind.METHOD)
                        {
                            ExecutableElement m = (ExecutableElement) e;
                            element = (TypeElement) typeUtils.asElement(m.getReturnType());
                            break;
                        }
                    }

                    String elementName = element.getSimpleName().toString();
                    if (hasMethod(testComponent, "with"+ elementName))
                    {
                        String name = Utils.toParameterName(element.getSimpleName().toString());
                        overridenMethod.addCode(String.format(".with%s(this.%s)\n", elementName, name));
                    }
                    created.add(classNameString);
                }
            }

            overridenMethod.addStatement(".build()");
            builder.addMethod(overridenMethod.build());
        }

        List<String> created = new ArrayList<>();
        for (TypeMirror createdInterface : createdInterfaces)
        {
            String classNameString = createdInterface.toString();
            if (!created.contains(classNameString))
            {

                TypeElement element = (TypeElement) typeUtils.asElement(createdInterface);
                for (Element e : element.getEnclosedElements())
                {
                    if (e.getKind() == ElementKind.METHOD)
                    {
                        ExecutableElement method = (ExecutableElement) e;
                        element = (TypeElement) typeUtils.asElement(method.getReturnType());
                        break;
                    }
                }

                ClassName typeName = ClassName.bestGuess(classNameString);
                String name = Utils.toParameterName(element.getSimpleName().toString());
                builder.addField(typeName, name, Modifier.PRIVATE);
                MethodSpec.Builder b = MethodSpec.methodBuilder("with" + element.getSimpleName().toString());
                b.addParameter(typeName, "provider");
                b.addModifiers(Modifier.PUBLIC).returns(ClassName.bestGuess(this.getClassName()));
                b.addStatement(String.format("this.%s = provider", name));
                b.addStatement("return this");
                builder.addMethod(b.build());
                created.add(classNameString);
            }

        }

        try
        {
            String classNameString = "android.support.test.InstrumentationRegistry";
            Class.forName(classNameString);
            ClassName instrumentationRegistry = ClassName.bestGuess(classNameString);

            builder.addMethod(MethodSpec.methodBuilder("apply")
                                        .addModifiers(Modifier.PUBLIC)
                                        .addStatement("$T.getInstrumentation().callApplicationOnCreate(this)", instrumentationRegistry)
                                        .returns(void.class)
                                        .build());
        } catch (Exception e)
        {

        }

        return builder;
    }

    private boolean hasMethod(TypeElement testComponent, String methodName)
    {
        for (Element element : testComponent.getEnclosedElements())
        {
            if (element.getKind() == ElementKind.METHOD) {
                ExecutableElement executableElement = (ExecutableElement) element;
                String name = executableElement.getSimpleName().toString();
                if (name.equals(methodName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String getClassName()
    {
        return prefix + provider.getSimpleName().toString();
    }
}
