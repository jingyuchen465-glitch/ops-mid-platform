---
title: "YanQue 表结构：student_learning_plan"
type: text-to-sql-schema
system: yanque
table: "student_learning_plan"
primaryKey: "id"
businessDomain: "学习计划"
resourceUri: "bear://yanque/text-to-sql/schema/student_learning_plan"
---

# YanQue 表结构：student_learning_plan

## 表说明

线上学生学习计划，关联学生、课程、入学 SOP 和计划状态。

## 字段

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `bigint` | 线上学习计划ID |
| `student_id` | `bigint` | 学生ID |
| `course_id` | `bigint` | 课程ID |
| `sop_id` | `bigint` | SOP记录ID |
| `start_date` | `date` | 开始学习日期 |
| `status` | `varchar(20)` | 状态：ACTIVE生效，CANCELED取消 |
| `created_at` | `datetime` | 创建时间 |
| `updated_at` | `datetime` | 更新时间 |

## 关联关系

| 目标表 | 本表字段 | 目标字段 | 说明 |
| --- | --- | --- | --- |
| `student` | `student_id` | `id` | 关联学生 |
| `sys_course` | `course_id` | `id` | 关联课程 |
| `student_sop` | `sop_id` | `id` | 关联入学 SOP |
| `student_learning_calendar` | `id` | `plan_id` | 关联学习日历 |

## 索引

| 索引名 | 字段 | 唯一 |
| --- | --- | --- |
| `idx_student_learning_plan_student` | `student_id` | 否 |
| `idx_student_learning_plan_course` | `course_id` | 否 |
| `idx_student_learning_plan_sop` | `sop_id` | 否 |
| `idx_student_learning_plan_status` | `status` | 否 |
