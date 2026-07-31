---
title: "YanQue 表结构：student_sop"
type: text-to-sql-schema
system: yanque
table: "student_sop"
primaryKey: "id"
businessDomain: "学生服务"
resourceUri: "bear://yanque/text-to-sql/schema/student_sop"
---

# YanQue 表结构：student_sop

## 表说明

学生入学 SOP 记录，包含导师、SOP 时间、视频和完成状态。

## 字段

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `bigint` | 学生SOP记录ID |
| `student_id` | `bigint` | 学生ID |
| `mentor_id` | `bigint` | 导师用户ID |
| `sop_video_object_key` | `varchar(255)` | SOP视频对象Key |
| `sop_video_file_name` | `varchar(255)` | SOP视频文件名 |
| `sop_time` | `datetime` | SOP时间 |
| `status` | `varchar(30)` | 状态：ASSIGNED已分配，CANCELED已取消 |
| `remark` | `varchar(500)` | 备注 |
| `created_at` | `datetime` | 创建时间 |
| `updated_at` | `datetime` | 更新时间 |

## 关联关系

| 目标表 | 本表字段 | 目标字段 | 说明 |
| --- | --- | --- | --- |
| `student` | `student_id` | `id` | 关联学生 |
| `student_learning_plan` | `id` | `sop_id` | 关联学习计划 |

## 字段安全

- 禁止查询字段：无
- 脱敏字段：
  - `sop_video_file_name`：`NAME`

## 索引

| 索引名 | 字段 | 唯一 |
| --- | --- | --- |
| `idx_student_status` | `student_id`, `status` | 否 |
| `idx_mentor_id` | `mentor_id` | 否 |
| `idx_sop_time` | `sop_time` | 否 |
