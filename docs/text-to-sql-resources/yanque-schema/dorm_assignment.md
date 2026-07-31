---
title: "YanQue 表结构：dorm_assignment"
type: text-to-sql-schema
system: yanque
table: "dorm_assignment"
primaryKey: "id"
businessDomain: ""
resourceUri: "bear://yanque/text-to-sql/schema/dorm_assignment"
---

# YanQue 表结构：dorm_assignment

## 表说明

宿舍入住记录表

## 字段

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `bigint` | 入住记录ID |
| `student_id` | `bigint` | 学生ID（student.id） |
| `bed_id` | `bigint` | 床位ID（dorm_bed.id） |
| `room_id` | `bigint` | 房间ID（冗余，便于查询） |
| `building_id` | `bigint` | 楼栋ID（冗余，便于查询） |
| `check_in_date` | `date` | 入住日期 |
| `check_out_date` | `date` | 退宿日期，在住时为null |
| `status` | `varchar(20)` | 状态：LIVING在住/CHECKED_OUT已退宿 |
| `assigned_by` | `bigint` | 分配操作人（管理端用户ID） |
| `remark` | `varchar(255)` | 备注 |
| `created_at` | `datetime` | 创建时间 |
| `updated_at` | `datetime` | 更新时间 |

## 关联关系

暂无关联关系。

## 索引

| 索引名 | 字段 | 唯一 |
| --- | --- | --- |
| `idx_student_id` | `student_id` | 否 |
| `idx_bed_id` | `bed_id` | 否 |
| `idx_status` | `status` | 否 |
