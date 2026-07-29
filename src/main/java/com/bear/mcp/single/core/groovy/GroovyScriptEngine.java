package com.bear.mcp.single.core.groovy;

import com.bear.mcp.single.core.datasource.ExternalDataSourceSqlExecutor;
import com.bear.mcp.single.core.request.RequestConfigService;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import jakarta.annotation.PreDestroy;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class GroovyScriptEngine {

    private static final Logger log = LoggerFactory.getLogger(GroovyScriptEngine.class);

    /**
     * 动态脚本不能直接随便访问外部 HTTP 地址。
     * 如果脚本确实要调用企业接口，必须通过 mcp_request_config 中提前登记好的配置。
     */
    private final RequestConfigService requestConfigService;

    /**
     * 动态脚本访问外部数据库时，只能通过受控的 runSql 入口。
     */
    private final ExternalDataSourceSqlExecutor sqlExecutor;

    /**
     * Groovy 脚本可能由外部配置产生，不能直接占用 Web 请求线程执行。
     * 这里用独立线程池执行脚本，方便做超时控制，也避免脚本卡住主请求线程。
     */
    private final java.util.concurrent.ExecutorService executor = Executors.newFixedThreadPool(8);

    /**
     * Groovy 编译配置。
     * 里面包含默认 import 和安全限制，所有动态脚本编译时都会使用这套规则。
     */
    private final CompilerConfiguration compilerConfiguration;

    public GroovyScriptEngine(RequestConfigService requestConfigService,
                              ExternalDataSourceSqlExecutor sqlExecutor) {
        this.requestConfigService = requestConfigService;
        this.sqlExecutor = sqlExecutor;

        /*
         * 构造引擎时只创建一次编译配置。
         * 后面每次执行脚本都会复用这套配置，避免把默认导入和安全规则散落在执行逻辑里。
         */
        this.compilerConfiguration = createCompilerConfiguration();
    }

    /**
     * 执行动态工具中的 Groovy 脚本。
     *
     * @param script  数据库中保存的 Groovy 脚本文本
     * @param context 当前工具调用上下文，包括用户、工具名、入参和允许访问的白名单
     * @return 脚本执行结果，包含成功/失败、返回值或错误信息、耗时
     */
    public ScriptResult execute(String script, ScriptContext context) {
        long startedAt = System.currentTimeMillis();
        try {
            /*
             * 第一层安全校验：在真正交给 Groovy 编译前，先做一次字符串级别的快速检查。
             * 例如脚本里直接出现 Runtime.exec、System.exit、new File 等危险内容，会提前拒绝。
             */
            GroovySecurityCustomizer.validate(script);

            /*
             * Binding 是脚本运行时的变量表。
             * 放进去的 params、userId、runRequest 等变量，脚本里可以直接使用。
             */
            Binding binding = createBinding(context);

            Future<Object> future = executor.submit(() -> {
                /*
                 * GroovyShell 负责把脚本文本编译成 Script 对象。
                 * compilerConfiguration 会在编译阶段继续做 import 处理和 AST 安全限制。
                 */
                GroovyShell shell = new GroovyShell(binding, compilerConfiguration);
                Script compiledScript = shell.parse(script);

                /*
                 * 真正运行脚本。
                 * 脚本最后一行的表达式，或者 return 的内容，就是这里返回的 result。
                 */
                return compiledScript.run();
            });

            /*
             * 动态脚本必须有超时时间。
             * context 里没有指定时，默认最多执行 30 秒，避免死循环或慢脚本拖垮服务。
             */
            long timeout = context.timeoutMs() > 0 ? context.timeoutMs() : 30000;
            Object result = future.get(timeout, TimeUnit.MILLISECONDS);
            return ScriptResult.success(result, System.currentTimeMillis() - startedAt);
        } catch (TimeoutException e) {
            return ScriptResult.failure("脚本执行超时", System.currentTimeMillis() - startedAt);
        } catch (Exception e) {
            log.warn("Groovy script failed, toolName={}, error={}", context.toolName(), e.getMessage());
            return ScriptResult.failure(e.getMessage(), System.currentTimeMillis() - startedAt);
        }
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdownNow();
    }

    /**
     * 创建 Groovy 编译配置。
     *
     * 这一步不是执行脚本，而是在规定“脚本以后怎么被编译”：
     * 1. 默认导入常用 JSON、集合、时间和数学类，减少课堂脚本里的 import 噪音。
     * 2. 挂上 GroovySecurityCustomizer，在 AST 编译阶段限制危险类和危险方法。
     */
    private CompilerConfiguration createCompilerConfiguration() {
        CompilerConfiguration configuration = new CompilerConfiguration();

        ImportCustomizer imports = new ImportCustomizer();

        /*
         * 允许脚本直接使用 JsonOutput、JsonSlurper。
         * 这样脚本里可以写 JsonOutput.toJson(data)，不用先写 import。
         */
        imports.addImports("groovy.json.JsonOutput", "groovy.json.JsonSlurper");

        /*
         * 默认导入常用包。
         * 例如脚本里可以直接使用 List、Map、Date、BigDecimal 等类型。
         */
        imports.addStarImports("java.util", "java.time", "java.math");

        configuration.addCompilationCustomizers(imports, GroovySecurityCustomizer.create());
        return configuration;
    }

    /**
     * 构造脚本可见的变量。
     *
     * 示例脚本中可以直接写：
     *
     * <pre>
     * def name = params.name
     * def result = runRequest.runRequest("demo_clock", params)
     * def rows = runSql.runSql(1L, "select id, name from user limit 10")
     * return [userId: userId, data: result]
     * </pre>
     */
    private Binding createBinding(ScriptContext context) {
        Binding binding = new Binding();
        binding.setVariable("params", context.params() != null ? context.params() : Map.of());
        binding.setVariable("userId", context.userId());
        binding.setVariable("userName", context.userName());
        binding.setVariable("toolName", context.toolName());

        /*
         * runRequest 是暴露给脚本的受控调用入口。
         * 脚本不能绕过它直接访问任意企业接口，只能调用当前动态工具提前关联过的 request config。
         */
        binding.setVariable("runRequest", new ScriptRunRequest(requestConfigService, context.linkedRequestKeys()));
        binding.setVariable("runSql", new ScriptRunSql(sqlExecutor, context.linkedDataSourceIds()));
        return binding;
    }

    /**
     * 暴露给 Groovy 脚本使用的请求调用对象。
     *
     * 它看起来像一个普通工具对象，但内部会先检查 key 是否在白名单里。
     * 白名单来自 mcp_dynamic_tool.linked_request_keys。
     */
    public static class ScriptRunRequest {
        private final RequestConfigService requestConfigService;

        /**
         * 当前动态工具允许调用的请求配置 key 列表。
         */
        private final List<String> allowedKeys;

        public ScriptRunRequest(RequestConfigService requestConfigService, List<String> allowedKeys) {
            this.requestConfigService = requestConfigService;
            this.allowedKeys = allowedKeys != null ? allowedKeys : List.of();
        }

        /**
         * 在 Groovy 脚本中调用企业请求配置。
         *
         * @param key    mcp_request_config.config_key
         * @param params 请求参数
         * @return 请求配置执行后的结果
         */
        public Object runRequest(String key, Map<String, Object> params) {
            if (!allowedKeys.contains(key)) {
                throw new IllegalArgumentException("未关联的请求配置: " + key);
            }

            return requestConfigService.execute(key, params != null ? params : Map.of());
        }
    }

    /**
     * 暴露给 Groovy 脚本使用的数据源查询对象。
     *
     * <p>它只负责白名单校验，SQL 只读校验、长度限制和最大返回行数由 ExternalDataSourceSqlExecutor 统一处理。</p>
     */
    public static class ScriptRunSql {
        private final ExternalDataSourceSqlExecutor sqlExecutor;
        private final List<Long> allowedDataSourceIds;

        public ScriptRunSql(ExternalDataSourceSqlExecutor sqlExecutor, List<Long> allowedDataSourceIds) {
            this.sqlExecutor = sqlExecutor;
            this.allowedDataSourceIds = allowedDataSourceIds != null ? allowedDataSourceIds : List.of();
        }

        /**
         * 当前工具只绑定一个数据源时，可以省略 datasourceId。
         */
        public Object runSql(String sql) {
            if (allowedDataSourceIds.isEmpty()) {
                throw new IllegalArgumentException("当前动态工具未关联数据源");
            }
            if (allowedDataSourceIds.size() > 1) {
                throw new IllegalArgumentException("当前动态工具关联了多个数据源，请显式传入 datasourceId");
            }
            return sqlExecutor.query(allowedDataSourceIds.get(0), sql);
        }

        /**
         * 在 Groovy 脚本中查询已绑定的数据源。
         */
        public Object runSql(Number datasourceId, String sql) {
            if (datasourceId == null) {
                throw new IllegalArgumentException("datasourceId 不能为空");
            }
            Long id = datasourceId.longValue();
            if (!allowedDataSourceIds.contains(id)) {
                throw new IllegalArgumentException("未关联的数据源: " + id);
            }
            return sqlExecutor.query(id, sql);
        }
    }
}
