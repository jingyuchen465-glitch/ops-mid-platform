---
title: "YanQue 指标口径：订单支付"
type: text-to-sql-metrics
system: yanque
businessDomain: "订单支付"
resourceUri: "bear://yanque/text-to-sql/metrics/order"
---

# YanQue 指标口径：订单支付

## 业务域说明

课程产品、预支付订单、支付流水、退款流水和学生已购产品。

## 指标口径

| 指标 | 业务含义 | 计算口径 | 主表 | 时间字段 | 可用维度 |
| --- | --- | --- | --- | --- | --- |
| 支付成功订单数 | 已成功支付的订单数量。 | `COUNT(order_payment.id)` where `order_payment.status = 'SUCCESS'` | `order_payment` | `pay_success_time` | 产品、授课方式、学生、日期 |
| 支付成功金额 / 销售额 | 已成功支付订单的支付金额合计。 | `SUM(order_payment.order_amount)` where `order_payment.status = 'SUCCESS'` | `order_payment` | `pay_success_time` | 产品、授课方式、学生、日期 |
| 已申请退款金额 | 支付订单上记录的已申请退款金额，包含处理中和成功退款。 | `SUM(order_payment.refunded_amount)` | `order_payment` | `created_at` 或 `pay_success_time` | 产品、授课方式、学生、日期 |
| 退款成功订单数 | 已成功退款的退款订单数量。 | `COUNT(order_refund.id)` where `order_refund.status = 'SUCCESS'` | `order_refund` | `refund_success_time` | 原支付订单、日期 |
| 退款成功金额 | 已成功退款的退款金额合计。 | `SUM(order_refund.refund_amount)` where `order_refund.status = 'SUCCESS'` | `order_refund` | `refund_success_time` | 原支付订单、日期 |
| 客单价 | 每笔支付成功订单的平均支付金额。 | `SUM(order_payment.order_amount) / COUNT(order_payment.id)` where `order_payment.status = 'SUCCESS'` | `order_payment` | `pay_success_time` | 产品、授课方式、日期 |
| 退款率 | 退款成功金额占支付成功金额的比例。 | `SUM(order_refund.refund_amount where status='SUCCESS') / SUM(order_payment.order_amount where status='SUCCESS')`，通过 `order_payment.order_no = order_refund.payment_order_no` 关联 | `order_payment`, `order_refund` | 支付用 `pay_success_time`，退款用 `refund_success_time` | 产品、授课方式、日期 |
| 预支付订单数 | 创建的预支付订单数量。 | `COUNT(prepay_order.id)` | `prepay_order` | `created_at` | 产品、订单状态、日期 |
| 已支付预订单数 | 预支付订单状态为已支付的数量。 | `COUNT(prepay_order.id)` where `prepay_order.order_status = 'PAID'` | `prepay_order` | `created_at` | 产品、日期 |
| 产品开通数 | 学生产品关系启用数量。 | `COUNT(student_product.id)` where `student_product.status = 'ACTIVE'` | `student_product` | `created_at` | 产品、学生、日期 |

## 口径说明

- 金额字段类型为 `decimal(10,2)`，具体币种和元/分单位按源系统字段描述确认；本资源不做单位换算假设。
- 销售额默认使用支付成功口径，不使用预支付订单的产品金额。
- 退款率存在“按金额”和“按订单数”两种口径；本资源默认“按金额”，如需按订单数应显式说明。
