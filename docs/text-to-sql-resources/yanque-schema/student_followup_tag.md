---
title: "YanQue 表结构：student_followup_tag"
type: text-to-sql-schema
system: yanque
table: "student_followup_tag"
primaryKey: "id"
businessDomain: "学生回访"
resourceUri: "bear://yanque/text-to-sql/schema/student_followup_tag"
---

# YanQue 表结构：student_followup_tag

## 表说明

学生回访标签配置，定义各标签对应的回访间隔和启用状态。

## 字段

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `bigint` | 主键ID |
| `student_tag` | `varchar(64)` | 学生标签 |
| `followup_interval_days` | `int` | 回访间隔天数 |
| `status` | `varchar(32)` | 状态：ACTIVE启用，INACTIVE停用 |
| `remark` | `varchar(512)` | 备注 |
| `created_at` | `datetime` | 创建时间 |
| `updated_at` | `datetime` | 更新时间 |

## 关联关系

| 目标表 | 本表字段 | 目标字段 | 说明 |
| --- | --- | --- | --- |
| `student_followup_record` | `id` | `followup_tag_id` | 关联回访记录 |

## 索引

| 索引名 | 字段 | 唯一 |
| --- | --- | --- |
| `uk_student_followup_tag_student_tag` | `student_tag` | 是 |
