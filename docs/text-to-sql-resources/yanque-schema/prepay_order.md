---
title: "YanQue 表结构：prepay_order"
type: text-to-sql-schema
system: yanque
table: "prepay_order"
primaryKey: "id"
businessDomain: "订单支付"
resourceUri: "bear://yanque/text-to-sql/schema/prepay_order"
---

# YanQue 表结构：prepay_order

## 表说明

预支付订单，记录学生购买产品、原价、优惠和待支付或已支付状态。

## 字段

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `bigint` | 预支付订单ID |
| `order_no` | `varchar(32)` | 订单号 |
| `student_name` | `varchar(50)` | 学生姓名 |
| `student_phone` | `varchar(30)` | 手机号 |
| `product_id` | `bigint` | 产品ID |
| `product_amount` | `decimal(10,2)` | 产品金额 |
| `discount_amount` | `decimal(10,2)` | 优惠金额 |
| `order_status` | `varchar(30)` | 订单状态：PENDING_PAYMENT待支付，PAID已支付，CANCELED已取消 |
| `created_at` | `datetime` | 创建时间 |
| `updated_at` | `datetime` | 更新时间 |

## 关联关系

| 目标表 | 本表字段 | 目标字段 | 说明 |
| --- | --- | --- | --- |
| `order_product` | `product_id` | `id` | 关联购买产品 |
| `order_payment` | `order_no` | `prepay_order_no` | 关联支付订单 |

## 字段安全

- 禁止查询字段：无
- 脱敏字段：
  - `student_name`：`NAME`
  - `student_phone`：`PHONE`

## 索引

| 索引名 | 字段 | 唯一 |
| --- | --- | --- |
| `uk_order_no` | `order_no` | 是 |
| `idx_student_phone` | `student_phone` | 否 |
| `idx_product_id` | `product_id` | 否 |
| `idx_order_status` | `order_status` | 否 |
