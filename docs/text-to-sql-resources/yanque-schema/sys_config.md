---
title: "YanQue 表结构：sys_config"
type: text-to-sql-schema
system: yanque
table: "sys_config"
primaryKey: "id"
businessDomain: ""
resourceUri: "bear://yanque/text-to-sql/schema/sys_config"
---

# YanQue 表结构：sys_config

## 表说明

系统配置表

## 字段

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `bigint` | 主键ID |
| `k` | `varchar(100)` | 配置Key |
| `v` | `varchar(500)` | 配置Value |

## 关联关系

暂无关联关系。

## 索引

| 索引名 | 字段 | 唯一 |
| --- | --- | --- |
| `k` | `k` | 是 |
