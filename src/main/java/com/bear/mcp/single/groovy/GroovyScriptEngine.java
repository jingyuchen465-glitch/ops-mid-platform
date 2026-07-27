package com.bear.mcp.single.groovy;

import com.bear.mcp.single.request.RequestConfigService;
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

    private final RequestConfigService requestConfigService;
    private final java.util.concurrent.ExecutorService executor = Executors.newFixedThreadPool(8);
    private final CompilerConfiguration compilerConfiguration;

    public GroovyScriptEngine(RequestConfigService requestConfigService) {
        this.requestConfigService = requestConfigService;
        this.compilerConfiguration = createCompilerConfiguration();
    }

    public ScriptResult execute(String script, ScriptContext context) {
        long startedAt = System.currentTimeMillis();
        try {
            GroovySecurityCustomizer.validate(script);
            Binding binding = createBinding(context);
            Future<Object> future = executor.submit(() -> {
                GroovyShell shell = new GroovyShell(binding, compilerConfiguration);
                Script compiledScript = shell.parse(script);
                return compiledScript.run();
            });
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

    private CompilerConfiguration createCompilerConfiguration() {
        CompilerConfiguration configuration = new CompilerConfiguration();
        ImportCustomizer imports = new ImportCustomizer();
        imports.addImports("groovy.json.JsonOutput", "groovy.json.JsonSlurper");
        imports.addStarImports("java.util", "java.time", "java.math");
        configuration.addCompilationCustomizers(imports, GroovySecurityCustomizer.create());
        return configuration;
    }

    private Binding createBinding(ScriptContext context) {
        Binding binding = new Binding();
        binding.setVariable("params", context.params() != null ? context.params() : Map.of());
        binding.setVariable("userId", context.userId());
        binding.setVariable("userName", context.userName());
        binding.setVariable("toolName", context.toolName());
        binding.setVariable("runRequest", new ScriptRunRequest(requestConfigService, context.linkedRequestKeys()));
        return binding;
    }

    public static class ScriptRunRequest {
        private final RequestConfigService requestConfigService;
        private final List<String> allowedKeys;

        public ScriptRunRequest(RequestConfigService requestConfigService, List<String> allowedKeys) {
            this.requestConfigService = requestConfigService;
            this.allowedKeys = allowedKeys != null ? allowedKeys : List.of();
        }

        public Object runRequest(String key, Map<String, Object> params) {
            if (!allowedKeys.contains(key)) {
                throw new IllegalArgumentException("未关联的请求配置: " + key);
            }
            return requestConfigService.execute(key, params != null ? params : Map.of());
        }
    }
}
