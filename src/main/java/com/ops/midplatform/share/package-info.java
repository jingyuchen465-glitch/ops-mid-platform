/**
 * 用户侧社区和创作空间相关接口。
 *
 * <p>share 包和 admin 包刻意分开：admin 面向管理员，负责治理、权限、审计和维护；
 * share 面向普通能力作者，负责浏览社区能力、创建 API、调试 API 和发布作品。</p>
 *
 * <p>第 7 课先实现 HTTP API 创作链路：录入外部接口、填写默认参数、发送调试、
 * 保存配置、上线或公开。后续课程再把这些 API 编排成 MCP 动态工具。</p>
 */
package com.bear.mcp.single.share;
