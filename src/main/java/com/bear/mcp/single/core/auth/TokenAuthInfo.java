package com.bear.mcp.single.core.auth;

import java.util.Set;

/** 鉴权流程组装出的业务信息，不对应任何单张数据库表。 */
public record TokenAuthInfo(
        Long tokenId,
        Long userId,
        String userName,
        Set<String> roleCodes,
        Set<String> allowedTools
) {
}
