package com.mvp.annotation.processor.unittest;

import com.mvp.annotation.processor.Gang;
import com.mvp.annotation.processor.Utils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.HashMap;
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

import static com.mvp.annotation.processor.Utils.getAnnotationValue;
import static com.mvp.annotation.processor.Utils.hasProvidesAnnotation;

/**
 * Created by Andy on 15.12.2016.
 */
public class PresenterBuilderClass extends AbsGeneratingClass{

    private Elements elementUtils;
    private Types typeUtil;
    private final Gang gang;
    private String shortestPackageName;

    private List<String> moduleFieldNames = new ArrayList<>();
    private List<List<String>> instanceFieldNames = new ArrayList<>();
    public PresenterBuilderClass(Filer filer, Elements elementUtils, Types typeUtil, String packageName, Gang gang, String shortestPackageName) {
        super(filer, packageName);
        this.elementUtils = elementUtils;
        this.typeUtil = typeUtil;
        this.gang = gang;
        this.shortestPackageName = shortestPackageName;
    }


    @Override
    protected TypeSpec.Builder build() {

        List<ClassName> modules = findModules(gang.getElementPresenterClass());

        HashMap<String, ArrayList<ModuleMethod>> instancesFromModules = extractProvidedClasses(modules);

        ClassName activityControllerClassName = ClassName.get(getPackageName(), gang.getActivityClass() + "Controller");
        ClassName presenterBuilderClass = ClassName.get(getPackageName(), gang.getPresenterClass().simpleName() + "Builder");
        String className = gang.getPresenterClass().simpleName() + "Builder";
        TypeSpec.Builder builder = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(activityControllerClassName, "controller")
                    .addCode("this.controller = controller;\n")
                    .build())
                .addField(activityControllerClassName, "controller", Modifier.PRIVATE);

        for (ClassName module : modules) {
            String parameterName = toParameterName(module);
            moduleFieldNames.add(parameterName);
            builder.addField(module, parameterName, Modifier.PRIVATE)
                    .addMethod(MethodSpec.methodBuilder("with")
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(module, parameterName)
                        .returns(presenterBuilderClass)
                        .addCode(String.format("this.%s = %s;\n", parameterName, parameterName))
                        .addCode("return this;\n")
                        .build());
        }

        for (Map.Entry<String, ArrayList<ModuleMethod>> instancesFromModule : instancesFromModules.entrySet()) {
            ArrayList<ModuleMethod> moduleMethods = instancesFromModule.getValue();
            List<String> fieldNames = new ArrayList<>();
            for (ModuleMethod moduleMethod : moduleMethods) {
                ClassName providedInstanceClassName = moduleMethod.getClassType();
                String parameterName = toParameterName(providedInstanceClassName);
                fieldNames.add(parameterName);
                builder.addField(providedInstanceClassName, parameterName, Modifier.PRIVATE)
                        .addMethod(MethodSpec.methodBuilder("withInstance")
                                .addModifiers(Modifier.PUBLIC)
                                .addParameter(providedInstanceClassName, parameterName)
                                .returns(presenterBuilderClass)
                                .addCode(String.format("this.%s = %s;\n", parameterName, parameterName))
                                .addCode("return this;\n")
                                .build());
            }
            instanceFieldNames.add(fieldNames);
        }

        builder.addField(gang.getPresenterClass(), "mockPresenter", Modifier.PRIVATE)
                .addMethod(MethodSpec.methodBuilder("withMock")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(gang.getPresenterClass(), "presenter")
                .addCode("this.mockPresenter = presenter;\n")
                .addCode("return this;\n")
                .returns(presenterBuilderClass)
                .build());

        builder.addField(gang.getViewClass(), "view", Modifier.PRIVATE);
        builder.addMethod(MethodSpec.methodBuilder("withView")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(gang.getViewClass(), "view")
                .addCode("this.view = view;\n")
                .addCode("return this;\n")
                .returns(presenterBuilderClass)
                .build());

        ClassName testingContext = ClassName.get(shortestPackageName, "TestingContext");
        builder.addField(testingContext, "testingContext", Modifier.PRIVATE);
        builder.addMethod(MethodSpec.methodBuilder("with")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(testingContext, "testingContext")
                .addCode("this.testingContext = testingContext;\n" +
                        "controller.with(this.testingContext);\n" +
                        "return this;")
                .returns(presenterBuilderClass)
                .build());

        builder.addMethod(MethodSpec.methodBuilder("withActivityAsView")
                .addModifiers(Modifier.PUBLIC)
                .addCode("this.view = controller.activity();\n" +
                        "return this;")
                .returns(presenterBuilderClass)
                .build());



        for (int i = 0; i < modules.size(); i++){
            ClassName module = modules.get(i);
            String canonicalName = toCanonicalName(module);
            TypeElement typeElement = elementUtils.getTypeElement(canonicalName);
            List<? extends Element> enclosedElements = typeElement.getEnclosedElements();
            List<? extends VariableElement> parameters = new ArrayList<>();
            for (Element enclosedElement : enclosedElements) {
                if (enclosedElement.getKind() == ElementKind.CONSTRUCTOR){
                    ExecutableElement executableElement = (ExecutableElement) enclosedElement;
                    parameters = executableElement.getParameters();
                }
            }
            String name = module.simpleName() + "Delegate";
            TypeSpec.Builder createModuleClassBuilder = TypeSpec.classBuilder(name)
                    .addModifiers(Modifier.PROTECTED, Modifier.STATIC)
                    .superclass(module);
            MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder();
            ArrayList<ModuleMethod> moduleMethods = instancesFromModules.get(canonicalName);
            String superCall = "super(";
            for (int j = 0; j < parameters.size(); j++) {
                superCall += "null";
                if (j < parameters.size() - 1){
                    superCall += ", ";
                }
            }
            superCall += ");\n";
            constructorBuilder.addCode(superCall);
            for (int j = 0; j < moduleMethods.size(); j++) {
                ModuleMethod moduleMethod = moduleMethods.get(j);
                ClassName n = moduleMethod.getClassType();
                List<String> fieldNames = instanceFieldNames.get(i);
                String fieldName = fieldNames.get(j);
                constructorBuilder.addParameter(n, fieldName);
                createModuleClassBuilder.addField(n, fieldName, Modifier.PRIVATE);
                constructorBuilder.addCode(String.format("this.%s = %s;\n", fieldName, fieldName));
                ExecutableElement executableElement = moduleMethod.getExecutableElement();
                createModuleClassBuilder.addMethod(MethodSpec.methodBuilder(executableElement.getSimpleName().toString())
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(Override.class)
                        .returns(ClassName.get(executableElement.getReturnType()))
                        .beginControlFlow(String.format("if (%s == null)", fieldName))
                        .addCode(String.format("return super.%s();\n", executableElement.getSimpleName().toString()))
                        .nextControlFlow("else")
                        .addCode(String.format("return %s;\n}", fieldName))
                        .build());
            }
            createModuleClassBuilder.addMethod(constructorBuilder.build());
            builder.addType(createModuleClassBuilder.build());
        }


        for (int position = 0; position < modules.size(); position++) {
            ClassName module = modules.get(position);
            String canonicalName = toCanonicalName(module);
            List<ModuleMethod> containingInstanceClasses = instancesFromModules.get(canonicalName);
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("create" + module.simpleName()).addModifiers(Modifier.PUBLIC);
            if (!containingInstanceClasses.isEmpty()) {
                String parameterList = "";
                String ifStatement = "if (";
                List<String> fieldNames = instanceFieldNames.get(position);
                for (int i = 0; i < fieldNames.size(); i++) {
                    String fieldName = fieldNames.get(i);
                    ifStatement += fieldName + " != null";
                    parameterList += fieldName;
                    if (i < fieldNames.size() - 1) {
                        ifStatement += " || ";
                        parameterList += ", ";
                    }
                }
                ifStatement += ")";
                methodBuilder.returns(module);
                String moduleFieldName = toParameterName(module);
                methodBuilder.beginControlFlow(String.format("if (%s != null)", moduleFieldName));
                methodBuilder.addCode(String.format("return %s;\n", moduleFieldName));
                methodBuilder.nextControlFlow(ifStatement);
                methodBuilder.addCode(String.format("return new %s(%s);\n", module.simpleName() + "Delegate", parameterList));
                methodBuilder.endControlFlow();
                methodBuilder.addCode(String.format("throw new java.lang.IllegalStateException(\"%s not set!\");\n", moduleFieldName));
                builder.addMethod(methodBuilder.build());
            }
        }

        ParameterizedTypeName robolectricActivityControllerClass = ParameterizedTypeName.get(ClassName.get("org.robolectric.util", "ActivityController"), gang.getActivityClass());

        TypeSpec.Builder bindingResultBuilder = TypeSpec.classBuilder("BindingResult")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addField(robolectricActivityControllerClass, "controller", Modifier.PRIVATE)
                .addField(gang.getPresenterClass(), "presenter", Modifier.PRIVATE)
                .addField(gang.getViewClass(), "view", Modifier.PRIVATE)
                .addMethod(MethodSpec.constructorBuilder()
                        .addParameter(robolectricActivityControllerClass, "controller")
                        .addParameter(gang.getPresenterClass(), "presenter")
                        .addParameter(gang.getViewClass(), "view")
                        .addCode("this.controller = controller;\n")
                        .addCode("this.presenter = presenter;\n")
                        .addCode("this.view = view;\n")
                        .build())
                .addMethod(MethodSpec.methodBuilder("controller")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(robolectricActivityControllerClass)
                        .addCode("return controller;\n")
                        .build())
                .addMethod(MethodSpec.methodBuilder("presenter")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(gang.getPresenterClass())
                        .addCode("return presenter;\n")
                        .build())
                .addMethod(MethodSpec.methodBuilder("view")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(gang.getViewClass())
                        .addCode("return view;\n")
                        .build());



        builder.addType(bindingResultBuilder.build());

        String presenterFieldName = Utils.findPresenterFieldInViewImplementationClass(gang.getElementActivityClass());

        ClassName mockitoClassName = ClassName.get("org.mockito", "Mockito");


        ClassName daggerComponentClass = ClassName.get(getPackageName(gang.getElementPresenterClass()), "DaggerComponent" + gang.getPresenterClass().simpleName());
        ClassName modulePresenterDependenciesClass = ClassName.get(getPackageName(gang.getElementPresenterClass()), "Testable" + gang.getPresenterClass().simpleName() + "Dependencies");

        ClassName bindingResultClassName = presenterBuilderClass.nestedClass("BindingResult");

        ParameterizedTypeName presenterComponent = ParameterizedTypeName.get(ClassName.get("com.mvp", "PresenterComponent"), gang.getViewClass(), gang.getPresenterClass());
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("build");
        methodBuilder
                .addModifiers(Modifier.PUBLIC)
                .addCode(String.format("$T %s;\n", presenterFieldName), gang.getPresenterClass())
                .beginControlFlow("if (mockPresenter != null)")
                .addCode("            " + presenterFieldName + " = controller.with(new $T() {\n" +
                        "                @Override\n" +
                        "                public $T newInstance() {\n" +
                        "                    $T.when(mockPresenter.getView()).thenReturn(view);\n" +
                        "                    return mockPresenter;\n" +
                        "                }\n" +
                        "\n" +
                        "                @Override\n" +
                        "                public $T view() {\n" +
                        "                    return view;\n" +
                        "                }\n" +
                        "            }).build();", presenterComponent, gang.getPresenterClass(), mockitoClassName, gang.getViewClass())
                .nextControlFlow("else")
                .addCode(presenterFieldName + " = controller.with(\n")
                .addCode("$T.builder()\n", daggerComponentClass)
                .addCode(".module" + gang.getPresenterClass().simpleName() + "Dependencies(new $T(controller.activity(), view))\n", modulePresenterDependenciesClass);

        for (ClassName module : modules) {
            String methodName = Character.toLowerCase(module.simpleName().charAt(0)) + module.simpleName().substring(1);
            methodBuilder.addCode("." + methodName + "(" + "create" + module.simpleName() +"())\n");
        }

        ClassName moduleEventBusClass = ClassName.get("com.mvp", "ModuleEventBus");
        methodBuilder.addCode(".moduleEventBus(new $T(testingContext.eventBus()))\n", moduleEventBusClass);

        methodBuilder.addCode(".build()\n).build();\n")
        .endControlFlow()
        .addCode("return new BindingResult(this.controller.controller(), " + presenterFieldName + ", view);\n")
        .returns(bindingResultClassName);

        builder.addMethod(methodBuilder.build());

        return builder;
    }

    private String getPackageName(Element viewElement) {
        return elementUtils.getPackageOf(viewElement).getQualifiedName().toString();
    }

    private String toCanonicalName(ClassName module) {
        return module.packageName() + "." + module.simpleName();
    }

    private HashMap<String, ArrayList<ModuleMethod>> extractProvidedClasses(List<ClassName> modules) {
        HashMap<String, ArrayList<ModuleMethod>> classNames = new HashMap<>();
        for (ClassName module : modules) {
            String moduleCanonicalClassName = toCanonicalName(module);
            TypeElement typeElement = elementUtils.getTypeElement(moduleCanonicalClassName);
            List<? extends Element> enclosedElements = typeElement.getEnclosedElements();
            for (Element enclosedElement : enclosedElements) {
                if (enclosedElement.getKind() == ElementKind.METHOD){
                    ExecutableElement executableElement = (ExecutableElement) enclosedElement;
                    if (hasProvidesAnnotation(executableElement)){
                        TypeMirror returnType = executableElement.getReturnType();
                        if (!classNames.containsKey(moduleCanonicalClassName)) {
                            classNames.put(moduleCanonicalClassName, new ArrayList<ModuleMethod>());
                        }
                        ClassName className = ClassName.bestGuess(returnType.toString());
                        classNames.get(moduleCanonicalClassName).add(new ModuleMethod(className, executableElement));
                    }
                }
            }
        }
        return classNames;
    }

    private String toParameterName(ClassName module) {
        return Character.toLowerCase(module.simpleName().charAt(0)) + module.simpleName().substring(1);
    }

    private List<ClassName> findModules(Element presenterElement) {
        List<ClassName> classNames = new ArrayList<>();
        AnnotationValue value = getAnnotationValue(presenterElement, "needsModules");
        List<Object> moduleClasses = value != null ? (List<Object>) value.getValue() : new ArrayList<>();
        for (Object moduleClass : moduleClasses) {
            String m = moduleClass.toString().replace(".class", "");
            classNames.add(ClassName.bestGuess(m));
        }
        return classNames;
    }

    private static class ModuleMethod {
        private final ClassName classType;
        private final ExecutableElement executableElement;
        ModuleMethod(ClassName classType, ExecutableElement executableElement){
            this.classType = classType;
            this.executableElement = executableElement;
        }

        public ClassName getClassType() {
            return classType;
        }

        public ExecutableElement getExecutableElement() {
            return executableElement;
        }
    }

}
