---
title: "YanQue 表结构：exam_paper"
type: text-to-sql-schema
system: yanque
table: "exam_paper"
primaryKey: "id"
businessDomain: "考试"
resourceUri: "bear://yanque/text-to-sql/schema/exam_paper"
---

# YanQue 表结构：exam_paper

## 表说明

试卷信息，包含试卷名称、所属课程、阶段和总分。

## 字段

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `bigint` | 试卷ID |
| `paper_name` | `varchar(128)` | 试卷名称 |
| `course_id` | `bigint` | 课程ID |
| `stage_name` | `varchar(64)` | 阶段名称，为空表示整门课程考试 |
| `total_score` | `decimal(10,1)` | 总分数 |
| `created_at` | `datetime` | 创建时间 |
| `updated_at` | `datetime` | 更新时间 |

## 关联关系

| 目标表 | 本表字段 | 目标字段 | 说明 |
| --- | --- | --- | --- |
| `sys_course` | `course_id` | `id` | 关联课程 |
| `exam_paper_question` | `id` | `paper_id` | 关联试卷题目 |
| `exam` | `id` | `paper_id` | 关联考试安排 |

## 字段安全

- 禁止查询字段：无
- 脱敏字段：
  - `paper_name`：`NAME`
  - `stage_name`：`NAME`

## 索引

| 索引名 | 字段 | 唯一 |
| --- | --- | --- |
| `idx_exam_paper_course_stage` | `course_id`, `stage_name` | 否 |
| `idx_exam_paper_name` | `paper_name` | 否 |
