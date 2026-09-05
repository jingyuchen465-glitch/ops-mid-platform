package com.bear.mcp.single.core.selection;

import com.bear.mcp.single.core.entity.McpUserToolSelectionEntity;
import com.bear.mcp.single.core.mapper.McpUserToolSelectionMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ToolSelectionService {

    /**
     * mcp_user_tool_selection：记录每一把 Token 实际选择了哪些工具。
     */
    private final McpUserToolSelectionMapper selectionMapper;

    public ToolSelectionService(McpUserToolSelectionMapper selectionMapper) {
        this.selectionMapper = selectionMapper;
    }

    public List<String> listSelectedTools(Long tokenId) {
        if (tokenId == null) {
            return List.of();
        }

        /*
         * 这里只返回 enabled=1 的选择记录。
         * 被禁用或没有写入选择表的工具，都不会进入当前 Token 的工具集合。
         */
        return selectionMapper.findEnabledByTokenId(tokenId)
                .stream()
                .map(McpUserToolSelectionEntity::getToolName)
                .toList();
    }

    /**
     * 严格模式：
     * 工具必须存在于当前 Token 的启用选择记录中，才能出现在 tools/list，
     * 也才能通过动态工具 tools/call 校验。
     */
    public boolean isToolSelected(Long tokenId, String toolName) {
        return toolName != null && !toolName.isBlank() && listSelectedTools(tokenId).contains(toolName);
    }
}
