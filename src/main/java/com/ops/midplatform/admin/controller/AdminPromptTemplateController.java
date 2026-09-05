package com.ops.midplatform.admin.controller;

import com.ops.midplatform.admin.res.AdminPromptTemplateRes;
import com.ops.midplatform.admin.service.AdminPromptTemplateService;
import com.ops.midplatform.common.api.ApiResponse;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/admin")
public class AdminPromptTemplateController {
    private final AdminPromptTemplateService promptTemplateService;

    public AdminPromptTemplateController(AdminPromptTemplateService promptTemplateService) {
        this.promptTemplateService = promptTemplateService;
    }

    @GetMapping("/prompt-templates")
    public ApiResponse<List<AdminPromptTemplateRes>> listOptions() {
        return ApiResponse.success(promptTemplateService.listOptions());
    }
}
