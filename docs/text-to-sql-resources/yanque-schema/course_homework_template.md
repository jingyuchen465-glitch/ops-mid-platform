---
title: "YanQue 表结构：course_homework_template"
type: text-to-sql-schema
system: yanque
table: "course_homework_template"
primaryKey: "id"
businessDomain: "作业"
resourceUri: "bear://yanque/text-to-sql/schema/course_homework_template"
---

# YanQue 表结构：course_homework_template

## 表说明

课程作业标准或训练集，按课程、授课方式、阶段或天数定义。

## 字段

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `bigint` | 课程作业标准ID |
| `course_id` | `bigint` | 课程ID |
| `teaching_mode` | `varchar(20)` | 上课方式：ONLINE线上，OFFLINE线下 |
| `stage_name` | `varchar(64)` | 阶段名称，线上课程使用 |
| `day_number` | `int` | 第几天，线下课程使用 |
| `content_object_key` | `varchar(500)` | 作业标准文档对象Key |
| `content_file_name` | `varchar(255)` | 作业标准文档文件名 |
| `status` | `varchar(20)` | 状态：ACTIVE生效，INACTIVE失效 |
| `remark` | `varchar(500)` | 备注 |
| `created_at` | `datetime` | 创建时间 |
| `updated_at` | `datetime` | 更新时间 |

## 关联关系

| 目标表 | 本表字段 | 目标字段 | 说明 |
| --- | --- | --- | --- |
| `sys_course` | `course_id` | `id` | 关联课程 |

## 字段安全

- 禁止查询字段：无
- 脱敏字段：
  - `stage_name`：`NAME`
  - `content_file_name`：`NAME`

## 索引

| 索引名 | 字段 | 唯一 |
| --- | --- | --- |
| `uk_course_stage` | `course_id`, `teaching_mode`, `stage_name` | 是 |
| `uk_course_day` | `course_id`, `teaching_mode`, `day_number` | 是 |
| `idx_course_homework_template_course` | `course_id` | 否 |
| `idx_course_homework_template_status` | `status` | 否 |
