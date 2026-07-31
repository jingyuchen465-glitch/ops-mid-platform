---
title: "YanQue 表结构：sys_course_detail"
type: text-to-sql-schema
system: yanque
table: "sys_course_detail"
primaryKey: "id"
businessDomain: "课程"
resourceUri: "bear://yanque/text-to-sql/schema/sys_course_detail"
---

# YanQue 表结构：sys_course_detail

## 表说明

课程阶段或每日课程内容，记录阶段名称、天数和上课内容。

## 字段

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `bigint` | 课程详情ID |
| `course_id` | `bigint` | 课程ID |
| `stage_name` | `varchar(128)` | 阶段 |
| `day_number` | `int` | 第几天，线下课程使用 |
| `class_content` | `varchar(1000)` | 上课内容，线下课程使用 |
| `created_at` | `datetime` | 创建时间 |
| `updated_at` | `datetime` | 更新时间 |

## 关联关系

| 目标表 | 本表字段 | 目标字段 | 说明 |
| --- | --- | --- | --- |
| `sys_course` | `course_id` | `id` | 关联课程 |
| `sys_class_schedule` | `id` | `course_detail_id` | 关联班级课表 |

## 字段安全

- 禁止查询字段：无
- 脱敏字段：
  - `stage_name`：`NAME`

## 索引

| 索引名 | 字段 | 唯一 |
| --- | --- | --- |
| `idx_course_id` | `course_id` | 否 |
