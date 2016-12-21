package com.mvp.annotation.processor.unittest;

import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;

/**
 * Created by Andy on 20.12.2016.
 */

public class PresenterEnumType extends AbsGeneratingType {

    private static final String ENUM_TYPE_NAME = "PresenterType";

    public PresenterEnumType(Filer filer, String packageName) {
        super(filer, packageName);
    }

    @Override
    protected TypeSpec.Builder build() {
        return TypeSpec.enumBuilder(ENUM_TYPE_NAME)
                .addModifiers(Modifier.PUBLIC)
                .addEnumConstant("REAL")
                .addEnumConstant("MOCK");
    }
}
