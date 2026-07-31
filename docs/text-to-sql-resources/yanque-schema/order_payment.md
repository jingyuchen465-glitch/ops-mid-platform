---
title: "YanQue 表结构：order_payment"
type: text-to-sql-schema
system: yanque
table: "order_payment"
primaryKey: "id"
businessDomain: "订单支付"
resourceUri: "bear://yanque/text-to-sql/schema/order_payment"
---

# YanQue 表结构：order_payment

## 表说明

支付订单流水，记录支付金额、支付状态、支付成功时间和退款申请金额。

## 字段

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `bigint` | 支付订单ID |
| `order_no` | `varchar(64)` | 支付订单号 |
| `student_phone` | `varchar(30)` | 学生手机号 |
| `student_name` | `varchar(50)` | 学生姓名 |
| `product_id` | `varchar(64)` | 产品ID |
| `order_amount` | `decimal(10,2)` | 订单支付金额 |
| `refunded_amount` | `decimal(10,2)` | 已申请退款金额，包含退款处理中和退款成功金额 |
| `prepay_order_no` | `varchar(32)` | 预支付订单号 |
| `status` | `varchar(30)` | 支付订单状态 |
| `unique_order_no` | `varchar(128)` | 支付渠道唯一订单号 |
| `pay_success_time` | `datetime` | 支付成功时间 |
| `created_at` | `datetime` | 创建时间 |
| `updated_at` | `datetime` | 更新时间 |

## 关联关系

| 目标表 | 本表字段 | 目标字段 | 说明 |
| --- | --- | --- | --- |
| `prepay_order` | `prepay_order_no` | `order_no` | 关联预支付订单 |
| `order_product` | `product_id` | `id` | 关联购买产品 |
| `order_refund` | `order_no` | `payment_order_no` | 关联退款订单 |

## 字段安全

- 禁止查询字段：`unique_order_no`
- 脱敏字段：
  - `student_phone`：`PHONE`
  - `student_name`：`NAME`

## 索引

| 索引名 | 字段 | 唯一 |
| --- | --- | --- |
| `uk_order_no` | `order_no` | 是 |
| `idx_student_phone` | `student_phone` | 否 |
| `idx_prepay_order_no` | `prepay_order_no` | 否 |
| `idx_status` | `status` | 否 |
