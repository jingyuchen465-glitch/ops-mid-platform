---
title: "YanQue 表结构：dorm_bed"
type: text-to-sql-schema
system: yanque
table: "dorm_bed"
primaryKey: "id"
businessDomain: ""
resourceUri: "bear://yanque/text-to-sql/schema/dorm_bed"
---

# YanQue 表结构：dorm_bed

## 表说明

宿舍床位表

## 字段

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `bigint` | 床位ID |
| `room_id` | `bigint` | 所属房间ID（dorm_room.id） |
| `bed_no` | `varchar(16)` | 床位号，如"A"或"1" |
| `status` | `varchar(20)` | 状态：FREE空闲/OCCUPIED占用/LOCKED锁定 |
| `current_student_id` | `bigint` | 当前入住学生ID（student.id），空闲时为null |
| `created_at` | `datetime` | 创建时间 |
| `updated_at` | `datetime` | 更新时间 |

## 关联关系

暂无关联关系。

## 索引

| 索引名 | 字段 | 唯一 |
| --- | --- | --- |
| `uk_room_bed` | `room_id`, `bed_no` | 是 |
| `idx_room_id` | `room_id` | 否 |
| `idx_current_student_id` | `current_student_id` | 否 |
