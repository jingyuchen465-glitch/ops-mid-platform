package com.ops.midplatform.core.tools;

import com.ops.midplatform.core.log.VolcengineLogService;
import com.volcengine.model.tls.response.SearchLogsResponseV2;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/** 查询 yanque-admin 在火山引擎日志服务中的日志。 */
@Component
public class YanqueLogTools {

    private static final int DEFAULT_LIMIT = 20;
    private static final long DEFAULT_WINDOW_MS = 30 * 60 * 1000L;

    private final VolcengineLogService logService;

    public YanqueLogTools(VolcengineLogService logService) {
        this.logService = logService;
    }

    @Tool(name = "search_yanque_admin_logs",
            description = "查询 yanque-admin 在火山引擎日志服务中的日志，支持按关键词、ERROR、异常名或 guid 检索")
    public Map<String, Object> searchLogs(
            @ToolParam(description = "日志查询关键词，例如 ERROR、异常名或 guid", required = true)
            String query,
            @ToolParam(description = "开始时间，毫秒级 Unix 时间戳，默认最近 30 分钟")
            Long startTime,
            @ToolParam(description = "结束时间，毫秒级 Unix 时间戳，默认当前时间")
            Long endTime,
            @ToolParam(description = "返回数量，最大 100，默认 20")
            Integer limit
    ) throws Exception {
        long end = endTime == null ? System.currentTimeMillis() : endTime;
        long start = startTime == null ? end - DEFAULT_WINDOW_MS : startTime;
        SearchLogsResponseV2 response = logService.search(query, start, end, limit == null ? DEFAULT_LIMIT : limit);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("count", response.getCount());
        result.put("hitCount", response.getHitCount());
        result.put("listOver", response.isListOver());
        result.put("logs", response.getLogs());
        return result;
    }
}