package com.bear.mcp.single.admin.config;

import com.bear.mcp.single.admin.interceptor.AdminJwtAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** 管理端 Web 入口配置，独立于 MCP 协议过滤链。 */
@Configuration
public class AdminWebMvcConfig implements WebMvcConfigurer {
    private final AdminJwtAuthInterceptor jwtAuthInterceptor;
    public AdminWebMvcConfig(AdminJwtAuthInterceptor jwtAuthInterceptor) {
        this.jwtAuthInterceptor = jwtAuthInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns("/api/admin/**")
                .excludePathPatterns("/api/admin/auth/login");
    }
}
