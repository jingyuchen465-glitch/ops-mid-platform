---
title: "YanQue 表结构：sys_role"
type: text-to-sql-schema
system: yanque
table: "sys_role"
primaryKey: "id"
businessDomain: ""
resourceUri: "bear://yanque/text-to-sql/schema/sys_role"
---

# YanQue 表结构：sys_role

## 表说明

角色表

## 字段

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `bigint` | 角色ID |
| `role_code` | `varchar(64)` | 角色编码 |
| `role_name` | `varchar(64)` | 角色名称 |
| `description` | `varchar(255)` | 角色描述 |
| `status` | `varchar(64)` | status |
| `created_at` | `datetime` | 创建时间 |
| `updated_at` | `datetime` | 更新时间 |

## 关联关系

暂无关联关系。

## 字段安全

- 禁止查询字段：无
- 脱敏字段：
  - `role_name`：`NAME`

## 索引

| 索引名 | 字段 | 唯一 |
| --- | --- | --- |
| `role_code` | `role_code` | 是 |
