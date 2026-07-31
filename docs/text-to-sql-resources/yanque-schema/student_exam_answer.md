---
title: "YanQue 表结构：student_exam_answer"
type: text-to-sql-schema
system: yanque
table: "student_exam_answer"
primaryKey: "id"
businessDomain: "考试"
resourceUri: "bear://yanque/text-to-sql/schema/student_exam_answer"
---

# YanQue 表结构：student_exam_answer

## 表说明

学生单题答题记录，包含题目、答案内容、对错和得分。

## 字段

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `bigint` | 学生考试答案ID |
| `record_id` | `bigint` | 学生考试记录ID |
| `exam_id` | `bigint` | 考试ID |
| `paper_id` | `bigint` | 试卷ID |
| `question_id` | `bigint` | 题目ID |
| `question_type` | `varchar(32)` | 题型 |
| `question_score` | `decimal(10,2)` | 题目分数 |
| `answer_content` | `text` | 学生答案内容 |
| `correct` | `tinyint(1)` | 是否正确，主观题/编程题可为空 |
| `score` | `decimal(10,2)` | 本题得分 |
| `created_at` | `datetime` | 创建时间 |
| `updated_at` | `datetime` | 更新时间 |

## 关联关系

| 目标表 | 本表字段 | 目标字段 | 说明 |
| --- | --- | --- | --- |
| `student_exam_record` | `record_id` | `id` | 关联考试记录 |
| `exam_question` | `question_id` | `id` | 关联题目 |

## 索引

| 索引名 | 字段 | 唯一 |
| --- | --- | --- |
| `uk_record_question` | `record_id`, `question_id` | 是 |
| `idx_student_exam_answer_record` | `record_id` | 否 |
| `idx_student_exam_answer_exam` | `exam_id` | 否 |
| `idx_student_exam_answer_question` | `question_id` | 否 |
