---
title: "YanQue 表结构：student_followup_record"
type: text-to-sql-schema
system: yanque
table: "student_followup_record"
primaryKey: "id"
businessDomain: "学生回访"
resourceUri: "bear://yanque/text-to-sql/schema/student_followup_record"
---

# YanQue 表结构：student_followup_record

## 表说明

学生回访记录，包含回访标签、应回访日期、回访内容、回访人和状态。

## 字段

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `bigint` | 主键ID |
| `student_id` | `bigint` | 学生ID |
| `learning_plan_id` | `bigint` | 线上学习计划ID |
| `student_tag` | `varchar(64)` | 生成时学生标签快照 |
| `followup_tag_id` | `bigint` | 回访标签配置ID |
| `enroll_date` | `date` | 线上入学日期 |
| `last_followup_time` | `datetime` | 生成时上一条回访时间 |
| `due_date` | `date` | 应回访日期 |
| `followup_interval_days` | `int` | 生成时回访间隔天数快照 |
| `status` | `varchar(32)` | 状态：NEED_FOLLOWUP需要回访，FOLLOWED已回访，CANCELED已取消 |
| `followup_user_id` | `bigint` | 回访人ID |
| `followup_time` | `datetime` | 回访时间 |
| `followup_content` | `varchar(2000)` | 回访内容 |
| `followup_video_object_key` | `varchar(512)` | 回访会议视频对象Key |
| `followup_video_file_name` | `varchar(255)` | 回访会议视频文件名 |
| `remark` | `varchar(512)` | 备注 |
| `created_at` | `datetime` | 创建时间 |
| `updated_at` | `datetime` | 更新时间 |

## 关联关系

| 目标表 | 本表字段 | 目标字段 | 说明 |
| --- | --- | --- | --- |
| `student` | `student_id` | `id` | 关联学生 |
| `student_followup_tag` | `followup_tag_id` | `id` | 关联回访标签 |
| `student_learning_plan` | `learning_plan_id` | `id` | 关联学习计划 |

## 字段安全

- 禁止查询字段：无
- 脱敏字段：
  - `followup_video_file_name`：`NAME`

## 索引

| 索引名 | 字段 | 唯一 |
| --- | --- | --- |
| `uk_student_followup_due` | `student_id`, `due_date` | 是 |
| `idx_student_followup_due_status` | `due_date`, `status` | 否 |
| `idx_student_followup_student` | `student_id` | 否 |
| `idx_student_followup_tag` | `student_tag` | 否 |
| `idx_student_followup_user` | `followup_user_id` | 否 |
