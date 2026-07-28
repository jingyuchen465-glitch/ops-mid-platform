package com.bear.mcp.single.core.selection;

import com.bear.mcp.single.core.entity.McpUserToolSelectionEntity;
import com.bear.mcp.single.core.mapper.McpUserToolSelectionMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ToolSelectionService {

    private final McpUserToolSelectionMapper selectionMapper;

    public ToolSelectionService(McpUserToolSelectionMapper selectionMapper) {
        this.selectionMapper = selectionMapper;
    }

    public List<String> listSelectedTools(Long tokenId) {
        if (tokenId == null) {
            return List.of();
        }
        return selectionMapper.findEnabledByTokenId(tokenId)
                .stream()
                .map(McpUserToolSelectionEntity::getToolName)
                .toList();
    }

    /** 严格模式：工具必须存在于当前 Token 的启用选择记录中。 */
    public boolean isToolSelected(Long tokenId, String toolName) {
        return toolName != null && !toolName.isBlank() && listSelectedTools(tokenId).contains(toolName);
    }
}
