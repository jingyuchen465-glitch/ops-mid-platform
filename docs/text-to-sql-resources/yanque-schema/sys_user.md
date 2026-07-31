---
title: "YanQue 表结构：sys_user"
type: text-to-sql-schema
system: yanque
table: "sys_user"
primaryKey: "id"
businessDomain: ""
resourceUri: "bear://yanque/text-to-sql/schema/sys_user"
---

# YanQue 表结构：sys_user

## 表说明

用户表

## 字段

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `bigint` | 用户ID |
| `username` | `varchar(64)` | 登录用户名 |
| `password` | `varchar(255)` | 登录密码，BCrypt加密 |
| `nickname` | `varchar(64)` | 用户昵称 |
| `real_name` | `varchar(64)` | 真实姓名 |
| `phone` | `varchar(20)` | 手机号 |
| `email` | `varchar(128)` | 邮箱 |
| `union_id` | `varchar(128)` | 飞书 union_id |
| `status` | `varchar(128)` | status |
| `created_at` | `datetime` | 创建时间 |
| `updated_at` | `datetime` | 更新时间 |

## 关联关系

暂无关联关系。

## 字段安全

- 禁止查询字段：`password`
- 脱敏字段：
  - `real_name`：`NAME`
  - `phone`：`PHONE`

## 索引

| 索引名 | 字段 | 唯一 |
| --- | --- | --- |
| `username` | `username` | 是 |
| `union_id` | `union_id` | 是 |
