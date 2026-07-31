---
title: "YanQue 表结构：student_exam_record"
type: text-to-sql-schema
system: yanque
table: "student_exam_record"
primaryKey: "id"
businessDomain: "考试"
resourceUri: "bear://yanque/text-to-sql/schema/student_exam_record"
---

# YanQue 表结构：student_exam_record

## 表说明

学生参加考试的记录，包含开始、截止、提交时间、状态、评分状态和总分。

## 字段

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `bigint` | 学生考试记录ID |
| `exam_id` | `bigint` | 考试ID |
| `student_id` | `bigint` | 学生ID |
| `start_time` | `datetime` | 实际开始答题时间 |
| `deadline_time` | `datetime` | 当前学生本次考试截止时间 |
| `submit_time` | `datetime` | 提交时间 |
| `status` | `varchar(32)` | 状态：IN_PROGRESS/SUBMITTED/TIMEOUT |
| `grading_status` | `varchar(32)` | 批改状态：PENDING/GRADING/COMPLETED |
| `score` | `decimal(10,2)` | 得分 |
| `created_at` | `datetime` | 创建时间 |
| `updated_at` | `datetime` | 更新时间 |

## 关联关系

| 目标表 | 本表字段 | 目标字段 | 说明 |
| --- | --- | --- | --- |
| `exam` | `exam_id` | `id` | 关联考试安排 |
| `student` | `student_id` | `id` | 关联学生 |
| `student_exam_answer` | `id` | `record_id` | 关联答题明细 |

## 索引

| 索引名 | 字段 | 唯一 |
| --- | --- | --- |
| `uk_exam_student` | `exam_id`, `student_id` | 是 |
| `idx_student_exam_record_student` | `student_id`, `status` | 否 |
| `idx_student_exam_record_exam` | `exam_id` | 否 |
