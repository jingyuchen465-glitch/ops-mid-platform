package com.ops.midplatform.publicapi.controller;

import com.ops.midplatform.core.skill.SkillPackageService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 公开 Skill ZIP 下载接口，供 ops-skill CLI 使用。 */
@RestController
@RequestMapping("/api/public/skills")
public class PublicSkillController {
    private final SkillPackageService skillPackageService;

    public PublicSkillController(SkillPackageService skillPackageService) {
        this.skillPackageService = skillPackageService;
    }

    @GetMapping("/{skillCode}/download")
    public ResponseEntity<byte[]> download(@PathVariable String skillCode) {
        try {
            SkillPackageService.SkillPackage skillPackage = skillPackageService.exportZip(skillCode);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/zip"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + skillPackage.filename() + "\"")
                    .body(skillPackage.bytes());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
