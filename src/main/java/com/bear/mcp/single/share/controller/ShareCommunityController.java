package com.bear.mcp.single.share.controller;

import com.bear.mcp.single.common.api.ApiResponse;
import com.bear.mcp.single.share.req.ShareCommunityLikeReq;
import com.bear.mcp.single.share.res.ShareCommunityApiRes;
import com.bear.mcp.single.share.res.ShareCommunityLikeRes;
import com.bear.mcp.single.share.res.ShareCommunityToolRes;
import com.bear.mcp.single.share.res.ShareStudioPromptRes;
import com.bear.mcp.single.share.res.ShareStudioResourceRes;
import com.bear.mcp.single.share.res.ShareStudioSkillRes;
import com.bear.mcp.single.share.service.ShareCommunityService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    public ApiResponse<List<ShareCommunityToolRes>> tools(HttpServletRequest request) {
        return ApiResponse.success(communityService.listTools(currentUserId(request)));
    }

    /**
     * 查询当前单体项目真实注册的内置 MCP Tools。
     */
    @GetMapping("/builtin-tools")
    public ApiResponse<List<ShareCommunityToolRes>> builtinTools(HttpServletRequest request) {
        return ApiResponse.success(communityService.listBuiltinTools(currentUserId(request)));
    }

    /**
     * 查询公开的 API 能力。
     */
    @GetMapping("/apis")
    public ApiResponse<List<ShareCommunityApiRes>> apis() {
        return ApiResponse.success(communityService.listApis());
    }

    /**
     * 查询公开的 Skills。
     */
    @GetMapping("/skills")
    public ApiResponse<List<ShareStudioSkillRes>> skills(HttpServletRequest request) {
        return ApiResponse.success(communityService.listSkills(currentUserId(request)));
    }

    /**
     * 查询公开的 MCP Prompts。
     */
    @GetMapping("/prompts")
    public ApiResponse<List<ShareStudioPromptRes>> prompts(HttpServletRequest request) {
        return ApiResponse.success(communityService.listPrompts(currentUserId(request)));
    }

    /**
     * 查询公开的 MCP Resources。
     */
    @GetMapping("/resources")
    public ApiResponse<List<ShareStudioResourceRes>> resources(HttpServletRequest request) {
        return ApiResponse.success(communityService.listResources(currentUserId(request)));
    }

    /**
     * 读取公开 MCP Resource 内容。
     */
    @GetMapping("/resources/{id}/content")
    public ApiResponse<String> resourceContent(@PathVariable Long id) {
        return ApiResponse.success(communityService.readResourceContent(id));
    }

    /**
     * 切换当前用户对公开能力的点赞状态。
     */
    @PostMapping("/likes/toggle")
    public ApiResponse<ShareCommunityLikeRes> toggleLike(@RequestBody ShareCommunityLikeReq req,
                                                         HttpServletRequest request) {
        return ApiResponse.success(communityService.toggleLike(req.getTargetType(), req.getTargetKey(), currentUserId(request)));
    }

    private Long currentUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("adminUserId");
    }
}
