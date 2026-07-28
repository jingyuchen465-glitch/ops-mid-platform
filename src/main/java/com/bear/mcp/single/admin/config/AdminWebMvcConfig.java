package com.bear.mcp.single.admin.config;

import com.bear.mcp.single.admin.interceptor.AdminJwtAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
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
                .addPathPatterns("/api/admin/**", "/api/share/**")
                .excludePathPatterns("/api/admin/auth/login");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/admin").setViewName("forward:/index.html");
        registry.addViewController("/share").setViewName("forward:/index.html");
        registry.addViewController("/share/studio").setViewName("forward:/index.html");
        registry.addViewController("/share/studio/apis").setViewName("forward:/index.html");
        registry.addViewController("/share/studio/apis/edit").setViewName("forward:/index.html");
    }
}
