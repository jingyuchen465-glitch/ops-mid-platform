---
title: "YanQue 表结构：course_video"
type: text-to-sql-schema
system: yanque
table: "course_video"
primaryKey: "id"
businessDomain: ""
resourceUri: "bear://yanque/text-to-sql/schema/course_video"
---

# YanQue 表结构：course_video

## 表说明

课程视频表

## 字段

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `bigint` | 课程视频ID |
| `course_id` | `bigint` | 所属课程ID |
| `title` | `varchar(200)` | 视频标题 |
| `description` | `varchar(1000)` | 视频说明 |
| `video_url` | `varchar(1000)` | 视频播放地址 |
| `duration_seconds` | `int` | 视频总时长，秒 |
| `sort_order` | `int` | 固定课程顺序 |
| `status` | `varchar(20)` | 状态：ACTIVE生效，INACTIVE停用 |
| `created_at` | `datetime` | 创建时间 |
| `updated_at` | `datetime` | 更新时间 |

## 关联关系

暂无关联关系。

## 索引

| 索引名 | 字段 | 唯一 |
| --- | --- | --- |
| `idx_course_video_course_order` | `course_id`, `sort_order` | 否 |
