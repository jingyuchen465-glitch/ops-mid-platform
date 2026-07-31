---
title: "YanQue 表结构：ai_chat_session"
type: text-to-sql-schema
system: yanque
table: "ai_chat_session"
primaryKey: "id"
businessDomain: "AI问答"
resourceUri: "bear://yanque/text-to-sql/schema/ai_chat_session"
---

# YanQue 表结构：ai_chat_session

## 表说明

学生 AI 问答会话，记录会话标题、状态和压缩后的历史摘要。

## 字段

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `bigint` | 主键ID |
| `student_id` | `bigint` | 学生ID |
| `title` | `varchar(100)` | 会话标题 |
| `status` | `varchar(20)` | 状态：ACTIVE 正常，DELETED 删除 |
| `summary` | `text` | 已压缩的历史对话摘要 |
| `last_compressed_message_id` | `bigint` | 最后压缩到的消息ID |
| `created_at` | `datetime` | 创建时间 |
| `updated_at` | `datetime` | 更新时间 |

## 关联关系

| 目标表 | 本表字段 | 目标字段 | 说明 |
| --- | --- | --- | --- |
| `student` | `student_id` | `id` | 关联学生 |
| `ai_chat_message` | `id` | `session_id` | 关联会话消息 |

## 索引

| 索引名 | 字段 | 唯一 |
| --- | --- | --- |
| `idx_student_status_updated` | `student_id`, `status`, `updated_at` | 否 |
