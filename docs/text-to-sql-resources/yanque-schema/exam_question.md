---
title: "YanQue 表结构：exam_question"
type: text-to-sql-schema
system: yanque
table: "exam_question"
primaryKey: "id"
businessDomain: "考试题库"
resourceUri: "bear://yanque/text-to-sql/schema/exam_question"
---

# YanQue 表结构：exam_question

## 表说明

题库题目，记录题型、题干、答案、难度和启用状态。

## 字段

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `bigint` | id |
| `question_type` | `varchar(32)` | 题目类型：SINGLE单选，MULTIPLE多选，JUDGE判断，FILL填空，SHORT简答 |
| `question_content` | `text` | 题干 |
| `answer_content` | `text` | 正确答案 |
| `analysis_content` | `text` | 答案解析 |
| `difficulty` | `varchar(32)` | 难度：EASY简单，NORMAL普通，HARD困难 |
| `status` | `varchar(32)` | 状态：ENABLED启用，DISABLED停用 |
| `created_at` | `datetime` | 创建时间 |
| `updated_at` | `datetime` | 更新时间 |

## 关联关系

| 目标表 | 本表字段 | 目标字段 | 说明 |
| --- | --- | --- | --- |
| `exam_question_option` | `id` | `question_id` | 关联题目选项 |
| `exam_question_course` | `id` | `question_id` | 关联课程阶段 |
| `exam_paper_question` | `id` | `question_id` | 关联试卷题目 |

## 索引

| 索引名 | 字段 | 唯一 |
| --- | --- | --- |
| `idx_exam_question_type` | `question_type` | 否 |
| `idx_exam_question_status` | `status` | 否 |
| `idx_exam_question_difficulty` | `difficulty` | 否 |
