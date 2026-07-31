---
title: "YanQue 表结构：order_product"
type: text-to-sql-schema
system: yanque
table: "order_product"
primaryKey: "id"
businessDomain: "产品订单"
resourceUri: "bear://yanque/text-to-sql/schema/order_product"
---

# YanQue 表结构：order_product

## 表说明

可售课程产品及价格信息。

## 字段

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `bigint` | 产品ID |
| `course_content` | `varchar(1000)` | 课程内容 |
| `teaching_mode` | `varchar(20)` | 上课方式：ONLINE线上，OFFLINE线下 |
| `price` | `decimal(10,2)` | 价格 |
| `created_at` | `datetime` | 创建时间 |
| `updated_at` | `datetime` | 更新时间 |

## 关联关系

| 目标表 | 本表字段 | 目标字段 | 说明 |
| --- | --- | --- | --- |
| `prepay_order` | `id` | `product_id` | 关联预支付订单 |
| `order_payment` | `id` | `product_id` | 关联支付订单 |
| `student_product` | `id` | `product_id` | 关联学生已购产品 |

## 索引

暂无索引元数据。
