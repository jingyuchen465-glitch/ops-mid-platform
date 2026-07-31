---
title: "YanQue 表结构：sys_class_schedule"
type: text-to-sql-schema
system: yanque
table: "sys_class_schedule"
primaryKey: "id"
businessDomain: "班级教学"
resourceUri: "bear://yanque/text-to-sql/schema/sys_class_schedule"
---

# YanQue 表结构：sys_class_schedule

## 表说明

班级每日课表，记录上课日期、老师、课程内容和课程阶段。

## 字段

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `bigint` | 课表ID |
| `class_id` | `bigint` | 班级ID |
| `teacher_id` | `bigint` | 老师ID |
| `schedule_date` | `date` | 上课日期 |
| `course_detail_id` | `bigint` | 课程详情ID |
| `course_content` | `varchar(1000)` | 课程内容 |
| `class_type` | `varchar(32)` | 上课类型：CLASS/SELF_STUDY/HOLIDAY |
| `created_at` | `datetime` | 创建时间 |
| `updated_at` | `datetime` | 更新时间 |

## 关联关系

| 目标表 | 本表字段 | 目标字段 | 说明 |
| --- | --- | --- | --- |
| `sys_class` | `class_id` | `id` | 关联班级 |
| `sys_course_detail` | `course_detail_id` | `id` | 关联课程详情 |

## 索引

| 索引名 | 字段 | 唯一 |
| --- | --- | --- |
| `uk_class_date` | `class_id`, `schedule_date` | 是 |
| `idx_teacher_id` | `teacher_id` | 否 |
| `idx_course_detail_id` | `course_detail_id` | 否 |
