package com.mvp.annotation.processor.graph;

import com.mvp.annotation.processor.Utils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public class ResultNode
{
    protected final TypeMirror clazz;
    protected final ClassName outerClass;
    private final TypeElement providingClass;
    private final ExecutableElement providingMethod;
    private final Elements elementUtils;
    private final Types typeUtils;
    protected List<ResultNode> params = new ArrayList<>();

    private boolean isDependentOnParentComponent = false;
    private boolean isDependentOnModule = false;
    private boolean isDependentOnInjectConstructor;
    private boolean isProvider;
    private MemberNode memberNode;

    public ResultNode(Elements elementUtils, Types typeUtils, String outerClass, TypeMirror clazz, TypeElement providingClass, ExecutableElement providingMethod)
    {
        this.elementUtils = elementUtils;
        this.typeUtils = typeUtils;
        this.outerClass = ClassName.bestGuess(outerClass);
        this.clazz = clazz;
        this.providingClass = providingClass;
        this.providingMethod = providingMethod;
    }

    public static List<TypeElement> searchForInjectMembers(Elements elementUtils, TypeMirror clazz)
    {
        List<TypeElement> elements = new ArrayList<>();
        TypeElement typeElement = elementUtils.getTypeElement(clazz.toString());
        for (Element element : typeElement.getEnclosedElements())
        {
            if (element.getKind() == ElementKind.FIELD)
            {
                if (element.getAnnotation(Inject.class) != null)
                {
                    VariableElement variableElement = (VariableElement) element;
                    TypeMirror typeMirror = variableElement.asType();
                    elements.add(elementUtils.getTypeElement(typeMirror.toString()));
                }
            }
        }
        return elements;
    }

    public void addResultNode(ResultNode resultNode)
    {
        params.add(resultNode);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("ResultNode for class: ").append(clazz.toString()).append("\n");
        sb.append("Provided in class: ").append(providingClass.toString()).append("\n");
        sb.append("With Method: ").append(providingMethod.getSimpleName().toString()).append("\n");
        for (ResultNode param : params)
        {
            sb.append("BEGIN PARAM").append("\n");
            sb.append(param.toString()).append("\n");
            sb.append("END PARAM").append("\n");
        }
        return sb.toString();
    }

    public String getInterfaceName()
    {
        return "I" + Utils.extractClassName(this.clazz) + "Provider";
    }

    private ClassName getInnerClass(String className)
    {
        return outerClass.nestedClass(className);
    }

    public void buildDelegateInterface(TypeSpec.Builder componentBuilder, List<String> createdInterfaces)
    {

        for (ResultNode param : this.params)
        {
            param.buildDelegateInterface(componentBuilder, createdInterfaces);
        }

        if (!this.isDependentOnParentComponent)
        {

            String className = this.getInterfaceName();
            if (!createdInterfaces.contains(className))
            {
                TypeSpec.Builder builder = TypeSpec.interfaceBuilder(className);
                builder.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
                MethodSpec.Builder getMethodBuilder = MethodSpec.methodBuilder("get");
                getMethodBuilder.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
                getMethodBuilder.returns(ClassName.get(this.clazz));
                for (ResultNode param : this.params)
                {
                    TypeName clazz;
                    if (param.isProvider)
                    {
                        clazz = ParameterizedTypeName.get(ClassName.get(Provider.class), ClassName.get(param.clazz));
                    } else
                    {
                        clazz = ClassName.get(param.clazz);
                    }
                    String name = Utils.toParameterName(Utils.extractClassName(param.clazz));
                    getMethodBuilder.addParameter(clazz, name);
                }
                builder.addMethod(getMethodBuilder.build());
                componentBuilder.addType(builder.build());
                createdInterfaces.add(className);
            }
        }
    }

    public void toClass(TypeSpec.Builder componentBuilder, List<String> createdClasses)
    {
        for (ResultNode param : this.params)
        {
            param.toClass(componentBuilder, createdClasses);
        }
        TypeSpec.Builder builder = null;
        ParameterizedTypeName provider = ParameterizedTypeName.get(ClassName.get(Provider.class), ClassName.get(clazz));
        String className = this.getProviderClassName();

        if (createdClasses.contains(className))
        {
            return;
        }

        if (isDependentOnParentComponent)
        {

            String paramName = "component";
            builder = TypeSpec.classBuilder(className);
            builder.addField(ClassName.get(providingClass.asType()), paramName);
            builder.addSuperinterface(provider);
            MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder();

            if (this.memberNode != null) {
                TypeName providerTypeName = this.memberNode.getProviderTypeName();
                String providerName = this.memberNode.getProviderName(this.memberNode);
                constructorBuilder.addParameter(providerTypeName, providerName);
                builder.addField(providerTypeName, providerName, Modifier.PRIVATE);
                constructorBuilder.addStatement(String.format("this.%s = %s", providerName, providerName));
            }

            constructorBuilder.addModifiers(Modifier.PUBLIC)
                              .addParameter(ClassName.get(providingClass.asType()), paramName)
                              .addStatement(String.format("this.%s = %s", paramName, paramName));
            builder.addMethod(constructorBuilder.build());
            builder.addMethod(MethodSpec.methodBuilder("get")
                                        .addModifiers(Modifier.PUBLIC)
                                        .addAnnotation(Override.class)
                                        .returns(ClassName.get(clazz))
                                        .addStatement(String.format("return %s.%s()", paramName, providingMethod.getSimpleName().toString()))
                                        .build());

        } else if (this.isDependentOnModule)
        {

            String paramName = "module";
            builder = TypeSpec.classBuilder(className);
            builder.addField(ClassName.get(providingClass.asType()), paramName);
            builder.addSuperinterface(provider);
            MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                                                              .addModifiers(Modifier.PUBLIC)
                                                              .addStatement(String.format("this.%s = %s", paramName, paramName));

            if (this.memberNode != null)
            {
                TypeName providerTypeName = this.memberNode.getProviderTypeName();
                String providerName = this.memberNode.getProviderName(this.memberNode);
                constructorBuilder.addParameter(providerTypeName, providerName);
                builder.addField(providerTypeName, providerName, Modifier.PRIVATE);
                constructorBuilder.addStatement(String.format("this.%s = %s", providerName, providerName));
            }

            constructorBuilder.addParameter(ClassName.get(providingClass.asType()), paramName);

            builder.addField(this.getDelegateTypeName(), "delegate");
            constructorBuilder.addStatement("this.delegate = delegate");
            for (ResultNode param : this.params)
            {
                TypeName typeName = param.getProviderTypeName();
                String name = Utils.toParameterName(Utils.extractClassName(param.providingClass.asType()));
                constructorBuilder.addParameter(typeName, name);
                builder.addField(typeName, name, Modifier.PRIVATE);
                constructorBuilder.addStatement(String.format("this.%s = %s", name, name));
            }
            constructorBuilder.addParameter(this.getDelegateTypeName(), "delegate");
            builder.addMethod(constructorBuilder.build());

            StringBuilder sb = new StringBuilder();

            if (this.memberNode != null) {
                sb.append("this.").append(memberNode.getProviderName(memberNode));
                if (!this.params.isEmpty()) {
                    sb.append(", ");
                }
            }

            for (int position = 0; position < this.params.size(); position++)
            {
                ResultNode node = this.params.get(position);
                String parameterName = Utils.toParameterName(Utils.extractClassName(node.providingClass.asType()));
                if (!node.isProvider)
                {
                    sb.append(String.format("%s.get()", parameterName));
                } else
                {
                    sb.append(String.format("%s", parameterName));
                }

                if (position < this.params.size() - 1)
                {
                    sb.append(", ");
                }
            }

            MethodSpec.Builder getMethod = MethodSpec.methodBuilder("get")
                                                     .addModifiers(Modifier.PUBLIC)
                                                     .addAnnotation(Override.class)
                                                     .returns(ClassName.get(clazz));
            getMethod.beginControlFlow("if (this.delegate != null)");
            String name = this.clazz.toString();
            String p = Utils.toParameterName(Utils.extractClassName(this.clazz));
            getMethod.addStatement(String.format("%s %s = delegate.get(%s)", name, p, sb.toString()));
            if (this.memberNode != null) {
                getMethod.addStatement(String.format("this.%s.injectMembers(%s)", memberNode.getProviderName(memberNode), p));
            }
            getMethod.addStatement(String.format("return %s", p));
            getMethod.nextControlFlow("else");
            String methodName = providingMethod.getSimpleName().toString();
            getMethod.addStatement(String.format("return %s.%s(%s)", paramName, methodName, sb.toString()));
            getMethod.endControlFlow();

            builder.addMethod(getMethod.build());

        } else if (this.isDependentOnInjectConstructor)
        {
            builder = TypeSpec.classBuilder(className);
            builder.addSuperinterface(provider);
            MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder();

            if (this.memberNode != null)
            {
                TypeName providerTypeName = this.memberNode.getProviderTypeName();
                String providerName = this.memberNode.getProviderName(this.memberNode);
                constructorBuilder.addParameter(providerTypeName, providerName);
                builder.addField(providerTypeName, providerName, Modifier.PRIVATE);
                constructorBuilder.addStatement(String.format("this.%s = %s", providerName, providerName));
            }

            TypeName delegateType = this.getDelegateTypeName();
            builder.addField(delegateType, "delegate", Modifier.PRIVATE);
            constructorBuilder.addParameter(delegateType, "delegate");
            constructorBuilder.addStatement("this.delegate = delegate");
            for (ResultNode param : this.params)
            {
                String paramName = getProviderName(param);
                ParameterizedTypeName paramType = ParameterizedTypeName.get(ClassName.get(Provider.class), ClassName.get(param.clazz));
                builder.addField(paramType, paramName);
                constructorBuilder.addParameter(paramType, paramName);
                constructorBuilder.addStatement(String.format("this.%s = %s", paramName, paramName));
            }
            builder.addMethod(constructorBuilder.build());
            MethodSpec.Builder methodGet = MethodSpec.methodBuilder("get")
                                                     .addAnnotation(Override.class)
                                                     .returns(ClassName.get(this.clazz));

            StringBuilder sb1 = new StringBuilder();
            for (int i = 0; i < this.params.size(); i++)
            {
                ResultNode param = this.params.get(i);
                String providerName = getProviderName(param);
                sb1.append(providerName);
                if (!param.isProvider)
                {
                    sb1.append(".get()");
                }
                if (i < this.params.size() - 1)
                    sb1.append(",");
            }

            methodGet.beginControlFlow("if (this.delegate != null)");
            String name = this.clazz.toString();
            String paramName = Utils.toParameterName(Utils.extractClassName(this.clazz));
            methodGet.addStatement(String.format("%s %s = this.delegate.get(%s)", name, paramName, sb1.toString()));
            if (this.memberNode != null) {
                methodGet.addStatement(String.format("this.%s.injectMembers(%s)", memberNode.getProviderName(memberNode), paramName));
            }
            methodGet.addStatement(String.format("return %s", paramName));
            methodGet.nextControlFlow("else");

            boolean isInnerClass = Utils.isInnerClass(typeUtils, this.clazz);
            String joiner = isInnerClass ? "_" : ".";
            String factoryClassName = Utils.extractPackage(this.clazz) + joiner + Utils.extractClassName(this.clazz) + "_Factory";
            TypeName typeName = ClassName.bestGuess(factoryClassName);
            TypeElement factoryElement = elementUtils.getTypeElement(factoryClassName);
            VariableElement firstParam = this.getFirstParamOfCreateMethod(factoryElement);
            StringBuilder sb = new StringBuilder();
            if (firstParam != null && firstParam.asType().toString().contains("MembersInjector") && this.memberNode == null)
            {
                sb.append("dagger.internal.MembersInjectors.noOp()");
                if (!this.params.isEmpty())
                {
                    sb.append(", ");
                }
            }else if(firstParam != null && firstParam.asType().toString().contains("MembersInjector") && this.memberNode != null) {
                sb.append("this.").append(this.memberNode.getProviderName(this.memberNode));
                if (!this.params.isEmpty())
                {
                    sb.append(", ");
                }
            }

            for (int i = 0; i < this.params.size(); i++)
            {
                ResultNode param = this.params.get(i);
                String providerName = getProviderName(param);
                sb.append(providerName);
                if (i < this.params.size() - 1)
                    sb.append(",");
            }

            methodGet.addStatement(String.format("return $T.create(%s).get()", sb.toString()), typeName);
            methodGet.endControlFlow();
            methodGet.addModifiers(Modifier.PUBLIC);
            builder.addMethod(methodGet.build());

        }

        createdClasses.add(className);
        componentBuilder.addType(builder.build());
    }

    protected TypeName getProviderTypeName()
    {
        return ParameterizedTypeName.get(ClassName.get(Provider.class), ClassName.get(this.clazz));
    }

    private VariableElement getFirstParamOfCreateMethod(TypeElement factoryElement)
    {
        if (factoryElement == null)
        {
            return null;
        }
        for (Element element : factoryElement.getEnclosedElements())
        {
            if (element.getKind() == ElementKind.METHOD)
            {
                ExecutableElement executableElement = (ExecutableElement) element;
                if (executableElement.getSimpleName().toString().equals("create") && executableElement.getModifiers().contains(Modifier.STATIC))
                {
                    if (!executableElement.getParameters().isEmpty())
                    {
                        return executableElement.getParameters().get(0);
                    }
                }
            }
        }
        return null;
    }

    private TypeName getDelegateTypeName()
    {
        return this.getInnerClass(this.getInterfaceName());
    }

    public String getProviderClassName()
    {
        if (this.isDependentOnParentComponent)
        {
            return providingClass.getSimpleName().toString() + Utils.extractClassName(clazz) + "Provider";
        } else if (this.isDependentOnModule)
        {
            return providingClass.getSimpleName().toString() + Utils.extractClassName(clazz) + "Provider";
        } else
        {
            return providingClass.getSimpleName().toString() + "Provider";
        }
    }

    protected String getProviderName(ResultNode param)
    {
        return Utils.toParameterName(Utils.extractClassName(param.clazz)) + "Provider";
    }

    private String getDelegateName(ResultNode param)
    {
        return Utils.toParameterName(Utils.extractClassName(param.clazz)) + "Delegate";
    }

    public void buildDelegateField(TopNode topNode, List<String> createdDelegates)
    {

        for (ResultNode param : this.params)
        {
            param.buildDelegateField(topNode, createdDelegates);
        }

        TypeSpec.Builder componentBuilder = topNode.getClassBuilder();

        if (!this.isDependentOnParentComponent)
        {

            String delegateName = this.getDelegateName(this);
            if (!createdDelegates.contains(delegateName))
            {
                FieldSpec.Builder builder = FieldSpec.builder(this.getDelegateTypeName(), delegateName, Modifier.PRIVATE);
                componentBuilder.addField(builder.build());
                createdDelegates.add(delegateName);
            }
        }
    }

    public boolean createsDelegateInterface()
    {
        return !this.isDependentOnParentComponent;
    }

    public void buildProviderField(TopNode topNode, List<String> createdProviders)
    {

        for (ResultNode param : this.params)
        {
            param.buildProviderField(topNode, createdProviders);
        }

        TypeSpec.Builder componentBuilder = topNode.getClassBuilder();
        String providerName = this.getProviderName(this);
        if (!createdProviders.contains(providerName))
        {
            ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(ClassName.get(Provider.class), ClassName.get(this.clazz));
            FieldSpec.Builder builder = FieldSpec.builder(parameterizedTypeName, providerName, Modifier.PRIVATE);
            componentBuilder.addField(builder.build());
            createdProviders.add(providerName);
        }
    }

    public void buildProviders(MethodSpec.Builder builder, List<String> constructedProviders)
    {
        for (ResultNode param : this.params)
        {
            param.buildProviders(builder, constructedProviders);
        }

        String newProviderStatement = this.getNewProviderStatement(constructedProviders);
        if (newProviderStatement != null)
        {
            ClassName doubleCheckType = ClassName.bestGuess("dagger.internal.DoubleCheck");
            builder.addStatement(newProviderStatement, doubleCheckType);
        }
    }

    private String getNewProviderStatement(List<String> constructedProviders)
    {
        String providerField = this.getProviderName(this);
        if (!constructedProviders.contains(providerField))
        {
            String providerClassName = this.getProviderClassName();
            StringBuilder sb = new StringBuilder();
            String result;

            if (this.memberNode != null)
            {
                sb.append("this.").append(this.memberNode.getProviderName(this.memberNode));
                sb.append(", ");
            }

            if (isDependentOnParentComponent)
            {
                sb.append("this.").append(Utils.toParameterName(Utils.extractClassName(this.providingClass.asType())));
                result = String.format("this.%s = $T.provider(new %s(%s))", providerField, providerClassName, sb.toString());
            } else if (isDependentOnModule)
            {
                sb.append("this.").append(Utils.toParameterName(Utils.extractClassName(this.providingClass.asType())));
                for (ResultNode param : this.params)
                {
                    sb.append(", ").append("this.").append(param.getProviderName(param));
                }
                sb.append(", ").append(this.getDelegateName(this));
                result = String.format("this.%s = $T.provider(new %s(%s))", providerField, providerClassName, sb.toString());
            } else
            {
                sb.append(this.getDelegateName(this));
                for (ResultNode param : this.params)
                {
                    sb.append(", ").append("this.").append(param.getProviderName(param));
                }
                result = String.format("this.%s = $T.provider(new %s(%s))", providerField, providerClassName, sb.toString());
            }
            constructedProviders.add(providerField);
            return result;
        } else
        {
            return null;
        }
    }

    public boolean isDependentOnParentComponent()
    {
        return this.isDependentOnParentComponent;
    }

    public void setDependentOnParentComponent(boolean dependentOnParentComponent)
    {
        isDependentOnParentComponent = dependentOnParentComponent;
    }

    public boolean isDependentOnModule()
    {
        return isDependentOnModule;
    }

    public void setDependentOnModule(boolean dependentOnModule)
    {
        isDependentOnModule = dependentOnModule;
    }

    public void buildWithDelegateMethod(TopNode topNode, List<String> createdDelegates)
    {

        for (ResultNode param : this.params)
        {
            param.buildWithDelegateMethod(topNode, createdDelegates);
        }

        TypeSpec.Builder classBuilder = topNode.getClassBuilder();

        if (!this.isDependentOnParentComponent)
        {
            String o = this.clazz.toString();
            if (!createdDelegates.contains(o))
            {
                TypeName typeName = this.getDelegateTypeName();
                MethodSpec.Builder builder = MethodSpec.methodBuilder("with" + Utils.extractClassName(this.clazz));
                builder.addModifiers(Modifier.PUBLIC)
                       .returns(ClassName.bestGuess(topNode.getDaggerClassName()))
                       .addParameter(typeName, "provider")
                       .addStatement(String.format("this.%s = provider", this.getDelegateName(this)))
                       .addStatement("return this");
                classBuilder.addMethod(builder.build());
                createdDelegates.add(o);
            }
        }
    }

    public void findAndAddModules(Set<TypeElement> modules)
    {
        for (ResultNode param : this.params)
        {
            param.findAndAddModules(modules);
        }
        if (this.isDependentOnModule())
        {
            modules.add(this.providingClass);
        }
    }

    public void setDependentOnInjectConstructor(boolean dependentOnInjectConstructor)
    {
        this.isDependentOnInjectConstructor = dependentOnInjectConstructor;
    }

    public boolean tryOverrideComponentMethod(Elements elements, TypeSpec.Builder builder, ExecutableElement method)
    {
        for (ResultNode param : this.params)
        {
            if (param.tryOverrideComponentMethod(elements, builder, method))
            {
                return true;
            }
        }

        if (method.getReturnType().toString().equals(this.clazz.toString()))
        {

            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(method.getSimpleName().toString());
            methodBuilder.addAnnotation(Override.class);
            methodBuilder.addModifiers(Modifier.PUBLIC);
            methodBuilder.returns(ClassName.get(method.getReturnType()));
            for (VariableElement variableElement : method.getParameters())
            {
                methodBuilder.addParameter(ClassName.get(variableElement.asType()), Utils.toParameterName(variableElement.getSimpleName().toString()));
            }
            methodBuilder.addStatement(String.format("return %s.get()", this.getProviderName(this)));
            builder.addMethod(methodBuilder.build());

            return true;

        } else
        {
            return false;
        }

    }

    public Collection<? extends TypeMirror> getGeneratedInterfaceType()
    {
        List<TypeMirror> result = new ArrayList<>();
        for (ResultNode param : params)
        {
            result.addAll(param.getGeneratedInterfaceType());
        }
        if (this.createsDelegateInterface())
        {
            ClassName innerClass = this.getInnerClass(this.getInterfaceName());
            TypeMirror e = elementUtils.getTypeElement(innerClass.toString()).asType();
            result.add(e);
        }
        return result;
    }

    public String toAnnotation(String id, String parentId)
    {
        StringBuilder sb = new StringBuilder();
        for (int position = 0; position < this.params.size(); position++)
        {
            ResultNode resultNode = this.params.get(position);
            sb.append(resultNode.toAnnotation(UUID.randomUUID().toString(), id));
            if (position < this.params.size() - 1)
            {
                sb.append(", ");
            }
        }

        String annotation = "@com.mvp.annotation.internal.ResultNode(id = \"%s\", parentId = \"%s\", dataType = %s, dependentClass = %s, methodName = \"%s\", isInjector = false)\n";
        String dataType = this.clazz.toString() + ".class";
        String dependentClass = this.providingClass.toString() + ".class";
        String methodName = this.providingMethod.getSimpleName().toString();
        String s = sb.toString();
        String seperator = s.isEmpty() ? "" : ", ";
        return String.format(annotation, id, parentId, dataType, dependentClass, methodName) + seperator + s;
    }

    public void setIsProvider(boolean isProvider)
    {
        this.isProvider = isProvider;
    }

    public boolean isProvider()
    {
        return isProvider;
    }

    public void setProvider(boolean isProvider)
    {
        this.isProvider = isProvider;
    }

    public void setMemberNode(MemberNode memberNode)
    {
        this.memberNode = memberNode;
    }
}
