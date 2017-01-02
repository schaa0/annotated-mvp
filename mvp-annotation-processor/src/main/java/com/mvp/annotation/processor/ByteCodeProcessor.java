package com.mvp.annotation.processor;

import com.mvp.annotation.ModuleParam;
import com.mvp.annotation.Provider;
import com.mvp.annotation.Presenter;
import com.mvp.annotation.ProvidesComponent;
import com.mvp.annotation.UIView;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.annotation.AnnotationMemberValue;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.BooleanMemberValue;
import javassist.bytecode.annotation.ByteMemberValue;
import javassist.bytecode.annotation.CharMemberValue;
import javassist.bytecode.annotation.ClassMemberValue;
import javassist.bytecode.annotation.DoubleMemberValue;
import javassist.bytecode.annotation.EnumMemberValue;
import javassist.bytecode.annotation.FloatMemberValue;
import javassist.bytecode.annotation.IntegerMemberValue;
import javassist.bytecode.annotation.LongMemberValue;
import javassist.bytecode.annotation.MemberValue;
import javassist.bytecode.annotation.MemberValueVisitor;
import javassist.bytecode.annotation.ShortMemberValue;
import javassist.bytecode.annotation.StringMemberValue;
import weaver.common.WeaveEnvironment;
import weaver.instrumentation.injection.ClassInjector;
import weaver.processor.WeaverProcessor;

import static com.mvp.annotation.processor.JavassistUtils.ctClassToString;
import static com.mvp.annotation.processor.JavassistUtils.sameSignature;
import static com.mvp.annotation.processor.JavassistUtils.stringToCtClass;

/**
 * Created by Andy on 10.12.2016.
 */

public class ByteCodeProcessor extends WeaverProcessor {

    private static final String FIELD_VIEW_DELEGATE = "$$viewDelegate";
    private static final String FIELD_COMPONENT = "$$presenterComponent";
    private static final String FIELD_DEPENDENCY_PROVIDER = "$$provider";

    private static final String BUNDLE_CLASS = "android.os.Bundle";

    private static final String POINT_CUT = "org.aspectj.lang.JoinPoint";

    private static final String STATEMENT_DELEGATE_ONCREATE =
            "$s." + FIELD_VIEW_DELEGATE + ".onCreate($$);";

    private static final String STATEMENT_DELEGATE_ONSTOP =
            "$s." + FIELD_VIEW_DELEGATE + ".onStop();";

    private static final String STATEMENT_DELEGATE_ONPOSTRESUME =
            "$s." + FIELD_VIEW_DELEGATE + ".onPostResume();";

    private static final String STATEMENT_DELEGATE_ONDESTROY =
            "$s." + FIELD_VIEW_DELEGATE + ".onDestroy();";

    private static final String STATEMENT_DELEGATE_ONSAVEINSTANCESTATE =
            "$s." + FIELD_VIEW_DELEGATE + ".onSaveInstanceState($$);";

    private static final String STATEMENT_CALL_ATTACH = "$s." + FIELD_VIEW_DELEGATE + ".attachView($s);";

    private static final String STATEMENT_CALL_DETACH = "$s." + FIELD_VIEW_DELEGATE + ".detachView();";
    private static final String ASPECTJ_GEN_METHOD = "_aroundBody";
    private static final String STATEMENT_GET_PRESENTER =
            "$s.%s = (%s) $s." + FIELD_VIEW_DELEGATE + ".getPresenter();";

    private static final String STATEMENT_NEW_DELEGATE =
            "$s." + FIELD_VIEW_DELEGATE + " = new %sDelegateBinder($s, $s." + FIELD_COMPONENT + ", ((%s) $s%s.getApplication()).mvpEventBus());";

    private static final String STATEMENT_NEW_DELEGATE_IN_FRAGMENT =
            "$s." + FIELD_VIEW_DELEGATE + " = new %sDelegateBinder( (android.support.v7.app.AppCompatActivity) $s.getActivity(), $s." + FIELD_COMPONENT + ", ((%s) $s%s.getApplication()).mvpEventBus());";

    private static final String STATEMENT_MAKE_COMPONENT_IN_ACTIVITY =
            "$s." + FIELD_COMPONENT + " = ((%s) $s.%sgetApplication()).getProvider().%s($s, $s%s);";

    private static final String STATEMENT_MAKE_COMPONENT_IN_FRAGMENT =
            "$s." + FIELD_COMPONENT + " = ((%s) $s.%sgetApplication()).getProvider().%s((android.support.v7.app.AppCompatActivity) $s.getActivity(), $s%s);";

    private ClassPool pool;

    private String applicationClassName;

    HashMap<String, CtMethod> componentProviderMethods = new HashMap<>();
    HashMap<String, String> moduleParamClassToMethodName = new HashMap<>();

    @Override
    public synchronized void init(WeaveEnvironment env) {
        super.init(env);
        pool = env.getClassPool();
    }

    @Override
    public void transform(Set<? extends CtClass> candidateClasses) throws Exception {

        List<CtClass> classes = new ArrayList<>(candidateClasses);

        Collections.sort(classes, new Comparator<CtClass>() {
            @Override
            public int compare(CtClass o, CtClass t1) {
                boolean leftIsComponentProvider = isComponentProvider(o);
                boolean rightIsComponentProvider = isComponentProvider(t1);
                if (leftIsComponentProvider)
                    return -1;
                else if (rightIsComponentProvider)
                    return 1;
                else
                    return 0;
            }
        });

        log("Amount of classes in pool: " + classes.size());
        log("Starting Annotated-MVP-Weaving");

        CtClass dependencyProviderClass = pool.get("com.mvp.DependencyProvider");
        CtMethod[] methods = dependencyProviderClass.getMethods();
        for (CtMethod method : methods) {
            if (isComponentProviderMethod(method)){
                CtClass returnTypeClass = method.getReturnType();
                componentProviderMethods.put(returnTypeClass.getName(),method);
            }
        }

        CtClass activityClass = pool.getCtClass("android.support.v7.app.AppCompatActivity");
        CtClass fragmentClass = pool.getCtClass("android.support.v4.app.Fragment");

        for (int i = 0; i < classes.size(); i++) {

            CtClass ctClass = classes.get(i);

            if (i == 0){

                if (ctClass.hasAnnotation(Provider.class)){
                    ClassInjector classInjector = instrumentation.startWeaving(ctClass);
                    injectDependencyProviderField(ctClass, classInjector);
                    injectMethod("getProvider", classInjector);
                    applicationClassName = ctClass.getName();
                    writeClass(ctClass);
                }

            }else {

                if (ctClass.hasAnnotation(UIView.class) && ctClass.subclassOf(activityClass)) {
                    log("Start weaving " + ctClass.getSimpleName());

                    CtMethod[] methods1 = ctClass.getDeclaredMethods();
                    for (CtMethod method : methods1) {
                        if (method.hasAnnotation(ModuleParam.class)){
                            moduleParamClassToMethodName.put(method.getReturnType().getName(), method.getName());
                        }
                    }

                    String presenterClassName = getPresenterClassName(ctClass);
                    String presenterFieldName = getPresenterFieldName(ctClass);

                    String componentPresenterInterfaceName = getComponentPresenterInterfaceName(presenterClassName);

                    ClassInjector classInjector = instrumentation.startWeaving(ctClass);
                    injectDelegateField(ctClass, classInjector);
                    injectComponentField(ctClass, classInjector);
                    injectDelegateLifeCycleIntoActivity(ctClass, classInjector, presenterClassName, presenterFieldName, componentPresenterInterfaceName);
                    writeClass(ctClass);
                } else if (ctClass.hasAnnotation(UIView.class) && ctClass.subclassOf(fragmentClass)) {
                    log("Start weaving " + ctClass.getSimpleName());

                    CtMethod[] methods1 = ctClass.getDeclaredMethods();
                    for (CtMethod method: methods1) {
                        if (method.hasAnnotation(ModuleParam.class)){
                            moduleParamClassToMethodName.put(method.getReturnType().getName(), method.getName());
                        }
                    }

                    ClassInjector classInjector = instrumentation.startWeaving(ctClass);
                    injectDelegateField(ctClass, classInjector);
                    injectComponentField(ctClass, classInjector);
                    String presenterClassName = getPresenterClassName(ctClass);
                    String presenterFieldName = getPresenterFieldName(ctClass);

                    String componentPresenterInterfaceName = getComponentPresenterInterfaceName(presenterClassName);
                    injectDelegateLifeCycleIntoFragment(ctClass, classInjector, presenterClassName, presenterFieldName, componentPresenterInterfaceName);
                    writeClass(ctClass);
                }
            }
        }
    }

    private void injectMethod(String methodName, ClassInjector classInjector) {
        try {
            classInjector.insertMethod(methodName)
                    .createIfNotExists()
                    .returns(pool.get("com.mvp.DependencyProvider").toClass())
                    .addModifiers(Modifier.PUBLIC)
                    .withBody("{ return this." + FIELD_DEPENDENCY_PROVIDER + "; }")
                    .inject()
                    .inject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getComponentPresenterInterfaceName(String presenterClassName) {
        if (null != presenterClassName && presenterClassName.length() > 0 )
        {
            int endIndex = presenterClassName.lastIndexOf(".");
            if (endIndex != -1)
            {
                String javaPackage = presenterClassName.substring(0, endIndex); // not forgot to put check if(endIndex != -1)
                String simplePresenterClassName = presenterClassName.substring(endIndex+1 , presenterClassName.length());
                return javaPackage + ".Component" + simplePresenterClassName;
            }
        }
        return null;
    }

    private boolean isComponentProviderMethod(CtMethod method) {
        return method.hasAnnotation(ProvidesComponent.class);
    }

    private String getPresenterClassName(CtClass ctClass) {
        AnnotationsAttribute visible = (AnnotationsAttribute) ctClass.getClassFile().getAttribute(AnnotationsAttribute.visibleTag);
        MemberValue presenter = visible.getAnnotation(UIView.class.getCanonicalName()).getMemberValue("presenter");
        MyMemberValueVisitor visitor = new MyMemberValueVisitor();
        presenter.accept(visitor);
        return visitor.getClassName();
    }

    private boolean isComponentProvider(CtClass ctClass){
        return ctClass.hasAnnotation(Provider.class);
    }

    private String getPresenterFieldName(CtClass ctClass) throws NotFoundException {
        String presenterFieldName = null;
        CtField[] declaredFields = ctClass.getDeclaredFields();
        for (CtField declaredField : declaredFields) {
            if (declaredField.hasAnnotation(Presenter.class)){
                //CtClass type = declaredField.getType();
                presenterFieldName = declaredField.getName();
                break;
            }
        }
        return presenterFieldName;
    }

    @Override
    public String getName() {
        return "ByteCodeProcessor";
    }

    private void injectDependencyProviderField(CtClass ctClass, ClassInjector classInjector)
        throws Exception {
        String delegateClassName = "com.mvp.DependencyProvider";
        insertDependencyProviderField(classInjector, delegateClassName);
        CtMethod method = findBestMethod(ctClass, "onCreate");
        String statement = "this." + FIELD_DEPENDENCY_PROVIDER + " = new com.mvp.DependencyProvider(this);";
        classInjector.insertMethod(method.getName())
                .ifExistsButNotOverride()
                .override("{" +
                        "super." + method.getName() + "($$);" +
                        statement +
                        "}").inject()
                .ifExists()
                .atTheEnd(statement).inject()
                .inject();
    }

    private void injectDelegateField(CtClass ctClass, ClassInjector classInjector)
            throws Exception {
        String viewClassName = ctClass.getName();
        String delegateClassName = viewClassName + "DelegateBinder";
        insertDelegateField(classInjector, delegateClassName);
    }

    private void injectComponentField(CtClass ctClass, ClassInjector classInjector)
            throws Exception {
        String delegateClassName = "com.mvp.PresenterComponent";
        insertComponentField(classInjector, delegateClassName);
    }

    private void injectDelegateLifeCycleIntoActivity(CtClass ctClass, ClassInjector classInjector, String presenterClassName, String presenterFieldName, String componentPresenterInterfaceName)
            throws Exception {
        CtMethod onCreate = findBestMethod(ctClass, "onCreate", BUNDLE_CLASS);
        CtMethod onStart = findBestMethod(ctClass, "onStart");
        CtMethod onStop = findBestMethod(ctClass, "onStop");
        CtMethod onPostResume = findBestMethod(ctClass, "onPostResume");
        CtMethod onDestroy = findBestMethod(ctClass, "onDestroy");
        CtMethod onSaveInstanceState = findBestMethod(ctClass, "onSaveInstanceState", BUNDLE_CLASS);
        CtMethod ctMethod = componentProviderMethods.get(componentPresenterInterfaceName);
        CtClass[] parameterTypes = ctMethod.getParameterTypes();
        StringBuilder sb = new StringBuilder();
        if (parameterTypes.length > 2) {
            sb.append(", ");
            for (int i = 2; i < parameterTypes.length; i++) {
                CtClass parameterType = parameterTypes[i];
                String methodName = moduleParamClassToMethodName.get(parameterType.getName());
                sb.append("$s.").append(methodName).append("()");
                if (i < parameterTypes.length - 1) {
                    sb.append(", ");
                }
            }
        }
        String parameters = sb.toString();
        atTheEnd(classInjector, onCreate, String.format(STATEMENT_MAKE_COMPONENT_IN_ACTIVITY, applicationClassName, "", ctMethod.getName(), parameters));
        atTheEnd(classInjector, onCreate, String.format(STATEMENT_NEW_DELEGATE, ctClass.getName(), applicationClassName, ""));
        atTheEnd(classInjector, onCreate, STATEMENT_DELEGATE_ONCREATE);
        //atTheBeginning(classInjector, onStop, STATEMENT_DELEGATE_ONSTOP);
        atTheEnd(classInjector, onPostResume, STATEMENT_DELEGATE_ONPOSTRESUME);
        atTheEnd(classInjector, onSaveInstanceState, STATEMENT_DELEGATE_ONSAVEINSTANCESTATE);
        atTheBeginning(classInjector, onDestroy, STATEMENT_DELEGATE_ONDESTROY);
        afterSuper(classInjector, onStart, String.format(STATEMENT_GET_PRESENTER, presenterFieldName, presenterClassName));
    }

    private void injectDelegateLifeCycleIntoFragment(CtClass ctClass, ClassInjector classInjector, String presenterClassName, String presenterFieldName, String componentPresenterInterfaceName)
            throws Exception {
        CtMethod onCreate = findBestMethod(ctClass, "onCreate", BUNDLE_CLASS);
        CtMethod onActivityCreated = findBestMethod(ctClass, "onActivityCreated", BUNDLE_CLASS);
        CtMethod onResume = findBestMethod(ctClass, "onResume");
        CtMethod onPause = findBestMethod(ctClass, "onPause");
        CtMethod onDestroy = findBestMethod(ctClass, "onDestroy");
        CtMethod onSaveInstanceState = findBestMethod(ctClass, "onSaveInstanceState", BUNDLE_CLASS);

        CtMethod ctMethod = componentProviderMethods.get(componentPresenterInterfaceName);
        CtClass[] parameterTypes = ctMethod.getParameterTypes();
        StringBuilder sb = new StringBuilder();
        if (parameterTypes.length > 2) {
            sb.append(", ");
            for (int i = 2; i < parameterTypes.length; i++) {
                CtClass parameterType = parameterTypes[i];
                String methodName = moduleParamClassToMethodName.get(parameterType.getName());
                sb.append("$s.").append(methodName).append("()");
                if (i < parameterTypes.length - 1) {
                    sb.append(", ");
                }
            }
        }
        String parameters = sb.toString();
        atTheEnd(classInjector, onCreate, String.format(STATEMENT_MAKE_COMPONENT_IN_FRAGMENT, applicationClassName, "getActivity().", ctMethod.getName(), parameters));
        atTheEnd(classInjector, onCreate, String.format(STATEMENT_NEW_DELEGATE_IN_FRAGMENT, ctClass.getName(), applicationClassName, ".getActivity()"));
        afterSuper(classInjector, onResume, String.format(STATEMENT_GET_PRESENTER, presenterFieldName, presenterClassName));
        afterSuper(classInjector, onSaveInstanceState, STATEMENT_DELEGATE_ONSAVEINSTANCESTATE);
        beforeSuper(classInjector, onDestroy, STATEMENT_DELEGATE_ONDESTROY);
        afterSuper(classInjector, onActivityCreated, STATEMENT_DELEGATE_ONCREATE);
    }

    private void injectDelegateLifeCycleIntoCustomView(CtClass ctClass,ClassInjector classInjector)
            throws Exception {
        CtMethod onAttachedToWindow = findBestMethod(ctClass, "onAttachedToWindow");
        CtMethod onDetachedFromWindow = findBestMethod(ctClass, "onDetachedFromWindow");
        atTheEnd(classInjector, onAttachedToWindow, STATEMENT_CALL_ATTACH);

        atTheEnd(classInjector, onAttachedToWindow, STATEMENT_DELEGATE_ONCREATE);
//        atTheBeginning(classInjector, onDetachedFromWindow, STATEMENT_CALL_DETACH);

    }

    /**
     * It is possible that aspectj already manipulated this method, so in this case we should inject
     * our code into {@code methodName_aroundBodyX()} which X is the lowest number of all similar
     * methods to {@code methodName_aroundBody}.
     */
    private CtMethod findBestMethod(CtClass ctClass, String methodName, String... params)
            throws NotFoundException {
        CtMethod baseMethod = null;
        try {
            baseMethod = ctClass.getDeclaredMethod(methodName, stringToCtClass(pool, params));
        } catch (NotFoundException e) {
            for (CtMethod ctMethod : ctClass.getMethods()) {
                if (ctMethod.getName().equals(methodName) &&
                        sameSignature(Arrays.asList(params), ctMethod)) {
                    baseMethod = ctMethod;
                    break;
                }
            }
        }
        CtMethod bestAspectJMethod = null;
        for (CtMethod candidate : ctClass.getDeclaredMethods()) {
            //aspectj is already manipulated this class
            if (isAnAspectJMethod(baseMethod, candidate)) {
                bestAspectJMethod = getLowerNumberOfAspectJMethods(bestAspectJMethod, candidate);
            }
        }

        CtMethod bestMethod = bestAspectJMethod != null ? bestAspectJMethod : baseMethod;
        if (bestMethod != null) {
            log("Best method for " + methodName + " is: [" + bestMethod.getName() + "]");
        }
        return bestMethod;
    }

    private boolean isAnAspectJMethod(CtMethod baseMethod, CtMethod aspectMethodCandidate)
            throws NotFoundException {
        if (aspectMethodCandidate.getName().contains(baseMethod.getName() + ASPECTJ_GEN_METHOD)) {
            //first and last parameter of _aroundBody are baseView and PointCut, so we will ignore them
            boolean areSame = false;
            CtClass[] baseMethodParams = baseMethod.getParameterTypes();
            CtClass[] aspectMethodParams = aspectMethodCandidate.getParameterTypes();
            if (baseMethodParams.length == 0 && aspectMethodParams.length == 2) {
                return true;
            }
            if (aspectMethodParams.length - baseMethodParams.length > 2) {
                return false;
            }
            for (int i = 1; i < aspectMethodParams.length - 1; i++) {
                areSame = baseMethodParams[i - 1].getName().equals(aspectMethodParams[i].getName());
            }
            return areSame;
        }
        return false;
    }

    private CtMethod getLowerNumberOfAspectJMethods(CtMethod best, CtMethod candidate) {
        if (best == null) {
            return candidate;
        }
        int bestNum = getAspectJMethodNumber(best.getName());
        int candidateNum = getAspectJMethodNumber(candidate.getName());
        return bestNum < candidateNum ? best : candidate;
    }

    private int getAspectJMethodNumber(String methodName) {
        String num = methodName.substring(
                methodName.indexOf(ASPECTJ_GEN_METHOD) + ASPECTJ_GEN_METHOD.length());
        return Integer.valueOf(num);
    }

    private void atTheEnd(ClassInjector classInjector, CtMethod method,
                          String statement) throws Exception {
        if (method.getName().contains(ASPECTJ_GEN_METHOD)) {
            statement = statement.replaceAll("\\$s", "\\ajc\\$this");
            String methodName =
                    method.getName().substring(0, method.getName().indexOf(ASPECTJ_GEN_METHOD));
            classInjector.insertMethod(method.getName(),
                    ctClassToString(method.getParameterTypes()))
                    .ifExists()
                    .afterACallTo(methodName, statement).inject().inject();
        } else {
            statement = statement.replaceAll("\\$s", "this");
            classInjector.insertMethod(method.getName(),
                    ctClassToString(method.getParameterTypes()))
                    .ifExistsButNotOverride()
                    .override("{" +
                            "super." + method.getName() + "($$);" +
                            statement +
                            "}").inject()
                    .ifExists()
                    .atTheEnd(statement).inject()
                    .inject();
        }
    }

    private void afterSuper(ClassInjector classInjector, CtMethod method,
                                String statement) throws Exception {
        if (method.getName().contains(ASPECTJ_GEN_METHOD)) {
            String methodName =
                    method.getName().substring(0, method.getName().indexOf(ASPECTJ_GEN_METHOD));
            statement = statement.replaceAll("\\$s", "\\ajc\\$this");
            classInjector.insertMethod(method.getName(),
                    ctClassToString(method.getParameterTypes()))
                    .ifExists()
                    .afterACallTo(methodName, statement).inject().inject();
        } else {
            statement = statement.replaceAll("\\$s", "this");
            classInjector.insertMethod(method.getName(),
                    ctClassToString(method.getParameterTypes()))
                    .ifExistsButNotOverride()
                    .override("{" +
                            "super." + method.getName() + "($$);" +
                            statement +
                            "}").inject()
                    .ifExists()
                    .afterSuper(statement).inject()
                    .inject();
        }
    }

    private void beforeSuper(ClassInjector classInjector, CtMethod method, String statement)
            throws Exception {
        if (method.getName().contains(ASPECTJ_GEN_METHOD)) {
            statement = statement.replaceAll("\\$s", "\\ajc\\$this");
            String methodName =
                    method.getName().substring(0, method.getName().indexOf(ASPECTJ_GEN_METHOD));
            classInjector.insertMethod(method.getName(),
                    ctClassToString(method.getParameterTypes()))
                    .ifExists()
                    .beforeACallTo(methodName, statement).inject().inject();
        } else {
            statement = statement.replaceAll("\\$s", "this");
            classInjector.insertMethod(method.getName(),
                    ctClassToString(method.getParameterTypes()))
                    .ifExistsButNotOverride()
                    .override("{" +
                            statement +
                            "super." + method.getName() + "($$);" +
                            "}").inject()
                    .ifExists()
                    .beforeSuper(statement).inject()
                    .inject();
        }

    }

    private void atTheBeginning(ClassInjector classInjector, CtMethod method,
                                String statement) throws Exception {
        if (method.getName().contains(ASPECTJ_GEN_METHOD)) {
            statement = statement.replaceAll("\\$s", "\\ajc\\$this");
            classInjector.insertMethod(method.getName(),
                    ctClassToString(method.getParameterTypes()))
                    .ifExists()
                    .atTheBeginning(statement).inject().inject();
        } else {
            statement = statement.replaceAll("\\$s", "this");
            classInjector.insertMethod(method.getName(),
                    ctClassToString(method.getParameterTypes()))
                    .ifExistsButNotOverride()
                    .override("{" +
                            statement +
                            "super." + method.getName() + "($$);" +
                            "}").inject()
                    .ifExists()
                    .atTheBeginning(statement).inject()
                    .inject();
        }
    }

    private void insertDelegateField(ClassInjector classInjector, String delegateClassName)
            throws Exception {
        classInjector.insertField(delegateClassName, FIELD_VIEW_DELEGATE)
                .inject();
    }

    private void insertDependencyProviderField(ClassInjector classInjector, String delegateClassName)
            throws Exception {
        classInjector.insertField(delegateClassName, FIELD_DEPENDENCY_PROVIDER)
                .inject();
    }

    private void insertComponentField(ClassInjector classInjector, String delegateClassName)
            throws Exception {
        classInjector.insertField(delegateClassName, FIELD_COMPONENT)
                .inject();
    }

    void log(String message) {
        logger.info("[ANNOTATED-MVP] -> " + message);
    }

    private static class MyMemberValueVisitor implements MemberValueVisitor {
        private String className;

        @Override
        public void visitAnnotationMemberValue(AnnotationMemberValue node) {

        }

        @Override
        public void visitArrayMemberValue(ArrayMemberValue node) {

        }

        @Override
        public void visitBooleanMemberValue(BooleanMemberValue node) {

        }

        @Override
        public void visitByteMemberValue(ByteMemberValue node) {

        }

        @Override
        public void visitCharMemberValue(CharMemberValue node) {

        }

        @Override
        public void visitDoubleMemberValue(DoubleMemberValue node) {

        }

        @Override
        public void visitEnumMemberValue(EnumMemberValue node) {

        }

        @Override
        public void visitFloatMemberValue(FloatMemberValue node) {

        }

        @Override
        public void visitIntegerMemberValue(IntegerMemberValue node) {

        }

        @Override
        public void visitLongMemberValue(LongMemberValue node) {

        }

        @Override
        public void visitShortMemberValue(ShortMemberValue node) {

        }

        @Override
        public void visitStringMemberValue(StringMemberValue node) {

        }

        @Override
        public void visitClassMemberValue(ClassMemberValue node) {
            className = node.getValue();
            System.out.println("PRESENTERCLASS: " + className);
        }

        public String getClassName() {
            return className;
        }
    }
}
