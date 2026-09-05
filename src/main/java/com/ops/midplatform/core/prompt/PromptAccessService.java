package com.ops.midplatform.core.prompt;

import com.ops.midplatform.core.context.McpUserContext;
import com.ops.midplatform.core.entity.McpPromptTemplateEntity;
import com.ops.midplatform.core.entity.McpUserPromptSelectionEntity;
import com.ops.midplatform.core.mapper.McpUserPromptSelectionMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** MCP Prompt 的运行时权限过滤。 */
@Service
public class PromptAccessService {
    private final PromptTemplateService promptTemplateService;
    private final McpUserPromptSelectionMapper promptSelectionMapper;

    public PromptAccessService(PromptTemplateService promptTemplateService,
                               McpUserPromptSelectionMapper promptSelectionMapper) {
        this.promptTemplateService = promptTemplateService;
        this.promptSelectionMapper = promptSelectionMapper;
    }

    public List<McpPromptTemplateEntity> listAccessiblePrompts(McpUserContext context) {
        if (context == null || context.tokenId() == null || context.allowedPrompts().isEmpty()) {
            return List.of();
        }
        Set<String> selectedPrompts = selectedPrompts(context.tokenId());
        if (selectedPrompts.isEmpty()) {
            return List.of();
        }
        return promptTemplateService.listPublished().stream()
                .filter(prompt -> context.allowedPrompts().contains(prompt.getPromptName()))
                .filter(prompt -> selectedPrompts.contains(prompt.getPromptName()))
                .toList();
    }

    public boolean canAccess(McpUserContext context, String promptName) {
        if (context == null || promptName == null || promptName.isBlank()) {
            return false;
        }
        return context.allowedPrompts().contains(promptName) && selectedPrompts(context.tokenId()).contains(promptName);
    }

    private Set<String> selectedPrompts(Long tokenId) {
        if (tokenId == null) {
            return Set.of();
        }
        return promptSelectionMapper.findEnabledByTokenId(tokenId).stream()
                .map(McpUserPromptSelectionEntity::getPromptName)
                .collect(Collectors.toSet());
    }
}
