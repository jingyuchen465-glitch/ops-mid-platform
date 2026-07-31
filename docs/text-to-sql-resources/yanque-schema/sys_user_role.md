---
title: "YanQue 表结构：sys_user_role"
type: text-to-sql-schema
system: yanque
table: "sys_user_role"
primaryKey: "id"
businessDomain: ""
resourceUri: "bear://yanque/text-to-sql/schema/sys_user_role"
---

# YanQue 表结构：sys_user_role

## 表说明

用户角色关联表

## 字段

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `bigint` | 主键ID |
| `user_id` | `bigint` | 用户ID |
| `role_id` | `bigint` | 角色ID |
| `created_at` | `datetime` | 创建时间 |

## 关联关系

暂无关联关系。

## 索引

| 索引名 | 字段 | 唯一 |
| --- | --- | --- |
| `uk_user_role` | `user_id`, `role_id` | 是 |
