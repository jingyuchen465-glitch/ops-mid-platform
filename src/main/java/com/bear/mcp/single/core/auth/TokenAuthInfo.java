package com.bear.mcp.single.core.auth;

import java.util.Set;

/**
 * Token 鉴权成功后组装出的业务信息。
 *
 * <p>它不对应任何单张数据库表，而是由 Token、用户、用户角色、角色和角色工具权限共同组成。</p>
 */
public record TokenAuthInfo(
        /** 当前请求使用的 Token ID。 */
        Long tokenId,
        /** Token 归属用户 ID。 */
        Long userId,
        /** Token 归属用户名。 */
        String userName,
        /** 用户当前启用的角色编码集合。 */
        Set<String> roleCodes,
        /** 用户通过角色获得的工具资格集合。 */
        Set<String> allowedTools
) {
}
