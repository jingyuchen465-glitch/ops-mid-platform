---
title: "YanQue 表结构：ai_chat_message"
type: text-to-sql-schema
system: yanque
table: "ai_chat_message"
primaryKey: "id"
businessDomain: "AI问答"
resourceUri: "bear://yanque/text-to-sql/schema/ai_chat_message"
---

# YanQue 表结构：ai_chat_message

## 表说明

AI 问答消息，记录用户或助手角色、消息内容、模型和 Token 消耗。

## 字段

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `bigint` | 主键ID |
| `session_id` | `bigint` | 会话ID |
| `role` | `varchar(20)` | 角色：user 用户，assistant AI |
| `content` | `text` | 消息内容 |
| `model` | `varchar(100)` | 模型名称 |
| `tokens` | `int` | 消耗Token数量 |
| `compressed` | `tinyint(1)` | 是否已压缩进会话摘要 |
| `created_at` | `datetime` | 创建时间 |

## 关联关系

| 目标表 | 本表字段 | 目标字段 | 说明 |
| --- | --- | --- | --- |
| `ai_chat_session` | `session_id` | `id` | 关联会话 |

## 索引

| 索引名 | 字段 | 唯一 |
| --- | --- | --- |
| `idx_session_id` | `session_id` | 否 |
| `idx_session_created` | `session_id`, `created_at` | 否 |
| `idx_session_compressed_id` | `session_id`, `compressed`, `id` | 否 |
