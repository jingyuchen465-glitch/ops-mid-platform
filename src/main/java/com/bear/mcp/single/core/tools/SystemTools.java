package com.bear.mcp.single.core.tools;

import com.bear.mcp.single.core.context.McpUserContext;
import com.bear.mcp.single.core.context.McpUserContextHolder;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class SystemTools {

    @Tool(name = "hello", description = "测试 MCP 连接，返回问候语和当前用户。")
    public String hello(@ToolParam(description = "你的名字") String name) {
        McpUserContext context = McpUserContextHolder.get();
        String userName = context != null ? context.userName() : "anonymous";
        return "Hello, " + name + ". 当前 MCP 用户: " + userName;
    }

    @Tool(name = "current_time", description = "获取当前服务器时间，支持指定时区。")
    public Map<String, Object> currentTime(
            @ToolParam(description = "时区，如 Asia/Shanghai、UTC。默认 Asia/Shanghai", required = false)
            String timezone) {
        java.util.TimeZone timeZone;
        try {
            timeZone = java.util.TimeZone.getTimeZone(timezone != null && !timezone.isBlank() ? timezone : "Asia/Shanghai");
        } catch (Exception ignored) {
            timeZone = java.util.TimeZone.getTimeZone("Asia/Shanghai");
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        formatter.setTimeZone(timeZone);
        Map<String, Object> result = new LinkedHashMap<>();
        Date now = new Date();
        result.put("datetime", formatter.format(now));
        result.put("timezone", timeZone.getID());
        result.put("timestamp", now.getTime());
        return result;
    }

    @Tool(name = "system_info", description = "获取 MCP 服务运行信息。")
    public Map<String, Object> systemInfo() {
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("serviceName", "Bear MCP Single");
        result.put("version", "0.1.0");
        result.put("javaVersion", System.getProperty("java.version"));
        result.put("availableProcessors", runtime.availableProcessors());
        result.put("maxMemoryMB", runtime.maxMemory() / 1024 / 1024);
        return result;
    }
}
