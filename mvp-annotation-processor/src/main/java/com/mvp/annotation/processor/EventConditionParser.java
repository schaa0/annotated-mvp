package com.mvp.annotation.processor;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Created by Andy on 28.12.2016.
 */

public class EventConditionParser {

    private final Elements elementUtil;
    private final Types typeUtil;

    public EventConditionParser(Elements elementUtil, Types typeUtil) {
        this.elementUtil = elementUtil;
        this.typeUtil = typeUtil;
    }

    public String parse(TypeElement typeElement, String condition, TypeMirror dataType) {
        if (condition.equals("")) {
            return null;
        }
        Expression expression = parseExpression(condition);
        if (expression != null) {
            evaluateExpression(expression, typeElement, dataType);
            return expression.toString();
        }
        SimpleExpression simpleExpression = parseSimpleExpression(condition);
        if (simpleExpression != null) {
            evaluateSimpleExpression(simpleExpression, typeElement, dataType);
            return simpleExpression.toString();
        }
        throw new IllegalStateException();
    }

    private void evaluateSimpleExpression(SimpleExpression simpleExpression, TypeElement typeElement, TypeMirror dataType) {
        TypeMirror returnType = parseReturnType(simpleExpression.getObject(), simpleExpression.isMethod(), typeElement, dataType, simpleExpression.getMemberName());
        if (!(returnType.toString().equals(boolean.class.getName()) || returnType.toString().equals(Boolean.class.getName()))){
            throw new IllegalStateException();
        }
    }

    private SimpleExpression parseSimpleExpression(String condition) {
        Pattern pattern = Pattern.compile("((!)?(#|this)+\\.([A-Za-z_]+)(\\(\"?([A-Za-z_]+)?\"?\\))?)");
        Matcher matcher = pattern.matcher(condition);
        if (matcher.find()) {
            String expression = matcher.group(0);
            String leftObject = matcher.group(3);
            String memberName = matcher.group(4);
            boolean isMethod = matcher.group(5) != null && matcher.group(5).contains("(") && matcher.group(5).contains(")");
            String parameterName = matcher.group(6);
            return new SimpleExpression(expression, leftObject, memberName, isMethod);
        }
        return null;
    }

    private Expression parseExpression(String condition) {
        Pattern pattern = Pattern.compile("((#|this)\\.([a-zA-Z_]+)(\\(\\))?)(\\s*==\\s*)!?((#|this)\\.([a-zA-Z_]+)(\\(\\))?)");
        Matcher matcher = pattern.matcher(condition);
        if (matcher.find()) {
            String leftExpression = matcher.group(0);
            String leftObject = matcher.group(2);
            String leftMemberExpression = matcher.group(3);
            boolean leftMemberExpressionIsMethod = matcher.group(4) != null && matcher.group(4).equals("()");
            String comparison = matcher.group(5);
            String rightExpression = matcher.group(6);
            String rightObject = matcher.group(7);
            String rightMemberExpression = matcher.group(8);
            boolean rightMemberExpressionIsMethod = matcher.group(9) != null && matcher.group(9).equals("()");
            Expression expression = new Expression(leftExpression, leftObject, leftMemberExpression, leftMemberExpressionIsMethod, comparison, rightExpression, rightObject, rightMemberExpression, rightMemberExpressionIsMethod);
            return expression;
        }
        return null;
    }

    private void evaluateExpression(Expression expression, TypeElement typeElement, TypeMirror dataType) {
        String leftMemberExpression = expression.getLeftMemberExpression();
        String rightMemberExpression = expression.getRightMemberExpression();
        String leftObject = expression.getLeftObject();
        String rightObject = expression.getRightObject();
        boolean leftMemberExpressionIsMethod = expression.isLeftMemberExpressionIsMethod();
        boolean rightMemberExpressionIsMethod = expression.isRightMemberExpressionIsMethod();
        TypeMirror leftType = parseReturnType(leftObject, leftMemberExpressionIsMethod, typeElement, dataType, leftMemberExpression);
        TypeMirror rightType = parseReturnType(rightObject, rightMemberExpressionIsMethod, typeElement, dataType, rightMemberExpression);
        if (!leftType.toString().equals(rightType.toString())) {
            throw new IllegalStateException();
        }
    }

    private TypeMirror parseReturnType(String memberObject, boolean isMethod, TypeElement typeElement, TypeMirror dataType, String leftMemberExpression) {
        TypeElement element = memberObject.equals("this") ? typeElement : elementUtil.getTypeElement(dataType.toString());
        List<? extends Element> enclosedElements = element.getEnclosedElements();
        if (isMethod) {
            ExecutableElement method = getMethod(leftMemberExpression, enclosedElements);
            if (method == null) {
                throw new IllegalStateException();
            }
            return method.getReturnType();

        } else {
            VariableElement field = getField(leftMemberExpression, enclosedElements);
            if (field == null) {
                throw new IllegalStateException();
            }
            return field.asType();
        }
    }

    private ExecutableElement getMethod(String methodName, List<? extends Element> enclosedElements) {
        for (Element enclosedElement : enclosedElements) {
            if (enclosedElement.getKind() == ElementKind.METHOD) {
                ExecutableElement executableElement = (ExecutableElement) enclosedElement;
                if (executableElement.getSimpleName().toString().equals(methodName)) {
                    if (!executableElement.getModifiers().contains(Modifier.PUBLIC))
                        throw new IllegalStateException();
                    return executableElement;
                }
            }
        }
        return null;
    }

    private VariableElement getField(String fieldName, List<? extends Element> enclosedElements) {
        for (Element enclosedElement : enclosedElements) {
            if (enclosedElement.getKind() == ElementKind.FIELD) {
                if (enclosedElement.getSimpleName().toString().equals(fieldName)) {
                    if (!enclosedElement.getModifiers().contains(Modifier.PUBLIC))
                        throw new IllegalStateException();
                    return (VariableElement) enclosedElement;
                }
            }
        }
        return null;
    }

    public static class Expression {
        private final String leftExpression;
        private final String leftObject;
        private final String leftMemberExpression;
        private final boolean leftMemberExpressionIsMethod;
        private final String rightExpression;
        private final String comparison;
        private final String rightObject;
        private final String rightMemberExpression;
        private final boolean rightMemberExpressionIsMethod;

        public Expression(String leftExpression, String leftObject, String leftMemberExpression, boolean leftMemberExpressionIsMethod, String comparison, String rightExpression, String rightObject, String rightMemberExpression, boolean rightMemberExpressionIsMethod) {
            this.leftExpression = leftExpression;
            this.leftObject = leftObject;
            this.leftMemberExpression = leftMemberExpression;
            this.leftMemberExpressionIsMethod = leftMemberExpressionIsMethod;
            this.rightExpression = rightExpression;
            this.comparison = comparison;
            this.rightObject = rightObject;
            this.rightMemberExpression = rightMemberExpression;
            this.rightMemberExpressionIsMethod = rightMemberExpressionIsMethod;
        }

        public String getLeftObject() {
            return leftObject;
        }

        public String getRightObject() {
            return rightObject;
        }

        public String getLeftExpression() {
            return leftExpression;
        }

        public String getRightExpression() {
            return rightExpression;
        }

        public String getComparison() {
            return comparison;
        }

        public String getLeftMemberExpression() {
            return leftMemberExpression;
        }

        public boolean isLeftMemberExpressionIsMethod() {
            return leftMemberExpressionIsMethod;
        }

        public String getRightMemberExpression() {
            return rightMemberExpression;
        }

        public boolean isRightMemberExpressionIsMethod() {
            return rightMemberExpressionIsMethod;
        }

        @Override
        public String toString() {
            return leftExpression.replaceAll("#", "data").replaceAll("this.", "e.get().");
        }
    }

    static class SimpleExpression {

        private String expression;
        private final String object;
        private final String memberName;
        private final boolean isMethod;

        public SimpleExpression(String expression, String object, String memberName, boolean isMethod){
            this.expression = expression;
            this.object = object;
            this.memberName = memberName;
            this.isMethod = isMethod;
        }

        public String getObject() {
            return object;
        }

        public String getMemberName() {
            return memberName;
        }

        public boolean isMethod() {
            return isMethod;
        }

        @Override
        public String toString() {
            return expression.replaceAll("#", "data").replaceAll("this.", "e.get().");
        }
    }

}
