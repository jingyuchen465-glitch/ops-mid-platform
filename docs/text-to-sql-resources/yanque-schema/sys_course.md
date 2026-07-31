---
title: "YanQue 表结构：sys_course"
type: text-to-sql-schema
system: yanque
table: "sys_course"
primaryKey: "id"
businessDomain: "课程"
resourceUri: "bear://yanque/text-to-sql/schema/sys_course"
---

# YanQue 表结构：sys_course

## 表说明

课程基础信息，包含课程名称、天数和授课方式。

## 字段

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `bigint` | 课程ID |
| `course_name` | `varchar(128)` | 课程名称 |
| `course_days` | `int` | 课程天数 |
| `teaching_mode` | `varchar(20)` | 上课方式：ONLINE线上，OFFLINE线下 |
| `material_path` | `varchar(500)` | 资料路径 |
| `created_at` | `datetime` | 创建时间 |
| `updated_at` | `datetime` | 更新时间 |

## 关联关系

| 目标表 | 本表字段 | 目标字段 | 说明 |
| --- | --- | --- | --- |
| `sys_course_detail` | `id` | `course_id` | 关联课程阶段详情 |
| `sys_class` | `id` | `course_id` | 关联班级 |
| `exam_paper` | `id` | `course_id` | 关联课程试卷 |

## 字段安全

- 禁止查询字段：无
- 脱敏字段：
  - `course_name`：`NAME`

## 索引

暂无索引元数据。
