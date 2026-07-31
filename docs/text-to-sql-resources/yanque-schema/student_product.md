---
title: "YanQue 表结构：student_product"
type: text-to-sql-schema
system: yanque
table: "student_product"
primaryKey: "id"
businessDomain: "学生产品"
resourceUri: "bear://yanque/text-to-sql/schema/student_product"
---

# YanQue 表结构：student_product

## 表说明

学生已购产品关系，记录来源支付订单和产品开通状态。

## 字段

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `bigint` | 学生产品关系ID |
| `student_id` | `bigint` | 学生ID |
| `product_id` | `varchar(64)` | 产品ID |
| `source_order_no` | `varchar(64)` | 来源支付订单号 |
| `status` | `varchar(30)` | 状态：ACTIVE启用，INACTIVE停用 |
| `created_at` | `datetime` | 创建时间 |
| `updated_at` | `datetime` | 更新时间 |

## 关联关系

| 目标表 | 本表字段 | 目标字段 | 说明 |
| --- | --- | --- | --- |
| `student` | `student_id` | `id` | 关联学生 |
| `order_product` | `product_id` | `id` | 关联产品 |
| `order_payment` | `source_order_no` | `order_no` | 关联来源支付订单 |

## 索引

| 索引名 | 字段 | 唯一 |
| --- | --- | --- |
| `uk_source_order_no` | `source_order_no` | 是 |
| `idx_student_id` | `student_id` | 否 |
| `idx_product_id` | `product_id` | 否 |
| `idx_status` | `status` | 否 |
