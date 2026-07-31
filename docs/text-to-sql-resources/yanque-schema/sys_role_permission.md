---
title: "YanQue 表结构：sys_role_permission"
type: text-to-sql-schema
system: yanque
table: "sys_role_permission"
primaryKey: "id"
businessDomain: ""
resourceUri: "bear://yanque/text-to-sql/schema/sys_role_permission"
---

# YanQue 表结构：sys_role_permission

## 表说明

角色权限关联表

## 字段

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `bigint` | 主键ID |
| `role_id` | `bigint` | 角色ID |
| `permission_id` | `bigint` | 权限ID |
| `created_at` | `datetime` | 创建时间 |

## 关联关系

暂无关联关系。

## 索引

| 索引名 | 字段 | 唯一 |
| --- | --- | --- |
| `uk_role_permission` | `role_id`, `permission_id` | 是 |
