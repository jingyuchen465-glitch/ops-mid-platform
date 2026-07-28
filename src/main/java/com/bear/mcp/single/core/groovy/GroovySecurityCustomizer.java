package com.bear.mcp.single.core.groovy;

import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.control.customizers.SecureASTCustomizer;

import java.util.List;

/**
 * Groovy 动态脚本安全限制。
 *
 * <p>它不是完整沙箱，但能在课堂版里拦住最危险的一批操作：
 * 执行系统命令、退出 JVM、文件访问、反射、再创建 GroovyShell 等。</p>
 */
public final class GroovySecurityCustomizer {

    /**
     * 禁止调用的方法名。
     */
    private static final List<String> BLOCKED_METHODS = List.of(
            "execute", "exit", "halt", "load", "loadLibrary", "getRuntime", "exec", "start",
            "forName", "newInstance", "getClassLoader", "getMethod", "getDeclaredMethod", "invoke"
    );

    /**
     * 禁止脚本直接使用的类。
     */
    private static final List<String> BLOCKED_CLASSES = List.of(
            "java.lang.Runtime", "java.lang.ProcessBuilder", "java.lang.System", "java.lang.Thread",
            "java.io.File", "java.nio.file.Files", "java.nio.file.Paths", "java.net.Socket",
            "groovy.lang.GroovyShell", "groovy.lang.GroovyClassLoader"
    );

    private GroovySecurityCustomizer() {
    }

    /**
     * 创建 Groovy AST 编译阶段的安全检查器。
     *
     * <p>这层检查发生在 shell.parse(script) 阶段，脚本还没真正 run 就会先被检查。</p>
     */
    public static SecureASTCustomizer create() {
        SecureASTCustomizer customizer = new SecureASTCustomizer();

        /*
         * 不允许脚本声明 package，减少脚本伪装成项目类的可能。
         */
        customizer.setPackageAllowed(false);

        /*
         * 不允许脚本自己写静态 import。
         */
        customizer.setStaticImportsWhitelist(List.of());
        customizer.setStaticStarImportsWhitelist(List.of());

        /*
         * 禁止危险类作为方法调用接收者。
         */
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

    /**
     * 在编译前做一层字符串级快速校验。
     *
     * <p>它和 AST 校验互补：先用简单规则挡掉明显危险脚本，再交给 Groovy 编译器做结构化检查。</p>
     */
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
