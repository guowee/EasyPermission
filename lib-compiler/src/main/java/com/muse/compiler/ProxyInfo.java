package com.muse.compiler;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/**
 * Created by GuoWee on 2018/3/15.
 */

public class ProxyInfo {

    private String packageName;
    private String proxyClassName;

    private TypeElement typeElement;

    Map<Integer, String> grantedMethodMap = new HashMap<>();
    Map<Integer, String> deniedMethodMap = new HashMap<>();

    public static final String PROXY = "PermissionProxy";

    public ProxyInfo(Elements elements, TypeElement classElement) {
        PackageElement packageElement = elements.getPackageOf(classElement);
        String packageName = packageElement.getQualifiedName().toString();
        // class name
        String className = ClassValidator.getClassName(classElement, packageName);

        this.packageName = packageName;
        this.proxyClassName = className + "$$" + PROXY;

    }

    public String getProxyFullClassName() {
        return packageName + "." + proxyClassName;
    }

    public String generateJavaCode() {
        StringBuilder builder = new StringBuilder();
        builder.append("// Generated code. Do not modify!\n");
        builder.append("package ").append(packageName).append(";\n\n");
        builder.append("import com.muse.permission.*;\n");
        builder.append('\n');

        builder.append("public class ").append(proxyClassName).append(" implements " + ProxyInfo.PROXY + "<" + typeElement.getSimpleName() + ">");
        builder.append(" {\n");

        generateMethods(builder);
        builder.append('\n');

        builder.append("}\n");
        return builder.toString();


    }

    private void generateMethods(StringBuilder builder) {
        generateSuccessMethod(builder);
        generateFailMethod(builder);
    }

    private void generateFailMethod(StringBuilder builder) {
        builder.append("@Override\n ");
        builder.append("public void denied(" + typeElement.getSimpleName() + " source , int requestCode) {\n");
        builder.append("switch(requestCode) {\n");
        for (int code : deniedMethodMap.keySet()) {
            builder.append("case " + code + ":\n");
            builder.append("source." + deniedMethodMap.get(code) + "();");
            builder.append("break;");
        }

        builder.append("}\n");
        builder.append("  }\n");
    }

    private void generateSuccessMethod(StringBuilder builder) {
        builder.append("@Override\n ");
        builder.append("public void grant(" + typeElement.getSimpleName() + " source , int requestCode) {\n");
        builder.append("switch(requestCode) {\n");
        for (int code : grantedMethodMap.keySet()) {
            builder.append("case " + code + ":\n");
            builder.append("source." + grantedMethodMap.get(code) + "();");
            builder.append("break;");
        }

        builder.append("}\n");
        builder.append("  }\n");

    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getProxyClassName() {
        return proxyClassName;
    }

    public void setProxyClassName(String proxyClassName) {
        this.proxyClassName = proxyClassName;
    }

    public TypeElement getTypeElement() {
        return typeElement;
    }

    public void setTypeElement(TypeElement typeElement) {
        this.typeElement = typeElement;
    }
}
