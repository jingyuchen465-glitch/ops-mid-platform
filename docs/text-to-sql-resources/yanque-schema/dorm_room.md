---
title: "YanQue 表结构：dorm_room"
type: text-to-sql-schema
system: yanque
table: "dorm_room"
primaryKey: "id"
businessDomain: ""
resourceUri: "bear://yanque/text-to-sql/schema/dorm_room"
---

# YanQue 表结构：dorm_room

## 表说明

宿舍房间表

## 字段

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `bigint` | 房间ID |
| `building_id` | `bigint` | 所属楼栋ID（dorm_building.id） |
| `room_no` | `varchar(32)` | 房间号，如"301" |
| `floor` | `int` | 楼层 |
| `capacity` | `int` | 床位容量 |
| `room_type` | `varchar(20)` | 房型：FOUR四人间/SIX六人间等 |
| `status` | `varchar(20)` | 状态：ENABLED启用/DISABLED停用/MAINTENANCE维修 |
| `created_at` | `datetime` | 创建时间 |
| `updated_at` | `datetime` | 更新时间 |

## 关联关系

暂无关联关系。

## 索引

| 索引名 | 字段 | 唯一 |
| --- | --- | --- |
| `uk_building_room` | `building_id`, `room_no` | 是 |
| `idx_building_id` | `building_id` | 否 |
