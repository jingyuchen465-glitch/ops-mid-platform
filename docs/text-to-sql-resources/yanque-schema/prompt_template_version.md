---
title: "YanQue 表结构：prompt_template_version"
type: text-to-sql-schema
system: yanque
table: "prompt_template_version"
primaryKey: "id"
businessDomain: "AI配置"
resourceUri: "bear://yanque/text-to-sql/schema/prompt_template_version"
---

# YanQue 表结构：prompt_template_version

## 表说明

提示词模板历史版本，记录版本号、内容、备注和状态。

## 字段

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `bigint` | 主键ID |
| `template_id` | `bigint` | 提示词模板ID |
| `version_no` | `int` | 版本号 |
| `content` | `text` | 提示词内容 |
| `remark` | `varchar(500)` | 版本备注 |
| `status` | `varchar(20)` | 状态：ACTIVE/INACTIVE |
| `created_at` | `datetime` | 创建时间 |
| `updated_at` | `datetime` | 更新时间 |

## 关联关系

| 目标表 | 本表字段 | 目标字段 | 说明 |
| --- | --- | --- | --- |
| `prompt_template` | `template_id` | `id` | 关联提示词模板 |

## 索引

| 索引名 | 字段 | 唯一 |
| --- | --- | --- |
| `uk_prompt_template_version` | `template_id`, `version_no` | 是 |
| `idx_prompt_template_version_template` | `template_id` | 否 |
