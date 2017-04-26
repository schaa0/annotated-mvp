package com.mvp.annotation.processor.unittest;

import com.squareup.javapoet.TypeSpec;

import java.io.Serializable;
import java.util.List;

public class GraphCache implements Serializable
{

    private final String packageName;
    private final TypeSpec typeSpec;

    public GraphCache(String packageName, TypeSpec typeSpec) {
        this.packageName = packageName;
        this.typeSpec = typeSpec;
    }

    public String getPackageName()
    {
        return packageName;
    }

    public TypeSpec getTypeSpec()
    {
        return typeSpec;
    }
}
