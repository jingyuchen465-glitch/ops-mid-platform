---
title: "YanQue 表结构：exam_question_course"
type: text-to-sql-schema
system: yanque
table: "exam_question_course"
primaryKey: "question_id, course_id"
businessDomain: "考试题库"
resourceUri: "bear://yanque/text-to-sql/schema/exam_question_course"
---

# YanQue 表结构：exam_question_course

## 表说明

题目与课程阶段的关联关系。

## 字段

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `question_id` | `bigint` | 题目ID |
| `course_id` | `bigint` | 课程ID |
| `stage_name` | `varchar(64)` | 阶段名称 |
| `created_at` | `datetime` | 创建时间 |

## 关联关系

| 目标表 | 本表字段 | 目标字段 | 说明 |
| --- | --- | --- | --- |
| `exam_question` | `question_id` | `id` | 关联题目 |
| `sys_course` | `course_id` | `id` | 关联课程 |

## 字段安全

- 禁止查询字段：无
- 脱敏字段：
  - `stage_name`：`NAME`

## 索引

| 索引名 | 字段 | 唯一 |
| --- | --- | --- |
| `idx_exam_question_course_course` | `course_id` | 否 |
