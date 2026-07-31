---
title: "YanQue 表结构：student_dorm"
type: text-to-sql-schema
system: yanque
table: "student_dorm"
primaryKey: "id"
businessDomain: ""
resourceUri: "bear://yanque/text-to-sql/schema/student_dorm"
---

# YanQue 表结构：student_dorm

## 表说明

学生宿舍分配表。

## 字段

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `bigint` | 宿舍分配ID |
| `student_id` | `bigint` | 学生ID |
| `campus_name` | `varchar(100)` | 校区名称 |
| `building_name` | `varchar(100)` | 楼栋名称 |
| `room_no` | `varchar(50)` | 房间号 |
| `bed_no` | `varchar(50)` | 床位号 |
| `room_type` | `varchar(50)` | 房型 |
| `check_in_status` | `varchar(30)` | 入住状态：ASSIGNED 已分配，CHECKED_IN 已入住，CHECKED_OUT 已退宿 |
| `check_in_date` | `date` | 入住日期 |
| `roommate_names` | `varchar(255)` | 室友姓名，逗号分隔 |
| `manager_name` | `varchar(50)` | 宿管姓名 |
| `manager_phone` | `varchar(30)` | 宿管电话 |
| `remark` | `varchar(500)` | 备注 |
| `assigned_by` | `bigint` | 分配人用户ID |
| `assigned_at` | `datetime` | 分配时间 |
| `created_at` | `datetime` | 创建时间 |
| `updated_at` | `datetime` | 更新时间 |

## 关联关系

暂无关联关系。

## 字段安全

- 禁止查询字段：无
- 脱敏字段：
  - `campus_name`：`NAME`
  - `building_name`：`NAME`
  - `manager_name`：`NAME`
  - `manager_phone`：`PHONE`

## 索引

| 索引名 | 字段 | 唯一 |
| --- | --- | --- |
| `uk_student_id` | `student_id` | 是 |
| `idx_student_id` | `student_id` | 否 |
| `idx_check_in_status` | `check_in_status` | 否 |
