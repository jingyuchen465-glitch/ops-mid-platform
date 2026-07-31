---
title: "YanQue 表结构：student_learning_calendar"
type: text-to-sql-schema
system: yanque
table: "student_learning_calendar"
primaryKey: "id"
businessDomain: "学习计划"
resourceUri: "bear://yanque/text-to-sql/schema/student_learning_calendar"
---

# YanQue 表结构：student_learning_calendar

## 表说明

线上学生每日学习日历，记录学习日期、阶段、计划天数和完成状态。

## 字段

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `bigint` | 学习日历ID |
| `plan_id` | `bigint` | 学习计划ID |
| `student_id` | `bigint` | 学生ID |
| `study_date` | `date` | 学习日期 |
| `stage_name` | `varchar(128)` | 阶段名称 |
| `day_index` | `int` | 计划总第几天 |
| `stage_day_index` | `int` | 阶段第几天 |
| `status` | `varchar(20)` | 状态：TODO待学习，DONE已完成 |
| `created_at` | `datetime` | 创建时间 |
| `updated_at` | `datetime` | 更新时间 |

## 关联关系

| 目标表 | 本表字段 | 目标字段 | 说明 |
| --- | --- | --- | --- |
| `student_learning_plan` | `plan_id` | `id` | 关联学习计划 |
| `student` | `student_id` | `id` | 关联学生 |

## 字段安全

- 禁止查询字段：无
- 脱敏字段：
  - `stage_name`：`NAME`

## 索引

| 索引名 | 字段 | 唯一 |
| --- | --- | --- |
| `uk_learning_calendar_plan_date` | `plan_id`, `study_date` | 是 |
| `idx_learning_calendar_student` | `student_id` | 否 |
| `idx_learning_calendar_plan` | `plan_id` | 否 |
