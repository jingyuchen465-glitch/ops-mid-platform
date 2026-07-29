package com.bear.mcp.single.share.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创作空间保存动态 Tool 的请求。
 *
 * <p>Tool 创作用来把已保存的 API 配置包装成 MCP Tool，
 * Controller 只接收 Req，不直接接收 Entity。</p>
 */
@Data
public class ShareStudioToolSaveReq {

    /**
     * MCP Tool 名称。
     *
     * <p>AI Client 在 tools/list 和 tools/call 中看到的工具名。</p>
     */
    @NotBlank(message = "工具名不能为空")
    private String toolName;

    /**
     * MCP Tool 描述。
     *
     * <p>描述会影响 Agent 是否选择这个工具。</p>
     */
    @NotBlank(message = "工具描述不能为空")
    private String toolDescription;

    /**
     * MCP inputSchema JSON。
     *
     * <p>必须是 JSON 对象，描述 tools/call 允许传入的参数结构。</p>
     */
    @NotBlank(message = "入参Schema不能为空")
    private String inputSchema;

    /**
     * Groovy 脚本。
     *
     * <p>脚本中可以使用 params、userId、userName、toolName 和 runRequest。</p>
     */
    @NotBlank(message = "Groovy脚本不能为空")
    private String groovyScript;

    /**
     * 允许脚本调用的 API 配置 key 列表 JSON。
     *
     * <p>例如 ["query_course_list"]，脚本只能调用这里列出的请求配置。</p>
     */
    private String linkedRequestKeys;

    /**
     * 是否启用：1-启用，0-禁用。
     *
     * <p>管理开关，普通创作者主要通过发布状态控制上线和公开。</p>
     */
    private Integer enabled;

    /**
     * 发布状态。
     *
     * <p>0 表示草稿或下线；1 表示已上线但不公开；2 表示已上线且公开。</p>
     */
    private Integer publishStatus;
}
