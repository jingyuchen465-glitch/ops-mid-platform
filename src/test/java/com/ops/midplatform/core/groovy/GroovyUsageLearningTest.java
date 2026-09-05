package com.ops.midplatform.core.groovy;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * Groovy 基础语法演示。
 *
 * <p>这个测试类不讲 MCP，也不讲项目里的动态工具执行器，只让学生先看懂 Groovy 最常用的写法。</p>
 */
class GroovyUsageLearningTest {

    @Test
    @DisplayName("01 GroovyShell：执行一段字符串脚本")
    void runScriptByGroovyShell() {
        // GroovyShell 是 Groovy 提供的脚本执行入口。
        // evaluate(...) 会执行字符串里的 Groovy 代码，并把脚本返回值交给 Java。
        Object result = new GroovyShell().evaluate("""
                return "hello groovy"
                """);

        System.out.println("脚本返回结果: " + result);
    }

    @Test
    @DisplayName("02 def：定义变量")
    void defineVariableWithDef() {
        Object result = new GroovyShell().evaluate("""
                // def 表示定义变量，让 Groovy 自己推断类型。
                def message = "hello groovy"
                def count = 3

                // Groovy 可以直接调用 Java 对象的方法。
                def upper = message.toUpperCase()

                return upper + ", count=" + count
                """);

        System.out.println("def 变量演示: " + result);
    }

    @Test
    @DisplayName("03 字符串：普通字符串和插值")
    void stringAndInterpolation() {
        Object result = new GroovyShell().evaluate("""
                def name = "demo-admin"

                // 双引号字符串支持 ${变量名} 插值。
                def text = "current user is ${name}"

                return text
                """);

        System.out.println("字符串插值结果: " + result);
    }

    @Test
    @DisplayName("04 Map：创建和读取")
    void mapCreateAndRead() {
        Object result = new GroovyShell().evaluate("""
                // [key: value] 表示创建 Map。
                def params = [message: "hello mcp", count: 2]

                return [
                  // 点语法读取 Map，写起来短。
                  byDot: params.message,

                  // 中括号读取 Map，更接近 Java 的 map.get("message")。
                  byBracket: params["message"],

                  // 读取数字后可以直接运算。
                  nextCount: params.count + 1
                ]
                """);

        Map<?, ?> map = (Map<?, ?>) result;
        System.out.println("Map 点语法读取: " + map.get("byDot"));
        System.out.println("Map 中括号读取: " + map.get("byBracket"));
        System.out.println("Map 数字运算: " + map.get("nextCount"));
    }

    @Test
    @DisplayName("05 List：创建、过滤、转换、拼接")
    void listCreateFilterCollectJoin() {
        Object result = new GroovyShell().evaluate("""
                // ["a", "b"] 表示创建 List。
                def names = ["hello", "current_time", "echo_dynamic"]

                return [
                  // findAll 表示过滤集合。
                  // { ... } 是闭包，it 表示当前元素。
                  dynamicTools: names.findAll { it.endsWith("_dynamic") },

                  // collect 表示把每个元素转换成另一个值，类似 Java Stream#map。
                  upperNames: names.collect { it.toUpperCase() },

                  // join 表示把 List 拼成字符串。
                  joined: names.join(",")
                ]
                """);

        Map<?, ?> map = (Map<?, ?>) result;
        System.out.println("List findAll 过滤: " + map.get("dynamicTools"));
        System.out.println("List collect 转换: " + map.get("upperNames"));
        System.out.println("List join 拼接: " + map.get("joined"));
    }

    @Test
    @DisplayName("06 if else：条件判断")
    void ifElseCondition() {
        Object result = new GroovyShell().evaluate("""
                def score = 85

                if (score >= 90) {
                  return "优秀"
                } else if (score >= 60) {
                  return "及格"
                } else {
                  return "不及格"
                }
                """);

        System.out.println("if else 判断结果: " + result);
    }

    @Test
    @DisplayName("07 for：循环")
    void forLoop() {
        Object result = new GroovyShell().evaluate("""
                def numbers = [1, 2, 3, 4]
                def total = 0

                // Groovy 的 for 循环可以直接遍历 List。
                for (n in numbers) {
                  total = total + n
                }

                return total
                """);

        System.out.println("for 循环求和结果: " + result);
    }

    @Test
    @DisplayName("08 方法：定义和调用")
    void defineAndCallMethod() {
        Object result = new GroovyShell().evaluate("""
                // Groovy 脚本里也可以定义方法。
                def hello(name) {
                  return "hello " + name
                }

                return hello("student")
                """);

        System.out.println("方法调用结果: " + result);
    }

    @Test
    @DisplayName("09 闭包：一小段可以传递的代码")
    void closureBasicUsage() {
        Object result = new GroovyShell().evaluate("""
                // 闭包是 Groovy 里很重要的语法。
                // 先不要想得太复杂，可以先把它理解成：
                //
                //   一小段可以保存到变量里、也可以传给方法的代码。
                //
                // Java 里有 Lambda，例如：
                //
                //   x -> x + 1
                //
                // Groovy 里的闭包长这样：
                //
                //   { number -> number + 1 }
                //
                // 箭头左边 number 是参数，右边 number + 1 是执行逻辑。
                def addOne = { number -> number + 1 }

                // 调用闭包和调用方法很像，直接 addOne(10) 就可以。
                def addOneResult = addOne(10)

                // 闭包可以有多个参数。
                // 这里 a 和 b 都是参数，返回 a + b。
                def add = { a, b -> a + b }
                def addResult = add(3, 5)

                // 如果闭包只有一个参数，可以不写参数名。
                // Groovy 会默认把当前参数叫做 it。
                def doubleNumber = { it * 2 }
                def doubleResult = doubleNumber(6)

                // 闭包经常和 List 一起使用。
                def names = ["hello", "current_time", "echo_dynamic"]

                // findAll 会把 List 中每个元素交给闭包判断。
                // it.endsWith("_dynamic") 为 true 的元素会被保留下来。
                def dynamicTools = names.findAll { it.endsWith("_dynamic") }

                // collect 会把 List 中每个元素交给闭包转换。
                // 这里把每个工具名转换成大写。
                def upperNames = names.collect { it.toUpperCase() }

                // each 表示遍历。
                // each 通常用于执行动作，不强调返回新集合。
                def printedNames = []
                names.each { name ->
                  printedNames.add("tool=" + name)
                }

                // 闭包还可以访问外部变量。
                // prefix 定义在闭包外面，但闭包里面可以直接用。
                def prefix = "bear"
                def addPrefix = { toolName -> prefix + ":" + toolName }
                def prefixedName = addPrefix("echo_dynamic")

                // 最后返回一个 Map，方便控制台一次性看所有示例结果。
                return [
                  addOneResult: addOneResult,
                  addResult: addResult,
                  doubleResult: doubleResult,
                  dynamicTools: dynamicTools,
                  upperNames: upperNames,
                  printedNames: printedNames,
                  prefixedName: prefixedName
                ]
                """);

        Map<?, ?> map = (Map<?, ?>) result;
        System.out.println("闭包单参数 addOne(10): " + map.get("addOneResult"));
        System.out.println("闭包多参数 add(3, 5): " + map.get("addResult"));
        System.out.println("闭包默认参数 it，doubleNumber(6): " + map.get("doubleResult"));
        System.out.println("闭包传给 findAll 过滤: " + map.get("dynamicTools"));
        System.out.println("闭包传给 collect 转换: " + map.get("upperNames"));
        System.out.println("闭包传给 each 遍历: " + map.get("printedNames"));
        System.out.println("闭包访问外部变量 prefix: " + map.get("prefixedName"));
    }

    @Test
    @DisplayName("10 Binding：Java 给 Groovy 注入变量")
    void bindingInjectsVariables() {
        // Binding 是 Java 传给 Groovy 脚本的一组变量。
        Binding binding = new Binding();
        binding.setVariable("params", Map.of("message", "hello binding"));
        binding.setVariable("userName", "demo-admin");

        // 脚本里没有定义 params 和 userName，但可以直接用，
        // 因为它们已经通过 Binding 注入进去了。
        Object result = new GroovyShell(binding).evaluate("""
                return [
                  message: params.message,
                  currentUser: userName
                ]
                """);

        System.out.println("Binding 注入变量结果: " + result);
    }

    @Test
    @DisplayName("11 Binding：Java 给 Groovy 注入一个可调用对象")
    void bindingInjectsCallableObject() {
        // 前一个例子里，Binding 注入的是普通数据：
        //
        //   params   -> Map
        //   userName -> String
        //
        // 这个例子里，Binding 注入的是一个 Java 对象。
        // 这个对象有方法，所以 Groovy 脚本里可以直接调用它的方法。
        Binding binding = new Binding();

        // 注入普通 Map。
        binding.setVariable("params", Map.of("message", "hello runRequest"));

        // 注入普通字符串。
        binding.setVariable("userName", "demo-admin");

        // 注入 Java 对象。
        //
        // 变量名叫 runRequest，所以脚本里可以写：
        //
        //   runRequest.runRequest(...)
        //
        // 这里的第一个 runRequest 是变量名。
        // 第二个 runRequest 是 Java 对象里的方法名。
        binding.setVariable("runRequest", new DemoRunRequest());

        Object result = new GroovyShell(binding).evaluate("""
                // 这行看起来像 Groovy 自己的能力，其实不是。
                //
                // runRequest 变量来自 Java 的 binding.setVariable(...)
                // .runRequest(...) 调用的是 DemoRunRequest 这个 Java 对象的方法。
                //
                // [message: params.message] 是 Groovy Map，作为方法参数传给 Java。
                def clock = runRequest.runRequest("demo_clock", [message: params.message])

                return [
                  message: params.message,
                  currentUser: userName,
                  clock: clock
                ]
                """);

        System.out.println("Binding 注入 Java 对象后脚本返回: " + result);
    }

    @Test
    @DisplayName("12 parse：先编译成 Script 对象，再手动 run")
    void parseScriptThenRun() {
        Binding binding = new Binding();
        binding.setVariable("params", Map.of("message", "hello parse"));
        binding.setVariable("userName", "demo-admin");

        GroovyShell shell = new GroovyShell(binding);

        /*
         * evaluate(script) 是“编译 + 立刻执行”的快捷写法。
         *
         * parse(script) 则只先把字符串脚本编译成 Script 对象。
         * 真正执行发生在 compiledScript.run() 这一行。
         *
         * 项目里的 GroovyScriptEngine 使用 parse，是为了把“编译脚本”和“运行脚本”
         * 在代码上拆清楚，也方便给编译阶段挂安全配置 CompilerConfiguration。
         */
        Script compiledScript = shell.parse("""
                def text = params.message.toUpperCase()

                return [
                  currentUser: userName,
                  text: text
                ]
                """);

        Object result = compiledScript.run();

        System.out.println("parse 后 run 的脚本返回: " + result);
    }

    @Test
    @DisplayName("13 ImportCustomizer：Java 预先导入类，脚本里就不用写 import")
    void importCustomizerAllowsScriptUseClassesWithoutImport() {
        CompilerConfiguration configuration = new CompilerConfiguration();

        /*
         * ImportCustomizer 对应项目里的 createCompilerConfiguration()。
         *
         * addImports 表示导入具体类：
         *   import groovy.json.JsonOutput
         *   import groovy.json.JsonSlurper
         *
         * addStarImports 表示导入整个包：
         *   import java.util.*
         *   import java.math.*
         */
        ImportCustomizer imports = new ImportCustomizer();
        imports.addImports("groovy.json.JsonOutput", "groovy.json.JsonSlurper");
        imports.addStarImports("java.util", "java.math");
        configuration.addCompilationCustomizers(imports);

        GroovyShell shell = new GroovyShell(configuration);

        Object result = shell.evaluate("""
                // 这里没有写 import groovy.json.JsonOutput，
                // 但 Java 侧已经通过 imports.addImports(...) 预先导入了。
                def jsonText = JsonOutput.toJson([name: "bear", count: 2])

                // 这里也没有写 import groovy.json.JsonSlurper。
                def parsed = new JsonSlurper().parseText(jsonText)

                // 这里没有写 import java.util.Date。
                // 因为 Java 侧 addStarImports("java.util") 等价于预先导入 java.util.*。
                def now = new Date(0)

                // 这里没有写 import java.math.BigDecimal。
                // 因为 Java 侧 addStarImports("java.math") 等价于预先导入 java.math.*。
                def price = new BigDecimal("19.90")
                def total = price.multiply(new BigDecimal(parsed.count.toString()))

                // 这里没有写 import java.util.List / Map / ArrayList / HashMap。
                // 因为 Java 侧 addStarImports("java.util") 已经预先导入 java.util.*。
                List<String> toolNames = new ArrayList<>()
                toolNames.add("hello")
                toolNames.add("calculate")

                Map<String, Object> summary = new HashMap<>()
                summary.put("firstTool", toolNames.get(0))
                summary.put("toolCount", toolNames.size())

                return [
                  jsonText: jsonText,
                  parsedName: parsed.name,
                  dateTime: now.time,
                  total: total,
                  toolNames: toolNames,
                  firstTool: summary.get("firstTool"),
                  toolCount: summary.get("toolCount")
                ]
                """);

        System.out.println("默认导入后脚本可以直接使用类: " + result);
    }

    @Test
    @DisplayName("14 没有默认导入：脚本自己写 import 或完整类名")
    void scriptCanUseImportOrFullClassNameWithoutImportCustomizer() {
        GroovyShell shell = new GroovyShell();

        Object result = shell.evaluate("""
                // 方式一：脚本自己写 import。
                import groovy.json.JsonOutput
                import java.util.ArrayList
                import java.util.HashMap
                import java.math.BigDecimal

                def jsonText = JsonOutput.toJson([name: "bear"])

                def toolNames = new ArrayList()
                toolNames.add("hello")
                toolNames.add("calculate")

                def summary = new HashMap()
                summary.put("toolCount", toolNames.size())

                def price = new BigDecimal("19.90")

                // 方式二：不写 import，直接写完整类名。
                def parsed = new groovy.json.JsonSlurper().parseText(jsonText)
                def otherNames = new java.util.ArrayList()
                otherNames.add(parsed.name)

                return [
                  jsonText: jsonText,
                  toolNames: toolNames,
                  toolCount: summary.get("toolCount"),
                  price: price,
                  otherNames: otherNames
                ]
                """);

        System.out.println("没有默认导入时脚本自己 import 或写完整类名: " + result);
    }

    /**
     * 给 bindingInjectsCallableObject 用的教学版 Java 对象。
     *
     * <p>它故意不使用项目里的 ScriptRunRequest，只演示一件事：
     * Java 对象被 Binding 注入后，Groovy 脚本可以调用它的 public 方法。</p>
     */
    static class DemoRunRequest {

        public Map<String, Object> runRequest(String key, Map<String, Object> params) {
            System.out.println("Java 对象 DemoRunRequest 被 Groovy 调用了, key=" + key + ", params=" + params);
            return Map.of(
                    "key", key,
                    "params", params,
                    "mockResult", "这是 Java 方法返回给 Groovy 的结果"
            );
        }
    }

}
