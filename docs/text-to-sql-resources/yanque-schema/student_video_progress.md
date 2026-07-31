---
title: "YanQue 表结构：student_video_progress"
type: text-to-sql-schema
system: yanque
table: "student_video_progress"
primaryKey: "id"
businessDomain: ""
resourceUri: "bear://yanque/text-to-sql/schema/student_video_progress"
---

# YanQue 表结构：student_video_progress

## 表说明

学生视频观看进度表

## 字段

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `bigint` | 学生视频进度ID |
| `student_id` | `bigint` | 学生ID |
| `video_id` | `bigint` | 课程视频ID |
| `watched_seconds` | `int` | 已观看秒数 |
| `duration_seconds` | `int` | 视频总时长，秒 |
| `progress_percent` | `int` | 观看进度百分比 |
| `completed` | `char(1)` | 是否完成：Y/N |
| `last_watch_time` | `datetime` | 最后观看时间 |
| `created_at` | `datetime` | 创建时间 |
| `updated_at` | `datetime` | 更新时间 |

## 关联关系

暂无关联关系。

## 索引

| 索引名 | 字段 | 唯一 |
| --- | --- | --- |
| `uk_student_video` | `student_id`, `video_id` | 是 |
| `idx_video_progress_video` | `video_id` | 否 |
