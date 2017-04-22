package com.mvp.annotation.processor.graph;

import com.mvp.annotation.processor.Utils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.List;
import java.util.Set;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public class MemberNode extends ResultNode
{

    private TypeName injector;
    private String initializeInjectorStatement;

    public MemberNode(Elements elementUtils, Types typeUtils, String outerClass, TypeMirror clazz)
    {
        super(elementUtils, typeUtils, outerClass, clazz, null, null);
    }

    @Override
    public String toString()
    {
        return this.clazz.toString();
    }

    @Override
    public String getInterfaceName()
    {
        return "";
    }

    @Override
    public void buildDelegateInterface(TypeSpec.Builder componentBuilder, List<String> createdInterfaces)
    {
        for (ResultNode param : this.params)
        {
            param.buildDelegateInterface(componentBuilder, createdInterfaces);
        }
    }

    @Override
    public void toClass(TypeSpec.Builder componentBuilder, List<String> createdClasses)
    {
        for (ResultNode param : this.params)
        {
            param.toClass(componentBuilder, createdClasses);
        }
    }

    @Override
    public String getProviderClassName()
    {
        return "";
    }

    @Override
    public void buildDelegateField(TopNode topNode, List<String> createdDelegates)
    {
        for (ResultNode param : this.params)
        {
            param.buildDelegateField(topNode, createdDelegates);
        }
    }

    @Override
    public void buildProviderField(TopNode topNode, List<String> createdProviders)
    {
        for (ResultNode param : this.params)
        {
            param.buildProviderField(topNode, createdProviders);
        }
    }

    @Override
    public void buildProviders(MethodSpec.Builder builder, List<String> constructedProviders)
    {
        for (ResultNode param : this.params)
        {
            param.buildProviders(builder, constructedProviders);
        }
        builder.addStatement(this.initializeInjectorStatement, this.injector);
    }

    @Override
    public void findAndAddModules(Set<TypeElement> modules)
    {
        for (ResultNode param : this.params)
        {
            param.findAndAddModules(modules);
        }
    }

    @Override
    public void buildWithDelegateMethod(TopNode topNode, List<String> createdDelegates)
    {
        for (ResultNode param : this.params)
        {
            param.buildWithDelegateMethod(topNode, createdDelegates);
        }
    }

    @Override
    public boolean tryOverrideComponentMethod(Elements elements, TypeSpec.Builder builder, ExecutableElement method)
    {

        if (method.getReturnType().toString().equals(void.class.getName()) && !method.getParameters().isEmpty() && method.getParameters().get(0).asType().toString().equals(this.clazz.toString())){

            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(method.getSimpleName().toString())
                                                         .returns(ClassName.VOID)
                                                         .addAnnotation(Override.class);

            VariableElement param = method.getParameters().get(0);
            TypeName membersInjectorsTypeName = ClassName.bestGuess("dagger.internal.MembersInjectors");
            injector = ClassName.bestGuess(param.asType().toString() + "_MembersInjector");
            methodBuilder.addModifiers(Modifier.PUBLIC);
            methodBuilder.addParameter(ClassName.get(param.asType()), param.getSimpleName().toString());

            StringBuilder sb = new StringBuilder();
            for (int position = 0; position < this.params.size(); position++)
            {
                ResultNode resultNode = this.params.get(position);
                sb.append(resultNode.getProviderName(resultNode));
                if (position < this.params.size() - 1) {
                    sb.append(", ");
                }
            }

            ParameterizedTypeName typeName = ParameterizedTypeName.get(ClassName.bestGuess("dagger.MembersInjector"), ClassName.get(this.clazz));
            builder.addField(typeName, this.getInjectorFieldName(), Modifier.PRIVATE);

            initializeInjectorStatement = String.format("this.%s = $T.create(%s)", this.getInjectorFieldName(), sb.toString());

            methodBuilder.addStatement(String.format("$T.injectMembers(this.%s, %s)", this.getInjectorFieldName(), param.getSimpleName().toString()),
                    membersInjectorsTypeName);

            builder.addMethod(methodBuilder.build());

            return true;
        }else {
            return false;
        }
    }

    private String getInjectorFieldName()
    {
        return Utils.toParameterName(Utils.extractClassName(this.clazz)) + "Injector";
    }
}
