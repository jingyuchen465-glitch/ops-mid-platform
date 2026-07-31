---
title: "YanQue 表结构：homework"
type: text-to-sql-schema
system: yanque
table: "homework"
primaryKey: "id"
businessDomain: "作业"
resourceUri: "bear://yanque/text-to-sql/schema/homework"
---

# YanQue 表结构：homework

## 表说明

班级作业，记录作业日期、内容文件、答案文件、开始和截止时间。

## 字段

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `bigint` | 作业ID |
| `title` | `varchar(100)` | 作业标题 |
| `content_object_key` | `varchar(500)` | 作业内容对象存储Key |
| `content_file_name` | `varchar(255)` | 作业内容文件名 |
| `answer_object_key` | `varchar(500)` | 答案对象存储Key |
| `answer_file_name` | `varchar(255)` | 答案文件名 |
| `answer_student_visible` | `tinyint(1)` | 答案学生是否可见 |
| `class_id` | `bigint` | 班级ID |
| `homework_date` | `date` | 作业日期 |
| `class_content` | `varchar(255)` | 课程内容 |
| `start_time` | `datetime` | 开始时间 |
| `deadline` | `datetime` | 截止时间 |
| `remark` | `varchar(500)` | 备注 |
| `created_at` | `datetime` | 创建时间 |
| `updated_at` | `datetime` | 更新时间 |

## 关联关系

| 目标表 | 本表字段 | 目标字段 | 说明 |
| --- | --- | --- | --- |
| `sys_class` | `class_id` | `id` | 关联班级 |
| `homework_submission` | `id` | `homework_id` | 关联作业提交 |

## 字段安全

- 禁止查询字段：无
- 脱敏字段：
  - `content_file_name`：`NAME`
  - `answer_file_name`：`NAME`

## 索引

| 索引名 | 字段 | 唯一 |
| --- | --- | --- |
| `uk_class_homework_date` | `class_id`, `homework_date` | 是 |
| `idx_class_id` | `class_id` | 否 |
| `idx_start_time` | `start_time` | 否 |
| `idx_deadline` | `deadline` | 否 |
| `idx_homework_date` | `homework_date` | 否 |
