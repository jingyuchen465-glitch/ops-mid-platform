---
title: "YanQue 表结构：knowledge_base"
type: text-to-sql-schema
system: yanque
table: "knowledge_base"
primaryKey: "id"
businessDomain: "知识库"
resourceUri: "bear://yanque/text-to-sql/schema/knowledge_base"
---

# YanQue 表结构：knowledge_base

## 表说明

知识库基础信息，包含名称、描述和启用状态。

## 字段

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `bigint` | 知识库ID |
| `knowledge_base_name` | `varchar(100)` | 知识库名称 |
| `description` | `varchar(500)` | 描述 |
| `status` | `varchar(20)` | 状态：ACTIVE/INACTIVE |
| `created_at` | `datetime` | 创建时间 |
| `updated_at` | `datetime` | 更新时间 |

## 关联关系

| 目标表 | 本表字段 | 目标字段 | 说明 |
| --- | --- | --- | --- |
| `knowledge_document` | `id` | `knowledge_base_id` | 关联知识库文档 |

## 字段安全

- 禁止查询字段：无
- 脱敏字段：
  - `knowledge_base_name`：`NAME`

## 索引

| 索引名 | 字段 | 唯一 |
| --- | --- | --- |
| `uk_knowledge_base_name` | `knowledge_base_name` | 是 |
