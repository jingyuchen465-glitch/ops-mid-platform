---
title: "YanQue 表结构：sys_class"
type: text-to-sql-schema
system: yanque
table: "sys_class"
primaryKey: "id"
businessDomain: "班级"
resourceUri: "bear://yanque/text-to-sql/schema/sys_class"
---

# YanQue 表结构：sys_class

## 表说明

班级信息，包含班级期数、班主任、校区和课程。

## 字段

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `bigint` | 班级ID |
| `class_period` | `varchar(64)` | 班级期数 |
| `head_teacher_id` | `bigint` | 班主任ID |
| `campus_id` | `bigint` | 校区ID |
| `course_id` | `bigint` | 课程ID |
| `created_at` | `datetime` | 创建时间 |
| `updated_at` | `datetime` | 更新时间 |

## 关联关系

| 目标表 | 本表字段 | 目标字段 | 说明 |
| --- | --- | --- | --- |
| `sys_campus` | `campus_id` | `id` | 关联校区 |
| `sys_course` | `course_id` | `id` | 关联课程 |
| `student` | `id` | `class_id` | 关联班级学生 |
| `sys_class_schedule` | `id` | `class_id` | 关联课表 |

## 索引

| 索引名 | 字段 | 唯一 |
| --- | --- | --- |
| `idx_head_teacher_id` | `head_teacher_id` | 否 |
| `idx_campus_id` | `campus_id` | 否 |
| `idx_course_id` | `course_id` | 否 |
