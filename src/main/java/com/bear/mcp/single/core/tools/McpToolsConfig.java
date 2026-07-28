package com.bear.mcp.single.core.tools;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpToolsConfig {

    @Bean
    public ToolCallbackProvider builtinTools(SystemTools systemTools, CalculatorTools calculatorTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(systemTools, calculatorTools)
                .build();
    }
}
