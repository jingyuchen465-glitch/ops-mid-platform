---
title: "YanQue 表结构：sys_campus"
type: text-to-sql-schema
system: yanque
table: "sys_campus"
primaryKey: "id"
businessDomain: "校区"
resourceUri: "bear://yanque/text-to-sql/schema/sys_campus"
---

# YanQue 表结构：sys_campus

## 表说明

校区基础信息，包含校区地点和负责人。

## 字段

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `bigint` | 校区ID |
| `campus_location` | `varchar(255)` | 校区地点 |
| `manager_name` | `varchar(64)` | 负责人 |
| `manager_phone` | `varchar(32)` | 负责人电话 |
| `created_at` | `datetime` | 创建时间 |
| `updated_at` | `datetime` | 更新时间 |

## 关联关系

| 目标表 | 本表字段 | 目标字段 | 说明 |
| --- | --- | --- | --- |
| `sys_class` | `id` | `campus_id` | 关联校区班级 |
| `sys_class_duty` | `id` | `campus_id` | 关联校区值班 |

## 字段安全

- 禁止查询字段：无
- 脱敏字段：
  - `manager_name`：`NAME`
  - `manager_phone`：`PHONE`

## 索引

暂无索引元数据。
