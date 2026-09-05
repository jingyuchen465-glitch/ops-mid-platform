package com.ops.midplatform.core.groovy;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.control.customizers.SecureASTCustomizer;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Groovy 动态脚本安全限制。
 *
 * <p>它不是完整沙箱，但能在课堂版里拦住最危险的一批操作：
 * 执行系统命令、退出 JVM、文件访问、反射、再创建 GroovyShell 等。</p>
 */
public final class GroovySecurityCustomizer {

    /**
     * 禁止调用的方法名。
     *
     * <p>重点封堵网络外联（openConnection/openStream/http send）、反射读取字段、
     * 自定义类加载（loadClass/defineClass）等绕过 runRequest 受控入口的路径。</p>
     */
    private static final List<String> BLOCKED_METHODS = List.of(
            "execute", "exit", "halt", "load", "loadLibrary", "getRuntime", "exec", "start",
            "forName", "newInstance", "getClassLoader", "getClass", "getMethod", "getDeclaredMethod",
            "invoke", "newProxyInstance",
            "openConnection", "openStream", "getContent", "send", "newHttpClient",
            "loadClass", "defineClass", "toURL",
            "getDeclaredField", "getField", "setAccessible",
            "getResource", "getResourceAsStream",
            "getConstructor", "getDeclaredConstructor"
    );

    /**
     * 禁止脚本直接使用的类。
     *
     * <p>除命令执行/文件类外，额外封堵网络类（java.net.URL、HttpURLConnection、JDK HttpClient）、
     * InetAddress、内部类加载器、反射/Bean 间接调用、反序列化与 JNDI/RMI 等逃逸入口。</p>
     */
    private static final List<String> BLOCKED_CLASSES = List.of(
            "java.lang.Runtime", "java.lang.ProcessBuilder", "java.lang.System", "java.lang.Thread",
            "java.io.File", "java.nio.file.Files", "java.nio.file.Paths", "java.net.Socket",
            "groovy.lang.GroovyShell", "groovy.lang.GroovyClassLoader",
            "java.net.URL", "java.net.URLConnection", "java.net.HttpURLConnection",
            "java.net.http.HttpClient", "java.net.http.HttpRequest", "java.net.http.HttpResponse",
            "java.net.URI", "java.net.InetAddress", "java.net.InetSocketAddress",
            "java.net.DatagramSocket", "java.net.ServerSocket",
            "java.lang.ClassLoader", "java.net.URLClassLoader",
            "java.lang.reflect.AccessibleObject", "java.lang.reflect.Method",
            "java.lang.reflect.Field", "java.lang.reflect.Constructor",
            "java.beans.Expression", "java.beans.Statement",
            "java.io.ObjectInputStream",
            "javax.naming.InitialContext", "javax.naming.Context",
            "javax.naming.directory.InitialDirContext",
            "java.rmi.registry.LocateRegistry"
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
            if (expression instanceof ConstructorCallExpression constructorCall) {
                assertClassAllowed(constructorCall.getType());
            }
            if (expression instanceof StaticMethodCallExpression staticMethodCall) {
                assertClassAllowed(staticMethodCall.getOwnerType());
            }
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
            if (script.contains(blockedClass)
                    || script.contains(simpleName + ".")
                    || containsBlockedConstructor(script, blockedClass)
                    || containsBlockedConstructor(script, simpleName)) {
                throw new SecurityException("脚本包含禁止使用的类: " + blockedClass);
            }
        }
        for (String blockedMethod : BLOCKED_METHODS) {
            if (script.matches("(?s).*\\." + blockedMethod + "\\s*\\(.*")) {
                throw new SecurityException("脚本包含禁止调用的方法: " + blockedMethod);
            }
        }
    }

    private static boolean containsBlockedConstructor(String script, String className) {
        return Pattern.compile("(?s).*\\bnew\\s+" + Pattern.quote(className) + "\\s*\\(.*")
                .matcher(script)
                .matches();
    }

    private static void assertClassAllowed(ClassNode classNode) {
        if (classNode == null) {
            return;
        }
        String className = classNode.getName();
        String simpleName = classNode.getNameWithoutPackage();
        for (String blockedClass : BLOCKED_CLASSES) {
            String blockedSimpleName = blockedClass.substring(blockedClass.lastIndexOf('.') + 1);
            if (blockedClass.equals(className) || blockedSimpleName.equals(simpleName)) {
                throw new SecurityException("禁止使用类: " + blockedClass);
            }
        }
    }
}
