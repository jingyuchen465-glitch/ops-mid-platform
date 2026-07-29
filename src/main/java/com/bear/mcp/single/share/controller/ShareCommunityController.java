package com.bear.mcp.single.share.controller;

import com.bear.mcp.single.common.api.ApiResponse;
import com.bear.mcp.single.share.res.ShareCommunityApiRes;
import com.bear.mcp.single.share.res.ShareCommunityToolRes;
import com.bear.mcp.single.share.service.ShareCommunityService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Bear 社区公开能力接口。
 */
@RestController
@RequestMapping("/api/share/community")
public class ShareCommunityController {

    /**
     * 社区公开能力服务。
     */
    private final ShareCommunityService communityService;

    public ShareCommunityController(ShareCommunityService communityService) {
        this.communityService = communityService;
    }

    /**
     * 查询公开的 MCP Tools。
     */
    @GetMapping("/tools")
    public ApiResponse<List<ShareCommunityToolRes>> tools() {
        return ApiResponse.success(communityService.listTools());
    }

    /**
     * 查询当前单体项目真实注册的内置 MCP Tools。
     */
    @GetMapping("/builtin-tools")
    public ApiResponse<List<ShareCommunityToolRes>> builtinTools() {
        return ApiResponse.success(communityService.listBuiltinTools());
    }

    /**
     * 查询公开的 API 能力。
     */
    @GetMapping("/apis")
    public ApiResponse<List<ShareCommunityApiRes>> apis() {
        return ApiResponse.success(communityService.listApis());
    }
}
