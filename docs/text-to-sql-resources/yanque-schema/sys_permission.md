---
title: "YanQue 表结构：sys_permission"
type: text-to-sql-schema
system: yanque
table: "sys_permission"
primaryKey: "id"
businessDomain: ""
resourceUri: "bear://yanque/text-to-sql/schema/sys_permission"
---

# YanQue 表结构：sys_permission

## 表说明

权限表

## 字段

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `bigint` | 权限ID |
| `parent_id` | `bigint` | 父权限ID，根节点为0 |
| `permission_code` | `varchar(100)` | 权限编码 |
| `permission_name` | `varchar(100)` | 权限名称 |
| `permission_type` | `varchar(32)` | 权限类型，例如API、MENU、BUTTON |
| `api_path` | `varchar(255)` | API路径匹配规则，仅API权限使用 |
| `sort_num` | `int` | 排序值，越小越靠前 |
| `description` | `varchar(255)` | 权限描述 |
| `status` | `varchar(255)` | status |
| `created_at` | `datetime` | 创建时间 |
| `updated_at` | `datetime` | 更新时间 |

## 关联关系

暂无关联关系。

## 字段安全

- 禁止查询字段：无
- 脱敏字段：
  - `permission_name`：`NAME`

## 索引

| 索引名 | 字段 | 唯一 |
| --- | --- | --- |
| `permission_code` | `permission_code` | 是 |
| `uk_permission_code` | `permission_code` | 是 |
