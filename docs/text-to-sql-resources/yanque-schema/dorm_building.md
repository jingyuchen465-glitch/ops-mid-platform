---
title: "YanQue 表结构：dorm_building"
type: text-to-sql-schema
system: yanque
table: "dorm_building"
primaryKey: "id"
businessDomain: ""
resourceUri: "bear://yanque/text-to-sql/schema/dorm_building"
---

# YanQue 表结构：dorm_building

## 表说明

宿舍楼栋表

## 字段

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `bigint` | 楼栋ID |
| `campus_id` | `bigint` | 所属校区ID（sys_campus.id） |
| `building_name` | `varchar(64)` | 楼栋名称，如"1号楼" |
| `gender_type` | `varchar(10)` | 性别类型：MALE男寝/FEMALE女寝 |
| `manager_name` | `varchar(32)` | 宿管姓名 |
| `manager_phone` | `varchar(20)` | 宿管电话 |
| `status` | `varchar(20)` | 状态：ENABLED启用/DISABLED停用 |
| `created_at` | `datetime` | 创建时间 |
| `updated_at` | `datetime` | 更新时间 |

## 关联关系

暂无关联关系。

## 字段安全

- 禁止查询字段：无
- 脱敏字段：
  - `building_name`：`NAME`
  - `manager_name`：`NAME`
  - `manager_phone`：`PHONE`

## 索引

| 索引名 | 字段 | 唯一 |
| --- | --- | --- |
| `idx_campus_id` | `campus_id` | 否 |
