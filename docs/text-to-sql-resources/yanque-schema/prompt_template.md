---
title: "YanQue 表结构：prompt_template"
type: text-to-sql-schema
system: yanque
table: "prompt_template"
primaryKey: "id"
businessDomain: "AI配置"
resourceUri: "bear://yanque/text-to-sql/schema/prompt_template"
---

# YanQue 表结构：prompt_template

## 表说明

AI 提示词模板，维护提示词编码、名称、当前生效版本和状态。

## 字段

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `bigint` | 主键ID |
| `code` | `varchar(100)` | 提示词编码 |
| `name` | `varchar(100)` | 提示词名称 |
| `description` | `varchar(500)` | 提示词说明 |
| `active_version_id` | `bigint` | 当前生效版本ID |
| `status` | `varchar(20)` | 状态：ACTIVE/INACTIVE |
| `created_at` | `datetime` | 创建时间 |
| `updated_at` | `datetime` | 更新时间 |

## 关联关系

| 目标表 | 本表字段 | 目标字段 | 说明 |
| --- | --- | --- | --- |
| `prompt_template_version` | `active_version_id` | `id` | 关联当前提示词版本 |

## 字段安全

- 禁止查询字段：无
- 脱敏字段：
  - `name`：`NAME`

## 索引

| 索引名 | 字段 | 唯一 |
| --- | --- | --- |
| `uk_prompt_template_code` | `code` | 是 |
| `idx_prompt_template_active_version` | `active_version_id` | 否 |
