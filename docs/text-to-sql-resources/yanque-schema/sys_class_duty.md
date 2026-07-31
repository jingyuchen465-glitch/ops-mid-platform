---
title: "YanQue 表结构：sys_class_duty"
type: text-to-sql-schema
system: yanque
table: "sys_class_duty"
primaryKey: "id"
businessDomain: "班级教学"
resourceUri: "bear://yanque/text-to-sql/schema/sys_class_duty"
---

# YanQue 表结构：sys_class_duty

## 表说明

班级或校区值班安排，记录老师、日期、时间段和值班类型。

## 字段

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `bigint` | 值班ID |
| `class_id` | `bigint` | 班级ID，校区统一值班为空 |
| `campus_id` | `bigint` | 校区ID |
| `teacher_id` | `bigint` | 老师ID |
| `duty_date` | `date` | 值班日期 |
| `duty_type` | `varchar(50)` | 值班类型：EVENING_STUDY_CLASS/EVENING_STUDY_CAMPUS/SELF_STUDY_CLASS |
| `start_time` | `varchar(10)` | 开始时间 |
| `end_time` | `varchar(10)` | 结束时间 |
| `remark` | `varchar(255)` | 备注 |
| `created_at` | `datetime` | 创建时间 |
| `updated_at` | `datetime` | 更新时间 |

## 关联关系

| 目标表 | 本表字段 | 目标字段 | 说明 |
| --- | --- | --- | --- |
| `sys_class` | `class_id` | `id` | 关联班级 |
| `sys_campus` | `campus_id` | `id` | 关联校区 |

## 索引

| 索引名 | 字段 | 唯一 |
| --- | --- | --- |
| `idx_class_date` | `class_id`, `duty_date` | 否 |
| `idx_campus_date` | `campus_id`, `duty_date` | 否 |
| `idx_teacher_date` | `teacher_id`, `duty_date` | 否 |
| `idx_duty_type` | `duty_type` | 否 |
