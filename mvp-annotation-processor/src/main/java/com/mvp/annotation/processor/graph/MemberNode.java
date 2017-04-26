package com.mvp.annotation.processor.graph;

import com.mvp.annotation.processor.Utils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public class MemberNode extends ResultNode
{

    private final ParameterizedTypeName injectorType;
    private TypeName injector;
    private String initializeInjectorStatement;

    public MemberNode(Elements elementUtils, Types typeUtils, String outerClass, TypeMirror clazz)
    {
        super(elementUtils, typeUtils, outerClass, clazz, null, null);
        this.injectorType = ParameterizedTypeName.get(ClassName.bestGuess("dagger.MembersInjector"), ClassName.get(this.clazz));
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
        if (!createdProviders.contains(this.getProviderName(this)))
        {
            TypeSpec.Builder componentBuilder = topNode.getClassBuilder();
            componentBuilder.addField(injectorType, this.getInjectorFieldName(), Modifier.PRIVATE);
            createdProviders.add(this.getProviderName(this));
        }
    }

    @Override
    protected String getProviderName(ResultNode param)
    {
        return Utils.toParameterName(Utils.extractClassName(param.clazz)) + "Injector";
    }

    @Override
    public void buildProviders(MethodSpec.Builder builder, List<String> constructedProviders)
    {

        for (ResultNode param : this.params)
        {
            param.buildProviders(builder, constructedProviders);
        }

        if (!constructedProviders.contains(this.getProviderName(this)))
        {

            if (this.initializeInjectorStatement == null)
            {
                StringBuilder sb = new StringBuilder();
                for (int position = 0; position < this.params.size(); position++)
                {
                    ResultNode resultNode = this.params.get(position);
                    sb.append("this.").append(resultNode.getProviderName(resultNode));
                    if (position < this.params.size() - 1)
                    {
                        sb.append(", ");
                    }
                }
                String simpleClassName = Utils.extractClassName(this.clazz);
                builder.addStatement(String.format("this.%s = $T.create(%s)", this.getInjectorFieldName(), sb.toString()), ClassName.bestGuess(Utils.extractPackage(this.clazz) + "." + simpleClassName + "_MembersInjector"));
            } else if (this.initializeInjectorStatement != null && this.injector != null)
            {
                builder.addStatement(this.initializeInjectorStatement, this.injector);
            }
            constructedProviders.add(this.getProviderName(this));
        }
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

    @Override
    public boolean createsDelegateInterface()
    {
        return false;
    }

    @Override
    public String toAnnotation(String id, String parentId)
    {
        StringBuilder sb = new StringBuilder();
        for (int position = 0; position < this.params.size(); position++)
        {
            ResultNode resultNode = this.params.get(position);
            sb.append(resultNode.toAnnotation(UUID.randomUUID().toString(), id));
            if (position < this.params.size() - 1) {
                sb.append(", ");
            }
        }
        String annotation = "@com.mvp.annotation.internal.ResultNode(id = \"%s\", parentId = \"%s\", dataType = %s, isInjector = true)\n";
        String dataType = this.clazz.toString() + ".class";
        String s = sb.toString();
        String seperator = s.isEmpty() ? "" : ", ";
        return String.format(annotation, id, parentId, dataType) + seperator + sb.toString();
    }

    @Override
    protected TypeName getProviderTypeName()
    {
        return ParameterizedTypeName.get(ClassName.get(dagger.MembersInjector.class), ClassName.get(this.clazz));
    }
}
