package com.bear.mcp.single.selection;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ToolSelectionService {

    private final Map<Long, List<String>> selectedToolsByToken = new ConcurrentHashMap<>();

    public ToolSelectionService() {
        selectedToolsByToken.put(1L, List.of("hello", "current_time", "system_info", "calculate", "echo_dynamic"));
    }

    public List<String> listSelectedTools(Long tokenId) {
        if (tokenId == null) {
            return List.of();
        }
        return selectedToolsByToken.getOrDefault(tokenId, List.of());
    }
}
