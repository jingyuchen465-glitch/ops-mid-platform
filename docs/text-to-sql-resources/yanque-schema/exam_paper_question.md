---
title: "YanQue 表结构：exam_paper_question"
type: text-to-sql-schema
system: yanque
table: "exam_paper_question"
primaryKey: "id"
businessDomain: "考试"
resourceUri: "bear://yanque/text-to-sql/schema/exam_paper_question"
---

# YanQue 表结构：exam_paper_question

## 表说明

试卷与题目的关联关系，记录题目在试卷中的分值。

## 字段

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `bigint` | 关联ID |
| `paper_id` | `bigint` | 试卷ID |
| `question_id` | `bigint` | 题目ID |
| `question_score` | `decimal(10,1)` | 题目分数 |
| `created_at` | `datetime` | 创建时间 |
| `updated_at` | `datetime` | 更新时间 |

## 关联关系

| 目标表 | 本表字段 | 目标字段 | 说明 |
| --- | --- | --- | --- |
| `exam_paper` | `paper_id` | `id` | 关联试卷 |
| `exam_question` | `question_id` | `id` | 关联题目 |

## 索引

| 索引名 | 字段 | 唯一 |
| --- | --- | --- |
| `uk_exam_paper_question` | `paper_id`, `question_id` | 是 |
| `idx_exam_paper_question_paper` | `paper_id` | 否 |
| `idx_exam_paper_question_question` | `question_id` | 否 |
