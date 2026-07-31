---
title: "YanQue 表结构：exam"
type: text-to-sql-schema
system: yanque
table: "exam"
primaryKey: "id"
businessDomain: "考试"
resourceUri: "bear://yanque/text-to-sql/schema/exam"
---

# YanQue 表结构：exam

## 表说明

考试安排，关联试卷和班级，记录考试时间、时长、监考人和结果可见状态。

## 字段

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `bigint` | 考试ID |
| `paper_id` | `bigint` | 试卷ID |
| `class_id` | `bigint` | 班级ID |
| `start_time` | `datetime` | 可进入考试开始时间 |
| `end_time` | `datetime` | 可进入考试截止时间 |
| `duration_minutes` | `int` | 学生个人答题时长，单位分钟 |
| `invigilator_user_id` | `mediumtext` | 监考老师 |
| `answer_visible` | `tinyint(1)` | 是否向学生公布答卷判定结果 |
| `created_at` | `datetime` | 创建时间 |
| `updated_at` | `datetime` | 更新时间 |

## 关联关系

| 目标表 | 本表字段 | 目标字段 | 说明 |
| --- | --- | --- | --- |
| `exam_paper` | `paper_id` | `id` | 关联试卷 |
| `sys_class` | `class_id` | `id` | 关联班级 |
| `student_exam_record` | `id` | `exam_id` | 关联学生考试记录 |

## 索引

| 索引名 | 字段 | 唯一 |
| --- | --- | --- |
| `idx_exam_paper` | `paper_id` | 否 |
| `idx_exam_class_time` | `class_id`, `start_time`, `end_time` | 否 |
