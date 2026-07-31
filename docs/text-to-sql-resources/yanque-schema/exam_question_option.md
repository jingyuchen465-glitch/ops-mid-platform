---
title: "YanQue 表结构：exam_question_option"
type: text-to-sql-schema
system: yanque
table: "exam_question_option"
primaryKey: "id"
businessDomain: "考试题库"
resourceUri: "bear://yanque/text-to-sql/schema/exam_question_option"
---

# YanQue 表结构：exam_question_option

## 表说明

选择题选项，记录选项标识和选项内容。

## 字段

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `bigint` | id |
| `question_id` | `bigint` | 题目ID |
| `option_key` | `varchar(16)` | 选项标识：A/B/C/D |
| `option_content` | `text` | 选项内容 |
| `sort_order` | `int` | 排序值 |
| `created_at` | `datetime` | 创建时间 |
| `updated_at` | `datetime` | 更新时间 |

## 关联关系

| 目标表 | 本表字段 | 目标字段 | 说明 |
| --- | --- | --- | --- |
| `exam_question` | `question_id` | `id` | 关联题目 |

## 索引

| 索引名 | 字段 | 唯一 |
| --- | --- | --- |
| `uk_exam_question_option_key` | `question_id`, `option_key` | 是 |
| `idx_exam_question_option_question` | `question_id` | 否 |
