package com.bear.mcp.single.groovy;

import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.control.customizers.SecureASTCustomizer;

import java.util.List;

public final class GroovySecurityCustomizer {

    private static final List<String> BLOCKED_METHODS = List.of(
            "execute", "exit", "halt", "load", "loadLibrary", "getRuntime", "exec", "start",
            "forName", "newInstance", "getClassLoader", "getMethod", "getDeclaredMethod", "invoke"
    );

    private static final List<String> BLOCKED_CLASSES = List.of(
            "java.lang.Runtime", "java.lang.ProcessBuilder", "java.lang.System", "java.lang.Thread",
            "java.io.File", "java.nio.file.Files", "java.nio.file.Paths", "java.net.Socket",
            "groovy.lang.GroovyShell", "groovy.lang.GroovyClassLoader"
    );

    private GroovySecurityCustomizer() {
    }

    public static SecureASTCustomizer create() {
        SecureASTCustomizer customizer = new SecureASTCustomizer();
        customizer.setPackageAllowed(false);
        customizer.setStaticImportsWhitelist(List.of());
        customizer.setStaticStarImportsWhitelist(List.of());
        customizer.setReceiversBlackList(BLOCKED_CLASSES);
        customizer.addExpressionCheckers(expression -> {
            if (expression instanceof MethodCallExpression methodCall) {
                String methodName = methodCall.getMethodAsString();
                if (methodName != null && BLOCKED_METHODS.contains(methodName)) {
                    throw new SecurityException("禁止调用方法: " + methodName);
                }
            }
            return true;
        });
        return customizer;
    }

    public static void validate(String script) {
        if (script == null || script.isBlank()) {
            throw new IllegalArgumentException("脚本不能为空");
        }
        for (String blockedClass : BLOCKED_CLASSES) {
            String simpleName = blockedClass.substring(blockedClass.lastIndexOf('.') + 1);
            if (script.contains(blockedClass) || script.contains(simpleName + ".")) {
                throw new SecurityException("脚本包含禁止使用的类: " + blockedClass);
            }
        }
        for (String blockedMethod : BLOCKED_METHODS) {
            if (script.matches("(?s).*\\." + blockedMethod + "\\s*\\(.*")) {
                throw new SecurityException("脚本包含禁止调用的方法: " + blockedMethod);
            }
        }
    }
}
