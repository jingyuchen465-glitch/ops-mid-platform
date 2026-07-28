package com.bear.mcp.single.admin.interceptor;

import com.bear.mcp.single.admin.service.AdminJwtService;
import com.bear.mcp.single.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/** 校验管理端 JWT，并把当前管理员身份放入 request attribute。 */
@Component
public class AdminJwtAuthInterceptor implements HandlerInterceptor {
    private final AdminJwtService jwtService;

    public AdminJwtAuthInterceptor(AdminJwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new BusinessException(401, "未登录或Token缺失");
        }

        AdminJwtService.AdminJwtUser user = jwtService.verify(authorization.substring(7).trim());
        request.setAttribute("adminUserId", user.userId());
        request.setAttribute("adminUsername", user.username());
        return true;
    }
}
