---
title: "YanQue 表结构：homework_submission"
type: text-to-sql-schema
system: yanque
table: "homework_submission"
primaryKey: "id"
businessDomain: "作业"
resourceUri: "bear://yanque/text-to-sql/schema/homework_submission"
---

# YanQue 表结构：homework_submission

## 表说明

学生作业提交记录，包含提交时间、是否逾期、教师批注和成绩。

## 字段

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `bigint` | 作业提交ID |
| `homework_id` | `bigint` | 作业ID |
| `student_id` | `bigint` | 学生ID |
| `class_id` | `bigint` | 班级ID |
| `content_object_key` | `varchar(500)` | 提交内容对象存储Key |
| `content_file_name` | `varchar(255)` | 提交内容文件名 |
| `submit_time` | `datetime` | 提交时间 |
| `late_submitted` | `tinyint(1)` | 是否逾期提交 |
| `teacher_remark` | `varchar(500)` | 老师批注 |
| `score` | `int` | 分数 |
| `created_at` | `datetime` | 创建时间 |
| `updated_at` | `datetime` | 更新时间 |

## 关联关系

| 目标表 | 本表字段 | 目标字段 | 说明 |
| --- | --- | --- | --- |
| `homework` | `homework_id` | `id` | 关联作业 |
| `student` | `student_id` | `id` | 关联学生 |
| `sys_class` | `class_id` | `id` | 关联班级 |

## 字段安全

- 禁止查询字段：无
- 脱敏字段：
  - `content_file_name`：`NAME`

## 索引

| 索引名 | 字段 | 唯一 |
| --- | --- | --- |
| `uk_homework_student` | `homework_id`, `student_id` | 是 |
| `idx_student_id` | `student_id` | 否 |
| `idx_class_id` | `class_id` | 否 |
| `idx_submit_time` | `submit_time` | 否 |
