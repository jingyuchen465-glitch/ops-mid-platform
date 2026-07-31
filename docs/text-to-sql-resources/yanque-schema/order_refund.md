---
title: "YanQue 表结构：order_refund"
type: text-to-sql-schema
system: yanque
table: "order_refund"
primaryKey: "id"
businessDomain: "订单退款"
resourceUri: "bear://yanque/text-to-sql/schema/order_refund"
---

# YanQue 表结构：order_refund

## 表说明

退款订单流水，记录退款金额、退款状态、退款原因和退款成功时间。

## 字段

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `bigint` | 退款订单ID |
| `refund_order_no` | `varchar(64)` | 退款订单号 |
| `payment_order_no` | `varchar(64)` | 原支付订单号 |
| `payment_amount` | `decimal(10,2)` | 原支付金额 |
| `refund_amount` | `decimal(10,2)` | 退款金额 |
| `status` | `varchar(30)` | 退款状态：INIT初始化，PROCESSING处理中，SUCCESS成功，FAIL失败，CLOSED关闭 |
| `reason` | `varchar(200)` | 退款原因 |
| `unique_refund_no` | `varchar(128)` | 易宝退款流水号 |
| `fail_reason` | `varchar(500)` | 失败原因 |
| `refund_success_time` | `datetime` | 退款成功时间 |
| `created_at` | `datetime` | 创建时间 |
| `updated_at` | `datetime` | 更新时间 |

## 关联关系

| 目标表 | 本表字段 | 目标字段 | 说明 |
| --- | --- | --- | --- |
| `order_payment` | `payment_order_no` | `order_no` | 关联原支付订单 |

## 索引

| 索引名 | 字段 | 唯一 |
| --- | --- | --- |
| `uk_refund_order_no` | `refund_order_no` | 是 |
| `idx_payment_order_no` | `payment_order_no` | 否 |
| `idx_status` | `status` | 否 |
