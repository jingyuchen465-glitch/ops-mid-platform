---
title: "YanQue 业务术语：订单支付"
type: text-to-sql-business-terms
system: yanque
businessDomain: "订单支付"
resourceUri: "bear://yanque/text-to-sql/business/order"
---

# YanQue 业务术语：订单支付

## 业务域说明

课程产品、预支付订单、支付流水、退款流水和学生已购产品。

## 核心业务术语

| 术语 | 含义 | 主要表字段 |
| --- | --- | --- |
| 产品 / 课程产品 | 可售卖的课程产品，包含课程内容、授课方式和价格。 | `order_product.id`, `order_product.course_content`, `order_product.teaching_mode`, `order_product.price` |
| 预支付订单 | 学生发起购买后生成的待支付或已支付订单，用于记录产品原价、优惠金额和预支付状态。 | `prepay_order.order_no`, `prepay_order.product_id`, `prepay_order.product_amount`, `prepay_order.discount_amount`, `prepay_order.order_status` |
| 支付订单 / 支付流水 | 实际支付流水，记录支付订单号、支付金额、支付状态和支付成功时间。 | `order_payment.order_no`, `order_payment.order_amount`, `order_payment.status`, `order_payment.pay_success_time` |
| 支付成功订单 | 支付状态为成功的支付订单。 | `order_payment.status = 'SUCCESS'` |
| 退款订单 / 退款流水 | 针对原支付订单发起的退款流水，记录退款金额、退款状态、退款原因和退款成功时间。 | `order_refund.refund_order_no`, `order_refund.payment_order_no`, `order_refund.refund_amount`, `order_refund.status`, `order_refund.refund_success_time` |
| 退款成功订单 | 退款状态为成功的退款订单。 | `order_refund.status = 'SUCCESS'` |
| 学生已购产品 | 学生与课程产品的开通关系，来源可以追溯到支付订单。 | `student_product.student_id`, `student_product.product_id`, `student_product.source_order_no`, `student_product.status` |
| 线上产品 / 线下产品 | 按产品授课方式区分的课程产品。 | `order_product.teaching_mode = 'ONLINE'`, `order_product.teaching_mode = 'OFFLINE'` |
| 客单价 | 支付成功金额除以支付成功订单数。 | 指标口径见订单支付指标资源 |

## 常用状态值

| 对象 | 字段 | 值 | 含义 |
| --- | --- | --- | --- |
| 预支付订单 | `prepay_order.order_status` | `PENDING_PAYMENT` | 待支付 |
| 预支付订单 | `prepay_order.order_status` | `PAID` | 已支付 |
| 预支付订单 | `prepay_order.order_status` | `CANCELED` | 已取消 |
| 支付订单 | `order_payment.status` | `INIT` | 初始化 |
| 支付订单 | `order_payment.status` | `FAIL` | 失败 |
| 支付订单 | `order_payment.status` | `PROCESSING` | 处理中 |
| 支付订单 | `order_payment.status` | `SUCCESS` | 成功 |
| 支付订单 | `order_payment.status` | `TIMEOUT` | 超时 |
| 退款订单 | `order_refund.status` | `INIT` | 初始化 |
| 退款订单 | `order_refund.status` | `PROCESSING` | 处理中 |
| 退款订单 | `order_refund.status` | `SUCCESS` | 成功 |
| 退款订单 | `order_refund.status` | `FAIL` | 失败 |
| 退款订单 | `order_refund.status` | `CLOSED` | 关闭 |
| 学生产品 | `student_product.status` | `ACTIVE` | 启用 |
| 学生产品 | `student_product.status` | `INACTIVE` | 停用 |

## 常用关联

| 关系 | 连接字段 |
| --- | --- |
| 支付订单关联预支付订单 | `order_payment.prepay_order_no = prepay_order.order_no` |
| 预支付订单关联支付订单 | `prepay_order.order_no = order_payment.prepay_order_no` |
| 支付订单关联退款订单 | `order_payment.order_no = order_refund.payment_order_no` |
| 支付订单关联产品 | `order_payment.product_id = order_product.id` |
| 预支付订单关联产品 | `prepay_order.product_id = order_product.id` |
| 产品关联学生已购产品 | `order_product.id = student_product.product_id` |
| 学生已购产品关联来源支付订单 | `student_product.source_order_no = order_payment.order_no` |
